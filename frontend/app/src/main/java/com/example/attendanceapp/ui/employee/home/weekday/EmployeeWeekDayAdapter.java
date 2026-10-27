package com.example.attendanceapp.ui.employee.home.weekday;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemHomeWeekDayBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeWeekDayAdapter extends RecyclerView.Adapter<EmployeeWeekDayAdapter.HomeWeekDayViewHolder> {

    private final List<WeekDay> items = new ArrayList<>();
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(WeekDay day);
    }

    public EmployeeWeekDayAdapter() {
    }

    public void submitList(List<WeekDay> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    @NonNull
    @Override
    public HomeWeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeWeekDayBinding binding = ItemHomeWeekDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new HomeWeekDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeWeekDayViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HomeWeekDayViewHolder extends RecyclerView.ViewHolder {

        private final ItemHomeWeekDayBinding binding;

        public HomeWeekDayViewHolder(ItemHomeWeekDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WeekDay weekDay) {
            if (weekDay == null) return;
            LocalDate today = LocalDate.now();
            LocalDate date = weekDay.getDate();

            binding.tvDayName.setText(getDayLetter(date));
            bindStatus(weekDay.getStatus());

            boolean isPastOrToday = date != null && (date.isBefore(today) || weekDay.isToday());

            // Màu chữ: ngày đã qua hoặc hôm nay -> primary
            int textColorAttr = isPastOrToday ? R.attr.colorPrimary : R.attr.colorOnSurfaceVariant;
            binding.tvDayName.setTextColor(ColorUtil.getThemeColor(itemView, textColorAttr));

            // Màu nền card: chỉ hôm nay nổi bật
            int cardColorAttr = weekDay.isToday() ? R.attr.colorPrimaryContainer : R.attr.colorSurfaceVariant;
            binding.cardWeekDay.setCardBackgroundColor(ColorUtil.getThemeColor(itemView, cardColorAttr));

            itemView.setOnClickListener(v -> {
                if (onDayClickListener != null) {
                    onDayClickListener.onDayClick(weekDay);
                }
            });
        }

        private String getDayLetter(LocalDate date) {
            if (date == null) return "";
            switch (date.getDayOfWeek()) {
                case MONDAY: return "T2";
                case TUESDAY: return "T3";
                case WEDNESDAY: return "T4";
                case THURSDAY: return "T5";
                case FRIDAY: return "T6";
                case SATURDAY: return "T7";
                case SUNDAY: return "CN";
                default: return "";
            }
        }

        private void bindStatus(AttendanceStatus status) {
            if (status == null) status = AttendanceStatus.NOT_CHECKED;

            int icon;
            int colorAttr;

            switch (status) {
                case PRESENT:
                    icon = R.drawable.ic_check;
                    colorAttr = R.attr.colorAttendancePresent;
                    break;
                case LATE:
                    icon = R.drawable.ic_warning;
                    colorAttr = R.attr.colorAttendanceLate;
                    break;
                case ABSENT:
                    icon = R.drawable.ic_close;
                    colorAttr = R.attr.colorAttendanceAbsent;
                    break;
                case LEAVE:
                    icon = R.drawable.ic_leave;
                    colorAttr = R.attr.colorAttendanceLeave;
                    break;
                case EARLY_LEAVE:
                    icon = R.drawable.ic_early_leave;
                    colorAttr = R.attr.colorAttendanceEarlyLeave;
                    break;
                case NOT_CHECKED:
                    icon = R.drawable.ic_remove;
                    colorAttr = R.attr.colorAttendanceNotChecked;
                    break;
                case WEEKEND:
                    icon = R.drawable.ic_remove;
                    colorAttr = R.attr.colorAttendanceWeekend;
                    break;
                default:
                    icon = R.drawable.ic_circle;
                    colorAttr = R.attr.colorAttendanceNotChecked;
                    break;
            }

            int color = ColorUtil.getThemeColor(itemView, colorAttr);
            binding.ivDayStatus.setImageResource(icon);
            binding.ivDayStatus.setColorFilter(color);
        }
    }
}
