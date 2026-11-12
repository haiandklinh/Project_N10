package com.example.attendanceapp.ui.employee.home.payroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.data.dto.response.staff.StaffPayrollResponse;
import com.example.attendanceapp.databinding.FragmentPayrollDetailBinding;
import com.example.attendanceapp.domain.repository.SessionRepository;

import java.text.DecimalFormat;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeePayrollDetailFragment extends Fragment {

    private FragmentPayrollDetailBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private boolean isPaid = false;
    private String month = "09/2026";
    private StaffPayrollResponse payrollData;

    @Inject
    SessionRepository sessionRepository;

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
            payrollData = (StaffPayrollResponse) getArguments().getSerializable("payroll_data");
        }

        setupListeners();
        loadEmployeeProfile();
        bindData();
    }

    private void setupListeners() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void loadEmployeeProfile() {
        if (sessionRepository == null) return;
        disposables.add(
                sessionRepository.getCurrentUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    if (binding == null) return;
                                    if (user != null) {
                                        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                                            binding.tvEmployeeName.setText(user.getFullName());
                                        }
                                        if (user.getCompanyName() != null && !user.getCompanyName().isEmpty()) {
                                            binding.tvDepartment.setText(user.getCompanyName());
                                        }
                                    }
                                },
                                error -> {}
                        )
        );
    }

    private void bindData() {
        binding.toolbarApp.setTitle("Phiếu lương – " + month.replace("Tháng ", ""));
        binding.tvLabelNetSalary.setText("Thực lĩnh " + month);

        DecimalFormat df = new DecimalFormat("#,###");

        if (payrollData != null) {
            double base = payrollData.getBaseSalary();
            double total = payrollData.getTotalSalary();
            int shifts = payrollData.getNumberOfWorkShifts();

            if (payrollData.getCompanyName() != null && !payrollData.getCompanyName().isEmpty()) {
                binding.tvDepartment.setText(payrollData.getCompanyName());
            }

            // Lương cơ bản
            binding.rowBaseSalary.ivRowIcon.setImageResource(R.drawable.ic_money);
            binding.rowBaseSalary.tvRowLabel.setText("Lương cơ bản");
            binding.rowBaseSalary.tvRowValue.setText(df.format((long) base) + " đ");

            // Ngày / Ca công thực tế
            binding.rowActualWork.ivRowIcon.setImageResource(R.drawable.ic_calendar);
            binding.rowActualWork.tvRowLabel.setText("Ca làm việc thực tế");
            binding.rowActualWork.tvRowValue.setText(shifts + " ca");

            // Thưởng & phụ cấp
            double bonusEstimate = Math.max(0, total - base);
            binding.rowBonus.ivRowIcon.setImageResource(R.drawable.ic_gift);
            binding.rowBonus.tvRowLabel.setText("Thưởng & phụ cấp");
            binding.rowBonus.tvRowValue.setText(bonusEstimate > 0 ? "+" + df.format((long) bonusEstimate) + " đ" : "0 đ");
            binding.rowBonus.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorAttendancePresent)
            );

            // Khấu trừ
            binding.rowDeductions.ivRowIcon.setImageResource(R.drawable.ic_heart_broken);
            binding.rowDeductions.tvRowLabel.setText("Khấu trừ & phạt");
            binding.rowDeductions.tvRowValue.setText("0 đ");
            binding.rowDeductions.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowBonus.tvRowValue, R.attr.colorAttendanceAbsent)
            );

            // Tổng thực lĩnh
            binding.rowTotalNet.ivRowIcon.setImageResource(R.drawable.ic_money);
            binding.rowTotalNet.tvRowLabel.setText("Tổng thực lĩnh");
            binding.rowTotalNet.tvRowLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            binding.rowTotalNet.tvRowValue.setText(df.format((long) total) + " đ");
            binding.rowTotalNet.tvRowValue.setTextColor(
                    ColorUtil.getThemeColor(binding.rowTotalNet.tvRowValue, R.attr.colorPrimary)
            );

            binding.tvNetSalaryAmount.setText(df.format((long) total) + " đ");

            // 3 Card tóm tắt phía dưới
            binding.tvSummaryDays.setText(String.valueOf(shifts));
            binding.tvSummaryBonus.setText(bonusEstimate > 0 ? "+" + formatShortAmount(bonusEstimate) : "0");
            binding.tvSummaryDeduction.setText("0");

            if (isPaid) {
                binding.tvPaymentDate.setVisibility(View.VISIBLE);
                binding.ivStatus.setVisibility(View.VISIBLE);
                String payDate = payrollData.getTimeCreate() != null && payrollData.getTimeCreate().length() >= 10
                        ? payrollData.getTimeCreate().substring(0, 10)
                        : "Kỳ thanh toán";
                binding.tvPaymentDate.setText("Đã xác nhận ngày " + payDate);
            } else {
                binding.tvPaymentDate.setVisibility(View.VISIBLE);
                binding.ivStatus.setVisibility(View.GONE);
                binding.tvPaymentDate.setText("Trạng thái: Đang tổng hợp công nợ");
            }
        } else {
            // Fallback khi không có object truyền sang
            binding.rowBaseSalary.ivRowIcon.setImageResource(R.drawable.ic_money);
            binding.rowBaseSalary.tvRowLabel.setText("Lương cơ bản");
            binding.rowBaseSalary.tvRowValue.setText("0 đ");

            binding.tvNetSalaryAmount.setText("0 đ");
            binding.tvPaymentDate.setVisibility(View.GONE);
            binding.ivStatus.setVisibility(View.GONE);
        }
    }

    private String formatShortAmount(double amount) {
        if (amount <= 0) return "0";
        if (amount >= 1_000_000) {
            return String.format(java.util.Locale.US, "%.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(java.util.Locale.US, "%.0fK", amount / 1_000);
        }
        return String.format(java.util.Locale.US, "%.0f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
