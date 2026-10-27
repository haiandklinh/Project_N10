package com.example.attendanceapp.ui.manager.schedule;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemScheduleWeekDayBinding;
import com.example.attendanceapp.domain.model.DayStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerWeekDaySelectorAdapter extends RecyclerView.Adapter<ManagerWeekDaySelectorAdapter.WeekDayViewHolder> {

    private final List<LocalDate> days = new ArrayList<>();
    private final Map<LocalDate, DayStatus> dayStatuses = new HashMap<>();
    private LocalDate selectedDate;
    private OnDateSelectedListener onDateSelectedListener;

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    public ManagerWeekDaySelectorAdapter(List<LocalDate> days, LocalDate initialSelected, OnDateSelectedListener listener) {
        if (days != null) {
            this.days.addAll(days);
        }
        this.selectedDate = initialSelected != null ? initialSelected : LocalDate.now();
        this.onDateSelectedListener = listener;
    }

    public void setDayStatuses(Map<LocalDate, DayStatus> statuses) {
        this.dayStatuses.clear();
        if (statuses != null) {
            this.dayStatuses.putAll(statuses);
        }
        notifyDataSetChanged();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }

    public void submitList(List<LocalDate> newDays, LocalDate newSelected) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        if (newSelected != null) {
            this.selectedDate = newSelected;
        }
        notifyDataSetChanged();
    }

    public void setSelectedDate(LocalDate date) {
        if (date != null && !date.equals(this.selectedDate)) {
            int previousIndex = days.indexOf(this.selectedDate);
            this.selectedDate = date;
            int newIndex = days.indexOf(this.selectedDate);

            if (previousIndex != -1) {
                notifyItemChanged(previousIndex);
            }
            if (newIndex != -1) {
                notifyItemChanged(newIndex);
            }
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

        public void bind(LocalDate date) {
            Context context = itemView.getContext();
            boolean isSelected = date.equals(selectedDate);

            // 1. Format Tên Thứ (T2, T3, T4... / CN)
            String dayOfWeekName = getShortDayOfWeekName(date);
            binding.tvDayOfWeek.setText(dayOfWeekName);

            // 2. Format Số Ngày (1, 2, 15, 28...)
            binding.tvDayNumber.setText(String.valueOf(date.getDayOfMonth()));

            // 3. Xử lý Icon chỉ báo trạng thái từ DayStatus
            DayStatus status = dayStatuses.getOrDefault(date, DayStatus.NONE);
            setupStatusIcon(status);

            // 4. Xử lý UI Trạng thái Được chọn (Selected) vs Chưa chọn (Unselected)
            int strokeWidth = 0;
            int strokeColor = 0;
            if (date.equals(LocalDate.now())) {
                strokeWidth = (int) (1 * context.getResources().getDisplayMetrics().density); // 1dp
                strokeColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorOnPrimary);
            }
            binding.cardDayItem.setStrokeWidth(strokeWidth);
            binding.cardDayItem.setStrokeColor(strokeColor);

            if (isSelected) {
                // Card nổi bật khi được chọn
                int containerColor = ColorUtil.getThemeColor(
                        context,
                        com.google.android.material.R.attr.colorPrimaryContainer
                );
                binding.cardDayItem.setCardBackgroundColor(containerColor);

                int primaryColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorSecondary);
                binding.tvDayOfWeek.setTextColor(primaryColor);
                binding.tvDayNumber.setTextColor(primaryColor);
            } else {
                // Background mờ khi không được chọn
                binding.cardDayItem.setCardBackgroundColor(
                        ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorSurfaceVariant)
                );

                int secondaryColor = ColorUtil.getThemeColor(context, com.google.android.material.R.attr.colorSecondary);
                binding.tvDayOfWeek.setTextColor(secondaryColor);
                binding.tvDayNumber.setTextColor(secondaryColor);
            }

            // 5. Sự kiện Click ô ngày
            itemView.setOnClickListener(v -> {
                setSelectedDate(date);
                if (onDateSelectedListener != null) {
                    onDateSelectedListener.onDateSelected(date);
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

        private void setupStatusIcon(DayStatus status) {
            if (status == DayStatus.NONE || status.getIconRes() == 0) {
                binding.ivDayStatus.setVisibility(View.INVISIBLE);
                return;
            }

            binding.ivDayStatus.setVisibility(View.VISIBLE);
            binding.ivDayStatus.setImageResource(status.getIconRes());
            binding.ivDayStatus.setImageTintList(ColorStateList.valueOf(
                    ColorUtil.getThemeColor(itemView.getContext(), status.getColorAttrRes())
            ));
        }
    }
}