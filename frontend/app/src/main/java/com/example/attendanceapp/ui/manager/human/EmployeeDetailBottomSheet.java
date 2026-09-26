package com.example.attendanceapp.ui.manager.human;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.BottomSheetEmployeeDetailBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;

public class EmployeeDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_EMPLOYEE_CODE = "employee_code";

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

        binding.tvName.setText(employee.getFullName());
        binding.tvPosition.setText(employee.getPosition() + " · " + employee.getDepartment());
        binding.tvPhone.setText(employee.getPhoneNumber());
        binding.tvJoinDate.setText("Ngày vào làm: " + employee.getJoinDateLabel());
        binding.tvShiftCount.setText(String.valueOf(employee.getShiftCountThisMonth()));
        binding.tvLeaveCount.setText(String.valueOf(employee.getLeaveCountThisMonth()));

        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.tvSalary.setText(currencyFormat.format(employee.getEstimatedSalaryThisMonth()) + " đ");

        boolean isActive = ManagerEmployeeItem.STATUS_ACTIVE.equals(employee.getStatus());
        binding.chipStatus.setText(isActive ? "Đang làm" : "Đang nghỉ phép");
        int bgAttr = isActive ? R.attr.colorAttendancePresentContainer : R.attr.colorAttendanceLeaveContainer;
        int textAttr = isActive ? R.attr.colorAttendancePresent : R.attr.colorAttendanceLeave;
        binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ColorUtil.getThemeColor(requireContext(), bgAttr)));
        binding.chipStatus.setTextColor(ColorUtil.getThemeColor(requireContext(), textAttr));

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
