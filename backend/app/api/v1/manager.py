from datetime import time

from fastapi import APIRouter, Depends

from app.api.deps import get_manager_service, get_current_account
from schemas.manager import ProfileResponse, CreateProfileRequest, UpdateProfileRequest, NotificationResponse, \
    ListNotificationResponse, InvitationCodeResponse, ShiftChangeRequest, LeaveDayRequest, JoinRequest

router = APIRouter(
    prefix="/managers",
    tags=["Manager"]
)


@router.post('/profile',response_model=ProfileResponse)
async def create_profile(request: CreateProfileRequest ,
    service =Depends(get_manager_service),
    id_account = Depends(get_current_account)) :
    return await service.create_profile(request,id_account)


@router.get('/profile', response_model=ProfileResponse)
async def get_profile(
    service=Depends(get_manager_service),
    id_account=Depends(get_current_account)
):
    return await service.get_profile(id_account)


@router.patch('/profile', response_model=ProfileResponse)
async def update_profile(
    request: UpdateProfileRequest,
    service=Depends(get_manager_service),
    id_account=Depends(get_current_account)
):
    return await service.update_profile(request, id_account)

@router.get('/me/notifications',response_model=ListNotificationResponse)
async def get_notification(
    id_account = Depends(get_current_account),
    service = Depends(get_manager_service)
) :
    return service.get_notification(id_account)

@router.get('/invitation-code',response_model=InvitationCodeResponse)
async def get_invitation_code(
    expires_in : time ,
    id_account=Depends(get_current_account),
    service=Depends(get_manager_service)
):
    return service.get_invitation_code(id_account,expires_in)

# Quản lý xử lý yêu cầu đổi ca
@router.patch("/shift-change-requests")
async def handle_shift_change_request(
    request: ShiftChangeRequest ,
    service=Depends(get_manager_service),
    id_account=Depends(get_current_account)
):
    return await service.handle_shift_change_request(request,id_account)


# Quản lý xử lý yêu cầu nghỉ phép
@router.patch("/leave-requests")
async def handle_leave_request(
    request :LeaveDayRequest,
    service=Depends(get_manager_service),
    id_account=Depends(get_current_account)
):
    return await service.handle_leave_request(request,id_account)


# Quản lý xử lý yêu cầu tham gia
@router.patch("/join-requests")
async def handle_join_request(
    request : JoinRequest ,
    service=Depends(get_manager_service),
    id_account=Depends(get_current_account)
):
    return await service.handle_join_request(request,id_account)