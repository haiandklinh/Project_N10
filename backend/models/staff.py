import uuid
from datetime import date

from sqlalchemy import ForeignKey, String, Date
from sqlalchemy.orm import Mapped, mapped_column


class Staff(Base):
    __tablename__ = "staffs"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )

    id_account: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("accounts.id"),
        nullable=False
    )

    full_name: Mapped[str] = mapped_column(
        String,
        nullable=False
    )

    date_of_birth: Mapped[date] = mapped_column(
        Date,
        nullable=False
    )

    gender: Mapped[str] = mapped_column(
        String,
        nullable=False
    )

    status: Mapped[str] = mapped_column(
        String,
        nullable=False
    )

    avatar: Mapped[str] = mapped_column(
        String,
        nullable=True
    )

    position: Mapped[str] = mapped_column(
        String,
        nullable=False
    )