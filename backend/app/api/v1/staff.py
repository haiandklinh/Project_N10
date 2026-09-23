from fastapi import APIRouter, Depends

from backend.app.api.deps import get_current_account, get_staff_service
from backend.schemas.manager import CreateProfileRequest
from backend.schemas.staff import ProfileResponse

router = APIRouter(
    prefix="/tenants",
    tags=["Tenant"]
)


@router.post('/profile',response_model=ProfileResponse)
async def create_profile(request: CreateProfileRequest ,
    service =Depends(get_staff_service),
    id_account = Depends(get_current_account)) :
    return await service.create_profile(request,id_account)