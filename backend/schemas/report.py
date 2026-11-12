from datetime import time, datetime

from schemas.base import BaseSchema


class StaffLeaveEarly(BaseSchema) :
    name : str
    gender : str
    avatar_url : str
    name_shift : str
    start_time_shift : time
    end_time_shift : time
    checkin_time_attendance : time
    checkout_time_attendance : time
class ListStaffLeaveEarly(BaseSchema) :
    data : list[StaffLeaveEarly]
    numbers : int

class StaffArriveLate(BaseSchema) :
    name : str
    gender : str
    avatar_url : str
    name_shift : str
    start_time_shift : time
    end_time_shift : time
    checkin_time_attendance : time
    checkout_time_attendance : time

class ListStaffArriveLate(BaseSchema) :
    data : list[StaffArriveLate]
    numbers : int

class StaffAbsent(BaseSchema) :
    name : str
    gender : str
    avatar_url : str
    name_shift : str
    start_time_shift : time
    end_time_shift : time

class ListStaffAbsent(BaseSchema) :
    data: list[StaffAbsent]
    numbers: int

class StaffNotFinishWorkShift(BaseSchema) :
    name : str
    gender : str
    avatar_url : str
    name_shift : str
    start_time_shift : time
    end_time_shift : time
    checkin_time_attendance : time | None = None
    checkout_time_attendance : time | None = None
class ListStaffNotFinishWorkShift(BaseSchema) :
    data: list[StaffNotFinishWorkShift]
    numbers: int

class StaffSalary(BaseSchema) :
    name : str
    gender : str
    avatar_url : str
    salary : float
    bonus_salary :float
    deduction_salary : float
    receive_time :datetime
class ListStaffSalary(BaseSchema) :
    data : list[StaffSalary]
    total : float

class StaffOverviewReport(BaseSchema):
    total_assigned_shifts: int   # Tổng số ca làm được phân công
    completed_shifts: int        # Ca đã hoàn thành (check-in & check-out hợp lệ)
    incomplete_shifts: int       # Ca chưa hoàn thành (vắng hoặc thiếu chấm công)
    late_arrivals: int           # Số lần đi muộn
    early_leaves: int            # Số lần về sớm
    total_working_hours: float   # Tổng giờ làm thực tế
    total_bonus: float           # Tổng tiền thưởng
    total_penalty: float         # Tổng tiền phạt
    expected_salary: float       # Tiền lương dự kiến/đã chốt