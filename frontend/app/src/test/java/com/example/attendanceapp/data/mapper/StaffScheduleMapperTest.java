package com.example.attendanceapp.data.mapper;

import static org.junit.Assert.assertEquals;

import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.domain.model.Shift;
import com.example.attendanceapp.domain.model.ShiftOption;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class StaffScheduleMapperTest {

    @Test
    public void toShifts_prefersShiftInfoAddressOverOtherFallbacks() {
        StaffWorkScheduleResponse.ShiftInfo info =
                new StaffWorkScheduleResponse.ShiftInfo("ws1", "Ca Sáng", "08:00:00", "12:00:00");
        info.setAddress("123 Đường Cầu Giấy, Hà Nội, Việt Nam");

        StaffWorkScheduleResponse schedule = new StaffWorkScheduleResponse(
                LocalDate.now().toString(),
                Collections.singletonList(info)
        );
        ShiftOption option = new ShiftOption("s1", "Ca Sáng", "08:00", "12:00", 21.0285, 105.8542);

        List<Shift> shifts = StaffScheduleMapper.toShifts(
                Collections.singletonList(schedule),
                Collections.singletonList(option),
                null,
                "emp1",
                "Công ty ABC"
        );

        assertEquals(1, shifts.size());
        assertEquals("123 Đường Cầu Giấy, Hà Nội", shifts.get(0).getLocation());
    }

    @Test
    public void toShifts_usesMatchedShiftOptionCoordinatesWhenAddressMissing() {
        StaffWorkScheduleResponse.ShiftInfo info =
                new StaffWorkScheduleResponse.ShiftInfo("ws1", "Ca Chiều", "13:00:00", "17:00:00");

        StaffWorkScheduleResponse schedule = new StaffWorkScheduleResponse(
                LocalDate.now().toString(),
                Collections.singletonList(info)
        );
        ShiftOption option = new ShiftOption("s2", "Ca Chiều", "13:00", "17:00", 21.02851, 105.85421);

        List<Shift> shifts = StaffScheduleMapper.toShifts(
                Collections.singletonList(schedule),
                Collections.singletonList(option),
                null,
                "emp1",
                "Công ty ABC"
        );

        assertEquals(1, shifts.size());
        String expectedCoords = String.format(Locale.getDefault(), "%.5f, %.5f", 21.02851, 105.85421);
        assertEquals(expectedCoords, shifts.get(0).getLocation());
    }

    @Test
    public void toShifts_fallsBackToCompanyNameWhenNoLocationOrCoordsExist() {
        StaffWorkScheduleResponse.ShiftInfo info =
                new StaffWorkScheduleResponse.ShiftInfo("ws1", "Ca Tối", "18:00:00", "22:00:00");

        StaffWorkScheduleResponse schedule = new StaffWorkScheduleResponse(
                LocalDate.now().toString(),
                Collections.singletonList(info)
        );

        List<Shift> shifts = StaffScheduleMapper.toShifts(
                Collections.singletonList(schedule),
                Collections.emptyList(),
                null,
                "emp1",
                "Công ty ABC"
        );

        assertEquals(1, shifts.size());
        assertEquals("Công ty ABC", shifts.get(0).getLocation());
    }
}
