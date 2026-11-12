package com.example.attendanceapp.ui.manager.payroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.AvatarLoader;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.data.dto.request.manager.CalculatePayrollRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateBonusRequest;
import com.example.attendanceapp.data.dto.request.manager.CreateDeductionRequest;
import com.example.attendanceapp.data.dto.response.manager.StaffPayrollSummaryResponse;
import com.example.attendanceapp.databinding.FragmentManagerPayrollDetailBinding;
import com.example.attendanceapp.databinding.ItemSalaryAdjustmentBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerPayrollDetailFragment extends Fragment {

    private FragmentManagerPayrollDetailBinding binding;
    private ManagerPayrollCalendarAdapter calendarAdapter;
    private AdjustmentAdapter adjustmentAdapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private String staffId = "";
    private String staffName = "";
    private String staffCode = "";
    private String staffAvatar = "";
    private String staffPosition = "";

    private double currentCalculatedBaseSalary = 0;
    private final Map<LocalDate, List<StaffPayrollSummaryResponse.ShiftItem>> shiftMap = new HashMap<>();

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

        if (getArguments() != null) {
            staffId = getArguments().getString("staff_id", "");
            staffName = getArguments().getString("staff_name", "");
            staffCode = getArguments().getString("staff_code", "");
            staffAvatar = getArguments().getString("staff_avatar", "");
            staffPosition = getArguments().getString("staff_position", "");
        }

        setupToolbar();
        setupViews();
        loadPayrollDetail();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Chi tiết tính lương");
    }

    private void setupViews() {
        binding.tvDetailName.setText(staffName.isEmpty() ? "Nhân viên" : staffName);
        binding.tvDetailCode.setText("Mã: " + (staffCode.isEmpty() ? "NV" : staffCode) + " • Vị trí: " + (staffPosition.isEmpty() ? "Nhân sự" : staffPosition));
        AvatarLoader.loadAvatar(requireContext(), staffAvatar, binding.ivAvatar);

        adjustmentAdapter = new AdjustmentAdapter();
        binding.rvAdjustments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdjustments.setAdapter(adjustmentAdapter);
    }

    private void loadPayrollDetail() {
        if (staffId == null || staffId.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy mã nhân viên", Toast.LENGTH_SHORT).show();
            return;
        }

        disposables.add(
                managerRepository.getStaffPayrollSummary(staffId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::bindSummaryData,
                                error -> {
                                    Toast.makeText(getContext(), "Lỗi tải dữ liệu bảng lương: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }

    private void bindSummaryData(StaffPayrollSummaryResponse response) {
        if (response == null || binding == null) return;

        int shiftsCount = response.getNumberOfWorkShifts();
        double totalBonus = response.getTotalBonus();
        double totalDeduction = response.getTotalDeduction();
        double totalHours = 0;

        if (response.getShifts() != null) {
            for (StaffPayrollSummaryResponse.ShiftItem shift : response.getShifts()) {
                totalHours += calculateWorkedHours(shift);
            }
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("SALARY_CONFIG_PREFS", Context.MODE_PRIVATE);
        String shiftRateStr = prefs.getString("rate_morning", "200000").replaceAll("[.,]", "");
        double shiftRate = 200000;
        try {
            shiftRate = Double.parseDouble(shiftRateStr);
        } catch (Exception ignored) {}

        double baseSalary = shiftsCount * shiftRate;
        currentCalculatedBaseSalary = baseSalary;
        double finalSalary = Math.max(0, baseSalary + totalBonus - totalDeduction);

        // Thống kê làm việc
        binding.rowShifts.tvStatTitle.setText("Số ca làm");
        binding.rowShifts.tvStatValue.setText(shiftsCount + " ca");

        binding.rowPaidLeave.tvStatTitle.setText("Đơn giá ca");
        binding.rowPaidLeave.tvStatValue.setText(formatCurrency(shiftRate));

        binding.rowUnpaidLeave.tvStatTitle.setText("Tổng giờ làm");
        binding.rowUnpaidLeave.tvStatValue.setText(String.format(Locale.US, "%.1f giờ", totalHours));

        binding.rowBasicSalary.tvStatTitle.setText("Lương cơ bản");
        binding.rowBasicSalary.tvStatValue.setText(formatCurrency(baseSalary));

        // Tổng kết
        binding.tvTotalBonus.setText("+" + formatCurrency(totalBonus));
        binding.tvTotalDeduction.setText("-" + formatCurrency(totalDeduction));
        binding.tvFinalSalary.setText(formatCurrency(finalSalary));

        // Thưởng & Khấu trừ
        List<AdjustmentItem> adjustmentItems = new ArrayList<>();
        if (response.getBonuses() != null) {
            for (StaffPayrollSummaryResponse.BonusItem b : response.getBonuses()) {
                String desc = (b.getReason() != null ? b.getReason() : "Thưởng") 
                        + (b.getNote() != null && !b.getNote().isEmpty() ? " (" + b.getNote() + ")" : "");
                adjustmentItems.add(new AdjustmentItem(desc, b.getAmount(), true));
            }
        }
        if (response.getDeductions() != null) {
            for (StaffPayrollSummaryResponse.DeductionItem d : response.getDeductions()) {
                String desc = (d.getReason() != null ? d.getReason() : "Khấu trừ")
                        + (d.getType() != null && !d.getType().isEmpty() ? " [" + d.getType() + "]" : "");
                adjustmentItems.add(new AdjustmentItem(desc, d.getAmount(), false));
            }
        }
        adjustmentAdapter.setItems(adjustmentItems);

        // Xử lý Lịch làm việc
        shiftMap.clear();
        LocalDate now = LocalDate.now();
        List<WeekDay> days = new ArrayList<>();

        if (response.getShifts() != null) {
            for (StaffPayrollSummaryResponse.ShiftItem shift : response.getShifts()) {
                if (shift.getWorkDay() != null) {
                    try {
                        LocalDate date = LocalDate.parse(shift.getWorkDay().substring(0, 10));
                        List<StaffPayrollSummaryResponse.ShiftItem> list = shiftMap.computeIfAbsent(date, k -> new ArrayList<>());
                        list.add(shift);
                    } catch (Exception ignored) {}
                }
            }
        }

        // Tạo 30 ngày gần nhất
        LocalDate startDate = now.minusDays(29);
        for (int i = 0; i < 30; i++) {
            LocalDate d = startDate.plusDays(i);
            AttendanceStatus status = AttendanceStatus.NOT_CHECKED;

            if (shiftMap.containsKey(d)) {
                List<StaffPayrollSummaryResponse.ShiftItem> dayShifts = shiftMap.get(d);
                // Ưu tiên trạng thái xấu nhất để hiển thị trên lịch: ABSENT > LATE > PRESENT
                status = AttendanceStatus.PRESENT;
                for (StaffPayrollSummaryResponse.ShiftItem s : dayShifts) {
                    String st = s.getStatus();
                    if ("LATE".equalsIgnoreCase(st) && status != AttendanceStatus.ABSENT) {
                        status = AttendanceStatus.LATE;
                    } else if ("ABSENT".equalsIgnoreCase(st)) {
                        status = AttendanceStatus.ABSENT;
                    }
                }
            } else if (d.getDayOfWeek().getValue() == 7) {
                status = AttendanceStatus.WEEKEND;
            }

            days.add(new WeekDay(d, status, d.equals(now)));
        }

        setupCalendar(days);
    }

    private void setupCalendar(List<WeekDay> days) {
        calendarAdapter = new ManagerPayrollCalendarAdapter(days, this::updateDayNote);
        binding.rvPayrollCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.rvPayrollCalendar.setAdapter(calendarAdapter);
    }

    private String formatCurrency(double amount) {
        if (amount == 0) return "0đ";
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format((long) amount) + "đ";
    }

    private void updateDayNote(WeekDay day) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        binding.tvNoteSelectedDay.setText("Ngày: " + day.getDate().format(formatter));

        List<StaffPayrollSummaryResponse.ShiftItem> shifts = shiftMap.get(day.getDate());
        if (shifts != null && !shifts.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (StaffPayrollSummaryResponse.ShiftItem shift : shifts) {
                String shiftName = shift.getShiftName() != null ? shift.getShiftName() : "Ca làm";
                String checkIn = shift.getCheckInTime() != null ? shift.getCheckInTime() : "--:--";
                String checkOut = shift.getCheckOutTime() != null ? shift.getCheckOutTime() : "--:--";
                if (sb.length() > 0) sb.append("\n");
                sb.append("Ca: ").append(shiftName)
                        .append(" (Check-in: ").append(checkIn)
                        .append(" - Check-out: ").append(checkOut).append(")");
            }
            binding.tvNoteShifts.setText(sb.toString());
            binding.tvNoteShifts.setTextColor(ContextCompat.getColor(requireContext(), R.color.attendance_present));
            return;
        }

        String statusText;
        int colorRes;

        switch (day.getStatus()) {
            case PRESENT:
                statusText = "Ca làm: Đã chấm công đúng giờ";
                colorRes = R.color.attendance_present;
                break;
            case LATE:
                statusText = "Ca làm: Đi muộn";
                colorRes = R.color.attendance_late;
                break;
            case ABSENT:
                statusText = "Vắng mặt không phép";
                colorRes = R.color.attendance_absent;
                break;
            case WEEKEND:
                statusText = "Ngày nghỉ tuần";
                colorRes = R.color.attendance_weekend;
                break;
            default:
                statusText = "Không có ca làm việc";
                colorRes = R.color.attendance_not_checked;
                break;
        }

        binding.tvNoteShifts.setText(statusText);
        binding.tvNoteShifts.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void setupListeners() {
        binding.btnAddAdjustment.setOnClickListener(v -> showAddAdjustmentDialog());

        binding.btnConfirmSalary.setOnClickListener(v -> {
            if (staffId == null || staffId.isEmpty()) return;

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xác nhận tính lương")
                    .setMessage("Bạn có chắc chắn muốn lưu và chốt bảng lương cho nhân viên này?")
                    .setPositiveButton("Xác nhận", (dialog, which) -> {
                        CalculatePayrollRequest request = new CalculatePayrollRequest(staffId, currentCalculatedBaseSalary);
                        disposables.add(
                                managerRepository.calculatePayroll(request)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                res -> {
                                                    Toast.makeText(getContext(), "Tính và lưu bảng lương thành công!", Toast.LENGTH_SHORT).show();
                                                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                                                },
                                                err -> {
                                                    Toast.makeText(getContext(), "Lỗi khi chốt lương: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                        )
                        );
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showAddAdjustmentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_salary_adjustment, null);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_adjustment_type);
        EditText etAmount = dialogView.findViewById(R.id.et_adjustment_amount);
        EditText etReason = dialogView.findViewById(R.id.et_adjustment_reason);
        EditText etNote = dialogView.findViewById(R.id.et_adjustment_note);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm thưởng / khấu trừ")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim().replaceAll("[.,]", "");
                    String reason = etReason.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (amountStr.isEmpty() || reason.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập số tiền và lý do", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isBonus = rgType.getCheckedRadioButtonId() == R.id.rb_bonus;

                    if (isBonus) {
                        CreateBonusRequest req = new CreateBonusRequest(staffId, amount, reason, note);
                        disposables.add(
                                managerRepository.createBonus(req)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                res -> {
                                                    Toast.makeText(getContext(), "Đã thêm tiền thưởng thành công", Toast.LENGTH_SHORT).show();
                                                    loadPayrollDetail();
                                                },
                                                err -> Toast.makeText(getContext(), "Thêm thưởng thất bại: " + err.getMessage(), Toast.LENGTH_SHORT).show()
                                        )
                        );
                    } else {
                        CreateDeductionRequest req = new CreateDeductionRequest(staffId, amount, "PENALTY", reason);
                        disposables.add(
                                managerRepository.createDeduction(req)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                res -> {
                                                    Toast.makeText(getContext(), "Đã thêm khoản phạt thành công", Toast.LENGTH_SHORT).show();
                                                    loadPayrollDetail();
                                                },
                                                err -> Toast.makeText(getContext(), "Thêm khấu trừ thất bại: " + err.getMessage(), Toast.LENGTH_SHORT).show()
                                        )
                        );
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }

    private double calculateWorkedHours(StaffPayrollSummaryResponse.ShiftItem shift) {
        LocalTime checkIn = parseTime(shift.getCheckInTime());
        LocalTime checkOut = parseTime(shift.getCheckOutTime());

        if (checkIn == null || checkOut == null) return 0.0;

        Duration duration = Duration.between(checkIn, checkOut);

        // Xử lý trường hợp ca làm việc xuyên đêm
        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }

        return duration.getSeconds() / 3600.0;
    }

    private LocalTime parseTime(Object timeObj) {
        if (timeObj == null) return null;
        String timeStr = timeObj.toString().trim();
        if (timeStr.isEmpty()) return null;
        try {
            if (timeStr.matches("^\\d+(\\.\\d+)?$")) {
                double seconds = Double.parseDouble(timeStr);
                return java.time.Instant.ofEpochMilli((long) (seconds * 1000))
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalTime();
            }
            if (timeStr.contains("T")) {
                return java.time.LocalDateTime.parse(timeStr).toLocalTime();
            }
            if (timeStr.length() >= 8) {
                return LocalTime.parse(timeStr.substring(0, 8));
            }
            if (timeStr.length() >= 5) {
                return LocalTime.parse(timeStr.substring(0, 5));
            }
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Adapter & Models for Adjustments ====================

    private static class AdjustmentItem {
        final String title;
        final double amount;
        final boolean isBonus;

        AdjustmentItem(String title, double amount, boolean isBonus) {
            this.title = title;
            this.amount = amount;
            this.isBonus = isBonus;
        }
    }

    private class AdjustmentAdapter extends RecyclerView.Adapter<AdjustmentAdapter.ViewHolder> {
        private final List<AdjustmentItem> list = new ArrayList<>();

        void setItems(List<AdjustmentItem> items) {
            list.clear();
            if (items != null) list.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSalaryAdjustmentBinding b = ItemSalaryAdjustmentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AdjustmentItem item = list.get(position);
            holder.binding.tvAdjustmentTitle.setText(item.title);

            DecimalFormat df = new DecimalFormat("#,###");
            if (item.isBonus) {
                holder.binding.tvAdjustmentAmount.setText("+" + df.format((long) item.amount) + "đ");
                holder.binding.tvAdjustmentAmount.setTextColor(
                        ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendancePresent)
                );
            } else {
                holder.binding.tvAdjustmentAmount.setText("-" + df.format((long) item.amount) + "đ");
                holder.binding.tvAdjustmentAmount.setTextColor(
                        ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendanceAbsent)
                );
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemSalaryAdjustmentBinding binding;
            ViewHolder(ItemSalaryAdjustmentBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
