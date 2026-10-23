from datetime import date as Date, time, datetime
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
class  NotificationResponse(BaseSchema) :
    send_person :str
    company_name:str
    content : dict
    send_at: datetime
    notification_type :str

class ListNotificationResponse(BaseSchema) :
    notifications: list[NotificationResponse]

class StaffWorkShiftResponse(BaseSchema):
    id_work_schedule : UUID
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

class JoinRequest(BaseSchema) :
    OTP :str
    fullname : str
    avatar : str

class JoinResponse(BaseSchema) :
    company :str
    OTP :str

class CompanyResponse(BaseSchema) :
    name_company : str
    name_manager : str
    position_staff : str
    id_manager : UUID
class ListCompany(BaseSchema) :
    data : list[CompanyResponse]

