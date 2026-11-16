package com.example.attendanceapp.ui.manager.payroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.dto.request.manager.CalculatePayrollRequest;
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.CalculatePayrollResponse;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.databinding.FragmentManagerPayrollBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerPayrollFragment extends Fragment {

    private FragmentManagerPayrollBinding binding;
    private ManagerPayrollAdapter adapter;
    private final List<ManagerEmployeeItem> fullEmployeeList = new ArrayList<>();
    private final List<ManagerEmployeeItem> employeeList = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private int selectedYear;
    private int selectedMonth;

    @Inject
    ManagerRepository managerRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerPayrollBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocalDate now = LocalDate.now();
        selectedYear = now.getYear();
        selectedMonth = now.getMonthValue();

        setupToolbar();
        setupList();
        setupListeners();
        updateMonthDisplay();
        loadPayrollFromApi();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPayrollFromApi();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Quản lý tiền lương");
    }

    private void updateMonthDisplay() {
        if (binding == null) return;
        binding.tvMonthPicker.setText(String.format(Locale.getDefault(), "Tháng %02d/%d", selectedMonth, selectedYear));
    }

    private void setupList() {
        binding.rvPayrollList.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ManagerPayrollAdapter(new ArrayList<>(), employee -> {
            Bundle bundle = new Bundle();
            bundle.putString("staff_id", employee.getUserId());
            bundle.putString("staff_name", employee.getFullName());
            bundle.putString("staff_code", employee.getEmployeeCode());
            bundle.putString("staff_avatar", employee.getAvatarUrl());
            bundle.putString("staff_position", employee.getPosition());
            bundle.putInt("month", selectedMonth);
            bundle.putInt("year", selectedYear);

            Navigation.findNavController(requireView()).navigate(
                    R.id.action_managerPayrollFragment_to_managerPayrollDetailFragment, bundle
            );
        });

        binding.rvPayrollList.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.tvMonthPicker.setOnClickListener(v -> showMonthPickerDialog());
        binding.layoutFilter.setEndIconOnClickListener(v -> showMonthPickerDialog());

        binding.btnSalaryConfig.setOnClickListener(v -> showSalaryConfigDialog());

        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds != null && !checkedIds.isEmpty()) {
                applyFilter(checkedIds.get(0));
            } else {
                applyFilter(R.id.chip_filter_all);
            }
        });

        binding.btnConfirmAll.setOnClickListener(v -> {
            if (employeeList.isEmpty()) {
                Toast.makeText(getContext(), "Không có nhân viên để chốt lương", Toast.LENGTH_SHORT).show();
                return;
            }

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xác nhận chốt lương")
                    .setMessage("Bạn có chắc chắn muốn chốt lương cho toàn bộ " + employeeList.size() + " nhân viên trong danh sách?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> confirmAllPayrolls())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void showMonthPickerDialog() {
        Context context = requireContext();
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        int paddingHoriz = getResources().getDimensionPixelSize(R.dimen.spacing_xl);
        int paddingVert = getResources().getDimensionPixelSize(R.dimen.spacing_l);
        container.setPadding(paddingHoriz, paddingVert, paddingHoriz, paddingVert);

        NumberPicker monthPicker = new NumberPicker(context);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(selectedMonth);
        String[] monthNames = new String[12];
        for (int i = 1; i <= 12; i++) {
            monthNames[i - 1] = String.format(Locale.getDefault(), "Tháng %02d", i);
        }
        monthPicker.setDisplayedValues(monthNames);
        monthPicker.setWrapSelectorWheel(true);

        NumberPicker yearPicker = new NumberPicker(context);
        int currentYear = LocalDate.now().getYear();
        yearPicker.setMinValue(currentYear - 5);
        yearPicker.setMaxValue(currentYear + 5);
        yearPicker.setValue(selectedYear);
        yearPicker.setWrapSelectorWheel(false);

        LinearLayout.LayoutParams lpMonth = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f
        );
        LinearLayout.LayoutParams lpYear = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
        );
        lpMonth.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.spacing_m));
        monthPicker.setLayoutParams(lpMonth);
        yearPicker.setLayoutParams(lpYear);

        container.addView(monthPicker);
        container.addView(yearPicker);

        new MaterialAlertDialogBuilder(context)
                .setTitle("Chọn tháng xem lương")
                .setView(container)
                .setPositiveButton("Chọn", (dialog, which) -> {
                    selectedMonth = monthPicker.getValue();
                    selectedYear = yearPicker.getValue();
                    updateMonthDisplay();
                    loadPayrollFromApi();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadPayrollFromApi() {
        if (managerRepository == null) return;
        showLoading();

        LocalDate now = LocalDate.now();
        final boolean isCurrentMonth = (selectedYear == now.getYear() && selectedMonth == now.getMonthValue());
        final int targetYear = selectedYear;
        final int targetMonth = selectedMonth;

        disposables.add(
                Single.zip(
                        managerRepository.getStaffs().onErrorReturnItem(new BaseListResponse<>(new ArrayList<>())),
                        managerRepository.getPayrollHistory(null).onErrorReturnItem(new BaseListResponse<>(new ArrayList<>())),
                        (staffResponse, payrollResponse) -> {
                            Map<String, CalculatePayrollResponse> payrollByStaffForMonth = new HashMap<>();
                            if (payrollResponse != null && payrollResponse.getData() != null) {
                                for (CalculatePayrollResponse p : payrollResponse.getData()) {
                                    if (p != null && p.getStaffId() != null && isMatchingMonth(p.getTimeCreate(), targetYear, targetMonth)) {
                                        CalculatePayrollResponse current = payrollByStaffForMonth.get(p.getStaffId());
                                        if (current == null || isMoreRecent(p.getTimeCreate(), current.getTimeCreate())) {
                                            payrollByStaffForMonth.put(p.getStaffId(), p);
                                        }
                                    }
                                }
                            }

                            Map<String, StaffItemResponse> staffInfoMap = new HashMap<>();
                            if (staffResponse != null && staffResponse.getData() != null) {
                                for (StaffItemResponse staff : staffResponse.getData()) {
                                    if (staff != null && staff.getStaffId() != null) {
                                        staffInfoMap.put(staff.getStaffId(), staff);
                                    }
                                }
                            }

                            List<ManagerEmployeeItem> items = new ArrayList<>();

                            if (isCurrentMonth) {
                                if (staffResponse != null && staffResponse.getData() != null) {
                                    for (StaffItemResponse staff : staffResponse.getData()) {
                                        String id = staff.getStaffId();
                                        String code = "NV-" + (id != null && id.length() >= 4 ? id.substring(id.length() - 4) : (id != null ? id : ""));
                                        EmployeeStatus status = "deactive".equalsIgnoreCase(staff.getStatus())
                                                ? EmployeeStatus.TERMINATED
                                                : EmployeeStatus.ACTIVE;
                                        String pos = staff.getPosition() != null && !staff.getPosition().isEmpty() ? staff.getPosition() : "Nhân viên";

                                        CalculatePayrollResponse payroll = id != null ? payrollByStaffForMonth.get(id) : null;
                                        int shifts = payroll != null ? payroll.getNumberOfWorkShifts() : 0;
                                        long salary = payroll != null ? (long) payroll.getTotalSalary() : 0L;
                                        String payrollStatus = payroll != null ? payroll.getStatus() : null;

                                        ManagerEmployeeItem item = new ManagerEmployeeItem(
                                                id, code,
                                                staff.getNameStaff() != null ? staff.getNameStaff() : "Nhân viên",
                                                staff.getAvatar() != null ? staff.getAvatar() : "",
                                                pos, "Phòng ban chung", "",
                                                staff.getDateOfBirth() != null ? staff.getDateOfBirth() : "",
                                                status, shifts, 0, salary
                                        );
                                        item.setPayrollStatus(payrollStatus);
                                        items.add(item);
                                    }
                                }

                                if (items.isEmpty() && !payrollByStaffForMonth.isEmpty()) {
                                    for (Map.Entry<String, CalculatePayrollResponse> entry : payrollByStaffForMonth.entrySet()) {
                                        String id = entry.getKey();
                                        CalculatePayrollResponse payroll = entry.getValue();
                                        String code = "NV-" + (id != null && id.length() >= 4 ? id.substring(id.length() - 4) : (id != null ? id : ""));
                                        String name = payroll.getStaffName() != null && !payroll.getStaffName().isEmpty() ? payroll.getStaffName() : "Nhân viên";
                                        ManagerEmployeeItem item = new ManagerEmployeeItem(
                                                id, code, name, "", "Nhân viên", "Phòng ban chung", "", "",
                                                EmployeeStatus.ACTIVE, payroll.getNumberOfWorkShifts(), 0, (long) payroll.getTotalSalary()
                                        );
                                        item.setPayrollStatus(payroll.getStatus());
                                        items.add(item);
                                    }
                                }
                            } else {
                                for (Map.Entry<String, CalculatePayrollResponse> entry : payrollByStaffForMonth.entrySet()) {
                                    String id = entry.getKey();
                                    CalculatePayrollResponse payroll = entry.getValue();
                                    StaffItemResponse staff = staffInfoMap.get(id);

                                    String code = "NV-" + (id != null && id.length() >= 4 ? id.substring(id.length() - 4) : (id != null ? id : ""));
                                    String name = (staff != null && staff.getNameStaff() != null && !staff.getNameStaff().isEmpty())
                                            ? staff.getNameStaff()
                                            : (payroll.getStaffName() != null && !payroll.getStaffName().isEmpty() ? payroll.getStaffName() : "Nhân viên");
                                    String avatar = (staff != null && staff.getAvatar() != null) ? staff.getAvatar() : "";
                                    String pos = (staff != null && staff.getPosition() != null && !staff.getPosition().isEmpty()) ? staff.getPosition() : "Nhân viên";
                                    String dob = (staff != null && staff.getDateOfBirth() != null) ? staff.getDateOfBirth() : "";
                                    EmployeeStatus status = (staff != null && "deactive".equalsIgnoreCase(staff.getStatus()))
                                            ? EmployeeStatus.TERMINATED
                                            : EmployeeStatus.ACTIVE;

                                    int shifts = payroll.getNumberOfWorkShifts();
                                    long salary = (long) payroll.getTotalSalary();
                                    String payrollStatus = payroll.getStatus();

                                    ManagerEmployeeItem item = new ManagerEmployeeItem(
                                            id, code, name, avatar, pos, "Phòng ban chung", "", dob,
                                            status, shifts, 0, salary
                                    );
                                    item.setPayrollStatus(payrollStatus);
                                    items.add(item);
                                }
                            }

                            return items;
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        items -> {
                            if (items == null || items.isEmpty()) {
                                showEmptyState();
                            } else {
                                showContent(items);
                            }
                        },
                        error -> {
                            Toast.makeText(getContext(), "Không thể tải danh sách lương: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        }
                )
        );
    }

    private void showLoading() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvPayrollList.setVisibility(View.GONE);
        binding.layoutEmptyState.getRoot().setVisibility(View.GONE);
    }

    private void showContent(List<ManagerEmployeeItem> items) {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutEmptyState.getRoot().setVisibility(View.GONE);
        binding.rvPayrollList.setVisibility(View.VISIBLE);
        binding.cardPayrollSummary.setVisibility(View.VISIBLE);
        binding.layoutHeaderList.setVisibility(View.VISIBLE);
        binding.btnSalaryConfig.setVisibility(View.VISIBLE);
        binding.scrollViewChips.setVisibility(View.VISIBLE);
        binding.chipGroupStatus.setVisibility(View.VISIBLE);
        binding.btnConfirmAll.setVisibility(View.VISIBLE);

        fullEmployeeList.clear();
        fullEmployeeList.addAll(items);

        updateSummaryHeader(fullEmployeeList);

        int checkedId = binding.chipGroupStatus.getCheckedChipId();
        applyFilter(checkedId != View.NO_ID ? checkedId : R.id.chip_filter_all);
    }

    private void showEmptyState() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.rvPayrollList.setVisibility(View.GONE);
        binding.cardPayrollSummary.setVisibility(View.GONE);
        binding.layoutHeaderList.setVisibility(View.GONE);
        binding.btnSalaryConfig.setVisibility(View.GONE);
        binding.scrollViewChips.setVisibility(View.GONE);
        binding.chipGroupStatus.setVisibility(View.GONE);
        binding.layoutEmptyState.getRoot().setVisibility(View.VISIBLE);
        binding.btnConfirmAll.setVisibility(View.GONE);

        binding.layoutEmptyState.tvEmptyTitle.setText("Chưa có nhân sự");
        String monthStr = String.format(Locale.getDefault(), "%02d/%d", selectedMonth, selectedYear);
        binding.layoutEmptyState.tvEmptySubtitle.setText("Tháng " + monthStr + " chưa có nhân sự nào có dữ liệu tính lương.");

        fullEmployeeList.clear();
        employeeList.clear();
        adapter.updateData(employeeList);
    }

    private void updateSummaryHeader(List<ManagerEmployeeItem> items) {
        if (binding == null) return;
        long totalFund = 0;
        int calculatedCount = 0;
        for (ManagerEmployeeItem item : items) {
            long sal = item.getEstimatedSalaryThisMonth();
            if (sal > 0) {
                totalFund += sal;
            }
            if (sal > 0 || (item.getPayrollStatus() != null && !item.getPayrollStatus().isEmpty())) {
                calculatedCount++;
            }
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        binding.tvTotalFund.setText(df.format(totalFund) + " đ");
        binding.tvCalculatedProgress.setText(calculatedCount + "/" + items.size() + " đã tính");
    }

    private void applyFilter(int checkedChipId) {
        employeeList.clear();
        if (checkedChipId == R.id.chip_filter_uncalculated) {
            for (ManagerEmployeeItem item : fullEmployeeList) {
                boolean isCalculated = item.getEstimatedSalaryThisMonth() > 0 
                        || (item.getPayrollStatus() != null && !item.getPayrollStatus().isEmpty());
                if (!isCalculated) {
                    employeeList.add(item);
                }
            }
        } else if (checkedChipId == R.id.chip_filter_calculated) {
            for (ManagerEmployeeItem item : fullEmployeeList) {
                boolean isCalculated = item.getEstimatedSalaryThisMonth() > 0 
                        || (item.getPayrollStatus() != null && !item.getPayrollStatus().isEmpty());
                if (isCalculated) {
                    employeeList.add(item);
                }
            }
        } else {
            employeeList.addAll(fullEmployeeList);
        }
        adapter.updateData(employeeList);
    }

    private void showSalaryConfigDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("SALARY_CONFIG_PREFS", Context.MODE_PRIVATE);
        String savedBaseStr = prefs.getString("rate_fulltime", "");
        String savedShiftRateStr = prefs.getString("rate_parttime", "");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padH = getResources().getDimensionPixelSize(R.dimen.spacing_l);
        int padV = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        layout.setPadding(padH, padV, padH, 0);

        com.google.android.material.textfield.TextInputLayout tilBase = new com.google.android.material.textfield.TextInputLayout(requireContext());
        tilBase.setHint("Lương cơ bản mặc định (VNĐ/tháng)");
        com.google.android.material.textfield.TextInputEditText etBase = new com.google.android.material.textfield.TextInputEditText(tilBase.getContext());
        etBase.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!savedBaseStr.isEmpty()) etBase.setText(savedBaseStr);
        tilBase.addView(etBase);
        layout.addView(tilBase);

        com.google.android.material.textfield.TextInputLayout tilShift = new com.google.android.material.textfield.TextInputLayout(requireContext());
        tilShift.setHint("Đơn giá ca mặc định (VNĐ/ca)");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        tilShift.setLayoutParams(lp);
        com.google.android.material.textfield.TextInputEditText etShift = new com.google.android.material.textfield.TextInputEditText(tilShift.getContext());
        etShift.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!savedShiftRateStr.isEmpty()) etShift.setText(savedShiftRateStr);
        tilShift.addView(etShift);
        layout.addView(tilShift);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cấu hình mức lương công ty")
                .setMessage("Thiết lập mức lương cơ bản và đơn giá ca làm mặc định áp dụng khi chốt lương:")
                .setView(layout)
                .setPositiveButton("Lưu cấu hình", (dialog, which) -> {
                    String baseInput = etBase.getText() != null ? etBase.getText().toString().trim().replaceAll("[.,]", "") : "";
                    String shiftInput = etShift.getText() != null ? etShift.getText().toString().trim().replaceAll("[.,]", "") : "";

                    prefs.edit()
                            .putString("rate_fulltime", baseInput)
                            .putString("rate_parttime", shiftInput)
                            .apply();

                    Toast.makeText(getContext(), "Đã lưu cấu hình mức lương thành công!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static boolean isMatchingMonth(String timeCreate, int targetYear, int targetMonth) {
        if (timeCreate == null || timeCreate.trim().isEmpty()) return false;
        try {
            String datePart = timeCreate.length() >= 7 ? timeCreate.substring(0, 7) : timeCreate;
            String[] parts = datePart.split("-");
            if (parts.length >= 2) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                return year == targetYear && month == targetMonth;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static boolean isMoreRecent(String time1, String time2) {
        if (time1 == null) return false;
        if (time2 == null) return true;
        return time1.compareTo(time2) >= 0;
    }

    private void confirmAllPayrolls() {
        SharedPreferences prefs = requireContext().getSharedPreferences("SALARY_CONFIG_PREFS", Context.MODE_PRIVATE);
        String defaultRateStr = prefs.getString("rate_fulltime", null);
        double defaultBaseSalary = 0.0;
        if (defaultRateStr != null && !defaultRateStr.trim().isEmpty()) {
            try {
                defaultBaseSalary = Double.parseDouble(defaultRateStr.replaceAll("[.,]", ""));
            } catch (Exception ignored) {}
        }

        if (defaultBaseSalary <= 0) {
            promptBaseSalaryForBulkCalculation();
            return;
        }

        executeBulkPayrollCalculation(defaultBaseSalary);
    }

    private void promptBaseSalaryForBulkCalculation() {
        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        int paddingHorizontal = getResources().getDimensionPixelSize(R.dimen.spacing_l);
        int paddingTop = getResources().getDimensionPixelSize(R.dimen.spacing_m);
        container.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, 0);

        com.google.android.material.textfield.TextInputLayout inputLayout = new com.google.android.material.textfield.TextInputLayout(requireContext());
        inputLayout.setHint("Lương cơ bản mặc định (VNĐ)");

        com.google.android.material.textfield.TextInputEditText editText = new com.google.android.material.textfield.TextInputEditText(inputLayout.getContext());
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputLayout.addView(editText);
        container.addView(inputLayout);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thiết lập lương cơ bản để chốt hàng loạt")
                .setMessage("Chưa tìm thấy cấu hình lương cơ bản. Vui lòng nhập mức lương cơ bản áp dụng cho danh sách nhân viên:")
                .setView(container)
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    String input = editText.getText() != null ? editText.getText().toString().trim().replaceAll("[.,]", "") : "";
                    if (input.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập mức lương hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double enteredSalary = Double.parseDouble(input);
                        if (enteredSalary <= 0) {
                            Toast.makeText(getContext(), "Mức lương phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        executeBulkPayrollCalculation(enteredSalary);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeBulkPayrollCalculation(double baseSalary) {
        int total = employeeList.size();
        final int[] finishedCount = {0};

        Toast.makeText(getContext(), "Đang tiến hành chốt lương cho " + total + " nhân sự...", Toast.LENGTH_SHORT).show();

        for (ManagerEmployeeItem emp : employeeList) {
            if (emp.getUserId() == null || emp.getUserId().isEmpty()) {
                finishedCount[0]++;
                if (finishedCount[0] == total) {
                    loadPayrollFromApi();
                }
                continue;
            }
            CalculatePayrollRequest req = new CalculatePayrollRequest(emp.getUserId(), baseSalary);
            disposables.add(
                    managerRepository.calculatePayroll(req)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    res -> {
                                        finishedCount[0]++;
                                        if (finishedCount[0] == total) {
                                            Toast.makeText(getContext(), "Đã chốt và tính lương thành công cho " + total + " nhân sự!", Toast.LENGTH_SHORT).show();
                                            loadPayrollFromApi();
                                        }
                                    },
                                    err -> {
                                        finishedCount[0]++;
                                        if (finishedCount[0] == total) {
                                            Toast.makeText(getContext(), "Đã hoàn thành chốt lương đợt này!", Toast.LENGTH_SHORT).show();
                                            loadPayrollFromApi();
                                        }
                                    }
                            )
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
