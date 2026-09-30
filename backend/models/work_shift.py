from datetime import time
import uuid

from sqlalchemy import Float, ForeignKey, String,  Time
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class WorkShift(Base):
    __tablename__ = "work_shifts"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4
    )

    id_manager: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("managers.id"),
        nullable=False
    )

    name: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    start_time: Mapped[time] = mapped_column(
        Time,
        nullable=False
    )

    end_time: Mapped[time] = mapped_column(
        Time,
        nullable=False
    )

    allowed_check_in_time: Mapped[time] = mapped_column(
        Time,
        nullable=False
    )

    check_in_radius: Mapped[float] = mapped_column(
        Float,
        nullable=False
    )

    longitude: Mapped[float] = mapped_column(
        Float,
        nullable=False
    )

    latitude: Mapped[float] = mapped_column(
        Float,
        nullable=False
    )

    status: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )