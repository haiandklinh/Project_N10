package com.example.attendanceapp.ui.employee.schedule.weekday;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemScheduleWeekDayBinding;
import com.example.attendanceapp.domain.model.AttendanceStatus;
import com.example.attendanceapp.domain.model.WeekDay;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeScheduleWeekDayAdapter extends RecyclerView.Adapter<EmployeeScheduleWeekDayAdapter.WeekDayViewHolder> {

    private final List<WeekDay> days = new ArrayList<>();
    private LocalDate selectedDate;
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(WeekDay day);
    }

    public EmployeeScheduleWeekDayAdapter(List<WeekDay> days) {
        if (days != null) {
            this.days.addAll(days);
        }
        this.selectedDate = LocalDate.now();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    public void submitList(List<WeekDay> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null && !date.equals(this.selectedDate)) {
            this.selectedDate = date;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleWeekDayBinding binding = ItemScheduleWeekDayBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new WeekDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekDayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    class WeekDayViewHolder extends RecyclerView.ViewHolder {

        private final ItemScheduleWeekDayBinding binding;

        public WeekDayViewHolder(ItemScheduleWeekDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(WeekDay weekDay) {
            Context context = itemView.getContext();
            LocalDate date = weekDay.getDate();
            boolean isSelected = date.equals(selectedDate);

            // 1. Format Tên Thứ (T2, T3, T4... / CN)
            String dayOfWeekName = getShortDayOfWeekName(date);
            binding.tvDayOfWeek.setText(dayOfWeekName);

            // 2. Format Số Ngày (1, 2, 15...)
            binding.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));

            // 3. Xử lý Icon chỉ báo trạng thái ca làm dựa theo AttendanceStatus
            setupStatusIcon(weekDay.getStatus());

            // 4. Xử lý UI Trạng thái Được chọn (Selected) vs Chưa chọn (Unselected)
            if (isSelected) {
                // Card trắng nổi bật khi chọn
                // Lấy màu từ Theme Attribute (R.attr.colorPrimaryContainer)
                int containerColor = MaterialColors.getColor(
                        context,
                        com.google.android.material.R.attr.colorPrimaryContainer,
                        0 // Giá trị màu mặc định nếu không tìm thấy attribute
                );
                // Gán màu vào MaterialCardView
                binding.cardDayItem.setCardBackgroundColor(containerColor);

                int primaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, 0);
                binding.tvDayOfWeek.setTextColor(primaryColor);
                binding.tvDayNumber.setTextColor(primaryColor);
                binding.ivDayStatus.setImageTintList(ColorStateList.valueOf(primaryColor));
            } else {
                // Background mờ khi không chọn
                binding.cardDayItem.setCardBackgroundColor(
                        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceVariant, 0)
                );

                int secondaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, 0);
                binding.tvDayOfWeek.setTextColor(secondaryColor);
                binding.tvDayNumber.setTextColor(secondaryColor);
                binding.ivDayStatus.setImageTintList(ColorStateList.valueOf(secondaryColor));
            }

            // 5. Sự kiện Click ô ngày
            itemView.setOnClickListener(v -> {
                setSelectedDate(date);
                if (onDayClickListener != null) {
                    onDayClickListener.onDayClick(weekDay);
                }
            });
        }

        private String getShortDayOfWeekName(LocalDate date) {
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

        private void setupStatusIcon(AttendanceStatus status) {
            int iconRes;

            switch (status) {
                case PRESENT:
                case LEAVE:
                case EARLY_LEAVE:
                    iconRes = R.drawable.ic_check;
                    break;
                case LATE:
                    iconRes = R.drawable.ic_warning;
                    break;
                case ABSENT:
                    iconRes = R.drawable.ic_close;
                    break;
                case NOT_CHECKED:
                case WEEKEND:
                default:
                    iconRes = R.drawable.ic_remove; // Chưa chấm công hoặc cuối tuần -> ic_remove
                    break;
            }

            binding.ivDayStatus.setVisibility(View.VISIBLE);
            binding.ivDayStatus.setImageResource(iconRes);
        }
    }
}