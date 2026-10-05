from datetime import datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from models.manager import Manager
from models.manager_staff import ManagerStaff
from models.notification import Notification
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift


class ManagerRepository :
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_profile_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Manager)
            .where(Manager.id_account == id_account)
        )

        return result.scalar_one_or_none()
    async def create_profile(self, fullName, gender, avatar, date, id_account,company):
        result = await self.get_profile_by_id_account(id_account)
        if result is not None:
            return None  # Kiểm tra xem profile đã từng được tạo hay chưa
        manager = Manager(
            full_name=fullName,
            company = company,
            date_of_birth = date ,
            gender=gender,
            avatar=avatar,
            id_account=id_account
        )
        self.db.add(manager)

        await self.db.commit()
        await self.db.refresh(manager)

        return manager

    async def update_profile(self,full_name,gender,avatar,date,id_account,company):

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
        if company is not None :
            profile.company = company

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

    async def get_staff_by_manager(self, id_manager,id_staff):
        result = await self.db.execute(
            select(ManagerStaff)
            .where(ManagerStaff.id_manager == id_manager,
                   ManagerStaff.id_staff==id_staff)
        )
        return result.scalar_one_or_none()
    async def change_workshift(self,id_staff,id_work_shift_cur,id_work_shift_change,workday):
        result = await self.db.execute(
            select(WorkSchedule)
            .where(
                WorkSchedule.id_staff ==id_staff ,
                WorkSchedule.id_work_shift == id_work_shift_cur ,
                WorkSchedule.work_day ==workday
            )
        )
        work_schedule = result.scalar_one_or_none()
        if work_schedule is None:
            return False

        work_schedule.id_work_shift = id_work_shift_change

        await self.db.commit()

        return True

    async def create_notification_change_workshift(self, id_staff, name_work_shift_cur, name_work_shift_change, workday,id_manager,message ,reason):
        content ={
            "message ": message,
            "reason" : reason if reason is not None else "" ,
            "name_work_shift_cur" : name_work_shift_cur ,
            "name_work_shift_change" : name_work_shift_change ,
            "workday" : workday
        }
        notification = Notification(
            id_staff=id_staff,
            id_manager=id_manager,
            title="Thông báo về yêu cầu xin đổi ca",
            content=content,
            created_at=datetime.now(),
            notification_type="Response Change WorkShift"
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def get_work_shift_by_id(self, id_work_shift):
        result = await self.db.execute(
            select(WorkShift)
            .where(WorkShift.id == id_work_shift)
        )
        return result.scalar_one_or_none()

    async def create_notification_leaveday(self, id_staff, name_shift_off, workday_off, id_manager, message, reason):
        content ={
            "message ": message,
            "reason": reason if reason is not None else "",
            "name_work_shift_off": name_shift_off,
            "workday": workday_off
        }
        notification = Notification(
            id_staff=id_staff,
            id_manager=id_manager,
            title="Thông báo về yêu cầu xin nghỉ",
            content=content,
            created_at=datetime.now(),
            notification_type="Response Leave"
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def handle_leave_day_off(self, id_staff, id_work_shift_off, workday):
        result = await self.db.execute(
            select(WorkSchedule)
            .where(
                WorkSchedule.id_staff == id_staff,
                WorkSchedule.id_work_shift == id_work_shift_off,
                WorkSchedule.work_day == workday
            )
        )
        work_schedule = result.scalar_one_or_none()
        if work_schedule is None:
            return False
        work_schedule.status = False
        await self.db.commit()
        return True

    async def add_staff_in_company_by_manager(self, manager_id ,id_staff, position):
        new_staff =ManagerStaff(
            id_manager =manager_id,
            id_staff =id_staff ,
            position = position,
        )
        self.db.add(new_staff)
        await self.db.commit()
        await self.db.refresh(new_staff)