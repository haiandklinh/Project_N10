from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from backend.models.staff import Staff


class StaffRepository :
    def __init__(self, db: AsyncSession):
        self.db = db


    async def get_profile_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Staff)
            .where(Staff.id_account == id_account)
        )

        return result.scalar_one_or_none()
    async def create_profile(self, fullName, gender, avatar, date, id_account):
        result = self.get_profile_by_id_account(id_account)
        if result is not None:
            return None  # Kiểm tra xem profile đã từng được tạo hay chưa
        staff = Staff(
            fullname=fullName,
            date_of_birth = date ,
            gender=gender,
            avatar=avatar,
            account_id=id_account
        )
        self.db.add(staff)

        await self.db.commit()
        await self.db.refresh(staff)

        return staff