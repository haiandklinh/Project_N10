from uuid import UUID

from fastapi import HTTPException

from models.attendance_record import AttendanceRecord
from models.staff import Staff
from models.work_shift import WorkShift
from repositories.report_repository import ReportRepository
from schemas.report import (
    ListStaffAbsent,
    ListStaffArriveLate,
    ListStaffLeaveEarly,
    ListStaffNotFinishWorkShift,
    ListStaffSalary,
    StaffAbsent,
    StaffArriveLate,
    StaffLeaveEarly,
    StaffNotFinishWorkShift,
    StaffOverviewReport,
    StaffSalary,
)


class ReportService:

    def __init__(self, repository: ReportRepository):
        self.repository = repository

    async def get_staffs_leave_early(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ) -> ListStaffLeaveEarly:
        records, total = await self.repository.get_staffs_leave_early(
            id_account, month, week, year, page, limit,
        )
        data = [
            StaffLeaveEarly(**self._staff_fields(staff, shift), **self._attendance_fields(attendance))
            for staff, shift, attendance in records
        ]
        return ListStaffLeaveEarly(data=data, numbers=total)

    async def get_staffs_arrive_late(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ) -> ListStaffArriveLate:
        records, total = await self.repository.get_staffs_arrive_late(
            id_account, month, week, year, page, limit,
        )
        data = [
            StaffArriveLate(**self._staff_fields(staff, shift), **self._attendance_fields(attendance))
            for staff, shift, attendance in records
        ]
        return ListStaffArriveLate(data=data, numbers=total)

    async def get_staffs_absent(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ) -> ListStaffAbsent:
        records, total = await self.repository.get_staffs_absent(
            id_account, month, week, year, page, limit,
        )
        data = [StaffAbsent(**self._staff_fields(staff, shift)) for staff, shift in records]
        return ListStaffAbsent(data=data, numbers=total)

    async def get_staffs_not_finish__work_shift(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ) -> ListStaffNotFinishWorkShift:
        records, total = await self.repository.get_staffs_not_finish__work_shift(
            id_account, month, week, year, page, limit,
        )
        data = [
            StaffNotFinishWorkShift(**self._staff_fields(staff, shift), **self._attendance_fields(attendance))
            for staff, shift, attendance in records
        ]
        return ListStaffNotFinishWorkShift(data=data, numbers=total)

    async def get_payroll_report(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ) -> ListStaffSalary:
        records, total_payable_salary, _ = await self.repository.get_payroll_report(
            id_account, month, week, year, page, limit,
        )
        data = [
            StaffSalary(
                name=record.name,
                gender=record.gender,
                avatar_url=record.avatar or "",
                salary=record.net_salary,
                bonus_salary=record.bonus,
                deduction_salary=record.penalty,
                receive_time=record.receive_time,
            )
            for record in records
        ]
        return ListStaffSalary(data=data, total=total_payable_salary)

    async def get_staff_overview_report(
        self, id_account: UUID, month: int | None, week: int | None, year: int | None,
        *, id_manager: str | UUID | None = None,
    ) -> StaffOverviewReport:
        try:
            manager_id = UUID(str(id_manager))
        except ValueError as error:
            raise HTTPException(status_code=422, detail="id_manager must be a valid UUID") from error

        metrics = await self.repository.get_staff_overview_report(
            id_account, month, week, year, id_manager=manager_id,
        )
        return StaffOverviewReport.model_validate(metrics)

    @staticmethod
    def _staff_fields(staff: Staff, shift: WorkShift):
        return {
            "name": staff.full_name,
            "gender": staff.gender,
            "avatar_url": staff.avatar or "",
            "name_shift": shift.name,
            "start_time_shift": shift.start_time,
            "end_time_shift": shift.end_time,
        }

    @staticmethod
    def _attendance_fields(attendance: AttendanceRecord):
        check_in = attendance.check_in_time
        check_out = attendance.check_out_time
        return {
            "checkin_time_attendance": check_in.time() if check_in is not None else None,
            "checkout_time_attendance": check_out.time() if check_out is not None else None,
        }