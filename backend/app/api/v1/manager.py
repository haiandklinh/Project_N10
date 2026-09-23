from fastapi import APIRouter, Depends

from backend.app.api.deps import get_manager_service, get_current_account
from backend.schemas.manager import ProfileResponse, CreateProfileRequest

router = APIRouter(
    prefix="/auth",
    tags=["Auth"]
)


@router.post('/profile',response_model=ProfileResponse)
async def create_profile(request: CreateProfileRequest ,
    service =Depends(get_manager_service()),
    id_account = Depends(get_current_account)) :
    return await service.create_profile(request,id_account)