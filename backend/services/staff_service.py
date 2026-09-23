from fastapi import HTTPException

from repositories.staff_repository import StaffRepository
from schemas.staff import CreateProfileRequest, ProfileResponse


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