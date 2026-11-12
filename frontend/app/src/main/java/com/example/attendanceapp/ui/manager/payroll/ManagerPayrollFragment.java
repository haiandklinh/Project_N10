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
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.databinding.FragmentManagerPayrollBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Quản lý tiền lương");
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
        loadEmployeesFromApi();
    }

    private void loadEmployeesFromApi() {
        if (managerRepository == null) return;
        disposables.add(
                managerRepository.getStaffs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                                        employeeList.clear();
                                        for (StaffItemResponse staff : response.getData()) {
                                            String id = staff.getStaffId();
                                            String code = "NV-" + (id != null && id.length() >= 4 ? id.substring(id.length() - 4) : (id != null ? id : ""));
                                            EmployeeStatus status = "deactive".equalsIgnoreCase(staff.getStatus())
                                                    ? EmployeeStatus.TERMINATED
                                                    : EmployeeStatus.ACTIVE;
                                            String pos = staff.getPosition() != null && !staff.getPosition().isEmpty() ? staff.getPosition() : "Nhân viên";
                                            employeeList.add(new ManagerEmployeeItem(
                                                    id, code,
                                                    staff.getNameStaff() != null ? staff.getNameStaff() : "Nhân viên",
                                                    staff.getAvatar() != null ? staff.getAvatar() : "",
                                                    pos, "Phòng ban chung", "",
                                                    staff.getDateOfBirth() != null ? staff.getDateOfBirth() : "",
                                                    status, 0, 0, 0
                                            ));
                                        }
                                        adapter.updateData(employeeList);
                                    }
                                },
                                error -> {
                                    Toast.makeText(getContext(), "Không thể tải danh sách nhân viên: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }

    private void setupListeners() {
        binding.tvMonthPicker.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Kỳ lương hiện tại: Tháng 10/2026", Toast.LENGTH_SHORT).show();
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
        String defaultRateStr = prefs.getString("rate_fulltime", "400000").replaceAll("[.,]", "");
        double defaultBaseSalary = 7500000.0;
        try {
            defaultBaseSalary = Double.parseDouble(defaultRateStr) * 22;
        } catch (Exception ignored) {}

        final double baseSalary = defaultBaseSalary;
        int total = employeeList.size();
        final int[] successCount = {0};

        for (ManagerEmployeeItem emp : employeeList) {
            if (emp.getUserId() == null || emp.getUserId().isEmpty()) continue;
            CalculatePayrollRequest req = new CalculatePayrollRequest(emp.getUserId(), baseSalary);
            disposables.add(
                    managerRepository.calculatePayroll(req)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    res -> {
                                        successCount[0]++;
                                        if (successCount[0] == total) {
                                            Toast.makeText(getContext(), "Đã chốt và tính lương thành công cho " + total + " nhân sự!", Toast.LENGTH_SHORT).show();
                                        }
                                    },
                                    err -> {
                                        // continue other requests
                                    }
                            )
            );
        }

        Toast.makeText(getContext(), "Đang tiến hành chốt lương cho " + total + " nhân sự...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
