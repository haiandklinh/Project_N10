from datetime import time
from uuid import UUID

from schemas.base import BaseSchema


class CreateShiftRequest(BaseSchema):
    name: str
    start_time: time
    end_time: time
    allowed_check_in_time: time
    check_in_radius: float
    longitude: float
    latitude: float


class UpdateShiftRequest(BaseSchema):
    name: str | None = None
    start_time: time | None = None
    end_time: time | None = None
    allowed_check_in_time: time | None = None
    check_in_radius: float | None = None
    longitude: float | None = None
    latitude: float | None = None


class ShiftResponse(BaseSchema):
    id: UUID
    name: str
    start_time: time
    end_time: time
    allowed_check_in_time: time
    check_in_radius: float
    longitude: float
    latitude: float
    status: str


class ListShiftResponse(BaseSchema):
    data: list[ShiftResponse]