from fastapi import HTTPException

from repositories.shift_repository import ShiftRepository
from schemas.shift import (
    CreateShiftRequest,
    UpdateShiftRequest,
    ShiftResponse,
    ListShiftResponse
)


class ShiftService:

    def __init__(self, repository: ShiftRepository):
        self.repository = repository

    # 1. Lấy danh sách ca làm

    async def get_all_shifts(
        self,
        id_account
    ) -> ListShiftResponse:

        manager = await self.repository.get_manager_by_id_account(
            id_account
        )

        if manager is None:
            raise HTTPException(
                404,
                "Not found Manager in the system"
            )

        work_shifts = await self.repository.get_all_active_shifts(
            manager.id
        )

        result = [
            ShiftResponse(
                id=work_shift.id,
                name=work_shift.name,
                start_time=work_shift.start_time,
                end_time=work_shift.end_time,
                allowed_check_in_time=work_shift.allowed_check_in_time,
                check_in_radius=work_shift.check_in_radius,
                longitude=work_shift.longitude,
                latitude=work_shift.latitude,
                status=work_shift.status
            )
            for work_shift in work_shifts
        ]

        return ListShiftResponse(
            data=result
        )

    # 2. Lấy chi tiết ca làm

    async def get_shift_detail(
        self,
        id_shift,
        id_account
    ) -> ShiftResponse:

        manager = await self.repository.get_manager_by_id_account(
            id_account
        )

        if manager is None:
            raise HTTPException(
                404,
                "Not found Manager in the system"
            )

        work_shift = await self.repository.get_shift_by_id(
            id_shift,
            manager.id
        )

        if work_shift is None:
            raise HTTPException(
                404,
                "Work shift not found"
            )

        return ShiftResponse(
            id=work_shift.id,
            name=work_shift.name,
            start_time=work_shift.start_time,
            end_time=work_shift.end_time,
            allowed_check_in_time=work_shift.allowed_check_in_time,
            check_in_radius=work_shift.check_in_radius,
            longitude=work_shift.longitude,
            latitude=work_shift.latitude,
            status=work_shift.status
        )

    # 3. Tạo ca làm

    async def create_shift(
        self,
        request: CreateShiftRequest,
        id_account
    ) -> ShiftResponse:

        manager = await self.repository.get_manager_by_id_account(
            id_account
        )

        if manager is None:
            raise HTTPException(
                404,
                "Not found Manager in the system"
            )

        work_shift = await self.repository.create_shift(
            manager.id,
            request.name,
            request.start_time,
            request.end_time,
            request.allowed_check_in_time,
            request.check_in_radius,
            request.longitude,
            request.latitude
        )

        return ShiftResponse(
            id=work_shift.id,
            name=work_shift.name,
            start_time=work_shift.start_time,
            end_time=work_shift.end_time,
            allowed_check_in_time=work_shift.allowed_check_in_time,
            check_in_radius=work_shift.check_in_radius,
            longitude=work_shift.longitude,
            latitude=work_shift.latitude,
            status=work_shift.status
        )

    # 4. Cập nhật ca làm

    async def update_shift(
        self,
        id_shift,
        request: UpdateShiftRequest,
        id_account
    ) -> ShiftResponse:

        manager = await self.repository.get_manager_by_id_account(
            id_account
        )

        if manager is None:
            raise HTTPException(
                404,
                "Not found Manager in the system"
            )

        work_shift = await self.repository.get_shift_by_id(
            id_shift,
            manager.id
        )

        if work_shift is None:
            raise HTTPException(
                404,
                "Work shift not found"
            )

        if work_shift.status != "active":
            raise HTTPException(
                400,
                "Work shift is inactive"
            )

        work_shift = await self.repository.update_shift(
            work_shift,
            request.name,
            request.start_time,
            request.end_time,
            request.allowed_check_in_time,
            request.check_in_radius,
            request.longitude,
            request.latitude
        )

        return ShiftResponse(
            id=work_shift.id,
            name=work_shift.name,
            start_time=work_shift.start_time,
            end_time=work_shift.end_time,
            allowed_check_in_time=work_shift.allowed_check_in_time,
            check_in_radius=work_shift.check_in_radius,
            longitude=work_shift.longitude,
            latitude=work_shift.latitude,
            status=work_shift.status
        )

    # 5. Ngừng hoạt động ca làm

    async def deactivate_shift(
        self,
        id_shift,
        id_account
    ) -> ShiftResponse:

        manager = await self.repository.get_manager_by_id_account(
            id_account
        )

        if manager is None:
            raise HTTPException(
                404,
                "Not found Manager in the system"
            )

        work_shift = await self.repository.get_shift_by_id(
            id_shift,
            manager.id
        )

        if work_shift is None:
            raise HTTPException(
                404,
                "Work shift not found"
            )

        if work_shift.status != "active":
            raise HTTPException(
                400,
                "Work shift is already inactive"
            )

        work_shift = await self.repository.deactivate_shift(
            work_shift
        )

        return ShiftResponse(
            id=work_shift.id,
            name=work_shift.name,
            start_time=work_shift.start_time,
            end_time=work_shift.end_time,
            allowed_check_in_time=work_shift.allowed_check_in_time,
            check_in_radius=work_shift.check_in_radius,
            longitude=work_shift.longitude,
            latitude=work_shift.latitude,
            status=work_shift.status
        )