package com.example.attendanceapp.ui.employee.home.payroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentPayrollDetailBinding;
import com.example.attendanceapp.core.utils.ColorUtil;

public class EmployeePayrollDetailFragment extends Fragment {

    private FragmentPayrollDetailBinding binding;
    private boolean isPaid = false;
    private String month = "09/2026";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPayrollDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            isPaid = getArguments().getBoolean("isPaid", false);
            month = getArguments().getString("month", "09/2026");
        }

        setupListeners();
        bindData();
    }

    private void setupListeners() {
        // Bấm nút back -> Nút quay lại của Activity/Fragment
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void bindData() {
        // Thông tin chung
        binding.toolbarApp.setTitle("Phiếu lương – " + month.replace("Tháng ", ""));
        binding.tvEmployeeName.setText("Nguyễn Văn An");
        binding.tvDepartment.setText("Kỹ thuật");
        binding.tvLabelNetSalary.setText("Thực lĩnh " + month);

        // Lương cơ bản dùng chung
        binding.rowBaseSalary.ivRowIcon.setImageResource(R.drawable.ic_money);
        binding.rowBaseSalary.tvRowLabel.setText("Lương cơ bản / tháng");
        binding.rowBaseSalary.tvRowValue.setText("15.000.000 đ");


        if (!isPaid) {
            // ==================== TRẠNG THÁI: ĐANG TÍNH ====================
            binding.tvNetSalaryAmount.setText("Đang tính");
            binding.tvPaymentDate.setVisibility(View.GONE); // Ẩn dòng ngày chi trả
            binding.ivStatus.setVisibility(View.GONE);

            // Ngày công thực tế
            binding.rowActualWork.ivRowIcon.setImageResource(R.drawable.ic_calendar);
            binding.rowActualWork.tvRowLabel.setText("Ngày công (8/22 ngày)");
            binding.rowActualWork.tvRowValue.setText("5.454.545 đ");


            // Thưởng & phụ cấp (dấu — màu xanh)
            binding.rowBonus.ivRowIcon.setImageResource(R.drawable.ic_gift);
            binding.rowBonus.tvRowLabel.setText("Thưởng & phụ cấp");
            binding.rowBonus.tvRowValue.setText("—");
            binding.rowBonus.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorAttendancePresent)
            );

            // Khấu trừ & phạt (dấu — màu đỏ)
            binding.rowDeductions.ivRowIcon.setImageResource(R.drawable.ic_heart_broken);
            binding.rowDeductions.tvRowLabel.setText("Khấu trừ & phạt");
            binding.rowDeductions.tvRowValue.setText("—");
            binding.rowDeductions.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorAttendanceAbsent)
            );

            // Tổng thực lĩnh
            binding.rowTotalNet.ivRowIcon.setImageResource(R.drawable.ic_money);
            binding.rowTotalNet.tvRowLabel.setText("Tổng thực lĩnh");
            binding.rowTotalNet.tvRowLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.rowTotalNet.tvRowValue.setText("Đang tính");
            binding.rowTotalNet.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowTotalNet.tvRowValue, R.attr.colorPrimary)
            );

            // 3 Card tóm tắt phía dưới
            binding.tvSummaryDays.setText("8/22");
            binding.tvSummaryBonus.setText("0");
            binding.tvSummaryDeduction.setText("0");

        } else {
            // ==================== TRẠNG THÁI: ĐÃ THANH TOÁN ====================
            binding.tvNetSalaryAmount.setText("14.818.182 đ");
            binding.tvPaymentDate.setVisibility(View.VISIBLE);
            binding.ivStatus.setVisibility(View.VISIBLE);
            binding.tvPaymentDate.setText("Đã chi trả ngày 5/9/2026");


            // Ngày công thực tế
            binding.rowActualWork.ivRowIcon.setImageResource(R.drawable.ic_calendar);
            binding.rowActualWork.tvRowLabel.setText("Ngày công (21/22 ngày)");
            binding.rowActualWork.tvRowValue.setText("14.318.182 đ");

            // Thưởng & phụ cấp
            binding.rowBonus.ivRowIcon.setImageResource(R.drawable.ic_gift);
            binding.rowBonus.tvRowLabel.setText("Thưởng & phụ cấp");
            binding.rowBonus.tvRowValue.setText("+500.000 đ");
            binding.rowBonus.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorAttendancePresent)
            );

            // Khấu trừ & phạt
            binding.rowDeductions.ivRowIcon.setImageResource(R.drawable.ic_heart_broken);
            binding.rowDeductions.tvRowLabel.setText("Khấu trừ & phạt");
            binding.rowDeductions.tvRowValue.setText("-681.818 đ");
            binding.rowDeductions.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorAttendanceAbsent)
            );

            // Tổng thực lĩnh
            binding.rowTotalNet.ivRowIcon.setImageResource(R.drawable.ic_money);
            binding.rowTotalNet.tvRowLabel.setText("Tổng thực lĩnh");
            binding.rowTotalNet.tvRowLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.rowTotalNet.tvRowValue.setText("14.818M đ");
            binding.rowTotalNet.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorPrimary)
            );

            // 3 Card tóm tắt phía dưới
            binding.tvSummaryDays.setText("21/22");
            binding.tvSummaryBonus.setText("+0.5M");
            binding.tvSummaryDeduction.setText("-0.68M");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}