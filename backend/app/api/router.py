from fastapi import APIRouter

from app.api.v1.auth import router as auth_router
from app.api.v1.staff import router as staff_router
from app.api.v1.manager import router as manager_router

api_router = APIRouter()

api_router.include_router(auth_router)
api_router.include_router(staff_router)
api_router.include_router(manager_router)
