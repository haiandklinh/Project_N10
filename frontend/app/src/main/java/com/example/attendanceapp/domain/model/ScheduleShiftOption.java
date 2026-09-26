package com.example.attendanceapp.domain.model;

/**
 * Một ca làm cố định trong danh mục ca của doanh nghiệp (Ca Sáng, Ca Chiều...),
 * hiển thị cho Quản lý lựa chọn khi "Xếp ca" cho nhân viên.
 */
public class ScheduleShiftOption {
    private final String id;
    private final String name;
    private final String timeRange;

    public ScheduleShiftOption(String id, String name, String timeRange) {
        this.id = id;
        this.name = name;
        this.timeRange = timeRange;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTimeRange() { return timeRange; }
}
