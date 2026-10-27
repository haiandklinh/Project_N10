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
import com.example.attendanceapp.domain.repository.ManagerRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerPayrollDetailFragment extends Fragment {

    private FragmentManagerPayrollDetailBinding binding;
    private ManagerPayrollCalendarAdapter calendarAdapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    ManagerRepository managerRepository;

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
        loadPayrollDetail();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Chi tiết tính lương");
    }

    private void loadPayrollDetail() {
        disposables.add(
                managerRepository.getPayrollDetail()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    try {
                                        String jsonStr = response.string();
                                        JSONObject json = new JSONObject(jsonStr);

                                        JSONObject employee = json.getJSONObject("employee");
                                        binding.tvDetailName.setText(employee.getString("full_name"));
                                        binding.tvDetailCode.setText("Mã NV: " + employee.getString("code") + " • Vị trí: " + employee.getString("position"));

                                        JSONObject stats = json.getJSONObject("statistics");
                                        binding.rowShifts.tvStatTitle.setText("Số ca làm");
                                        binding.rowShifts.tvStatValue.setText(stats.getInt("total_shifts") + " ca");

                                        binding.rowPaidLeave.tvStatTitle.setText("Nghỉ có phép");
                                        binding.rowPaidLeave.tvStatValue.setText(stats.getInt("paid_leave") + " ca");

                                        binding.rowUnpaidLeave.tvStatTitle.setText("Nghỉ không phép");
                                        binding.rowUnpaidLeave.tvStatValue.setText(stats.getInt("unpaid_leave") + " ca");

                                        JSONObject salary = json.getJSONObject("salary");
                                        binding.rowBasicSalary.tvStatTitle.setText("Lương cơ bản");
                                        binding.rowBasicSalary.tvStatValue.setText(formatCurrency(salary.getInt("basic_salary")));

                                        binding.tvTotalBonus.setText("+" + formatCurrency(salary.getInt("bonus")));
                                        binding.tvTotalDeduction.setText(formatCurrency(salary.getInt("deduction")));
                                        binding.tvFinalSalary.setText(formatCurrency(salary.getInt("final_salary")));

                                        JSONArray calendarArray = json.getJSONArray("attendance_calendar");
                                        List<WeekDay> days = new ArrayList<>();
                                        LocalDate now = LocalDate.now();

                                        for (int i = 0; i < calendarArray.length(); i++) {
                                            JSONObject dayItem = calendarArray.getJSONObject(i);
                                            LocalDate date = LocalDate.parse(dayItem.getString("date"));
                                            AttendanceStatus status = AttendanceStatus.valueOf(dayItem.getString("status"));
                                            days.add(new WeekDay(date, status, date.equals(now)));
                                        }

                                        setupCalendar(days);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getContext(), "Lỗi phân tích dữ liệu", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }

    private void setupCalendar(List<WeekDay> days) {
        calendarAdapter = new ManagerPayrollCalendarAdapter(days, day -> {
            updateDayNote(day);
        });

        binding.rvPayrollCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.rvPayrollCalendar.setAdapter(calendarAdapter);
    }

    private String formatCurrency(int amount) {
        if (amount == 0) return "0đ";
        return String.format("%,d đ", amount);
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
        disposables.clear();
        binding = null;
    }
}
