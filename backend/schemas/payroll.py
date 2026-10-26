from datetime import date, datetime
from uuid import UUID

from pydantic import Field

from schemas.base import BaseSchema


class StaffByCompany(BaseSchema):
    id_staff: UUID
    name_staff: str
    position: str | None = None
    date_of_birth: date
    gender: str
    status: str
    avatar: str | None = None


class ListStaffByCompany(BaseSchema):
    data: list[StaffByCompany]


class WorkShiftItem(BaseSchema):
    id_attendance: UUID
    work_day: date
    shift_name: str
    check_in_time: datetime
    check_out_time: datetime
    status: str | None = None


class BonusItem(BaseSchema):
    id_bonus: UUID
    bonus_amount: float
    reason: str | None = None
    note: str | None = None


class DeductionItem(BaseSchema):
    id_deduction: UUID
    amount: float
    deduction_type: str
    reason: str
    created_at: datetime


class StaffPayrollSummary(BaseSchema):
    id_staff: UUID
    number_of_work_shifts: int
    total_bonus: float
    total_deduction: float
    shifts: list[WorkShiftItem]
    bonuses: list[BonusItem]
    deductions: list[DeductionItem]


class CalculatePayrollRequest(BaseSchema):
    id_staff: UUID
    base_salary: float = Field(gt=0)


class PayrollResponse(BaseSchema):
    id_payroll: UUID
    id_staff: UUID
    name_staff: str
    base_salary: float
    number_of_work_shifts: int
    total_salary: float
    status: str
    time_create: datetime


class CreateBonusRequest(BaseSchema):
    id_staff: UUID
    bonus_amount: float = Field(gt=0)
    reason: str
    note: str = ""


class BonusResponse(BaseSchema):
    id_bonus: UUID
    id_staff: UUID
    name_staff: str
    bonus_amount: float
    reason: str | None = None
    note: str | None = None
    status: str


class CreateDeductionRequest(BaseSchema):
    id_staff: UUID
    amount: float = Field(gt=0)
    deduction_type: str
    reason: str


class DeductionResponse(BaseSchema):
    id_deduction: UUID
    id_staff: UUID
    name_staff: str
    amount: float
    deduction_type: str
    reason: str
    status: str
    created_at: datetime


# ---------- Lịch sử lương (nhân viên) ----------
class PayrollHistoryItem(BaseSchema):
    id_payroll: UUID
    id_manager: UUID
    name_company: str | None = None
    name_manager: str
    base_salary: float
    number_of_work_shifts: int
    total_salary: float
    status: str
    time_create: datetime


class ListPayrollHistory(BaseSchema):
    data: list[PayrollHistoryItem]
# ---------- Lịch sử trả lương của nhân viên (manager xem) ----------
class ManagerPayrollHistoryItem(BaseSchema):
    id_payroll: UUID
    id_staff: UUID
    name_staff: str
    base_salary: float
    number_of_work_shifts: int
    total_salary: float
    status: str
    time_create: datetime


class ListManagerPayrollHistory(BaseSchema):
    data: list[ManagerPayrollHistoryItem]