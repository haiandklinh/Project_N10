package com.example.attendanceapp.domain.usecase.home;

import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.example.attendanceapp.ui.manager.home.TodayShiftSummary;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetTodayShiftUseCaseTest {

    private GetTodayShiftUseCase useCase;

    @Before
    public void setUp() {
        useCase = new GetTodayShiftUseCase(null, null, null);
    }

    @Test
    public void mapToTodayShifts_withEmptySchedules_returnsEmptyList() {
        LocalDate today = LocalDate.now();
        List<TodayShiftSummary> result = useCase.mapToTodayShifts(Collections.emptyList(), Collections.emptyList(), today);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void mapToTodayShifts_withSchedulesOnOtherDates_returnsEmptyList() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        EmployeeWorkSchedule.ShiftInfo shift = new EmployeeWorkSchedule.ShiftInfo("s1", "Ca sáng", "08:00", "12:00");
        EmployeeWorkSchedule schedule = new EmployeeWorkSchedule("emp1", "Nguyen Van A", "NV", yesterday.toString(), null, "active", shift);

        List<TodayShiftSummary> result = useCase.mapToTodayShifts(Collections.singletonList(schedule), Collections.emptyList(), today);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void mapToTodayShifts_withSchedulesToday_groupsByShiftAndCountsEmployees() {
        LocalDate today = LocalDate.now();

        EmployeeWorkSchedule.ShiftInfo morningShift = new EmployeeWorkSchedule.ShiftInfo("s1", "Ca sáng", "08:00", "12:00");
        EmployeeWorkSchedule.ShiftInfo afternoonShift = new EmployeeWorkSchedule.ShiftInfo("s2", "Ca chiều", "13:00", "17:00");

        EmployeeWorkSchedule emp1 = new EmployeeWorkSchedule("emp1", "Nguyen Van A", "NV", today.toString(), null, "active", morningShift);
        EmployeeWorkSchedule emp2 = new EmployeeWorkSchedule("emp2", "Tran Thi B", "NV", today.toString(), null, "active", morningShift);
        EmployeeWorkSchedule emp3 = new EmployeeWorkSchedule("emp3", "Le Van C", "NV", today.toString(), null, "active", afternoonShift);

        List<EmployeeWorkSchedule> schedules = Arrays.asList(emp1, emp2, emp3);

        List<TodayShiftSummary> result = useCase.mapToTodayShifts(schedules, Collections.emptyList(), today);

        assertNotNull(result);
        assertEquals(2, result.size());

        TodayShiftSummary firstShift = result.get(0);
        assertEquals("Ca sáng", firstShift.getShiftName());
        assertEquals("08:00 - 12:00", firstShift.getTimeRange());
        assertEquals(2, firstShift.getEmployeeCount());

        TodayShiftSummary secondShift = result.get(1);
        assertEquals("Ca chiều", secondShift.getShiftName());
        assertEquals("13:00 - 17:00", secondShift.getTimeRange());
        assertEquals(1, secondShift.getEmployeeCount());
    }

    @Test
    public void mapToTodayShifts_withShiftOptions_enrichesTimesAndTitle() {
        LocalDate today = LocalDate.now();

        // ShiftInfo has only shiftId "s1"
        EmployeeWorkSchedule.ShiftInfo shiftInfo = new EmployeeWorkSchedule.ShiftInfo("s1", null, null, null);
        EmployeeWorkSchedule emp = new EmployeeWorkSchedule("emp1", "Nguyen Van A", "NV", today.toString(), null, "active", shiftInfo);

        ShiftOption option = new ShiftOption("s1", "Ca Sáng Tiêu Chuẩn", "08:30", "17:30", 0.0, 0.0);

        List<TodayShiftSummary> result = useCase.mapToTodayShifts(
                Collections.singletonList(emp),
                Collections.singletonList(option),
                today
        );

        assertNotNull(result);
        assertEquals(1, result.size());

        TodayShiftSummary summary = result.get(0);
        assertEquals("Ca Sáng Tiêu Chuẩn", summary.getShiftName());
        assertEquals("08:30 - 17:30", summary.getTimeRange());
        assertEquals(1, summary.getEmployeeCount());
    }
}
