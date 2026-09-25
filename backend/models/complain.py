import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String, Text
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class Complaint(Base):
    __tablename__ = "complaints"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4
    )

    id_staff: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("staffs.id"),
        nullable=False
    )

    id_manager: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("managers.id"),
        nullable=False
    )

    created_at: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False
    )

    complaint_date: Mapped[datetime] = mapped_column(
        DateTime,
        nullable=False
    )

    reason: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    request_content: Mapped[str] = mapped_column(
        Text,
        nullable=False
    )

    status: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )