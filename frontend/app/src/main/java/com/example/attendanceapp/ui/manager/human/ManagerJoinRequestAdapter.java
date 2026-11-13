package com.example.attendanceapp.ui.manager.human;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ItemManagerJoinRequestBinding;
import com.example.attendanceapp.domain.model.JoinRequestItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerJoinRequestAdapter extends RecyclerView.Adapter<ManagerJoinRequestAdapter.ViewHolder> {

    public interface OnActionListener {
        void onApprove(JoinRequestItem item, int position);
        void onReject(JoinRequestItem item, int position);
    }

    private final List<JoinRequestItem> list = new ArrayList<>();
    private final OnActionListener listener;

    public ManagerJoinRequestAdapter(List<JoinRequestItem> initialList, OnActionListener listener) {
        if (initialList != null) this.list.addAll(initialList);
        this.listener = listener;
    }

    public void updateData(List<JoinRequestItem> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerJoinRequestBinding binding = ItemManagerJoinRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JoinRequestItem item = list.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvName.setText(item.getFullName());
        holder.binding.tvPhone.setText(String.format("SĐT: %s", item.getPhoneNumber()));
        holder.binding.tvMeta.setText(String.format("Mã mời: %s", item.getInvitationCode()));

        // Format và hiển thị thời gian tương đối
        // Trường hợp 1: Nếu item trả về timestamp (milliseconds dạng long/Long)
        long timestamp = item.getRequestedAtTimestamp(); // Lấy timestamp millis từ model
        holder.binding.tvTime.setText(getRelativeTimeString(timestamp));

        // Trường hợp 2: Nếu item.getRequestedAtLabel() đã là chuỗi hiển thị
        // holder.binding.tvTime.setText(item.getRequestedAtLabel());

        loadAvatar(context, item.getAvatarUrl(), item.getFullName(), holder.binding.ivAvatar);

        holder.binding.btnApprove.setText(context.getString(R.string.human_btn_approve));
        holder.binding.btnReject.setText(context.getString(R.string.human_btn_reject));

        holder.binding.btnApprove.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 800);
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onApprove(item, pos);
            }
        });

        holder.binding.btnReject.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 800);
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onReject(item, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemManagerJoinRequestBinding binding;

        public ViewHolder(ItemManagerJoinRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void loadAvatar(Context context, String avatarUrl, String fullName, com.google.android.material.imageview.ShapeableImageView imageView) {
        com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(context, avatarUrl, imageView);
    }

    /**
     * Hàm trợ lý tính toán thời gian tương đối (ví dụ: Vừa xong, 5 phút trước, 2 giờ trước, 3 ngày trước)
     */
    private String getRelativeTimeString(long timeInMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        if (diff < 0) diff = 0;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 30) {
            return days + " ngày trước";
        } else {
            // Hoặc sử dụng DateUtils mặc định của Android
            return DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString();
        }
    }
}