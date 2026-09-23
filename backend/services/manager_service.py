from fastapi import HTTPException

from repositories.manager_repository import ManagerRepository
from schemas.manager import CreateProfileRequest, ProfileResponse


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