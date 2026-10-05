from uuid import UUID

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from models.manager import Manager
from models.work_shift import WorkShift


class ShiftRepository:

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_manager_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Manager)
            .where(
                Manager.id_account == id_account
            )
        )

        return result.scalar_one_or_none()

    async def get_all_active_shifts(self, id_manager):
        result = await self.db.execute(
            select(WorkShift)
            .where(
                WorkShift.id_manager == id_manager,
                WorkShift.status == "active"
            )
            .order_by(
                WorkShift.start_time
            )
        )

        return result.scalars().all()

    async def get_shift_by_id(
        self,
        id_shift: UUID,
        id_manager
    ):
        result = await self.db.execute(
            select(WorkShift)
            .where(
                WorkShift.id == id_shift,
                WorkShift.id_manager == id_manager
            )
        )

        return result.scalar_one_or_none()

    async def create_shift(
        self,
        id_manager,
        name,
        start_time,
        end_time,
        allowed_check_in_time,
        check_in_radius,
        longitude,
        latitude
    ):
        work_shift = WorkShift(
            id_manager=id_manager,
            name=name,
            start_time=start_time,
            end_time=end_time,
            allowed_check_in_time=allowed_check_in_time,
            check_in_radius=check_in_radius,
            longitude=longitude,
            latitude=latitude,
            status="active"
        )

        self.db.add(work_shift)

        await self.db.commit()
        await self.db.refresh(work_shift)

        return work_shift

    async def update_shift(
        self,
        work_shift,
        name,
        start_time,
        end_time,
        allowed_check_in_time,
        check_in_radius,
        longitude,
        latitude
    ):
        if name is not None:
            work_shift.name = name

        if start_time is not None:
            work_shift.start_time = start_time

        if end_time is not None:
            work_shift.end_time = end_time

        if allowed_check_in_time is not None:
            work_shift.allowed_check_in_time = allowed_check_in_time

        if check_in_radius is not None:
            work_shift.check_in_radius = check_in_radius

        if longitude is not None:
            work_shift.longitude = longitude

        if latitude is not None:
            work_shift.latitude = latitude

        await self.db.commit()
        await self.db.refresh(work_shift)

        return work_shift

    async def deactivate_shift(self, work_shift):
        work_shift.status = "inactive"

        await self.db.commit()
        await self.db.refresh(work_shift)

        return work_shift