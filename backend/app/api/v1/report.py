from fastapi import APIRouter, Depends, Query

from app.api.deps import get_current_account, get_report_service
from schemas.report import ListStaffLeaveEarly, ListStaffArriveLate, ListStaffAbsent, ListStaffNotFinishWorkShift, \
    ListStaffSalary, StaffOverviewReport

router = APIRouter(
    prefix="/reports",
    tags=["Report"]
)

# START MANAGER
# Trả về số lượng và danh sách nhân viên về sớm
@router.get('/leave-early',response_model=ListStaffLeaveEarly)
async def get_staff_leave_early(
    month: int | None = Query(None, description="Lọc theo tháng (1-12)", ge=1, le=12),
    week: int | None = Query(None, description="Lọc theo tuần trong năm (1-52)", ge=1, le=52),
    year: int | None = Query(None, description="Lọc theo năm"),
    page: int = Query(1, description="Trang hiện tại", ge=1),
    limit: int = Query(10, description="Số lượng bản ghi trên một trang", ge=1),
    id_account = Depends(get_current_account) ,
    service = Depends(get_report_service)
):
    return await service.get_staffs_leave_early(id_account,month,week,year,page,limit)


# Trả về số lượng và danh sách nhân viên đi muộn
@router.get('/arrive-late', response_model=ListStaffArriveLate)
async def get_staff_arrive_late(
    month: int | None = Query(None, description="Lọc theo tháng (1-12)", ge=1, le=12),
    week: int | None = Query(None, description="Lọc theo tuần trong năm (1-52)", ge=1, le=52),
    year: int | None = Query(None, description="Lọc theo năm"),
    page: int = Query(1, description="Trang hiện tại", ge=1),
    limit: int = Query(10, description="Số lượng bản ghi trên một trang", ge=1),
    id_account=Depends(get_current_account),
    service=Depends(get_report_service)
) :
    return await service.get_staffs_arrive_late(id_account,month,week,year,page,limit)


# Trả về số lượng và danh sách nhân viên vắng mặt
@router.get('/absent', response_model=ListStaffAbsent)
async def get_staff_absent(
    month: int | None = Query(None, description="Lọc theo tháng (1-12)", ge=1, le=12),
    week: int | None = Query(None, description="Lọc theo tuần trong năm (1-52)", ge=1, le=52),
    year: int | None = Query(None, description="Lọc theo năm"),
    page: int = Query(1, description="Trang hiện tại", ge=1),
    limit: int = Query(10, description="Số lượng bản ghi trên một trang", ge=1),
    id_account=Depends(get_current_account),
    service=Depends(get_report_service)
) :
    return await service.get_staffs_absent(id_account,month,week,year,page,limit)


# Trả về số nhân viên chưa hoàn thành ca ( điểm danh vào nhưng chưa ra hoặc điểm danh ra nhưng chưa điểm danh vào )
@router.get('/incomplete-shifts',response_model=ListStaffNotFinishWorkShift)
async def get_staff_not_finish__work_shift(
    month: int | None = Query(None, description="Lọc theo tháng (1-12)", ge=1, le=12),
    week: int | None = Query(None, description="Lọc theo tuần trong năm (1-52)", ge=1, le=52),
    year: int | None = Query(None, description="Lọc theo năm"),
    page: int = Query(1, description="Trang hiện tại", ge=1),
    limit: int = Query(10, description="Số lượng bản ghi trên một trang", ge=1),
    id_account=Depends(get_current_account),
    service=Depends(get_report_service)
) :
    return await service.get_staffs_not_finish__work_shift(id_account,month,week,year,page,limit)


# Trả về số tiền lương phải trả cho toàn bộ nhân viên và danh sách nhân viên đi kèm theo số lương của họ
@router.get('/payroll',response_model= ListStaffSalary)
async def get_payroll_report(
    month: int | None = Query(None, description="Lọc theo tháng (1-12)", ge=1, le=12),
    week: int | None = Query(None, description="Lọc theo tuần trong năm (1-52)", ge=1, le=52),
    year: int | None = Query(None, description="Lọc theo năm"),
    page: int = Query(1, description="Trang hiện tại", ge=1),
    limit: int = Query(10, description="Số lượng bản ghi trên một trang", ge=1),
    id_account = Depends(get_current_account),
    service = Depends(get_report_service)
):
    return await service.get_payroll_report(
        id_account=id_account,
        month=month,
        week=week,
        year=year,
        page=page,
        limit=limit
    )
#END MANAGER


#START STAFF
# Lấy ra tổng số ca làm , số ca đã hoàn thành , số ca chưa hoàn thành ,số lần đi muộn , số lần về sớm ,thưởng , phạt
@router.get('/staff/overview', response_model=StaffOverviewReport)
async def get_staff_overview_report(
    id_manager: str,
    month: int | None = Query(None, description="Lọc theo tháng (1-12)", ge=1, le=12),
    week: int | None = Query(None, description="Lọc theo tuần trong năm (1-52)", ge=1, le=52),
    year: int | None = Query(None, description="Lọc theo năm"),
    id_account = Depends(get_current_account),
    service = Depends(get_report_service)
):
    return await service.get_staff_overview_report(
        id_account=id_account,
        month=month,
        week=week,
        year=year,
        id_manager = id_manager,
    )
#END STAFF







