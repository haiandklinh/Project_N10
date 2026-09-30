from fastapi import APIRouter, Depends

from app.api.deps import get_current_account, get_staff_service
from schemas.manager import CreateProfileRequest
from schemas.staff import ProfileResponse, UpdateProfileRequest, ListStaffWorkScheduleResponse, StaffShiftChangeRequest, \
    StaffLeaveRequest

router = APIRouter(
    prefix="/staffs",
    tags=["Staff"]
)


@router.post('/profile',response_model=ProfileResponse)
async def create_profile(request: CreateProfileRequest ,
    service =Depends(get_staff_service),
    id_account = Depends(get_current_account)) :
    return await service.create_profile(request,id_account)

@router.get('/profile', response_model=ProfileResponse)
async def get_profile(
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
):
    return await service.get_profile(id_account)


@router.patch('/profile', response_model=ProfileResponse)
async def update_profile(
    request: UpdateProfileRequest,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
):
    return await service.update_profile(request, id_account)

@router.get('/me/work-schedules',response_model=ListStaffWorkScheduleResponse)
async def get_work_schedules(
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
):
    return await service.get_work_schedules(id_account)

@router.post('/work-schedules-requests')
async def request_shift_change(
    request: StaffShiftChangeRequest,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
) :
    return await service.request_shift_change(request ,id_account)

@router.post('/leave-requests')
async def create_leave_request(
    request : StaffLeaveRequest,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
) :
    return await service.create_leave_request(request , id_account)
