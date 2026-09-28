package com.example.attendanceapp.ui.manager.payroll;

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
import com.example.attendanceapp.databinding.FragmentManagerPayrollBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerPayrollFragment extends Fragment {

    private FragmentManagerPayrollBinding binding;

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

        // Mock danh sách nhân sự cần tính lương chuẩn theo Use Case
        List<ManagerEmployeeItem> mockEmployees = new ArrayList<>();
        mockEmployees.add(new ManagerEmployeeItem("1", "NV001", "Nguyễn Văn Hoàng", null, "Nhân viên Sale", "Phòng Kinh doanh", "0987654321", "01/01/2026", ManagerEmployeeItem.STATUS_ACTIVE, 24, 1, 7700000));
        mockEmployees.add(new ManagerEmployeeItem("2", "NV002", "Trần Thị Bình", null, "Thu ngân", "Phòng Kế toán", "0912345678", "15/02/2026", ManagerEmployeeItem.STATUS_ACTIVE, 22, 0, 6600000));
        mockEmployees.add(new ManagerEmployeeItem("3", "NV003", "Lê Văn Cường", null, "Nhân viên Kho", "Phòng Vận hành", "0933445566", "10/03/2026", ManagerEmployeeItem.STATUS_ACTIVE, 26, 2, 8200000));

        ManagerPayrollAdapter adapter = new ManagerPayrollAdapter(mockEmployees, employee -> {
            // Bước 3-5: Chọn nhân viên và chuyển tiếp sang màn hình tính toán lương chi tiết
            Navigation.findNavController(requireView()).navigate(R.id.action_managerPayrollFragment_to_managerPayrollDetailFragment);
        });

        binding.rvPayrollList.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.tvMonthPicker.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chọn tháng", Toast.LENGTH_SHORT).show();
        });

        binding.btnConfirmAll.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đã xác nhận chốt lương toàn bộ nhân sự", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
