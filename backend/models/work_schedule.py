import uuid
from datetime import date

from sqlalchemy import ForeignKey, Date
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class WorkSchedule(Base):
    __tablename__ = "work_schedules"

    id : Mapped[uuid.UUID] =mapped_column(
        primary_key=True,
        default=uuid.uuid4
    )
    id_staff :Mapped[uuid.UUID] = mapped_column(
        ForeignKey("staffs.id") ,
        nullable=True
    )
    id_work_shift :Mapped[uuid.UUID] = mapped_column(
        ForeignKey("work_shifts.id")
    )
    work_day :Mapped[date] =mapped_column(
        Date ,
        nullable=False
    )