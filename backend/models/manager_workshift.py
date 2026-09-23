from sqlalchemy import ForeignKey
from sqlalchemy.orm import Mapped, mapped_column

from db.base import Base


class ManagerWorkShift(Base):
    __tablename__ = "managerwork_shift"

    employee_id: Mapped[str] = mapped_column(
        ForeignKey("employees.id"),
        primary_key=True
    )

    work_shift_id: Mapped[str] = mapped_column(
        ForeignKey("work_shifts.id"),
        primary_key=True
    )