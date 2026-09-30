package com.example.attendanceapp.ui.manager.schedule;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemHistoryCalendarDayBinding;
import com.example.attendanceapp.domain.model.DayStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerMonthCalendarAdapter extends RecyclerView.Adapter<ManagerMonthCalendarAdapter.DayViewHolder> {

    private final List<LocalDate> days = new ArrayList<>();
    private final Map<LocalDate, DayStatus> dayStatuses = new HashMap<>();
    private LocalDate selectedDate;
    private final LocalDate currentMonth; // Dùng để làm mờ các ngày không thuộc tháng hiện tại
    private OnDateSelectedListener onDateSelectedListener;

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    public ManagerMonthCalendarAdapter(List<LocalDate> days, LocalDate currentMonth, LocalDate selectedDate, OnDateSelectedListener listener) {
        if (days != null) {
            this.days.addAll(days);
        }
        this.currentMonth = currentMonth;
        this.selectedDate = selectedDate;
        this.onDateSelectedListener = listener;
    }

    public void setDayStatuses(Map<LocalDate, DayStatus> statuses) {
        this.dayStatuses.clear();
        if (statuses != null) {
            this.dayStatuses.putAll(statuses);
        }
        notifyDataSetChanged();
    }

    public void submitList(List<LocalDate> newDays, LocalDate newSelected) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        this.selectedDate = newSelected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryCalendarDayBinding binding = ItemHistoryCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private final ItemHistoryCalendarDayBinding binding;

        DayViewHolder(ItemHistoryCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LocalDate date) {
            Context context = itemView.getContext();
            
            // 1. Hiển thị số ngày
            binding.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));

            // 2. Kiểm tra nếu ngày thuộc tháng khác thì làm mờ (alpha)
            boolean isSameMonth = date.getMonth() == currentMonth.getMonth() && date.getYear() == currentMonth.getYear();
            binding.getRoot().setAlpha(isSameMonth ? 1.0f : 0.3f);

            // 3. Xử lý trạng thái (Icon/Dot)
            DayStatus status = dayStatuses.getOrDefault(date, DayStatus.NONE);
            bindStatus(status);

            // 4. Highlight ngày hiện tại hoặc ngày đang chọn
            boolean isSelected = date.equals(selectedDate);
            boolean isToday = date.equals(LocalDate.now());

            if (isSelected) {
                binding.cardCalendarDay.setStrokeWidth(dpToPx(2));
                binding.cardCalendarDay.setStrokeColor(ColorUtil.getThemeColor(context, android.R.attr.colorPrimary));
                binding.cardCalendarDay.setCardBackgroundColor(ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorPrimaryContainer));
                binding.tvDayNumber.setTextColor(ColorUtil.getThemeColor(context, android.R.attr.colorPrimary));
            } else {
                binding.cardCalendarDay.setStrokeWidth(0);
                binding.cardCalendarDay.setCardBackgroundColor(ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorSurfaceVariant));
                binding.tvDayNumber.setTextColor(ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant));
                
                if (isToday) {
                    binding.cardCalendarDay.setStrokeWidth(dpToPx(1));
                    binding.cardCalendarDay.setStrokeColor(ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorOutline));
                }
            }

            // 5. Sự kiện click
            itemView.setOnClickListener(v -> {
                if (onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected(date);
                }
            });
        }

        private void bindStatus(DayStatus status) {
            int iconRes;
            int colorRes;

            switch (status) {
                case SUCCESS:
                    iconRes = R.drawable.ic_check;
                    colorRes = R.color.attendance_present;
                    break;
                case WARNING:
                    iconRes = R.drawable.ic_warning;
                    colorRes = R.color.attendance_late;
                    break;
                case CRITICAL:
                    iconRes = R.drawable.ic_close;
                    colorRes = R.color.attendance_absent;
                    break;
                default:
                    binding.ivDayStatus.setVisibility(View.INVISIBLE);
                    return;
            }

            binding.ivDayStatus.setVisibility(View.VISIBLE);
            binding.ivDayStatus.setImageResource(iconRes);
            binding.ivDayStatus.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.getContext(), colorRes)
            ));
        }

        private int dpToPx(int dp) {
            float density = binding.getRoot().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}