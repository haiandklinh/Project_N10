package com.example.attendanceapp.ui.manager.human;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.attendanceapp.databinding.BottomSheetEmployeeDetailBinding;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;

public class EmployeeDetailBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetEmployeeDetailBinding binding;
    private ManagerEmployeeItem employee;

    public static EmployeeDetailBottomSheet newInstance(ManagerEmployeeItem employee) {
        EmployeeDetailBottomSheet sheet = new EmployeeDetailBottomSheet();
        sheet.employee = employee;
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetEmployeeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (employee == null) {
            dismiss();
            return;
        }

        // 1. Gán thông tin nhân viên
        binding.tvName.setText(employee.getFullName());
        binding.tvPosition.setText(employee.getPosition() + " · " + employee.getDepartment());
        binding.tvPhone.setText(employee.getPhoneNumber());
        binding.tvJoinDate.setText("Ngày vào làm: " + employee.getJoinDateLabel());
        binding.tvShiftCount.setText(String.valueOf(employee.getShiftCountThisMonth()));
        binding.tvLeaveCount.setText(String.valueOf(employee.getLeaveCountThisMonth()));

        // 2. Định dạng lương
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.tvSalary.setText(currencyFormat.format(employee.getEstimatedSalaryThisMonth()) + " đ");

        // 3. Hiển thị Chip trạng thái trực tiếp từ Enum EmployeeStatus
        EmployeeStatus status = employee.getStatus() != null ? employee.getStatus() : EmployeeStatus.ACTIVE;
        binding.chipStatus.setText(status.getLabel());
        binding.chipStatus.setTextColor(ContextCompat.getColor(requireContext(), status.getTextColorRes()));
        binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), status.getContainerColorRes())
        ));

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}