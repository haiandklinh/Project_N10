import asyncio
from backend.core.config import engine
from backend.db.base import Base
from backend.models import account ,attendance_record , bonus_sheet ,complain , deduction ,manager, manager_staff,manager_workshift , notification,payroll,refresh_token,staff,work_shift

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


asyncio.run(init_db())