import asyncio
from core.config import engine
from db.base import Base
from models import account ,attendance_record , bonus_sheet ,complain , deduction ,manager, manager_staff , notification,payroll,refresh_token,staff,work_shift,work_schedule

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


asyncio.run(init_db())