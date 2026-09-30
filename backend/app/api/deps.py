from fastapi import Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from core.config import SessionLocal
from core.security import verify_token
from repositories.account_repository import AccountRepository
from repositories.manager_repository import ManagerRepository
from repositories.staff_repository import StaffRepository
from repositories.work_schedule_repository import WorkScheduleRepository
from services.account_service import AccountService
from services.manager_service import ManagerService
from services.staff_service import StaffService
from services.work_schedule_service import WorkScheduleService

security = HTTPBearer()

async def get_db():
    async with SessionLocal() as db:
        yield db

def get_account_service(
    db=Depends(get_db)
):
    repository = AccountRepository(db)

    return AccountService(repository)


def get_staff_service(
    db = Depends(get_db)
):
    repository = StaffRepository(db)
    return StaffService(repository)

def get_manager_service(
    db = Depends(get_db)
):
    repository = ManagerRepository(db)
    return ManagerService(repository)

def get_work_schedule_service(
        db =Depends(get_db)
):
    repository = WorkScheduleRepository(db)
    return WorkScheduleService(repository)

def get_current_account(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) :
    return verify_token(credentials.credentials)




