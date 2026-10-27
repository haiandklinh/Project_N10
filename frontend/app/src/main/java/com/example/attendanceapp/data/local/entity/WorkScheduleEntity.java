package com.example.attendanceapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;

@Entity(tableName = "work_schedules")
public class WorkScheduleEntity {
    @PrimaryKey
    @NonNull
    private final String id; // employeeId + "_" + workDate + "_" + shiftId
    private final String employeeId;
    private final String employeeName;
    private final String position;
    private final String avatar;
    private final String shiftId;
    private final String shiftName;
    private final String startTime;
    private final String endTime;
    private final String workDate;
    private final String status;

    public WorkScheduleEntity(@NonNull String id, String employeeId, String employeeName, String position, 
                              String avatar, String shiftId, String shiftName, String startTime, 
                              String endTime, String workDate, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.position = position;
        this.avatar = avatar;
        this.shiftId = shiftId;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workDate = workDate;
        this.status = status;
    }

    @NonNull
    public String getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getPosition() { return position; }
    public String getAvatar() { return avatar; }
    public String getShiftId() { return shiftId; }
    public String getShiftName() { return shiftName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getWorkDate() { return workDate; }
    public String getStatus() { return status; }

    public EmployeeWorkSchedule toDomain() {
        EmployeeWorkSchedule.ShiftInfo shiftInfo = null;
        if ((shiftId != null && !shiftId.trim().isEmpty()) || (shiftName != null && !shiftName.trim().isEmpty())) {
            shiftInfo = new EmployeeWorkSchedule.ShiftInfo(shiftId, shiftName, startTime, endTime);
        }
        return new EmployeeWorkSchedule(
                employeeId,
                employeeName,
                position,
                workDate,
                avatar,
                status,
                shiftInfo
        );
    }

    public static WorkScheduleEntity fromDomain(EmployeeWorkSchedule schedule) {
        String shiftId = null;
        String shiftName = null;
        String startTime = null;
        String endTime = null;
        if (schedule.getShift() != null) {
            shiftId = schedule.getShift().getShiftId();
            shiftName = schedule.getShift().getShiftName();
            startTime = schedule.getShift().getStartTime();
            endTime = schedule.getShift().getEndTime();
        }

        String empId = schedule.getEmployeeId() != null ? schedule.getEmployeeId() : "";
        String wDate = schedule.getWorkDate() != null ? schedule.getWorkDate() : "unassigned";
        String sId = shiftId != null ? shiftId : "none";
        String id = empId + "_" + wDate + "_" + sId;

        return new WorkScheduleEntity(
                id,
                empId,
                schedule.getName(),
                schedule.getPosition(),
                schedule.getAvatar(),
                shiftId,
                shiftName,
                startTime,
                endTime,
                schedule.getWorkDate(),
                schedule.getStatus()
        );
    }
}
