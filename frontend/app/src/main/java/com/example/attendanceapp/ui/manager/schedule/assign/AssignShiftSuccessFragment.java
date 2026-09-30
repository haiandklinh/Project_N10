package com.example.attendanceapp.ui.manager.schedule.assign;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.databinding.FragmentAssignShiftSuccessBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AssignShiftSuccessFragment extends Fragment {

    private FragmentAssignShiftSuccessBinding binding;
    private AssignedEmployeeSuccessAdapter employeeAdapter;
    private final List<ManagerEmployeeItem> assignedEmployees = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssignShiftSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadMockData();
        setupRecyclerView();
        setupActions();
    }

    private void loadMockData() {
        // Refactor: Truyền EmployeeStatus.ACTIVE thay vì chuỗi rỗng ""
        assignedEmployees.add(new ManagerEmployeeItem("u1", "NV001", "Nguyễn Văn A", null, "Nhân viên", "Q1", "", "", EmployeeStatus.ACTIVE, 0, 0, 0));
        assignedEmployees.add(new ManagerEmployeeItem("u2", "NV002", "Trần Văn B", null, "Nhân viên", "Q1", "", "", EmployeeStatus.ACTIVE, 0, 0, 0));
        assignedEmployees.add(new ManagerEmployeeItem("u3", "NV003", "Lê Thị C", null, "Thu ngân", "Q1", "", "", EmployeeStatus.ACTIVE, 0, 0, 0));

        // Cập nhật mô tả thành công
        String desc = "Đã xếp ca cho " + assignedEmployees.size() + " nhân viên vào ngày\n28/09/2026 (Thứ 2) - Ca sáng\n(08:00 - 12:00).";
        binding.tvSuccessDesc.setText(desc);
    }

    private void setupRecyclerView() {
        employeeAdapter = new AssignedEmployeeSuccessAdapter(assignedEmployees);
        binding.rvAssignedEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAssignedEmployees.setAdapter(employeeAdapter);
    }

    private void setupActions() {
        // 1. Nút Xuất ra bảng tính Excel
        binding.btnExportExcel.setOnClickListener(v ->
                Toast.makeText(getContext(), "Đang xuất danh sách ca ra bảng tính...", Toast.LENGTH_SHORT).show()
        );

        // 2. Nút Quay lại lịch
        binding.btnBackToSchedule.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 3. Nút Xếp ca khác
        binding.btnAssignAnother.setOnClickListener(v -> {
            // Điều hướng quay lại màn chọn nhân viên / xếp ca
            Toast.makeText(getContext(), "Chuyển sang xếp ca mới", Toast.LENGTH_SHORT).show();
            // Navigation.findNavController(v).navigate(R.id.action_success_to_selectEmployee);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}