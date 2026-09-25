import uuid

from sqlalchemy import ForeignKey, String, DateTime
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class Notification(Base):
    __tablename__ = "notifications"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )

    id_staff: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("staffs.id"),
        nullable=False
    )

    id_manager: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("managers.id"),
        nullable=False
    )

    title: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    content: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    created_at: Mapped[DateTime] = mapped_column(
        DateTime,
        nullable=False
    )

    notification_type: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )