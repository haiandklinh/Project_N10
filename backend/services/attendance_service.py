import math

from fastapi import HTTPException

from repositories.attendance_repository import AttendanceRepository
from schemas.account import MessageResponse
from schemas.attendance import CheckinRequest, CheckoutRequest, ListAttendanceRecordResponse, AttendanceRecordResponse

class AttendanceService:

    def __init__(self, repository: AttendanceRepository):
        self.repository = repository
    def check_valid_distance(self, latitude_check_out, longitude_check_out, latitude, longitude, check_in_radius):
        # Bán kính Trái Đất (tính bằng mét)
        R = 6371000

        # Chuyển đổi độ sang radian
        phi1 = math.radians(latitude_check_out)
        phi2 = math.radians(latitude)
        delta_phi = math.radians(latitude - latitude_check_out)
        delta_lambda = math.radians(longitude - longitude_check_out)

        # Công thức Haversine
        a = math.sin(delta_phi / 2.0) ** 2 + \
            math.cos(phi1) * math.cos(phi2) * \
            math.sin(delta_lambda / 2.0) ** 2

        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

        # Khoảng cách thực tế (mét)
        distance = R * c

        # Trả về True nếu khoảng cách thực tế <= bán kính cho phép
        return distance <= check_in_radius

    async def check_in(self, request: CheckinRequest, id_account) -> MessageResponse:
        staff = await  self.repository.get_staff_by_id_account(id_account)
        if staff is None:
            raise HTTPException(404, "Not found user in the system")
        workshift = await self.repository.get_work_shift_by_staff(staff.id, request.id_work_schedule, request.workday)

        if self.check_valid_distance(request.latitude_check_in, request.longitude_check_in, workshift.latitude,
                                     workshift.longitude, workshift.check_in_radius) is False:
            raise HTTPException(401, "Distance is not valid")
        attendance_record = await self.repository.check_in(request.id_work_schedule, request.check_in_time,
                                                           request.latitude_check_in, request.longitude_check_in)
        if attendance_record is not None:
            return MessageResponse(
                message="Check in Successful"
            )
        return MessageResponse(
            message="Check in Failed"
        )

    async def check_out(self, request: CheckoutRequest, id_account) -> MessageResponse:
        staff = await  self.repository.get_staff_by_id_account(id_account)
        if staff is None:
            raise HTTPException(404, "Not found user in the system")
        workshift = await self.repository.get_work_shift_by_staff(staff.id, request.id_work_schedule, request.workday)

        if self.check_valid_distance(request.latitude_check_out, request.longitude_check_out, workshift.latitude,
                                     workshift.longitude, workshift.check_in_radius) is False:
            raise HTTPException(401, "Distance is not valid")
        attendance_record = await self.repository.check_out(request.id_work_schedule, request.check_out_time,
                                                            request.latitude_check_out, request.longitude_check_out)
        if attendance_record is not None:
            return MessageResponse(
                message="Check out Successful"
            )
        return MessageResponse(
            message="Check out Failed"
        )

    async def get_attendance_record(self,id_manager, id_account,start_date,end_date,page,size) -> ListAttendanceRecordResponse:
        staff = await self.repository.get_staff_by_id_account(id_account)
        if staff is None :
            raise HTTPException(404, "Not found user in the system")

        offset = (page - 1) * size
        records ,total_items = await self.repository.get_attendance_record(
            id_manager=id_manager,
            id_staff=staff.id,
            start_date=start_date,
            end_date=end_date,
            offset = offset ,
            size=size
        )

        total_pages = math.ceil(total_items / size) if size > 0 else 0
        data = [
            AttendanceRecordResponse(
                check_in_time=record.check_in_time,
                check_out_time=record.check_out_time,
                workday=work_schedule.work_day,
                name_shift=workshift.nameshift,
                start_time=workshift.start_time,
                end_time=workshift.end_time,
                status=record.status
            )
            for record, workshift, work_schedule in records
        ]

        return ListAttendanceRecordResponse(
            data=data,
            total_items=total_items,
            total_pages=total_pages,
            current_page=page,
            size=size
        )


