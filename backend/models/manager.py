import uuid
from datetime import date

from sqlalchemy import ForeignKey, String, Date, Integer
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class Manager(Base):
    __tablename__ = "managers"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )

    invitation_code: Mapped[int] = mapped_column(
        Integer,
        unique=True,
        nullable=False
    )

    id_account: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("accounts.id"),
        nullable=False
    )

    full_name: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    date_of_birth: Mapped[date] = mapped_column(
        Date,
        nullable=False
    )

    gender: Mapped[str] = mapped_column(
        String(255),
        nullable=False
    )

    status: Mapped[str] = mapped_column(
        String(255),
        default="active"
    )

    avatar: Mapped[str] = mapped_column(
        String(255),
        nullable=True
    )

    company :Mapped[str] = mapped_column(
        String(255) ,
        nullable=True
    )

