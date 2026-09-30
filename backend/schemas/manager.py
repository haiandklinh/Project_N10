from datetime import date, datetime

from pydantic import BaseModel

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


class  NotificationResponse(BaseSchema) :
    avatar_send_person :str | None = None
    send_person :str
    content : dict
    send_at: datetime
class ListNotificationResponse(BaseSchema) :
    notifications: list[NotificationResponse]
