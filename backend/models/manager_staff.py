import uuid
from sqlalchemy import ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
from db.base import Base


class ManagerStaff(Base):
    __tablename__ = "manager_staff"

    id_manager: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("managers.id") ,
        primary_key=True,
    )
    id_staff: Mapped[uuid.UUID] = mapped_column(
        ForeignKey("staffs.id") ,
        primary_key=True,
    )