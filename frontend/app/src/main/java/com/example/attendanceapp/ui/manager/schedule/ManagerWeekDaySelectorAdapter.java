package com.example.attendanceapp.ui.manager.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemHomeWeekDayBinding;

import java.time.LocalDate;
import java.util.List;

/**
 * Danh sách 7 ngày trong tuần để Quản lý chọn ngày cần xếp ca.
 * Tái sử dụng layout item_home_week_day.xml của màn Employee Home.
 */
public class ManagerWeekDaySelectorAdapter extends RecyclerView.Adapter<ManagerWeekDaySelectorAdapter.ViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    private final List<LocalDate> days;
    private final OnDateSelectedListener listener;
    private LocalDate selectedDate;

    public ManagerWeekDaySelectorAdapter(List<LocalDate> days, LocalDate initialSelected, OnDateSelectedListener listener) {
        this.days = days;
        this.selectedDate = initialSelected;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeWeekDayBinding binding = ItemHomeWeekDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalDate date = days.get(position);
        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);

        holder.binding.tvDayName.setText(getDayLetter(date));
        holder.binding.ivDayStatus.setImageResource(R.drawable.ic_circle);

        int color = isSelected
                ? ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorPrimary)
                : ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorAttendanceNotChecked);
        holder.binding.ivDayStatus.setColorFilter(color);
        holder.binding.tvDayName.setTextColor(isSelected
                ? ColorUtil.getThemeColor(holder.itemView.getContext(), com.google.android.material.R.attr.colorOnPrimaryContainer)
                : ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorOnSurfaceVariant));

        holder.binding.getRoot().setCardBackgroundColor(isSelected
                ? ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorPrimaryContainer)
                : ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorSurfaceVariant));

        if (isToday) {
            holder.binding.getRoot().setStrokeColor(ColorUtil.getThemeColor(holder.itemView.getContext(), R.attr.colorPrimary));
            holder.binding.getRoot().setStrokeWidth((int) holder.itemView.getContext().getResources().getDimension(R.dimen.border_width_s));
        } else {
            holder.binding.getRoot().setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            LocalDate previous = selectedDate;
            selectedDate = date;
            notifyItemChanged(days.indexOf(previous));
            notifyItemChanged(position);
            if (listener != null) listener.onDateSelected(date);
        });
    }

    @Override
    public int getItemCount() {
        return days != null ? days.size() : 0;
    }

    private String getDayLetter(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "T2";
            case TUESDAY: return "T3";
            case WEDNESDAY: return "T4";
            case THURSDAY: return "T5";
            case FRIDAY: return "T6";
            case SATURDAY: return "T7";
            default: return "CN";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemHomeWeekDayBinding binding;

        public ViewHolder(ItemHomeWeekDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
