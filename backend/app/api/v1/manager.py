from fastapi import APIRouter, Depends

from app.api.deps import get_manager_service, get_current_account
from schemas.manager import ProfileResponse, CreateProfileRequest, UpdateProfileRequest, NotificationResponse, \
    ListNotificationResponse

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