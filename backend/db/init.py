from backend.core.config import engine
from backend.db.base import Base
from backend.models import account ,attendance_record , bonus_sheet ,complain , deduction ,manager, manager_staff,manager_workshift , notification,payroll,refresh_token,staff,work_shift

# Base.metadata.create_all(bind=engine)
# print(Base.metadata.tables.keys())