from datetime import date, datetime
from uuid import UUID

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from core.config import settings
from models.account import Account
from models.attendance_record import AttendanceRecord
from models.manager import Manager
from models.manager_staff import ManagerStaff
from models.notification import Notification
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift
import secrets


class AttendanceAdjustmentNotFoundError(Exception):
    pass


class AttendanceAdjustmentConflictError(Exception):
    pass


class ManagerRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_profile_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Manager)
            .where(Manager.id_account == id_account)
        )

        return result.scalar_one_or_none()

    async def get_staffs(self, id_manager):
        result = await self.db.execute(
            select(Staff, ManagerStaff.position)
            .join(ManagerStaff, ManagerStaff.id_staff == Staff.id)
            .where(ManagerStaff.id_manager == id_manager)
        )

        return result.all()

    async def create_profile(self, fullName, gender, avatar, date, id_account, company):
        result = await self.get_profile_by_id_account(id_account)
        if result is not None:
            return None

        while True:
            invitation_code = secrets.randbelow(900000) + 100000

            result = await self.db.execute(
                select(Manager).where(
                    Manager.invitation_code == invitation_code
                )
            )

            if result.scalar_one_or_none() is None:
                break

        manager = Manager(
            invitation_code=invitation_code,
            full_name=fullName,
            company=company,
            date_of_birth=date,
            gender=gender,
            avatar=avatar,
            id_account=id_account
        )

        self.db.add(manager)
        await self.db.commit()
        await self.db.refresh(manager)

        return manager

    async def update_profile(self, full_name, gender, avatar, date, id_account, company):

        profile = await self.get_profile_by_id_account(id_account)

        if profile is None:
            return None

        if full_name is not None:
            profile.full_name = full_name

        if gender is not None:
            profile.gender = gender

        if avatar is not None:
            profile.avatar = avatar

        if date is not None:
            profile.date_of_birth = date
        if company is not None:
            profile.company = company

        await self.db.commit()
        await self.db.refresh(profile)

        return profile

    async def get_notification(self, id_manager):
        result = await self.db.execute(
            select(Notification, Staff,Account.phone_number)
            .join(Staff,
                  Staff.id == Notification.id_staff)
            .join(Account,
                  Account.id ==Staff.id_account)
            .where(
                Notification.id_manager == id_manager,
                Notification.notification_type.in_([
                    settings.JOIN_COMPANY_REQUEST,
                    settings.CHANGE_LEAVE_REQUEST,
                    settings.CHANGE_WORK_SHIFT_REQUEST,
                    settings.ATTENDANCE_ADJUSTMENT_REQUEST
                ])
            )
        )

        return result.all()

    async def get_staff_by_manager(self, id_manager, id_staff):
        result = await self.db.execute(
            select(ManagerStaff)
            .where(ManagerStaff.id_manager == id_manager,
                   ManagerStaff.id_staff == id_staff)
        )
        return result.scalar_one_or_none()

    async def change_workshift(self, id_staff, id_work_shift_cur, id_work_shift_change, workday):
        result = await self.db.execute(
            select(WorkSchedule)
            .where(
                WorkSchedule.id_staff == id_staff,
                WorkSchedule.id_work_shift == id_work_shift_cur,
                WorkSchedule.work_day == workday
            )
        )
        work_schedule = result.scalar_one_or_none()
        if work_schedule is None:
            return False

        work_schedule.id_work_shift = id_work_shift_change

        await self.db.commit()

        return True

    async def create_notification_change_workshift(self, id_staff, name_work_shift_cur, name_work_shift_change, workday,
                                                   id_manager, message, reason):
        content = {
            "message ": message,
            "reason": reason if reason is not None else "",
            "name_work_shift_cur": name_work_shift_cur,
            "name_work_shift_change": name_work_shift_change,
            "workday": workday.isoformat()
        }
        notification = Notification(
            id_staff=id_staff,
            id_manager=id_manager,
            title="Thông báo về yêu cầu xin đổi ca",
            content=content,
            created_at=datetime.now(),
            notification_type=settings.CHANGE_WORK_SHIFT_RESPONSE
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def get_work_shift_by_id(self, id_work_shift):
        result = await self.db.execute(
            select(WorkShift)
            .where(WorkShift.id == id_work_shift)
        )
        return result.scalar_one_or_none()

    async def create_notification_leaveday(self, id_staff, name_shift_off, workday_off, id_manager, message, reason):
        content = {
            "message ": message,
            "reason": reason if reason is not None else "",
            "name_work_shift_off": name_shift_off,
            "workday": workday_off.isoformat()
        }
        notification = Notification(
            id_staff=id_staff,
            id_manager=id_manager,
            title="Thông báo về yêu cầu xin nghỉ",
            content=content,
            created_at=datetime.now(),
            notification_type=settings.CHANGE_LEAVE_RESPONSE
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def handle_leave_day_off(self, id_staff, id_work_shift_off, workday):
        result = await self.db.execute(
            select(WorkSchedule)
            .where(
                WorkSchedule.id_staff == id_staff,
                WorkSchedule.id_work_shift == id_work_shift_off,
                WorkSchedule.work_day == workday
            )
        )
        work_schedule = result.scalar_one_or_none()
        if work_schedule is None:
            return False
        work_schedule.status = False
        await self.db.commit()
        return True

    async def resolve_attendance_adjustment_request(
        self,
        id_manager: UUID,
        id_staff: UUID,
        id_work_shift: UUID,
        work_day: date,
        approved: bool,
        reason: str | None,
    ) -> Notification:
        result = await self.db.execute(
            select(WorkSchedule.id)
            .join(WorkShift, WorkSchedule.id_work_shift == WorkShift.id)
            .where(
                WorkSchedule.id_staff == id_staff,
                WorkSchedule.id_work_shift == id_work_shift,
                WorkSchedule.work_day == work_day,
                WorkShift.id_manager == id_manager,
            )
        )
        schedule_ids = [str(schedule_id) for schedule_id in result.scalars()]
        result = await self.db.execute(
            select(Notification)
            .where(
                Notification.id_manager == id_manager,
                Notification.id_staff == id_staff,
                Notification.notification_type == settings.ATTENDANCE_ADJUSTMENT_REQUEST,
                Notification.status == "active",
                Notification.content["id_work_schedule"].as_string().in_(schedule_ids),
                Notification.content["work_day"].as_string() == work_day.isoformat(),
            )
            .order_by(Notification.id)
            .limit(2)
            .with_for_update()
        )
        adjustments = result.scalars().all()
        if not adjustments:
            raise AttendanceAdjustmentNotFoundError("Không tìm thấy yêu cầu điều chỉnh chấm công đang chờ xử lý.")
        if len(adjustments) > 1:
            raise AttendanceAdjustmentConflictError("Có nhiều yêu cầu điều chỉnh đang chờ cho ca này.")

        adjustment = adjustments[0]
        id_work_schedule = UUID(adjustment.content["id_work_schedule"])
        if approved:
            result = await self.db.execute(
                select(AttendanceRecord)
                .join(WorkSchedule, AttendanceRecord.id_work_schedule == WorkSchedule.id)
                .join(WorkShift, WorkSchedule.id_work_shift == WorkShift.id)
                .where(
                    WorkSchedule.id == id_work_schedule,
                    WorkSchedule.id_staff == id_staff,
                    WorkSchedule.id_work_shift == id_work_shift,
                    WorkSchedule.work_day == work_day,
                    WorkShift.id_manager == id_manager,
                )
                .limit(2)
            )
            records = result.scalars().all()
            if not records:
                raise AttendanceAdjustmentNotFoundError("Không tìm thấy bản ghi chấm công của yêu cầu.")
            if len(records) > 1:
                raise AttendanceAdjustmentConflictError("Lịch làm việc có nhiều bản ghi chấm công.")
            records[0].status = "OK"

        adjustment.status = "APPROVED" if approved else "REJECTED"
        message = (
            "Yêu cầu điều chỉnh chấm công đã được đồng ý."
            if approved else "Yêu cầu điều chỉnh chấm công đã bị từ chối."
        )
        notification = Notification(
            id_staff=adjustment.id_staff,
            id_manager=id_manager,
            title="Phản hồi yêu cầu điều chỉnh chấm công",
            content={
                "id_work_schedule": str(id_work_schedule),
                "id_work_shift": str(id_work_shift),
                "work_day": work_day.isoformat(),
                "status": approved,
                "message": message,
                "reason": reason or "",
            },
            created_at=datetime.now(),
            notification_type=settings.ATTENDANCE_ADJUSTMENT_RESPONSE,
        )
        self.db.add(notification)
        await self.db.commit()
        await self.db.refresh(notification)
        return notification

    async def add_staff_in_company_by_manager(self, manager_id, id_staff, position):
        new_staff = ManagerStaff(
            id_manager=manager_id,
            id_staff=id_staff,
            position=position,
        )
        self.db.add(new_staff)
        await self.db.commit()
        await self.db.refresh(new_staff)


