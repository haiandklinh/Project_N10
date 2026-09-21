import datetime
import uuid

from sqlalchemy import Float, ForeignKey, String, Time, DateTime
from sqlalchemy.orm import Mapped, mapped_column

from backend.db.base import Base


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
        String,
        nullable=False
    )

    start_time: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False
    )

    end_time: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False
    )

    allowed_check_in_time: Mapped[datetime] = mapped_column(
        DateTime,
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
        String,
        nullable=False
    )