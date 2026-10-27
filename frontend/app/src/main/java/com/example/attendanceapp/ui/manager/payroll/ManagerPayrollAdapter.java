package com.example.attendanceapp.ui.manager.payroll;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attendanceapp.databinding.ItemManagerPayrollEmployeeBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
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
            binding.tvEmployeeName.setText(employee.getFullName());
            com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(binding.getRoot().getContext(), employee.getAvatarUrl(), binding.ivAvatar);
            binding.tvEmployeeRole.setText(employee.getPosition() + " • Mã: " + employee.getEmployeeCode());
            binding.tvEmployeeStats.setText(employee.getShiftCountThisMonth() + " ca làm • " 
                    + employee.getLeaveCountThisMonth() + " nghỉ có phép");

            binding.btnCalculateSalary.setOnClickListener(v -> {
                if (listener != null) listener.onCalculateSalary(employee);
            });
            
            // Logic hiển thị trạng thái nếu đã tính lương (tạm thời để ẩn hoặc mock)
            binding.tvSalaryStatus.setVisibility(View.GONE);
        }
    }
}
