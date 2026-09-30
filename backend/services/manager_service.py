from fastapi import HTTPException

from repositories.manager_repository import ManagerRepository
from schemas.manager import CreateProfileRequest, ProfileResponse, UpdateProfileRequest, NotificationResponse, \
    ListNotificationResponse


class ManagerService:

    def __init__(self, repository: ManagerRepository):
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

    async def get_notification(self,id_account )->ListNotificationResponse:
        manager = await self.repository.get_profile_by_id_account(id_account)
        if manager is None :
            raise HTTPException(404, "Not found Manager in the system")

        notifications = await self.repository.get_notification(manager.id)
        # cần lấy ra tên nhân viên từ id nhân viên trong notification ( Join )
        result = [
            NotificationResponse(
                avatar_send_person = staff.avatar,
                send_person = staff.full_name,
                content=notification.content,
                send_at=notification.created_at
            )
            for notification, staff in notifications
        ]

        return ListNotificationResponse(
            notifications = result
        )
