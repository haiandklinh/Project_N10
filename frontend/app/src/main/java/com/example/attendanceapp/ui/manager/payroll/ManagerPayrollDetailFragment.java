package com.example.attendanceapp.ui.manager.payroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerPayrollDetailBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerPayrollDetailFragment extends Fragment {

    private FragmentManagerPayrollDetailBinding binding;
    private ManagerPayrollCalendarAdapter calendarAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerPayrollDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupCalendar();
        setupMockData();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Chi tiết tính lương");
    }

    private void setupCalendar() {
        // Mock dữ liệu lịch chấm công trong tháng
        List<WeekDay> mockDays = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int daysInMonth = now.lengthOfMonth();
        
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = LocalDate.of(now.getYear(), now.getMonth(), i);
            AttendanceStatus status;
            
            // Mock ngẫu nhiên trạng thái để minh họa UI
            if (i % 7 == 0 || i % 7 == 6) {
                status = AttendanceStatus.WEEKEND;
            } else if (i == 5) {
                status = AttendanceStatus.LATE;
            } else if (i == 12) {
                status = AttendanceStatus.ABSENT;
            } else if (i > now.getDayOfMonth()) {
                status = AttendanceStatus.NOT_CHECKED;
            } else {
                status = AttendanceStatus.PRESENT;
            }
            
            mockDays.add(new WeekDay(date, status, date.equals(now)));
        }

        calendarAdapter = new ManagerPayrollCalendarAdapter(mockDays, day -> {
            updateDayNote(day);
        });

        binding.rvPayrollCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.rvPayrollCalendar.setAdapter(calendarAdapter);
    }

    private void updateDayNote(WeekDay day) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        binding.tvNoteSelectedDay.setText("Ngày: " + day.getDate().format(formatter));
        
        String statusText;
        int colorRes;
        
        switch (day.getStatus()) {
            case PRESENT:
                statusText = "Ca làm: 08:00 - 17:00 (Đã đi làm - Đúng giờ)";
                colorRes = R.color.attendance_present;
                break;
            case LATE:
                statusText = "Ca làm: 08:00 - 17:00 (Đi muộn - Check-in lúc 08:15)";
                colorRes = R.color.attendance_late;
                break;
            case ABSENT:
                statusText = "Ca làm: 08:00 - 17:00 (Vắng mặt không lý do)";
                colorRes = R.color.attendance_absent;
                break;
            case WEEKEND:
                statusText = "Ngày nghỉ tuần (Không có ca)";
                colorRes = R.color.attendance_weekend;
                break;
            case NOT_CHECKED:
                statusText = "Chưa đến thời gian chấm công";
                colorRes = R.color.attendance_not_checked;
                break;
            default:
                statusText = "Ca làm: Không có dữ liệu";
                colorRes = R.color.attendance_not_checked;
                break;
        }
        
        binding.tvNoteShifts.setText(statusText);
        binding.tvNoteShifts.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void setupMockData() {
        binding.tvDetailName.setText("Nguyễn Văn Hoàng");
        binding.tvDetailCode.setText("Mã NV: NV001 • Vị trí: Nhân viên Sale");

        binding.rowShifts.tvStatTitle.setText("Số ca làm");
        binding.rowShifts.tvStatValue.setText("24 ca");

        binding.rowPaidLeave.tvStatTitle.setText("Nghỉ có phép");
        binding.rowPaidLeave.tvStatValue.setText("1 ca");

        binding.rowUnpaidLeave.tvStatTitle.setText("Nghỉ không phép");
        binding.rowUnpaidLeave.tvStatValue.setText("0 ca");

        binding.rowBasicSalary.tvStatTitle.setText("Lương cơ bản");
        binding.rowBasicSalary.tvStatValue.setText("7.200.000 đ");

        binding.tvTotalBonus.setText("+500.000đ");
        binding.tvTotalDeduction.setText("0đ");
        binding.tvFinalSalary.setText("7.700.000 đ");
    }

    private void setupListeners() {
        binding.btnAddAdjustment.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Thêm khoản thưởng / khấu trừ", Toast.LENGTH_SHORT).show();
        });

        binding.btnConfirmSalary.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính lương thành công & Đã lưu bảng lương!", Toast.LENGTH_SHORT).show();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
