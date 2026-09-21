package com.example.attendanceapp.ui.employee.home.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemNotificationBinding;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    /**
     * Callback khi người dùng bấm vào 1 thông báo - dùng để đánh dấu đã đọc
     * và/hoặc điều hướng sang màn chi tiết tương ứng (bảng lương, ca làm...).
     */
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    public void updateData(List<Notification> newItems) {
        this.notifications.clear();
        if (newItems != null) {
            this.notifications.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification item = notifications.get(position);

        holder.binding.tvNotificationTitle.setText(item.getTitle());
        holder.binding.tvNotificationMessage.setText(item.getMessage());
        holder.binding.tvNotificationTime.setText(formatRelativeTime(item.getCreatedAt()));

        bindIcon(holder, item.getType());

        holder.binding.dotUnread.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

        // TODO: nếu muốn card chưa đọc có nền nổi bật hơn (VD: colorPrimaryContainer nhạt)
        // để phân biệt rõ với card đã đọc, thêm 1 attr riêng (ví dụ colorNotificationUnreadBg
        // theo cách đã làm với colorAttendancePresent...) rồi gọi ở đây, ví dụ:
        // int bg = MaterialColors.getColor(holder.itemView,
        //         item.isRead() ? com.google.android.material.R.attr.colorSurface
        //                       : R.attr.colorNotificationUnreadBg);
        // holder.binding.cardNotification.setCardBackgroundColor(bg);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(item);
            }
        });
    }

    private void bindIcon(NotificationViewHolder holder, NotificationType type) {
        int icon;

        switch (type) {
            case PAYROLL:
                icon = R.drawable.ic_payroll;
                break;
            case LEAVE:
                icon = R.drawable.ic_remove;
                break;
            case SCHEDULE:
                icon = R.drawable.ic_schedule;
                break;
            case ATTENDANCE:
                icon = R.drawable.ic_history;
                break;
            case SYSTEM:
            default:
                icon = R.drawable.ic_notifications;
                break;
        }

        holder.binding.ivNotificationIcon.setImageResource(icon);
    }

    /**
     * Hiện thời gian dạng "x phút trước / x giờ trước / Hôm qua / dd/MM/yyyy"
     * tương tự cách các app thông báo phổ biến hiển thị.
     */
    private String formatRelativeTime(LocalDateTime createdAt) {
        if (createdAt == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days == 1) {
            return "Hôm qua";
        } else if (days < 7) {
            return days + " ngày trước";
        } else {
            return createdAt.format(dateFormatter);
        }
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        public NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}