package com.example.attendanceapp.ui.manager.payroll;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemHistoryCalendarDayBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;

import java.util.ArrayList;
import java.util.List;

public class ManagerPayrollCalendarAdapter extends RecyclerView.Adapter<ManagerPayrollCalendarAdapter.ViewHolder> {

    private final List<WeekDay> days = new ArrayList<>();
    private final OnDayClickListener listener;
    private int selectedPosition = -1;

    public interface OnDayClickListener {
        void onDayClick(WeekDay day);
    }

    public ManagerPayrollCalendarAdapter(List<WeekDay> days, OnDayClickListener listener) {
        if (days != null) this.days.addAll(days);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryCalendarDayBinding binding = ItemHistoryCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeekDay day = days.get(position);
        holder.bind(day, position == selectedPosition);
        
        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryCalendarDayBinding binding;

        public ViewHolder(ItemHistoryCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WeekDay day, boolean isSelected) {
            binding.tvDayNumber.setText(String.valueOf(day.getDate().getDayOfMonth()));
            
            // Lấy màu sắc từ Theme dựa trên AttendanceStatus
            // Lưu ý: Các attr này được định nghĩa trong attrs.xml của project
            int statusColor = ColorUtil.getThemeColor(itemView.getContext(), day.getStatus().getColorAttr());
            int containerColor = ColorUtil.getThemeColor(itemView.getContext(), day.getStatus().getContainerColorAttr());
            
            binding.ivDayStatus.setImageResource(day.getStatus().getIconRes());
            binding.ivDayStatus.setImageTintList(ColorStateList.valueOf(statusColor));

            // Hiển thị highlight nếu được chọn
            if (isSelected) {
                binding.cardCalendarDay.setStrokeWidth(4);
                // Sử dụng R.attr.colorPrimary định nghĩa trong attrs.xml hoặc Material
                int primaryColor = ColorUtil.getThemeColor(itemView.getContext(), R.attr.colorPrimary);
                binding.cardCalendarDay.setStrokeColor(primaryColor);
            } else {
                binding.cardCalendarDay.setStrokeWidth(0);
            }
            
            // Set màu nền cho ngày dựa trên trạng thái
            binding.cardCalendarDay.setCardBackgroundColor(containerColor);
            
            // Highlight text nếu là hôm nay
            if (day.isToday()) {
                binding.tvDayNumber.setTextSize(16);
                binding.tvDayNumber.setTextColor(statusColor);
            } else {
                binding.tvDayNumber.setTextSize(14);
                // colorOnSurfaceVariant có trong attrs.xml
                int textColor = ColorUtil.getThemeColor(itemView.getContext(), R.attr.colorOnSurfaceVariant);
                binding.tvDayNumber.setTextColor(textColor);
            }
        }
    }
}
