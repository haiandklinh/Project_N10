from datetime import time ,date
from uuid import UUID

from schemas.base import BaseSchema


class CheckinRequest(BaseSchema) :
    id_work_schedule : UUID
    check_in_time : time
    latitude_check_in : float
    longitude_check_in :float
    workday :date

class CheckoutRequest(BaseSchema) :
    id_work_schedule : UUID
    check_out_time : time
    latitude_check_out : float
    longitude_check_out :float
    workday :date

class AttendanceRecordResponse(BaseSchema) :
    check_in_time:float
    check_out_time:float
    workday: date
    name_shift: str
    start_time: time
    end_time: time
    status :str
class ListAttendanceRecordResponse(BaseSchema) :
    data :list[AttendanceRecordResponse]
    total_items :int
    total_pages :int
    current_page :int
    size :int
