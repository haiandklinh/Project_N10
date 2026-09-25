from sqlalchemy import ForeignKey
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class StaffWorkShift(Base):
    __tablename__ = "staff_workshifts"

    id_staff: Mapped[str] = mapped_column(
        ForeignKey("staffs.id"),
        primary_key=True
    )

    work_shift_id: Mapped[str] = mapped_column(
        ForeignKey("work_shifts.id"),
        primary_key=True
    )