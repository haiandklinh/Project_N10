from datetime import date, datetime
from uuid import UUID

from schemas.base import BaseSchema


class WorkShift(BaseSchema) :
    id:UUID
    name :str
    start_time :datetime
    end_time: datetime

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
    name_staff :str
    position: str
    work_day :date | None = None
    id_work_shift :list[UUID]



class ListStaffWorkScheduleResponse(BaseSchema) :
    data :list[StaffWorkScheduleResponse]