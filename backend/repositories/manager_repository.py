from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from models.manager import Manager
from models.notification import Notification
from models.staff import Staff


class ManagerRepository :
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_profile_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Manager)
            .where(Manager.id_account == id_account)
        )

        return result.scalar_one_or_none()
    async def create_profile(self, fullName, gender, avatar, date, id_account):
        result = self.get_profile_by_id_account(id_account)
        if result is not None:
            return None  # Kiểm tra xem profile đã từng được tạo hay chưa
        staff = Manager(
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

    async def update_profile(self,full_name,gender,avatar,date,id_account):

        profile = await self.get_profile_by_id_account(id_account)

        if profile is None:
            return None

        if full_name is not None:
            profile.full_name = full_name

        if gender is not None:
            profile.gender = gender

        if avatar is not None:
            profile.avatar = avatar

        if date is not None:
            profile.date_of_birth = date

        await self.db.commit()
        await self.db.refresh(profile)

        return profile

    async def get_notification(self, id_manager):
        result = await self.db.execute(
            select(Notification, Staff)
            .join(Staff, Notification.id_staff == Staff.id)
            .where(Notification.id_manager == id_manager ,
                   Notification.notification_type !='Response')
            )

        return result.all()