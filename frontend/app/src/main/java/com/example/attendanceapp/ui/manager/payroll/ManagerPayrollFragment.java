package com.example.attendanceapp.ui.manager.payroll;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final List<ManagerEmployeeItem> employeeList = new ArrayList<>();
    private final CompositeDisposable disposables = new CompositeDisposable();

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

        setupToolbar();
        setupList();
        setupListeners();
        setupMonthHeader();
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

    private void setupMonthHeader() {
        try {
            LocalDate now = LocalDate.now();
            binding.tvMonthPicker.setText(String.format(Locale.getDefault(), "Tháng %02d/%d", now.getMonthValue(), now.getYear()));
        } catch (Exception ignored) {}
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

            Navigation.findNavController(requireView()).navigate(
                    R.id.action_managerPayrollFragment_to_managerPayrollDetailFragment, bundle
            );
        });

        binding.rvPayrollList.setAdapter(adapter);
    }

    private void loadPayrollFromApi() {
        if (managerRepository == null) return;
        disposables.add(
                Single.zip(
                        managerRepository.getStaffs().onErrorReturnItem(new BaseListResponse<>(new ArrayList<>())),
                        managerRepository.getPayrollHistory(null).onErrorReturnItem(new BaseListResponse<>(new ArrayList<>())),
                        (staffResponse, payrollResponse) -> {
                            Map<String, CalculatePayrollResponse> latestPayrollByStaff = new HashMap<>();
                            if (payrollResponse != null && payrollResponse.getData() != null) {
                                for (CalculatePayrollResponse p : payrollResponse.getData()) {
                                    if (p != null && p.getStaffId() != null) {
                                        CalculatePayrollResponse current = latestPayrollByStaff.get(p.getStaffId());
                                        if (current == null || isMoreRecent(p.getTimeCreate(), current.getTimeCreate())) {
                                            latestPayrollByStaff.put(p.getStaffId(), p);
                                        }
                                    }
                                }
                            }

                            List<ManagerEmployeeItem> items = new ArrayList<>();
                            if (staffResponse != null && staffResponse.getData() != null) {
                                for (StaffItemResponse staff : staffResponse.getData()) {
                                    String id = staff.getStaffId();
                                    String code = "NV-" + (id != null && id.length() >= 4 ? id.substring(id.length() - 4) : (id != null ? id : ""));
                                    EmployeeStatus status = "deactive".equalsIgnoreCase(staff.getStatus())
                                            ? EmployeeStatus.TERMINATED
                                            : EmployeeStatus.ACTIVE;
                                    String pos = staff.getPosition() != null && !staff.getPosition().isEmpty() ? staff.getPosition() : "Nhân viên";

                                    CalculatePayrollResponse payroll = id != null ? latestPayrollByStaff.get(id) : null;
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

                            // Fallback nếu danh sách staffs rỗng nhưng đã có dữ liệu payroll từ backend
                            if (items.isEmpty() && !latestPayrollByStaff.isEmpty()) {
                                for (Map.Entry<String, CalculatePayrollResponse> entry : latestPayrollByStaff.entrySet()) {
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

                            return items;
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        items -> {
                            employeeList.clear();
                            employeeList.addAll(items);
                            adapter.updateData(employeeList);
                        },
                        error -> {
                            Toast.makeText(getContext(), "Không thể tải danh sách lương: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                )
        );
    }

    private static boolean isMoreRecent(String time1, String time2) {
        if (time1 == null) return false;
        if (time2 == null) return true;
        return time1.compareTo(time2) >= 0;
    }

    private void setupListeners() {
        binding.tvMonthPicker.setOnClickListener(v -> {
            CharSequence currentMonth = binding.tvMonthPicker.getText();
            Toast.makeText(getContext(), "Kỳ lương hiện tại: " + (currentMonth != null ? currentMonth : ""), Toast.LENGTH_SHORT).show();
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
