import uuid
from datetime import datetime

from sqlalchemy import ForeignKey, Float, DateTime
from sqlalchemy.orm import Mapped, mapped_column

from backend.db.base import Base


class AttendanceRecord(Base):
    __tablename__ = "attendance_records"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )

    id_employee: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("employees.id"),
        nullable=False
    )

    id_salary_sheet: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("salary_sheets.id"),
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