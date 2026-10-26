from uuid import UUID

from fastapi import HTTPException

from app.websocket.manager import websocket_manager
from core.redis_custom import redis_client
from repositories.staff_repository import StaffRepository
from schemas.staff import CreateProfileRequest, ProfileResponse, UpdateProfileRequest, StaffWorkScheduleResponse, \
    ListStaffWorkScheduleResponse, StaffWorkShiftResponse, StaffShiftChangeRequest, StaffLeaveRequest, JoinRequest, \
    JoinResponse, ListNotificationResponse, NotificationResponse, CompanyResponse, ListCompany


class StaffService:

    def __init__(self, repository: StaffRepository):
        self.repository = repository

    async def create_profile(self, request: CreateProfileRequest, id_account: str) -> ProfileResponse:
        new_profile = await self.repository.create_profile(request.fullName, request.gender, request.avatar,
                                                           request.date, id_account)
        if new_profile is None:
            raise HTTPException(409, 'Hồ sơ đã được tạo')

        return ProfileResponse(
            fullName=new_profile.full_name,
            gender=new_profile.gender,
            status=new_profile.status,
            avatar=new_profile.avatar,
            date=new_profile.date_of_birth
        )

    async def get_profile(self, id_account: str) -> ProfileResponse:

        profile = await self.repository.get_profile_by_id_account(
            id_account
        )

        if profile is None:
            raise HTTPException(
                status_code=404,
                detail="Hồ sơ chưa được tạo"
            )

        return ProfileResponse(
            fullName=profile.full_name,
            gender=profile.gender,
            status=profile.status,
            avatar=profile.avatar,
            date=profile.date_of_birth
        )

    async def get_companies(self, id_account: str) -> ListCompany:
        staff = await self.repository.get_profile_by_id_account(id_account)
        if staff is None:
            raise HTTPException(404, "Not found Staff in the system")

        companies = await self.repository.get_companies(staff.id)

        return ListCompany(
            data=[
                CompanyResponse(
                    name_company=manager.company or "",
                    name_manager=manager.full_name,
                    position_staff=position or "",
                    id_manager=manager.id,
                )
                for manager, position in companies
            ]
        )

    async def update_profile(self, request: UpdateProfileRequest, id_account: str) -> ProfileResponse:

        profile = await self.repository.update_profile(
            request.full_name,
            request.gender,
            request.avatar,
            request.date,
            id_account
        )

        if profile is None:
            raise HTTPException(
                status_code=404,
                detail="Hồ sơ chưa được tạo"
            )

        return ProfileResponse(
            fullName=profile.full_name,
            gender=profile.gender,
            status=profile.status,
            avatar=profile.avatar,
            date=profile.date_of_birth
        )

    async def get_staff_in_company(self, id_account, id_manager):
        staff = await self.repository.get_profile_by_id_account(id_account)

        if staff is None:
            raise HTTPException(
                status_code=404,
                detail="Not found Staff in the system"
            )

        manager_staff = await self.repository.get_manager_staff(
            staff.id,
            id_manager
        )

        if manager_staff is None:
            raise HTTPException(
                status_code=403,
                detail="Staff does not belong to this company"
            )

        return staff

    async def get_notification(
            self,
            id_account,
            id_manager
    ) -> ListNotificationResponse:

        id_manager = UUID(id_manager)
        staff = await self.get_staff_in_company(
            id_account,
            id_manager
        )

        manager = await self.repository.get_manager_by_id(id_manager)

        if manager is None:
            raise HTTPException(
                status_code=404,
                detail="Not found Manager"
            )

        notifications = await self.repository.get_notification(
            id_manager,
            staff.id
        )

        result = [
            NotificationResponse(
                send_person=manager.full_name,
                company_name=manager.company_name,
                content=notification.content,
                send_at=notification.created_at,
                notification_type=notification.notification_type
            )
            for notification in notifications
        ]

        return ListNotificationResponse(
            notifications=result
        )

    async def get_work_schedules(
            self,
            id_account,
            id_manager
    ) -> ListStaffWorkScheduleResponse:

        id_manager = UUID(id_manager)
        staff = await self.get_staff_in_company(
            id_account,
            id_manager
        )

        work_schedules = await self.repository.get_work_schedule_by_staff(
            staff.id,
            id_manager
        )

        dict_work_schedules = {}

        for work_schedule in work_schedules:

            if work_schedule.work_day not in dict_work_schedules:
                dict_work_schedules[work_schedule.work_day] = []

            dict_work_schedules[work_schedule.work_day].append(
                StaffWorkShiftResponse(
                    id_work_schedule= work_schedule.id  ,
                    name_shift=work_schedule.name,
                    start_time=work_schedule.start_time,
                    end_time=work_schedule.end_time
                )
            )

        result = [
            StaffWorkScheduleResponse(
                workday=workday,
                workshits=work_shifts
            )
            for workday, work_shifts in dict_work_schedules.items()
        ]

        return ListStaffWorkScheduleResponse(
            data=result
        )

    async def request_shift_change(
            self,
            request: StaffShiftChangeRequest,
            id_account,
            id_manager
    ):
        id_manager = UUID(id_manager)
        staff = await self.get_staff_in_company(
            id_account,
            id_manager
        )

        current_shift = await self.repository.get_work_shift_by_id(
            request.id_work_shift_current ,
            id_manager
        )

        change_shift = await self.repository.get_work_shift_by_id(
            request.id_work_shift_change,
            id_manager
        )

        if current_shift is None or change_shift is None:
            raise HTTPException(
                status_code=404,
                detail="Not Found Work Shift"
            )

        notification = await self.repository.create_notification_change_shift(
            request.workday,
            request.reason,
            staff.id,
            id_manager,
            staff.full_name,
            current_shift.name,
            request.id_work_shift_current,
            change_shift.name,
            request.id_work_shift_change
        )

        await websocket_manager.send_to_manager(
            id_manager,
            {
                "type": notification.notification_type,
                "name_shift_cur": current_shift.name,
                "id_work_shift_cur": str(
                    request.id_work_shift_current
                ),
                "name_shift_change": change_shift.name,
                "id_work_shift_change": str(
                    request.id_work_shift_change
                ),
                "id_staff": str(staff.id),
                "avatar_staff": staff.avatar,
                "name_staff": staff.full_name,
                "workday": str(request.workday),
                "reason": request.reason
            }
        )

    async def create_leave_request(
            self,
            request: StaffLeaveRequest,
            id_account,
            id_manager
    ):

        id_manager = UUID(id_manager)
        staff = await self.get_staff_in_company(
            id_account,
            id_manager
        )

        work_shift_off = await self.repository.get_work_shift_by_id(
            request.id_work_shift_off ,
            id_manager
        )

        if work_shift_off is None:
            raise HTTPException(
                status_code=404,
                detail="Not Found Work Shift"
            )

        notification = await self.repository.create_notification_leave(
            staff.id,
            staff.full_name,
            id_manager,
            work_shift_off.name,
            work_shift_off.id,
            request.work_day_off,
            request.reason
        )

        await websocket_manager.send_to_manager(
            id_manager,
            {
                "id_staff": str(staff.id),
                "avatar_staff": staff.avatar,
                "name_staff": staff.full_name,
                "name_work_shift_off": work_shift_off.name,
                "id_work_shift_off": str(work_shift_off.id),
                "work_day_off": str(request.work_day_off),
                "reason": request.reason,
                "type": notification.notification_type
            }
        )

    async def send_join_request(self, request: JoinRequest, id_account):
        staff = await self.repository.get_profile_by_id_account(id_account)
        if staff is None:
            raise HTTPException(
                status_code=404,
                detail="Not found Staff in System"
            )
        phone_number = await self.repository.get_phone_number_by_id(id_account)
        # Lấy manager từ mã mời
        manager_id = await redis_client.get(request.OTP)
        if manager_id is None:
            raise HTTPException(
                status_code=400,
                detail="Invitation code is invalid or expired"
            )
        manager_id = UUID(manager_id)
        manager = await self.repository.get_manager_by_id(manager_id)
        if manager is None:
            raise HTTPException(
                status_code=404,
                detail="Not found Manager"
            )
        notification = await self.repository.create_notification_request_join(staff.id, manager_id, request.fullname,
                                                                              request.avatar)
        await websocket_manager.send_to_manager(
            manager_id,
            {
                "id_staff": str(staff.id),
                "avatar_staff": staff.avatar,
                "name_staff": staff.full_name,
                "type": notification.notification_type,
                "phone_number" : phone_number
            }
        )
        return JoinResponse(
            company=manager.company,
            OTP=request.OTP
        )

    async def get_id_manger_by_invitation_code(self,invitation_code,id_account):
        staff = await self.repository.get_profile_by_id_account(id_account)
        manager =await self.repository.get_manager_by_invitation_code(invitation_code,staff.id)
        if manager is None :
            raise HTTPException(401, "Staff is not approve in company")

        return manager.id_manager
