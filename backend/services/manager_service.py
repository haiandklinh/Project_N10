import secrets
from datetime import datetime

from fastapi import HTTPException

from app.websocket.staff import websocket_staff
from core.redis_custom import redis_client
from repositories.manager_repository import ManagerRepository
from schemas.manager import CreateProfileRequest, ProfileResponse, UpdateProfileRequest, NotificationResponse, \
    ListNotificationResponse, InvitationCodeResponse, ShiftChangeRequest, LeaveDayRequest, JoinRequest, \
    ListStaffByCompany, StaffByCompany


class ManagerService:

    def __init__(self, repository: ManagerRepository):
        self.repository = repository

    async def create_profile(self,request: CreateProfileRequest ,id_account: str ) -> ProfileResponse:
        new_profile = await self.repository.create_profile(request.fullName,request.gender,request.avatar,request.date,id_account,request.company)
        if new_profile is None :
            raise HTTPException(409 ,'Hồ sơ đã được tạo')

        return  ProfileResponse(
            fullName= new_profile.full_name,
            gender = new_profile.gender ,
            status= new_profile.status ,
            avatar= new_profile.avatar,
            date= new_profile.date_of_birth,
            company = new_profile.company
        )

    async def get_profile(self,id_account: str ) -> ProfileResponse:

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
            date=profile.date_of_birth,
            company = profile.company
        )

    async def get_staffs(self, id_account: str) -> ListStaffByCompany:
        manager = await self.repository.get_profile_by_id_account(id_account)
        if manager is None:
            raise HTTPException(404, "Not found Manager in the system")

        staffs = await self.repository.get_staffs(manager.id)

        return ListStaffByCompany(
            data=[
                StaffByCompany(
                    id_staff=staff.id,
                    name_staff=staff.full_name,
                    position=position or "",
                    date_of_birth=staff.date_of_birth,
                    gender=staff.gender,
                    status=staff.status,
                    avatar=staff.avatar or "",
                )
                for staff, position in staffs
            ]
        )

    async def update_profile(self,request: UpdateProfileRequest,id_account: str) -> ProfileResponse:

        profile = await self.repository.update_profile(
            request.full_name,
            request.gender,
            request.avatar,
            request.date,
            id_account ,
            request.company
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
            date=profile.date_of_birth ,
            company=profile.company
        )

    async def get_notification(self,id_account )->ListNotificationResponse:
        manager = await self.repository.get_profile_by_id_account(id_account)
        if manager is None :
            raise HTTPException(404, "Not found Manager in the system")

        notifications = await self.repository.get_notification(manager.id)
        # cần lấy ra tên nhân viên từ id nhân viên trong notification ( Join )
        result = [
            NotificationResponse(
                id_staff = staff.id ,
                avatar_send_person = staff.avatar,
                send_person = staff.full_name,
                content=notification.content,
                send_at=notification.created_at ,
                notification_type= notification.notification_type ,
                status = notification.status
            )
            for notification, staff in notifications
        ]

        return ListNotificationResponse(
            notifications = result
        )

    async def get_invitation_code(self,id_account,expires_in) ->InvitationCodeResponse:
        manager = await self.repository.get_profile_by_id_account(id_account)
        while True :
            otp = str(secrets.randbelow(1000000)).zfill(6)
            result = await redis_client.set(otp,str(manager.id),expires_in,nx=True)
            if result :
                break
        return InvitationCodeResponse(
            OTP = otp ,
            expires_in=expires_in
        )
    # Status = True mà Approve thành công thì message ="Update Successful" và reason sẽ rỗng
    # Status = True mà Approve thất bại thì message ="Update Failed" và reason sẽ rỗng
    # Status = False thì message sẽ rỗng  và reason sẽ ko rỗng
    async def handle_shift_change_request(self,request : ShiftChangeRequest,id_account):
        manager = await self.repository.get_profile_by_id_account(id_account)
        message =""
        if manager is None :
            raise HTTPException(404,"Not found Manager")
        staff = await self.repository.get_staff_by_manager(manager.id,request.id_staff)
        if staff is None :
            raise HTTPException(404,"Staff not in Company")
        current_shift = await self.repository.get_work_shift_by_id(
            request.id_work_shift_cur
        )

        change_shift = await self.repository.get_work_shift_by_id(
            request.id_work_shift_change
        )
        if current_shift is None or change_shift is None:
            raise HTTPException(404, "Work shift not found")
        if request.status :
            result = await self.repository.change_workshift(request.id_staff,request.id_work_shift_cur,request.id_work_shift_change,request.workday)
            if result is True:
                message= "Update Successful"
            else :
                message = "Update Failed"
        notifications = await self.repository.create_notification_change_workshift(request.id_staff,current_shift.name,change_shift.name,request.workday,manager.id,message,request.reason)

        await websocket_staff.send_to_staff(
            staff.id_staff,
            {
                "Message" : message  ,
                "Type" : notifications.notification_type ,
                "NameWorkShiftCur" : current_shift.name ,
                "NameWorkShiftChange" : change_shift.name ,
                "workday" : str(request.workday) ,
                "created_at" : str(datetime.now()),
                "reason" : request.reason
            }
        )

    async def handle_leave_request(self,request :LeaveDayRequest,id_account ):
        manager = await self.repository.get_profile_by_id_account(id_account)
        message =""
        if manager is None :
            raise HTTPException(404,"Not found Manager")
        staff = await self.repository.get_staff_by_manager(manager.id,request.id_staff)
        if staff is None :
            raise HTTPException(404,"Staff not in Company")
        shift_off = await self.repository.get_work_shift_by_id(
            request.id_work_shift_off
        )

        if shift_off is None :
            raise HTTPException(404, "Work shift not found")
        if request.status :
            result = await self.repository.handle_leave_day_off(request.id_staff,request.id_work_shift_off,request.workday_off)
            if result is True:
                message= "Update Successful"
            else :
                message = "Update Failed"
        notifications = await self.repository.create_notification_leaveday(request.id_staff,shift_off.name,request.workday_off,manager.id,message,request.reason)

        await websocket_staff.send_to_staff(
            staff.id_staff,
            {
                "Message" : message  ,
                "Type" : notifications.notification_type ,
                "NameWorkShiftOff" : shift_off.name ,
                "workday" : str(request.workday_off) ,
                "created_at" : str(datetime.now()),
                "reason" : request.reason
            }
        )

    async def handle_join_request(self,request :JoinRequest, id_account):
        manager = await self.repository.get_profile_by_id_account(id_account)
        if manager is None :
            raise HTTPException(404,"Not found Manager")
        staff = await self.repository.get_staff_by_manager(manager.id,request.id_staff)
        if staff is not None :
            raise HTTPException(401,"Staff is in company ")
        if request.status is True :
            await self.repository.add_staff_in_company_by_manager(manager.id,request.id_staff,request.position)
        await websocket_staff.send_to_staff(
            request.id_staff,
            {
                "status" :str(request.status)
            }
        )





