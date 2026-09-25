import uuid
from datetime import datetime

from sqlalchemy import ForeignKey, Float, DateTime
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class AttendanceRecord(Base):
    __tablename__ = "attendance_records"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )

    id_staff: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("staffs.id"),
        nullable=False
    )

    time: Mapped[datetime] = mapped_column(
        DateTime,
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