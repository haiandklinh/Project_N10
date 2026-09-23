import uuid

from sqlalchemy import ForeignKey, Float, String
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class BonusSheet(Base) :
    __tablename__ = "bonus_sheets"
    id : Mapped[uuid.UUID] =mapped_column(
        primary_key=True,
        default=uuid.uuid4,
    )
    id_employee: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("employees.id") ,
        nullable=False
    )
    id_manager: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("managers.id"),
        nullable=False
    )
    bonusAmount : Mapped[float] = mapped_column(
        Float ,
        nullable= True
    )
    reason : Mapped[str] = mapped_column(
        String ,
        nullable=True
    )

    status :Mapped[str] = mapped_column(
        String
    )
    note :Mapped[str] = mapped_column(
        String
    )