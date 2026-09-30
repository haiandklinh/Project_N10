from datetime import  date as Date, time
from uuid import UUID

from schemas.base import BaseSchema


class CreateProfileRequest(BaseSchema):
    fullName :str
    date :Date
    gender : str
    avatar : str


class ProfileResponse(BaseSchema) :
    fullName :str
    status :str
    date :Date
    gender : str
    avatar : str

class UpdateProfileRequest(BaseSchema):
    full_name: str | None = None
    gender: str | None = None
    avatar: str | None = None
    date: Date | None = None
class StaffWorkShiftResponse(BaseSchema):
    name_shift : str
    start_time: time
    end_time : time
class StaffWorkScheduleResponse(BaseSchema) :
    workday : Date
    workshits : list[StaffWorkShiftResponse]

class ListStaffWorkScheduleResponse(BaseSchema) :
    data : list[StaffWorkScheduleResponse]

class StaffShiftChangeRequest(BaseSchema) :
    id_work_shift_current : UUID
    id_work_shift_change : UUID
    workday :Date
    reason : str

class StaffLeaveRequest(BaseSchema) :
    work_day_off :Date
    id_work_shift_off :UUID
    reason :str


