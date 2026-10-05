from uuid import UUID

from fastapi import APIRouter, Depends

from app.api.deps import get_current_account, get_shift_service
from schemas.shift import (
    CreateShiftRequest,
    UpdateShiftRequest,
    ShiftResponse,
    ListShiftResponse
)


router = APIRouter(
    prefix="/shifts",
    tags=["Shift"]
)


@router.get(
    "",
    response_model=ListShiftResponse
)
async def get_all_shifts(
    service=Depends(get_shift_service),
    id_account=Depends(get_current_account)
):
    return await service.get_all_shifts(
        id_account
    )


@router.get(
    "/{id_shift}",
    response_model=ShiftResponse
)
async def get_shift_detail(
    id_shift: UUID,
    service=Depends(get_shift_service),
    id_account=Depends(get_current_account)
):
    return await service.get_shift_detail(
        id_shift,
        id_account
    )


@router.post(
    "",
    response_model=ShiftResponse
)
async def create_shift(
    request: CreateShiftRequest,
    service=Depends(get_shift_service),
    id_account=Depends(get_current_account)
):
    return await service.create_shift(
        request,
        id_account
    )


@router.patch(
    "/{id_shift}",
    response_model=ShiftResponse
)
async def update_shift(
    id_shift: UUID,
    request: UpdateShiftRequest,
    service=Depends(get_shift_service),
    id_account=Depends(get_current_account)
):
    return await service.update_shift(
        id_shift,
        request,
        id_account
    )


@router.delete(
    "/{id_shift}",
    response_model=ShiftResponse
)
async def delete_shift(
    id_shift: UUID,
    service=Depends(get_shift_service),
    id_account=Depends(get_current_account)
):
    return await service.deactivate_shift(
        id_shift,
        id_account
    )