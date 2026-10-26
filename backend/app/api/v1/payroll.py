from uuid import UUID

from fastapi import APIRouter, Depends

from app.api.deps import get_payroll_service, get_current_account
from schemas.payroll import (
    ListStaffByCompany, StaffPayrollSummary,
    CalculatePayrollRequest, PayrollResponse,
    CreateBonusRequest, BonusResponse,
    CreateDeductionRequest, DeductionResponse,
    ListPayrollHistory
)

router = APIRouter(tags=["Payroll"])


@router.get('/managers/me/staffs', response_model=ListStaffByCompany)
async def get_staffs_in_company(
    service=Depends(get_payroll_service),
    id_account=Depends(get_current_account)
):
    return await service.get_staffs_in_company(id_account)


@router.get('/managers/me/staffs/{id_staff}/payroll-summary', response_model=StaffPayrollSummary)
async def get_staff_payroll_summary(
    id_staff: UUID,
    service=Depends(get_payroll_service),
    id_account=Depends(get_current_account)
):
    return await service.get_staff_summary(id_staff, id_account)


@router.post('/managers/me/payrolls', response_model=PayrollResponse)
async def calculate_payroll(
    request: CalculatePayrollRequest,
    service=Depends(get_payroll_service),
    id_account=Depends(get_current_account)
):
    return await service.calculate_payroll(request, id_account)


@router.post('/managers/me/bonuses', response_model=BonusResponse)
async def create_bonus(
    request: CreateBonusRequest,
    service=Depends(get_payroll_service),
    id_account=Depends(get_current_account)
):
    return await service.create_bonus(request, id_account)


@router.post('/managers/me/deductions', response_model=DeductionResponse)
async def create_deduction(
    request: CreateDeductionRequest,
    service=Depends(get_payroll_service),
    id_account=Depends(get_current_account)
):
    return await service.create_deduction(request, id_account)


@router.get('/staffs/me/payrolls', response_model=ListPayrollHistory)
async def get_payroll_history(
    id_manager: UUID | None = None,
    service=Depends(get_payroll_service),
    id_account=Depends(get_current_account)
):
    return await service.get_payroll_history(id_account, id_manager)


@router.get('/managers/me/payrolls', response_model=ListManagerPayrollHistory)
async def get_payroll_history_by_manager(
        id_staff: UUID | None = None,
        service=Depends(get_payroll_service),
        id_account=Depends(get_current_account)
):
    return await service.get_payroll_history_by_manager(id_account, id_staff)
