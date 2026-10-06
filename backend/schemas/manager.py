from datetime import date as Date, datetime, time
from uuid import UUID

from schemas.base import BaseSchema


class CreateProfileRequest(BaseSchema):
    fullName :str
    company : str
    date :Date
    gender : str
    avatar : str


class ProfileResponse(BaseSchema) :
    fullName :str
    status :str
    company: str
    date :Date
    gender : str
    avatar : str

class UpdateProfileRequest(BaseSchema):
    full_name: str | None = None
    gender: str | None = None
    company: str | None = None
    avatar: str | None = None
    date: Date | None = None


class  NotificationResponse(BaseSchema) :
    id_staff :UUID
    avatar_send_person :str | None = None
    send_person :str
    content : dict
    send_at: datetime
    notification_type :str
class ListNotificationResponse(BaseSchema) :
    notifications: list[NotificationResponse]


class InvitationCodeResponse(BaseSchema) :
    OTP :str
    expires_in :time

class ShiftChangeRequest(BaseSchema) :
    id_staff :UUID
    id_work_shift_cur: UUID
    id_work_shift_change: UUID
    workday: Date
    status : bool
    reason : str | None = None

class LeaveDayRequest(BaseSchema) :
    id_staff :UUID
    id_work_shift_off: UUID
    workday_off: Date
    status : bool
    reason : str | None = None

class JoinRequest(BaseSchema) :
    id_staff :UUID
    position : str | None = None
    status :bool
