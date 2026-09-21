package com.example.attendanceapp.ui.employee.history.calendar;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemHistoryCalendarDayBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;

import java.util.ArrayList;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder> {

    private final List<WeekDay> days = new ArrayList<>();
    private OnDayClickListener onDayClickListener;

    public CalendarDayAdapter(List<WeekDay> days) {
        if (days != null) {
            this.days.addAll(days);
        }
    }

    public void submitList(List<WeekDay> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    public interface OnDayClickListener {
        void onDayClick(WeekDay day);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    @NonNull
    @Override
    public CalendarDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryCalendarDayBinding binding = ItemHistoryCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CalendarDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarDayViewHolder holder, int position) {
        WeekDay day = days.get(position);
        holder.bind(day);

        holder.binding.cardCalendarDay.setOnClickListener(v -> {
            if (onDayClickListener != null) {
                onDayClickListener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class CalendarDayViewHolder extends RecyclerView.ViewHolder {

        private final ItemHistoryCalendarDayBinding binding;

        CalendarDayViewHolder(ItemHistoryCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WeekDay day) {
            binding.tvDayNumber.setText(String.valueOf(day.getDate().getDayOfMonth()));
            bindStatus(day.getStatus());


            // Highlight hôm nay
            if (day.isToday()) {
                binding.getRoot().setBackgroundResource(
                        R.drawable.bg_weekday_today
                );

              binding.tvDayNumber.setTextColor(
                        ContextCompat.getColor(
                                itemView.getContext(),
                                R.color.attendance_present
                        )
                );
            }
        }

        private void bindStatus(AttendanceStatus status) {
            int icon;
            int color;

            switch (status) {
                case PRESENT:
                    icon = R.drawable.ic_check;
                    color = R.color.attendance_present;
                    break;
                case LATE:
                    icon = R.drawable.ic_warning;
                    color = R.color.attendance_late;
                    break;
                case ABSENT:
                    icon = R.drawable.ic_close;
                    color = R.color.attendance_absent;
                    break;
                case LEAVE:
                    icon = R.drawable.ic_check;
                    color = R.color.attendance_leave;
                    break;
                case NOT_CHECKED:
                    icon = R.drawable.ic_remove;
                    color = R.color.attendance_not_checked;
                    break;
                case WEEKEND:
                    icon = R.drawable.ic_remove;
                    color = R.color.attendance_weekend;
                    break;
                default:
                    icon = R.drawable.ic_remove;
                    color = R.color.attendance_not_checked;
                    break;
            }

            binding.ivDayStatus.setImageResource(icon);
            binding.ivDayStatus.setColorFilter(
                    ContextCompat.getColor(binding.getRoot().getContext(), color)
            );
        }

        private int dpToPx(int dp) {
            float density = binding.getRoot().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}