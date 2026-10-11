from datetime import date
from typing import Optional

from fastapi import APIRouter, Depends, Query

from app.api.deps import get_current_account, get_attendance_service
from models.attendance_record import AttendanceRecord
from schemas.account import MessageResponse
from schemas.attendance import CheckinRequest, CheckoutRequest, ListAttendanceRecordResponse

router = APIRouter(
    prefix="/attendances",
    tags=["Attendance"]
)


@router.post('/check-in', response_model= MessageResponse)
async def check_in(
    request : CheckinRequest ,
    id_account=Depends(get_current_account) ,
    service = Depends(get_attendance_service)
) :
    return await service.check_in(request, id_account)

@router.patch('/check-out', response_model= MessageResponse)
async def check_out(
    request : CheckoutRequest ,
    id_account=Depends(get_current_account) ,
    service = Depends(get_attendance_service)
) :
    return await service.check_out(request, id_account)
@router.get('', response_model=ListAttendanceRecordResponse)
async def get_attendance_record(
    id_manager: str,
    start_date: Optional[date] = Query(None, description="Ngày bắt đầu (YYYY-MM-DD)"),
    end_date: Optional[date] = Query(None, description="Ngày kết thúc (YYYY-MM-DD)"),
    page: int = Query(1, ge=1, description="Trang hiện tại"),
    size: int = Query(30, ge=1, le=100, description="Số lượng bản ghi trên một trang"),
    id_account=Depends(get_current_account),
    service=Depends(get_attendance_service),
):
    return await service.get_attendance_record(
        id_manager,
        id_account=id_account,
        start_date=start_date,
        end_date=end_date,
        page=page,
        size=size
    )