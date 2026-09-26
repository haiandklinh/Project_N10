package com.example.attendanceapp.ui.employee.home.payroll;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemPayrollHistoryBinding;
import com.example.attendanceapp.core.utils.ColorUtil;

import java.util.List;

public class EmployeePayrollAdapter extends RecyclerView.Adapter<EmployeePayrollAdapter.ViewHolder> {

    public static class PayrollItem {
        private String month;
        private String workDays;
        private String bonusText;
        private String amountText;
        private boolean isPaid;

        public PayrollItem(String month, String workDays, String bonusText, String amountText, boolean isPaid) {
            this.month = month;
            this.workDays = workDays;
            this.bonusText = bonusText;
            this.amountText = amountText;
            this.isPaid = isPaid;
        }

        public String getMonth() { return month; }
        public String getWorkDays() { return workDays; }
        public String getBonusText() { return bonusText; }
        public String getAmountText() { return amountText; }
        public boolean isPaid() { return isPaid; }
    }

    private final List<PayrollItem> items;

    public EmployeePayrollAdapter(List<PayrollItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPayrollHistoryBinding binding = ItemPayrollHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PayrollItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvMonth.setText(item.getMonth());
        holder.binding.tvWorkDays.setText(item.getWorkDays());
        holder.binding.tvSalaryAmount.setText(item.getAmountText());

        // 1. Xử lý TextView Tiền thưởng
        if (item.getBonusText() != null && !item.getBonusText().isEmpty()) {
            holder.binding.tvBonus.setVisibility(View.VISIBLE);
            holder.binding.tvBonus.setText(item.getBonusText());
        } else {
            holder.binding.tvBonus.setVisibility(View.GONE);
        }

        // 2. Xử lý Chip Trạng thái & Màu sắc tài chính
        if (item.isPaid()) {
            holder.binding.tvSalaryAmount.setTextColor(
                    ColorUtil.getThemeColor(holder.binding.tvSalaryAmount, R.attr.colorPrimary)
            );
            holder.binding.chipStatus.setText("Đã thanh toán");

            // Dùng setChipBackgroundColor + ColorStateList.valueOf
            holder.binding.chipStatus.setChipBackgroundColor(
                    ColorStateList.valueOf(ColorUtil.getThemeColor(holder.binding.chipStatus, R.attr.colorAttendancePresentContainer))
            );
            holder.binding.chipStatus.setTextColor(
                    ColorUtil.getThemeColor(holder.binding.chipStatus, R.attr.colorAttendancePresent)
            );
        } else {
            holder.binding.tvSalaryAmount.setTextColor(
                    ColorUtil.getThemeColor(holder.binding.tvSalaryAmount, R.attr.colorAttendanceLate)
            );
            holder.binding.chipStatus.setText("Chờ xử lý");

            // Dùng setChipBackgroundColor + ColorStateList.valueOf
            holder.binding.chipStatus.setChipBackgroundColor(
                    ColorStateList.valueOf(ColorUtil.getThemeColor(holder.binding.chipStatus, R.attr.colorAttendanceLateContainer))
            );
            holder.binding.chipStatus.setTextColor(
                    ColorUtil.getThemeColor(holder.binding.chipStatus, R.attr.colorAttendanceLate)
            );
        }

        // 3. Sự kiện Click chuyển màn hình
        holder.itemView.setOnClickListener(v -> {
            try {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isPaid", item.isPaid());
                bundle.putString("month", item.getMonth());

                Navigation.findNavController(v)
                        .navigate(R.id.action_employeePayrollFragment_to_employeePayrollDetailFragment, bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPayrollHistoryBinding binding;

        public ViewHolder(ItemPayrollHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}