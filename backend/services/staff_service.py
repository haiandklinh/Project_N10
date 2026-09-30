from fastapi import HTTPException

from app.websocket.manager import websocket_manager
from repositories.staff_repository import StaffRepository
from schemas.staff import CreateProfileRequest, ProfileResponse, UpdateProfileRequest, StaffWorkScheduleResponse, \
    ListStaffWorkScheduleResponse, StaffWorkShiftResponse, StaffShiftChangeRequest, StaffLeaveRequest


class StaffService:

    def __init__(self, repository: StaffRepository):
        self.repository = repository

    async def create_profile(self,request: CreateProfileRequest ,id_account: str ) -> ProfileResponse:
        new_profile = await self.repository.create_profile(request.fullName,request.gender,request.avatar,request.date,id_account)
        if new_profile is None :
            raise HTTPException(409 ,'Hồ sơ đã được tạo')

        return  ProfileResponse(
            fullName= new_profile.full_name,
            gender = new_profile.gender ,
            status= new_profile.status ,
            avatar= new_profile.avatar,
            date= new_profile.date_of_birth
        )

    async def get_profile(self,id_account: str) -> ProfileResponse:

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

    async def update_profile(self,request: UpdateProfileRequest,id_account: str) -> ProfileResponse:

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

    async def get_work_schedules(self,id_account)-> ListStaffWorkScheduleResponse:
        staff =await self.repository.get_profile_by_id_account(id_account)
        if staff is None :
            raise HTTPException(404,"Not found Staff in System")
        work_schedules = await self.repository.get_work_schedule_by_staff(staff.id)
        dict_work_schedules ={}
        for work_schedule in work_schedules :
            if work_schedule.work_day not in dict_work_schedules :
                dict_work_schedules[work_schedule.work_day] =[]
            dict_work_schedules[work_schedule.work_day].append(StaffWorkShiftResponse(
                name_shift =work_schedule.name ,
                start_time =work_schedule.start_time ,
                end_time= work_schedule.end_time
            ))
        result = [
            StaffWorkScheduleResponse(
                workday=key,
                workshits=value
            )
            for key , value in dict_work_schedules.items()
        ]
        return ListStaffWorkScheduleResponse(
            data= result
        )

    async def request_shift_change(self,request : StaffShiftChangeRequest,id_account):
        staff = await self.repository.get_profile_by_id_account(id_account)
        if staff is None:
            raise HTTPException(404, "Not found Staff in System")

        manager = await self.repository.get_manager_by_staff(staff.id)
        if manager is None :
            raise HTTPException(404, "The user have not Manager")
        current_shift = await self.repository.get_work_shift_by_id(
            request.id_work_shift_current
        )

        change_shift = await self.repository.get_work_shift_by_id(
            request.id_work_shift_change
        )
        if current_shift is None or change_shift is None :
            raise HTTPException(404, "Not Found Work Shift")
        notification = await self.repository.create_notification_change_shift(request.workday,request.reason,staff.id,manager.id_manager,staff.full_name,current_shift.name,request.id_work_shift_current,change_shift.name,request.id_work_shift_change)
        await websocket_manager.send_to_manager(
            manager.id_manager,
            {
                "type": notification.notification_type,
                "name_shift_cur" : current_shift.name ,
                "id_work_shift_cur" : str(request.id_work_shift_current),
                "name_shift_change": change_shift.name,
                "id_work_shift_change" : str(request.id_work_shift_change),
                "avatar_staff" :str(staff.avatar),
                "name_staff": str(staff.full_name),
                "workday": str(request.workday),
                "reason": request.reason
            }
        )

    async def create_leave_request(self,request: StaffLeaveRequest,id_account):
        staff = await self.repository.get_profile_by_id_account(id_account)
        if staff is None:
            raise HTTPException(404, "Not found Staff in System")

        manager = await self.repository.get_manager_by_staff(staff.id)
        if manager is None :
            raise HTTPException(404, "The user have not Manager")
        work_shift_off = await self.repository.get_work_shift_by_id(
            request.id_work_shift_off
        )

        notification = await self.repository.create_notification_leave(staff.id,staff.full_name,manager.id_manager,work_shift_off.name,work_shift_off.id,request.work_day_off,request.reason )
        await websocket_manager.send_to_manager(
            manager.id_manager,
            {
                "avatar_staff": str(staff.avatar),
                "name_staff": staff.full_name,
                "name_work_shift_off": work_shift_off.name,
                "id_work_shift_off": str(work_shift_off.id),
                "work_day_off": str(request.work_day_off),
                "reason": request.reason,
                "type" : notification.notification_type
            }
        )

