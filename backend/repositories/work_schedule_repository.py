from uuid import UUID

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from models.manager import Manager
from models.manager_staff import ManagerStaff
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift


class WorkScheduleRepository :
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_manager_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Manager)
            .where(Manager.id_account == id_account)
        )
        return result.scalar_one_or_none()

    async def get_staff_by_manager(self, id_manager):
        stmt = (
            select(Staff,ManagerStaff.position)
            .join(
                ManagerStaff,
                ManagerStaff.id_staff == Staff.id
            )
            .where(
                ManagerStaff.id_manager == id_manager
            )
        )

        result = await self.db.execute(stmt)

        return result.all()

    async def get_by_staff_ids(self, staff_ids: list[UUID]):
        stmt = (
            select(
                WorkSchedule,
                WorkShift
            )
            .join(
                WorkShift,
                WorkSchedule.id_work_shift == WorkShift.id
            )
            .where(
                WorkSchedule.id_staff.in_(staff_ids) ,
                WorkSchedule.status == True # Kiểm tra xem workschedule có tồn tại ko
            )
        )

        result = await self.db.execute(stmt)

        return result.all()

    async def get_work_shift_by_id(self, id_work_shift:list[UUID]):
        result = await self.db.execute(
            select(WorkShift)
            .where(WorkShift.id.in_(id_work_shift))
        )
        return result.scalars().all()

    async def assign_work_schedules(self, id_work_shifts, id_staff, work_day):
        for id_work_shift in id_work_shifts:
            work_schedule =WorkSchedule(
                id_staff =id_staff,
                id_work_shift =id_work_shift,
                work_day =work_day
            )
            self.db.add(work_schedule)
        await self.db.commit()

    async def get_staff_by_id(self, id_staff,id_manager):
        result = await self.db.execute(
            select(Staff, ManagerStaff.position)
            .join(
                ManagerStaff,
                ManagerStaff.id_staff == Staff.id
            )
            .where(
                Staff.id == id_staff,
                ManagerStaff.id_manager == id_manager
            )
        )

        return result.one_or_none()