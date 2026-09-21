import uuid
from datetime import datetime

from sqlalchemy import DateTime, Float, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from backend.db.base import Base


class Deduction(Base):
    __tablename__ = "deductions"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4
    )

    id_employee: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("employees.id"),
        nullable=False
    )

    id_manager: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("managers.id"),
        nullable=False
    )

    deduction_type: Mapped[str] = mapped_column(
        String,
        nullable=False
    )

    reason: Mapped[str] = mapped_column(
        String,
        nullable=False
    )

    amount: Mapped[float] = mapped_column(
        Float,
        nullable=False
    )

    created_at: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False
    )

    status: Mapped[str] = mapped_column(
        String,
        nullable=False
    )