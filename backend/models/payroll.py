from datetime import datetime
import uuid

from sqlalchemy import  Float, ForeignKey, Integer, String, DateTime
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class Payroll(Base):
    __tablename__ = "payrolls"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4
    )

    id_staff: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("staffs.id"),
        nullable=False
    )

    time_create: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False
    )

    base_salary: Mapped[float] = mapped_column(
        Float,
        nullable=False
    )

    number_of_work_shifts: Mapped[int] = mapped_column(
        Integer,
        nullable=False
    )

    total_salary: Mapped[float] = mapped_column(
        Float,
        nullable=False
    )

    status: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )