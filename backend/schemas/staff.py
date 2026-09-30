from datetime import  date, time
from uuid import UUID

from pydantic.v1 import UUID1

from schemas.base import BaseSchema


class CreateProfileRequest(BaseSchema):
    fullName :str
    date :date
    gender : str
    avatar : str


class ProfileResponse(BaseSchema) :
    fullName :str
    status :str
    date :date
    gender : str
    avatar : str

class UpdateProfileRequest(BaseSchema):
    full_name: str | None = None
    gender: str | None = None
    avatar: str | None = None
    date: date | None = None
class StaffWorkShiftResponse(BaseSchema):
    name_shift : str
    start_time: time
    end_time : time
class StaffWorkScheduleResponse(BaseSchema) :
    workday : date
    workshits : list[StaffWorkShiftResponse]

class ListStaffWorkScheduleResponse(BaseSchema) :
    data : list[StaffWorkScheduleResponse]

class StaffShiftChangeRequest(BaseSchema) :
    id_work_shift_current : UUID
    id_work_shift_change : UUID
    workday :date
    reason : str

class StaffLeaveRequest(BaseSchema) :
    work_day_off :date
    id_work_shift_off :UUID
    reason :str


