package com.example.attendanceapp.ui.employee.home.adjustment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemAttendanceAdjustmentBinding;
import com.example.attendanceapp.domain.model.AdjustmentReasonType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.AttendanceAdjustment;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeAdjustmentAdapter extends RecyclerView.Adapter<EmployeeAdjustmentAdapter.ViewHolder> {

    private final List<AttendanceAdjustment> list;

    public EmployeeAdjustmentAdapter(List<AttendanceAdjustment> list) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public void updateData(List<AttendanceAdjustment> newList) {
        this.list.clear();
        if (newList != null) {
            this.list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceAdjustmentBinding binding = ItemAttendanceAdjustmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceAdjustmentBinding binding;

        public ViewHolder(ItemAttendanceAdjustmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AttendanceAdjustment item) {
            Context context = itemView.getContext();

            // 1. Hiển thị loại lý do từ Enum AdjustmentReasonType
            AdjustmentReasonType reasonType = item.getReasonType();
            binding.tvReasonType.setText(reasonType != null ? reasonType.getLabel() : "");
            binding.tvReasonDetail.setText(item.getReasonDetail() != null ? item.getReasonDetail() : "");

            // 2. Định dạng ngày yêu cầu & ngày gửi
            if (item.getRequestDate() != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
                binding.tvRequestDate.setText(item.getRequestDate().format(dateFormatter));
            } else {
                binding.tvRequestDate.setText("");
            }

            if (item.getCreatedDate() != null) {
                binding.tvCreatedDate.setText("Gửi: " + item.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                binding.tvCreatedDate.setText("");
            }

            // 3. Check-in / Check-out Display
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            if (item.getCheckInTime() != null) {
                binding.containerCheckIn.setVisibility(View.VISIBLE);
                binding.tvCheckInTime.setText(item.getCheckInTime().format(timeFormatter));
            } else {
                binding.containerCheckIn.setVisibility(View.GONE);
            }

            if (item.getCheckOutTime() != null) {
                binding.containerCheckOut.setVisibility(View.VISIBLE);
                binding.tvCheckOutTime.setText(item.getCheckOutTime().format(timeFormatter));
            } else {
                binding.containerCheckOut.setVisibility(View.GONE);
            }

            // 4. Status Badge (Lấy trực tiếp nhãn và màu sắc từ Enum ApprovalStatus)
            ApprovalStatus status = item.getStatus() != null ? item.getStatus() : ApprovalStatus.PENDING;
            binding.tvStatus.setText(status.getLabel());
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, status.getContainerColorRes())
            ));
        }
    }
}