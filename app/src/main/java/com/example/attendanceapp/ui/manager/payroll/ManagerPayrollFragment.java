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
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupList() {
        // Mock dữ liệu danh sách nhân viên để tính lương
        binding.rvPayrollList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Ở đây sẽ set Adapter cho danh sách nhân sự kèm trạng thái lương
        // tạm thời để trống để bạn hoàn thiện Adapter sau
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
