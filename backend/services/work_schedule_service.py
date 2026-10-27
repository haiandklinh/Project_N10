from fastapi import HTTPException

from repositories.work_schedule_repository import WorkScheduleRepository
from schemas.work_schedule import ListStaffWorkScheduleResponse, StaffWorkScheduleResponse, WorkShift, \
    StaffWorkScheduleRequest


class WorkScheduleService:

    def __init__(self, repository: WorkScheduleRepository):
        self.repository = repository


    # Hay
    async def get_list_staff_work_schedule(self, id_account )->ListStaffWorkScheduleResponse:
        manager = await self.repository.get_manager_by_id_account(id_account)
        if manager is None :
            raise HTTPException(404,"Not Found Manager")
        staffs  = await self.repository.get_staff_by_manager(manager.id)
        if not staffs:
            return ListStaffWorkScheduleResponse(
                data=[]
            )

        # ==========================================
        # 2. Lấy staff_id
        # ==========================================

        staff_ids = []

        for staff ,position in staffs:
            staff_ids.append(staff.id)
        # ==========================================
        # 3. Lấy toàn bộ WorkSchedule + WorkShift
        # ==========================================

        schedules = await self.repository.get_by_staff_ids(
            staff_ids
        )
        # ==========================================
        # 4. Group theo staff_id + work_day
        # ==========================================

        schedule_by_staff = {}

        for schedule, shift in schedules:

            key = (
                schedule.id_staff,
                schedule.work_day
            )

            if key not in schedule_by_staff:
                schedule_by_staff[key] = []

            schedule_by_staff[key].append(shift)

        result = []

        for staff ,position in staffs:

            # --------------------------------------
            # Lấy tất cả ngày làm việc của Staff
            # --------------------------------------

            staff_schedule_found = False

            for (staff_id, work_day), shifts in schedule_by_staff.items():

                if staff_id != staff.id:
                    continue

                staff_schedule_found = True

                result.append(
                    StaffWorkScheduleResponse(
                        id_staff=staff.id,
                        name_staff=staff.full_name,
                        position=position,
                        work_day=work_day,
                        avatar=staff.avatar,
                        status="Nhân viên có ca làm việc ",
                        data=[
                            WorkShift.model_validate(shift)
                            for shift in shifts
                        ]
                    )
                )
            # --------------------------------------
            # Staff không có lịch
            # --------------------------------------

            if not staff_schedule_found:
                result.append(
                    StaffWorkScheduleResponse(
                        id_staff = staff.id,
                        name_staff=staff.full_name,
                        position=position,
                        work_day=None,
                        avatar=staff.avatar,
                        status="Nhân viên chưa có ca làm việc",
                        data=[]
                    )
                )

        return ListStaffWorkScheduleResponse(
            data=result
        )

    async def assign_work_schedules(self,request:StaffWorkScheduleRequest,id_account)->StaffWorkScheduleResponse:
        work_shifts = await self.repository.get_work_shift_by_id(request.id_work_shift)
        if len(work_shifts) < len(request.id_work_shift) :
            raise HTTPException(404, f"{len(request.id_work_shift)-len(work_shifts)} Work Shift is not exist in system")
        manager = await self.repository.get_manager_by_id_account(id_account)
        staff ,position = await self.repository.get_staff_by_id(request.id_staff,manager.id)
        if staff is None :
            raise HTTPException(404, "User not exist in System")

        await self.repository.assign_work_schedules(request.id_work_shift,request.id_staff,request.work_day)
        return StaffWorkScheduleResponse(
            id_staff= request.id_staff ,
            name_staff =staff.full_name ,
            position= position ,
            work_day= request.work_day,
            avatar= staff.avatar ,
            status ="Tạo lịch làm cho nhân viên thành công" ,
            data = [
                WorkShift(
                    id =work_shift.id ,
                    name = work_shift.name ,
                    start_time= work_shift.start_time,
                    end_time =work_shift.end_time
                )
                for work_shift in work_shifts
            ]

        )