from datetime import datetime
from uuid import UUID

from fastapi import HTTPException

from models.bonus_sheet import BonusSheet
from models.deduction import Deduction
from models.payroll import Payroll
from repositories.payroll_repository import PayrollRepository, PayrollConflictError, APPROVED
from schemas.payroll import (
    ListStaffByCompany, StaffByCompany,
    StaffPayrollSummary, WorkShiftItem, BonusItem, DeductionItem,
    CalculatePayrollRequest, PayrollResponse,
    CreateBonusRequest, BonusResponse,
    CreateDeductionRequest, DeductionResponse,
    PayrollHistoryItem, ListPayrollHistory
)


class PayrollService:

    def __init__(self, repository: PayrollRepository):
        self.repository = repository

    # ---------- helper ----------
    async def _get_manager(self, id_account):
        manager = await self.repository.get_manager_by_id_account(id_account)
        if manager is None:
            raise HTTPException(404, "Not found Manager")
        return manager

    async def _get_manager_and_staff(self, id_account, id_staff):
        manager = await self._get_manager(id_account)
        staff = await self.repository.get_staff_in_company(manager.id, id_staff)
        if staff is None:
            raise HTTPException(404, "Staff not in Company")
        return manager, staff

    async def _get_unpaid_data(self, id_manager, id_staff, until):
        """Ca (từ lần tính lương trước đến `until`), thưởng và phạt chưa gắn vào bảng lương nào."""
        last_payroll = await self.repository.get_last_payroll(id_manager, id_staff)
        since = last_payroll.time_create if last_payroll else None

        shift_rows = await self.repository.get_shifts_since(id_manager, id_staff, since, until)
        bonuses = await self.repository.get_unpaid_bonuses(id_manager, id_staff)
        deductions = await self.repository.get_unpaid_deductions(id_manager, id_staff)
        return shift_rows, bonuses, deductions

    async def get_staffs_in_company(self, id_account) -> ListStaffByCompany:
        manager = await self._get_manager(id_account)
        rows = await self.repository.get_staffs_by_manager(manager.id)

        return ListStaffByCompany(
            data=[
                StaffByCompany(
                    id_staff=staff.id,
                    name_staff=staff.full_name,
                    position=position,
                    date_of_birth=staff.date_of_birth,
                    gender=staff.gender,
                    status=staff.status,
                    avatar=staff.avatar
                )
                for staff, position in rows
            ]
        )

    async def get_staff_summary(self, id_staff, id_account) -> StaffPayrollSummary:
        manager, staff = await self._get_manager_and_staff(id_account, id_staff)
        shift_rows, bonuses, deductions = await self._get_unpaid_data(manager.id, staff.id, datetime.now())

        return StaffPayrollSummary(
            id_staff=staff.id,
            number_of_work_shifts=len(shift_rows),
            total_bonus=sum(b.bonusAmount or 0 for b in bonuses),
            total_deduction=sum(d.amount for d in deductions),
            shifts=[
                WorkShiftItem(
                    id_attendance=record.id,
                    work_day=schedule.work_day,
                    shift_name=shift.name,
                    check_in_time=record.check_in_time,
                    check_out_time=record.check_out_time,
                    status=record.status
                )
                for record, schedule, shift in shift_rows
            ],
            bonuses=[
                BonusItem(
                    id_bonus=b.id,
                    bonus_amount=b.bonusAmount or 0,
                    reason=b.reason,
                    note=b.note
                )
                for b in bonuses
            ],
            deductions=[
                DeductionItem(
                    id_deduction=d.id,
                    amount=d.amount,
                    deduction_type=d.deduction_type,
                    reason=d.reason,
                    created_at=d.created_at
                )
                for d in deductions
            ]
        )

    async def calculate_payroll(self, request: CalculatePayrollRequest, id_account) -> PayrollResponse:
        manager, staff = await self._get_manager_and_staff(id_account, request.id_staff)

        now = datetime.now()
        shift_rows, bonuses, deductions = await self._get_unpaid_data(manager.id, staff.id, now)

        if not shift_rows and not bonuses and not deductions:
            raise HTTPException(400, "No unpaid work to calculate")

        number_of_shifts = len(shift_rows)
        total_bonus = sum(b.bonusAmount or 0 for b in bonuses)
        total_deduction = sum(d.amount for d in deductions)

        # lương = cơ bản * số ca + thưởng - phạt
        total_salary = round(request.base_salary * number_of_shifts + total_bonus - total_deduction)

        payroll = Payroll(
            id_staff=staff.id,
            id_manager=manager.id,
            time_create=now,
            base_salary=request.base_salary,
            number_of_work_shifts=number_of_shifts,
            total_salary=total_salary,
            status="PENDING"
        )

        try:
            payroll = await self.repository.create_payroll(
                payroll,
                [b.id for b in bonuses],
                [d.id for d in deductions]
            )
        except PayrollConflictError:
            raise HTTPException(409, "Payroll data changed, please try again")

        return PayrollResponse(
            id_payroll=payroll.id,
            id_staff=payroll.id_staff,
            name_staff=staff.full_name,
            base_salary=payroll.base_salary,
            number_of_work_shifts=payroll.number_of_work_shifts,
            total_salary=payroll.total_salary,
            status=payroll.status,
            time_create=payroll.time_create
        )

    async def create_bonus(self, request: CreateBonusRequest, id_account) -> BonusResponse:
        manager, staff = await self._get_manager_and_staff(id_account, request.id_staff)

        bonus = await self.repository.create_bonus(
            BonusSheet(
                id_staff=staff.id,
                id_manager=manager.id,
                bonusAmount=request.bonus_amount,
                reason=request.reason,
                note=request.note,
                status=APPROVED
            )
        )

        return BonusResponse(
            id_bonus=bonus.id,
            id_staff=bonus.id_staff,
            name_staff=staff.full_name,
            bonus_amount=bonus.bonusAmount,
            reason=bonus.reason,
            note=bonus.note,
            status=bonus.status
        )

    async def create_deduction(self, request: CreateDeductionRequest, id_account) -> DeductionResponse:
        manager, staff = await self._get_manager_and_staff(id_account, request.id_staff)

        deduction = await self.repository.create_deduction(
            Deduction(
                id_staff=staff.id,
                id_manager=manager.id,
                deduction_type=request.deduction_type,
                reason=request.reason,
                amount=request.amount,
                status=APPROVED,
                created_at=datetime.now()
            )
        )

        return DeductionResponse(
            id_deduction=deduction.id,
            id_staff=deduction.id_staff,
            name_staff=staff.full_name,
            amount=deduction.amount,
            deduction_type=deduction.deduction_type,
            reason=deduction.reason,
            status=deduction.status,
            created_at=deduction.created_at
        )

    async def get_payroll_history(self, id_account, id_manager: UUID | None = None) -> ListPayrollHistory:
        staff = await self.repository.get_staff_by_id_account(id_account)
        if staff is None:
            raise HTTPException(404, "Not found Staff in the system")

        rows = await self.repository.get_payrolls_by_staff(staff.id, id_manager)

        return ListPayrollHistory(
            data=[
                PayrollHistoryItem(
                    id_payroll=payroll.id,
                    id_manager=manager.id,
                    name_company=manager.company,
                    name_manager=manager.full_name,
                    base_salary=payroll.base_salary,
                    number_of_work_shifts=payroll.number_of_work_shifts,
                    total_salary=payroll.total_salary,
                    status=payroll.status,
                    time_create=payroll.time_create
                )
                for payroll, manager in rows
            ]
        )

    async def get_payroll_history_by_manager(self, id_account,
                                             id_staff: UUID | None = None) -> ListManagerPayrollHistory:
        manager = await self._get_manager(id_account)

        if id_staff is not None:
            staff = await self.repository.get_staff_in_company(manager.id, id_staff)
            if staff is None:
                raise HTTPException(404, "Staff not in Company")

        rows = await self.repository.get_payrolls_by_manager(manager.id, id_staff)

        return ListManagerPayrollHistory(
            data=[
                ManagerPayrollHistoryItem(
                    id_payroll=payroll.id,
                    id_staff=staff.id,
                    name_staff=staff.full_name,
                    base_salary=payroll.base_salary,
                    number_of_work_shifts=payroll.number_of_work_shifts,
                    total_salary=payroll.total_salary,
                    status=payroll.status,
                    time_create=payroll.time_create
                )
                for payroll, staff in rows
            ]
        )
