package com.example.attendanceapp.ui.employee.home.weekday;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemHomeWeekDayBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;
import com.example.attendanceapp.core.utils.ColorUtil;

import java.time.LocalDate;

public class EmployeeWeekDayAdapter extends ListAdapter<WeekDay, EmployeeWeekDayAdapter.HomeWeekDayViewHolder> {

    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(WeekDay day);
    }

    public EmployeeWeekDayAdapter() {
        super(new DiffUtil.ItemCallback<WeekDay>() {
            @Override
            public boolean areItemsTheSame(@NonNull WeekDay oldItem, @NonNull WeekDay newItem) {
                return oldItem.getDate().equals(newItem.getDate());
            }

            @Override
            public boolean areContentsTheSame(@NonNull WeekDay oldItem, @NonNull WeekDay newItem) {
                return oldItem.getStatus() == newItem.getStatus()
                        && oldItem.isToday() == newItem.isToday();
            }
        });
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
        holder.bind(getItem(position));
    }

    class HomeWeekDayViewHolder extends RecyclerView.ViewHolder {

        private final ItemHomeWeekDayBinding binding;

        public HomeWeekDayViewHolder(ItemHomeWeekDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WeekDay weekDay) {
            LocalDate today = LocalDate.now();
            LocalDate date = weekDay.getDate();

            // 1. Gán Tên Thứ (T2, T3... CN)
            binding.tvDayName.setText(getDayLetter(date));

            // 2. Bind Icon và Màu trạng thái
            bindStatus(weekDay.getStatus());

            // 3. Highlight nền nếu là ngày hôm nay
            if (weekDay.isToday()) {
                binding.getRoot().setBackgroundResource(R.drawable.bg_weekday_today);
            }

            // 4. Đổi màu chữ tvDayName: Ngày trước hôm nay HOẶC hôm nay -> colorPrimary
            if (date != null && (date.isBefore(today) || weekDay.isToday())) {
                int primaryColor = ColorUtil.getThemeColor(itemView, R.attr.colorPrimary);
                binding.tvDayName.setTextColor(primaryColor);
            } else {
                int defaultTextColor = ColorUtil.getThemeColor(itemView, R.attr.colorOnSurfaceVariant);
                binding.tvDayName.setTextColor(defaultTextColor);
            }

            // 5. Sự kiện Click
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
                    icon = R.drawable.ic_check;
                    colorAttr = R.attr.colorAttendanceLeave;
                    break;
                case EARLY_LEAVE:
                    icon = R.drawable.ic_check;
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