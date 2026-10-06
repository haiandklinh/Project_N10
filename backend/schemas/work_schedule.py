from datetime import date, time
from uuid import UUID

from pydantic import ConfigDict

from schemas.base import BaseSchema


class WorkShift(BaseSchema) :
    model_config = ConfigDict(from_attributes=True)
    id:UUID
    name :str
    start_time :time
    end_time: time

class StaffWorkScheduleResponse(BaseSchema) :
    id_staff:UUID
    name_staff :str
    position: str
    work_day :date | None = None
    avatar :str
    status :str| None = None
    data : list[WorkShift]

class StaffWorkScheduleRequest(BaseSchema) :
    id_staff: UUID
    work_day :date | None = None
    id_work_shift :list[UUID]



class ListStaffWorkScheduleResponse(BaseSchema) :
    data :list[StaffWorkScheduleResponse]