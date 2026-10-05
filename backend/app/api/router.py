from fastapi import APIRouter

from app.api.v1.auth import router as auth_router
from app.api.v1.staff import router as staff_router
from app.api.v1.manager import router as manager_router
from app.api.v1.ws import router as ws_router
from app.api.v1.work_schedule import router as workschedule_router
from app.api.v1.shift import router as shift_router



api_router = APIRouter()

api_router.include_router(auth_router)
api_router.include_router(staff_router)
api_router.include_router(manager_router)
api_router.include_router(ws_router)
api_router.include_router(workschedule_router)
api_router.include_router(shift_router)


