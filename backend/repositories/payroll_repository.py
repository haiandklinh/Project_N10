from sqlalchemy import select, update
from sqlalchemy.ext.asyncio import AsyncSession

from models.attendance_record import AttendanceRecord
from models.bonus_sheet import BonusSheet
from models.deduction import Deduction
from models.manager import Manager
from models.manager_staff import ManagerStaff
from models.payroll import Payroll
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift

# Thưởng / phạt do manager tạo mặc định có status này; chỉ loại này được tính vào lương
APPROVED = "APPROVED"


class PayrollConflictError(Exception):
    """Thưởng / phạt vừa bị bảng lương khác lấy mất (2 request tính lương cùng lúc)."""


class PayrollRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    # ---------- Manager / Staff ----------
    async def get_manager_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Manager).where(Manager.id_account == id_account)
        )
        return result.scalar_one_or_none()

    async def get_staff_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Staff).where(Staff.id_account == id_account)
        )
        return result.scalar_one_or_none()

    async def get_staff_in_company(self, id_manager, id_staff):
        result = await self.db.execute(
            select(Staff)
            .join(ManagerStaff, ManagerStaff.id_staff == Staff.id)
            .where(
                ManagerStaff.id_manager == id_manager,
                ManagerStaff.id_staff == id_staff
            )
        )
        return result.scalar_one_or_none()

    async def get_staffs_by_manager(self, id_manager):
        result = await self.db.execute(
            select(Staff, ManagerStaff.position)
            .join(ManagerStaff, ManagerStaff.id_staff == Staff.id)
            .where(ManagerStaff.id_manager == id_manager)
            .order_by(Staff.full_name)
        )
        return result.all()

    # ---------- Dữ liệu CHƯA thanh toán ----------
    async def get_last_payroll(self, id_manager, id_staff):
        """Bảng lương gần nhất của nhân viên trong công ty (None nếu chưa từng tính)."""
        result = await self.db.execute(
            select(Payroll)
            .where(
                Payroll.id_manager == id_manager,
                Payroll.id_staff == id_staff
            )
            .order_by(Payroll.time_create.desc())
            .limit(1)
        )
        return result.scalar_one_or_none()

    async def get_shifts_since(self, id_manager, id_staff, since, until):
        """Ca đã check-in và check-out đầy đủ, check_out_time trong (since, until].
        since = None nghĩa là tính từ đầu."""
        conditions = [
            WorkSchedule.id_staff == id_staff,
            WorkShift.id_manager == id_manager,
            AttendanceRecord.check_in_time.is_not(None),
            AttendanceRecord.check_out_time.is_not(None),
            AttendanceRecord.check_out_time <= until
        ]
        if since is not None:
            conditions.append(AttendanceRecord.check_out_time > since)

        result = await self.db.execute(
            select(AttendanceRecord, WorkSchedule, WorkShift)
            .join(WorkSchedule, AttendanceRecord.id_work_schedule == WorkSchedule.id)
            .join(WorkShift, WorkSchedule.id_work_shift == WorkShift.id)
            .where(*conditions)
            .order_by(WorkSchedule.work_day, WorkShift.start_time)
        )
        return result.all()

    async def get_unpaid_bonuses(self, id_manager, id_staff):
        result = await self.db.execute(
            select(BonusSheet)
            .where(
                BonusSheet.id_manager == id_manager,
                BonusSheet.id_staff == id_staff,
                BonusSheet.status == APPROVED,
                BonusSheet.id_payroll.is_(None)
            )
        )
        return result.scalars().all()

    async def get_unpaid_deductions(self, id_manager, id_staff):
        result = await self.db.execute(
            select(Deduction)
            .where(
                Deduction.id_manager == id_manager,
                Deduction.id_staff == id_staff,
                Deduction.status == APPROVED,
                Deduction.id_payroll.is_(None)
            )
            .order_by(Deduction.created_at)
        )
        return result.scalars().all()

    # ---------- Thưởng / phạt ----------
    async def create_bonus(self, bonus: BonusSheet):
        self.db.add(bonus)
        await self.db.commit()
        await self.db.refresh(bonus)
        return bonus

    async def create_deduction(self, deduction: Deduction):
        self.db.add(deduction)
        await self.db.commit()
        await self.db.refresh(deduction)
        return deduction

    # ---------- Bảng lương ----------
    async def create_payroll(self, payroll: Payroll, bonus_ids, deduction_ids):
        """Lưu bảng lương và gán id_payroll cho thưởng / phạt đã dùng trong CÙNG 1 transaction.
        Nếu có khoản đã bị bảng lương khác lấy mất thì rollback và báo lỗi."""
        self.db.add(payroll)
        await self.db.flush()  # có payroll.id

        for model, ids in ((BonusSheet, bonus_ids), (Deduction, deduction_ids)):
            if not ids:
                continue
            result = await self.db.execute(
                update(model)
                .where(model.id.in_(ids), model.id_payroll.is_(None))
                .values(id_payroll=payroll.id)
            )
            if result.rowcount != len(ids):
                await self.db.rollback()
                raise PayrollConflictError()

        await self.db.commit()
        await self.db.refresh(payroll)
        return payroll

    async def get_payrolls_by_staff(self, id_staff, id_manager=None):
        query = (
            select(Payroll, Manager)
            .join(Manager, Manager.id == Payroll.id_manager)
            .where(Payroll.id_staff == id_staff)
            .order_by(Payroll.time_create.desc())
        )
        if id_manager is not None:
            query = query.where(Payroll.id_manager == id_manager)
        result = await self.db.execute(query)
        return result.all()

    async def get_payrolls_by_manager(self, id_manager, id_staff=None):
        """Các bảng lương của công ty, mới nhất lên đầu. id_staff = None: tất cả nhân viên."""
        query = (
            select(Payroll, Staff)
            .join(Staff, Staff.id == Payroll.id_staff)
            .where(Payroll.id_manager == id_manager)
            .order_by(Payroll.time_create.desc())
        )
        if id_staff is not None:
            query = query.where(Payroll.id_staff == id_staff)
        result = await self.db.execute(query)
        return result.all()