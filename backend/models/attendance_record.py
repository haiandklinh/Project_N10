import uuid
from datetime import datetime

from sqlalchemy import ForeignKey, Float, DateTime, String
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class AttendanceRecord(Base):
    __tablename__ = "attendance_records"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )
    id_work_schedule : Mapped[uuid.UUID] = mapped_column(
        ForeignKey("work_schedules.id"),
        nullable=False
    )
    check_in_time: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=True
    )

    check_out_time: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=True
    )

    check_in_longitude: Mapped[float] = mapped_column(
        Float,
        nullable=True
    )

    check_in_latitude: Mapped[float] = mapped_column(
        Float,
        nullable=True
    )

    check_out_latitude: Mapped[float] = mapped_column(
        Float,
        nullable=True
    )
    check_out_longitude : Mapped[float] = mapped_column(
        Float ,
        nullable= True
    )
    status :Mapped[str] = mapped_column(
        String(255) ,
        default= "NOT ATTENDED" ,
        nullable= True
    )