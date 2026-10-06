from fastapi import APIRouter, Depends

from app.api.deps import get_current_account, get_staff_service
from schemas.staff import ProfileResponse, UpdateProfileRequest, ListStaffWorkScheduleResponse, StaffShiftChangeRequest, \
    StaffLeaveRequest, JoinRequest, JoinResponse, CreateProfileRequest, ListNotificationResponse

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

@router.get('/invitation_code')
async def get_id_manger_by_invitation_code(
    invitation_code :str  ,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
) :
    return await service.get_id_manger_by_invitation_code(invitation_code,id_account)

@router.post('/join-requests',response_model=JoinResponse)
async def send_join_request(
    request: JoinRequest,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
):
    return await service.send_join_request(request,id_account)

@router.get('/me/work-schedules',response_model=ListStaffWorkScheduleResponse)
async def get_work_schedules(
    id_manager :str ,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
):
    return await service.get_work_schedules(id_account,id_manager)

@router.get('/me/notifications',response_model=ListNotificationResponse)
async def get_notification(
    id_manager: str ,
    id_account = Depends(get_current_account),
    service = Depends(get_staff_service)
) :
    return await service.get_notification(id_account,id_manager)

@router.post('/work-schedules-requests')
async def request_shift_change(
    id_manager:str ,
    request: StaffShiftChangeRequest,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
):
    return await service.request_shift_change(request ,id_account,id_manager)

@router.post('/leave-requests')
async def create_leave_request(
    id_manager :str ,
    request : StaffLeaveRequest,
    service=Depends(get_staff_service),
    id_account=Depends(get_current_account)
) :
    return await service.create_leave_request(request , id_account,id_manager)

