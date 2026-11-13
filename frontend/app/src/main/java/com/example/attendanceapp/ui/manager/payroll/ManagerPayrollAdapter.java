package com.example.attendanceapp.ui.manager.payroll;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.AvatarLoader;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerPayrollEmployeeBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import java.text.DecimalFormat;
import java.util.List;

public class ManagerPayrollAdapter extends RecyclerView.Adapter<ManagerPayrollAdapter.PayrollViewHolder> {

    private final List<ManagerEmployeeItem> employees;
    private final OnPayrollActionListener listener;

    public interface OnPayrollActionListener {
        void onCalculateSalary(ManagerEmployeeItem employee);
    }

    public ManagerPayrollAdapter(List<ManagerEmployeeItem> employees, OnPayrollActionListener listener) {
        this.employees = employees != null ? new java.util.ArrayList<>(employees) : new java.util.ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<ManagerEmployeeItem> newEmployees) {
        this.employees.clear();
        if (newEmployees != null) {
            this.employees.addAll(newEmployees);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PayrollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerPayrollEmployeeBinding binding = ItemManagerPayrollEmployeeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PayrollViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PayrollViewHolder holder, int position) {
        holder.bind(employees.get(position));
    }

    @Override
    public int getItemCount() {
        return employees != null ? employees.size() : 0;
    }

    class PayrollViewHolder extends RecyclerView.ViewHolder {
        private final ItemManagerPayrollEmployeeBinding binding;

        public PayrollViewHolder(ItemManagerPayrollEmployeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ManagerEmployeeItem employee) {
            Context context = binding.getRoot().getContext();
            binding.tvEmployeeName.setText(employee.getFullName());
            AvatarLoader.loadAvatar(context, employee.getAvatarUrl(), binding.ivAvatar);
            binding.tvEmployeeRole.setText(employee.getPosition() + " • Mã: " + employee.getEmployeeCode());

            long estimatedSalary = employee.getEstimatedSalaryThisMonth();
            int shifts = employee.getShiftCountThisMonth();
            String payrollStatus = employee.getPayrollStatus();
            DecimalFormat df = new DecimalFormat("#,###");

            binding.tvSalaryStatus.setVisibility(View.VISIBLE);

            if (estimatedSalary > 0 || (payrollStatus != null && !payrollStatus.isEmpty())) {
                String statusLabel;
                int statusColor;
                if ("PAID".equalsIgnoreCase(payrollStatus) || "COMPLETED".equalsIgnoreCase(payrollStatus)) {
                    statusLabel = "Đã thanh toán: " + df.format(estimatedSalary) + "đ";
                    statusColor = ColorUtil.getThemeColor(context, R.attr.colorAttendancePresent);
                } else if ("APPROVED".equalsIgnoreCase(payrollStatus) || "CONFIRMED".equalsIgnoreCase(payrollStatus)) {
                    statusLabel = "Đã chốt: " + df.format(estimatedSalary) + "đ";
                    statusColor = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
                } else {
                    statusLabel = "Đã tính: " + df.format(estimatedSalary) + "đ";
                    statusColor = ColorUtil.getThemeColor(context, R.attr.colorPrimary);
                }
                binding.tvSalaryStatus.setText(statusLabel);
                binding.tvSalaryStatus.setTextColor(statusColor);

                binding.tvEmployeeStats.setText(shifts + " ca làm • Thực nhận: " + df.format(estimatedSalary) + "đ");
                binding.btnCalculateSalary.setText("Chi tiết");
            } else {
                binding.tvSalaryStatus.setText("Chưa tính lương");
                binding.tvSalaryStatus.setTextColor(ColorUtil.getThemeColor(context, R.attr.colorAttendanceNotChecked));

                if (shifts > 0) {
                    binding.tvEmployeeStats.setText(shifts + " ca làm • Chưa chốt lương");
                } else {
                    binding.tvEmployeeStats.setText("Chưa có ca làm kỳ này");
                }
                binding.btnCalculateSalary.setText("Tính lương");
            }

            binding.btnCalculateSalary.setOnClickListener(v -> {
                if (listener != null) listener.onCalculateSalary(employee);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onCalculateSalary(employee);
            });
        }
    }
}
