from datetime import datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from models.manager import Manager
from models.manager_staff import ManagerStaff
from models.notification import Notification
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift


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
        result = await self.get_profile_by_id_account(id_account)
        if result is not None:
            return None  # Kiểm tra xem profile đã từng được tạo hay chưa
        staff = Staff(
            full_name=fullName,
            date_of_birth = date ,
            gender=gender,
            avatar=avatar,
            id_account=id_account
        )
        self.db.add(staff)

        await self.db.commit()
        await self.db.refresh(staff)

        return staff

    async def update_profile( self,full_name,gender,avatar,date,id_account):

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

    async def get_work_schedule_by_staff(self, id_staff):
        result = await self.db.execute(
            select(
                WorkSchedule.work_day ,
                WorkShift.name ,
                WorkShift.start_time ,
                WorkShift.end_time
            )
            .join(
                WorkShift,
                WorkShift.id == WorkSchedule.id_work_shift
            )
            .where(WorkSchedule.id_staff == id_staff,
                   WorkSchedule.status== True )
        )
        return result.scalars().all()

    async def get_manager_by_staff(self, id_staff):
        result = await self.db.execute(
            select(ManagerStaff)
            .where(ManagerStaff.id_staff == id_staff)
        )
        return result.scalar_one_or_none()



    async def get_work_shift_by_id(self, id_work_shift_current):
        result = await self.db.execute(
            select(WorkShift)
            .where(WorkShift.id == id_work_shift_current)
        )
        return result.scalar_one_or_none()

    async def create_notification_change_shift(self, workday, reason, id_staff, id_manager, staff_name, name_shift_cur, id_work_shift_current, name_shift_change,
                                  id_work_shift_change):
        content ={
            "name_shift_cur": name_shift_cur,
            "id_work_shift_cur": str(id_work_shift_current),
            "name_shift_change": name_shift_change,
            "id_work_shift_change": str(id_work_shift_change),
            "name_staff": staff_name,
            "workday": str(workday),
            "reason" :reason
        }
        notification =Notification(
            id_staff= id_staff,
            id_manager= id_manager,
            title ="Yêu cầu đổi ca",
            content =content,
            created_at= datetime.now() ,
            notification_type = "Change Work Shift"
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def create_notification_leave(self, id_staff, name_staff, id_manager, name_work_shift_off, id_work_shift_off, work_day_off,reason):
        content ={
            "name_staff" : name_staff,
            "name_work_shift_off" : name_work_shift_off ,
            "id_work_shift_off": id_work_shift_off ,
            "work_day_off" : work_day_off,
            "reason" : reason
        }
        notification =Notification(
            id_staff= id_staff,
            id_manager= id_manager,
            title ="Yêu cầu xin nghỉ",
            content =content,
            created_at= datetime.now() ,
            notification_type = "Leave Request"
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def create_notification_request_join(self, id_staff, manager_id, fullname, avatar):
        content ={
            "name_staff" : fullname,
            "avatar_staff" :avatar ,
        }
        notification =Notification(
            id_staff= id_staff,
            id_manager= manager_id,
            title ="Yêu cầu tham gia",
            content =content,
            created_at= datetime.now() ,
            notification_type = "Join Request"
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def get_manager_by_id(self, manager_id):
        result = await self.db.execute(
            select(Manager)
            .where(Manager.id == manager_id)
        )
        return result.scalar_one_or_none()


