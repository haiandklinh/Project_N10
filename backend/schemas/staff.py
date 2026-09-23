from datetime import datetime

from pydantic import BaseModel


class CreateProfileRequest(BaseModel):
    fullName :str
    date :datetime
    gender : str
    avatar : str


class ProfileResponse(BaseModel) :
    fullName :str
    status :str
    date :datetime
    gender : str
    avatar : str

class UpdateProfileRequest(BaseModel):
    full_name: str | None = None
    gender: str | None = None
    avatar: str | None = None
    date: datetime | None = None