import uuid
from datetime import datetime

from sqlalchemy import ForeignKey, String, DateTime, Boolean
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id: Mapped[uuid.UUID] = mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )

    id_account: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("accounts.id"),
        nullable=False
    )

    token: Mapped[str] = mapped_column(
        String,
        nullable=False
    )
    is_revoked :Mapped[bool] = mapped_column(
        Boolean ,
        default=False
    )
