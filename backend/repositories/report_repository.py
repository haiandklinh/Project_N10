from uuid import UUID

from sqlalchemy import and_, case, extract, func, literal, literal_column, or_, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import aliased

from models.attendance_record import AttendanceRecord
from models.bonus_sheet import BonusSheet
from models.deduction import Deduction
from models.manager import Manager
from models.manager_staff import ManagerStaff
from models.payroll import Payroll
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift


class ReportRepository:

    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_staffs_leave_early(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ):
        query = self._attendance_query(id_account).where(
            AttendanceRecord.check_in_time.is_not(None),
            AttendanceRecord.check_out_time < self._scheduled_end(),
        )
        return await self._get_page(query, month, week, year, page, limit)

    async def get_staffs_arrive_late(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ):
        query = self._attendance_query(id_account).where(
            AttendanceRecord.check_in_time > self._scheduled_start(),
            AttendanceRecord.check_out_time.is_not(None),
        )
        return await self._get_page(query, month, week, year, page, limit)

    async def get_staffs_absent(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ):
        same_day_schedule = aliased(WorkSchedule)
        same_day_shift = aliased(WorkShift)
        has_attendance = (
            select(AttendanceRecord.id)
            .join(same_day_schedule, AttendanceRecord.id_work_schedule == same_day_schedule.id)
            .join(same_day_shift, same_day_schedule.id_work_shift == same_day_shift.id)
            .where(
                same_day_schedule.id_staff == WorkSchedule.id_staff,
                same_day_schedule.work_day == WorkSchedule.work_day,
                same_day_schedule.status.is_(True),
                same_day_shift.id_manager == Manager.id,
                or_(
                    AttendanceRecord.check_in_time.is_not(None),
                    AttendanceRecord.check_out_time.is_not(None),
                ),
            )
            .exists()
        )
        query = self._schedule_query(id_account).where(~has_attendance)
        return await self._get_page(query, month, week, year, page, limit)

    async def get_staffs_not_finish__work_shift(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ):
        query = self._attendance_query(id_account).where(
            or_(
                and_(AttendanceRecord.check_in_time.is_not(None), AttendanceRecord.check_out_time.is_(None)),
                and_(AttendanceRecord.check_in_time.is_(None), AttendanceRecord.check_out_time.is_not(None)),
            )
        )
        return await self._get_page(query, month, week, year, page, limit)

    async def get_payroll_report(
        self, id_account: UUID, month: int | None, week: int | None,
        year: int | None, page: int, limit: int,
    ):
        manager_ids = select(Manager.id).where(Manager.id_account == id_account)
        salaries = (
            select(
                Payroll.id_staff,
                func.sum(Payroll.total_salary).label("net_salary"),
                func.max(Payroll.time_create).label("receive_time"),
            )
            .where(
                Payroll.id_manager.in_(manager_ids),
                *self._period_filters(Payroll.time_create, month, week, year),
            )
            .group_by(Payroll.id_staff)
            .subquery()
        )
        totals_query = (
            select(
                func.coalesce(func.sum(salaries.c.net_salary), 0.0).label("total_payable_salary"),
                func.count().label("total_records"),
            )
            .select_from(salaries)
        )
        totals = (await self.db.execute(totals_query)).one()

        staff_page = (
            select(
                Staff.id, Staff.full_name, Staff.gender, Staff.avatar,
                salaries.c.net_salary, salaries.c.receive_time,
            )
            .join(salaries, salaries.c.id_staff == Staff.id)
            .order_by(Staff.full_name, Staff.id)
            .offset((page - 1) * limit)
            .limit(limit)
            .cte("staff_page")
        )
        page_staff_ids = select(staff_page.c.id)
        shifts = self._shift_totals(
            page_staff_ids, month, week, year, WorkShift.id_manager.in_(manager_ids),
        )
        bonus = self._bonus_total(
            month, week, year, BonusSheet.id_staff == staff_page.c.id,
            BonusSheet.id_manager.in_(manager_ids),
        )
        penalty = self._amount_total(
            Deduction.amount, Deduction.id_staff == staff_page.c.id,
            Deduction.id_manager.in_(manager_ids),
            *self._period_filters(Deduction.created_at, month, week, year),
        )
        details_query = (
            select(
                staff_page.c.full_name.label("name"),
                staff_page.c.gender,
                staff_page.c.avatar,
                func.coalesce(shifts.c.total_shifts, 0).label("total_shifts"),
                (func.coalesce(shifts.c.working_seconds, 0) / 3600).label("total_hours"),
                bonus.label("bonus"),
                penalty.label("penalty"),
                staff_page.c.net_salary,
                staff_page.c.receive_time,
            )
            .select_from(staff_page)
            .outerjoin(shifts, shifts.c.id_staff == staff_page.c.id)
            .order_by(staff_page.c.full_name, staff_page.c.id)
        )
        records = (await self.db.execute(details_query)).all()
        return records, totals.total_payable_salary, totals.total_records

    async def get_staff_overview_report(
        self, id_account: UUID, month: int | None, week: int | None, year: int | None,
        *, id_manager: UUID,
    ):
        staff_ids = (
            select(Staff.id)
            .join(ManagerStaff, ManagerStaff.id_staff == Staff.id)
            .where(Staff.id_account == id_account, ManagerStaff.id_manager == id_manager)
        )
        shifts = self._shift_totals(staff_ids, month, week, year, WorkShift.id_manager == id_manager)
        bonus = self._bonus_total(
            month, week, year, BonusSheet.id_staff.in_(staff_ids), BonusSheet.id_manager == id_manager,
        )
        penalty = self._amount_total(
            Deduction.amount, Deduction.id_staff.in_(staff_ids), Deduction.id_manager == id_manager,
            *self._period_filters(Deduction.created_at, month, week, year),
        )
        salary = self._amount_total(
            Payroll.total_salary, Payroll.id_staff.in_(staff_ids), Payroll.id_manager == id_manager,
            *self._period_filters(Payroll.time_create, month, week, year),
        )
        assigned = func.coalesce(func.sum(shifts.c.total_shifts), 0)
        completed = func.coalesce(func.sum(shifts.c.completed_shifts), 0)
        query = select(
            assigned.label("total_assigned_shifts"),
            completed.label("completed_shifts"),
            (assigned - completed).label("incomplete_shifts"),
            func.coalesce(func.sum(shifts.c.late_arrivals), 0).label("late_arrivals"),
            func.coalesce(func.sum(shifts.c.early_leaves), 0).label("early_leaves"),
            (func.coalesce(func.sum(shifts.c.working_seconds), 0) / 3600).label("total_working_hours"),
            bonus.label("total_bonus"),
            penalty.label("total_penalty"),
            salary.label("expected_salary"),
        ).select_from(shifts)
        return (await self.db.execute(query)).mappings().one()

    @classmethod
    def _shift_totals(cls, staff_ids, month, week, year, *conditions):
        check_in = AttendanceRecord.check_in_time
        check_out = AttendanceRecord.check_out_time
        duration = case((check_out > check_in, func.timestampdiff(literal_column("SECOND"), check_in, check_out)))
        # Collapse repeated attendance records before counting shifts or summing their hours.
        per_shift = (
            select(
                WorkSchedule.id_staff,
                func.max(duration).label("working_seconds"),
                case((func.min(check_in) > cls._scheduled_start(), 1), else_=0).label("late"),
                case((func.max(check_out) < cls._scheduled_end(), 1), else_=0).label("early"),
            )
            .select_from(WorkSchedule)
            .join(WorkShift, WorkSchedule.id_work_shift == WorkShift.id)
            .outerjoin(AttendanceRecord, AttendanceRecord.id_work_schedule == WorkSchedule.id)
            .where(
                WorkSchedule.id_staff.in_(staff_ids), WorkSchedule.status.is_(True), *conditions,
                *cls._period_filters(WorkSchedule.work_day, month, week, year),
            )
            .group_by(WorkSchedule.id, WorkSchedule.id_staff, WorkSchedule.work_day, WorkShift.start_time, WorkShift.end_time)
            .subquery()
        )
        return (
            select(
                per_shift.c.id_staff,
                func.count().label("total_shifts"),
                func.count(per_shift.c.working_seconds).label("completed_shifts"),
                func.sum(per_shift.c.late).label("late_arrivals"),
                func.sum(per_shift.c.early).label("early_leaves"),
                func.sum(per_shift.c.working_seconds).label("working_seconds"),
            )
            .group_by(per_shift.c.id_staff)
            .subquery()
        )

    @staticmethod
    def _amount_total(amount, *conditions):
        return select(func.coalesce(func.sum(amount), 0.0)).where(*conditions).scalar_subquery()

    @classmethod
    def _bonus_total(cls, month, week, year, *conditions):
        # BonusSheet has no date or payroll reference to identify a reporting period.
        if any(value is not None for value in (month, week, year)):
            return literal(0.0)
        return cls._amount_total(BonusSheet.bonusAmount, *conditions)

    @staticmethod
    def _scheduled_start():
        return func.timestamp(WorkSchedule.work_day, WorkShift.start_time)

    @staticmethod
    def _scheduled_end():
        end_day = func.adddate(
            WorkSchedule.work_day,
            case((WorkShift.end_time < WorkShift.start_time, 1), else_=0),
        )
        return func.timestamp(end_day, WorkShift.end_time)

    @staticmethod
    def _schedule_query(id_account: UUID):
        return (
            select(Staff, WorkShift)
            .select_from(WorkSchedule)
            .join(Staff, WorkSchedule.id_staff == Staff.id)
            .join(WorkShift, WorkSchedule.id_work_shift == WorkShift.id)
            .join(Manager, WorkShift.id_manager == Manager.id)
            .join(
                ManagerStaff,
                and_(ManagerStaff.id_manager == Manager.id, ManagerStaff.id_staff == Staff.id),
            )
            .where(Manager.id_account == id_account, WorkSchedule.status.is_(True))
            .order_by(WorkSchedule.work_day.desc(), WorkSchedule.id)
        )

    def _attendance_query(self, id_account: UUID):
        return (
            self._schedule_query(id_account)
            .join(AttendanceRecord, AttendanceRecord.id_work_schedule == WorkSchedule.id)
            .add_columns(AttendanceRecord)
            .order_by(AttendanceRecord.id)
        )

    async def _get_page(self, query, month, week, year, page, limit):
        query = query.where(*self._period_filters(WorkSchedule.work_day, month, week, year))
        count_query = query.order_by(None).with_only_columns(func.count())
        total = await self.db.scalar(count_query)
        result = await self.db.execute(query.offset((page - 1) * limit).limit(limit))
        return result.all(), total

    @staticmethod
    def _period_filters(date_column, month, week, year):
        filters = []
        if month is not None:
            filters.append(extract("month", date_column) == month)
        if week is not None:
            # MySQL WEEK mode 3 uses ISO week numbers starting on Monday.
            filters.append(func.week(date_column, 3) == week)
        if year is not None:
            filters.append(extract("year", date_column) == year)
        return filters