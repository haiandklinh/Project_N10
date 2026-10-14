
from fastapi import HTTPException
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from models.attendance_record import AttendanceRecord
from models.staff import Staff
from models.work_schedule import WorkSchedule
from models.work_shift import WorkShift



class AttendanceRepository :
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_staff_by_id_account(self, id_account):
        result = await self.db.execute(
            select(Staff)
            .where(Staff.id_account == id_account)
        )
        return result.scalar_one_or_none()

    async def get_work_schedule_by_id(self, id_work_schedule):
        result = await self.db.execute(
            select(AttendanceRecord)
            .where(
                AttendanceRecord.id_work_schedule == id_work_schedule,
                AttendanceRecord.check_out_time.is_(None)
            )
            .order_by(AttendanceRecord.check_in_time.desc())
        )
        return result.scalars().first()## tôi sửa

    async def check_in(self, id_work_schedule, check_in_time, latitude_check_in, longitude_check_in):
        attendance_record = AttendanceRecord(
            id_work_schedule = id_work_schedule ,
            check_in_time = check_in_time ,
            check_in_latitude = latitude_check_in ,
            check_in_longitude = longitude_check_in,
            status ="NOT CHECKED OUT"
        )
        self.db.add(attendance_record)
        await self.db.commit()
        await self.db.refresh(attendance_record)
        return attendance_record

    async def check_out(self, id_work_schedule, check_out_time, latitude_check_out, longitude_check_out):
        attendance_record = await self.get_work_schedule_by_id(id_work_schedule)

        if not attendance_record:
            raise HTTPException(status_code=404, detail="Không tìm thấy bản ghi chấm công.")

        if attendance_record.check_out_time is not None:
            raise HTTPException(status_code=400, detail="Bạn đã check-out cho ca làm việc này rồi.")

        if attendance_record.check_in_time is None :
            attendance_record.status = 'NOT CHECKED IN'

        attendance_record.check_out_time = check_out_time
        attendance_record.check_out_latitude = latitude_check_out
        attendance_record.check_out_longitude = longitude_check_out
        attendance_record.status = "OK"

        await self.db.commit()

        await self.db.refresh(attendance_record)

        return attendance_record

    async def get_work_shift_by_staff(self, id_staff, id_work_schedule, workday):
        result = await self.db.execute(
            select(WorkShift)
            .join(WorkSchedule ,
                  WorkSchedule.id_work_shift == WorkShift.id)
            .where(
                WorkSchedule.work_day ==workday ,
                WorkSchedule.id_staff == id_staff,
                WorkSchedule.id == id_work_schedule 
            )
        )
        return result.scalar_one_or_none()

    async def get_attendance_record(
            self, id_manager, id_staff, start_date, end_date, offset, size
    ):
        # Đếm tổng số bản ghi phù hợp
        count_query = (
            select(func.count(AttendanceRecord.id))
            .join(
                WorkSchedule,
                AttendanceRecord.id_work_schedule == WorkSchedule.id
            )
            .join(
                WorkShift,
                WorkSchedule.id_work_shift == WorkShift.id
            )
            .where(
                WorkSchedule.id_staff == id_staff,
                WorkShift.id_manager == id_manager,
                WorkSchedule.work_day >= start_date,
                WorkSchedule.work_day <= end_date
            )
        )

        total_items = await self.db.scalar(count_query)

        # Lấy dữ liệu của trang hiện tại
        data_query = (
            select(AttendanceRecord, WorkShift, WorkSchedule)
            .join(
                WorkSchedule,
                AttendanceRecord.id_work_schedule == WorkSchedule.id
            )
            .join(
                WorkShift,
                WorkSchedule.id_work_shift == WorkShift.id
            )
            .where(
                WorkSchedule.id_staff == id_staff,
                WorkShift.id_manager == id_manager,
                WorkSchedule.work_day >= start_date,
                WorkSchedule.work_day <= end_date
            )
            .order_by(WorkSchedule.work_day.desc())
            .offset(offset)
            .limit(size)
        )

        result = await self.db.execute(data_query)
        records = result.all()

        return records, total_items

