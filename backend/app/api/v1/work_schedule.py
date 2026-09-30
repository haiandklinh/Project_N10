from fastapi import APIRouter, Depends

from app.api.deps import get_current_account, get_work_schedule_service
from schemas.work_schedule import ListStaffWorkScheduleResponse, StaffWorkScheduleResponse, StaffWorkScheduleRequest

router = APIRouter(
    prefix="/work-schedules",
    tags=["Staff"]
)


@router.get('', response_model=ListStaffWorkScheduleResponse)
async def get_list_staff_work_schedule(
    service =Depends(get_work_schedule_service),
    id_account = Depends(get_current_account)
):
   return await service.get_list_staff_work_schedule(id_account)

@router.post('',response_model=StaffWorkScheduleResponse)
async def assign_work_shifts(
        request : StaffWorkScheduleRequest,
        service=Depends(get_work_schedule_service),
        id_account=Depends(get_current_account)
):
    return await service.assign_work_shifts(request, id_account)