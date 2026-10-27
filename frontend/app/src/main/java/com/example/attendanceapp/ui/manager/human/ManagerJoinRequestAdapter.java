package com.example.attendanceapp.ui.manager.human;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        android.content.Context context = holder.itemView.getContext();

        holder.binding.tvName.setText(item.getFullName());
        holder.binding.tvPhone.setText(String.format("SĐT: %s", item.getPhoneNumber()));
        holder.binding.tvMeta.setText(String.format("Mã mời: %s · %s", item.getInvitationCode(), item.getRequestedAtLabel()));

        loadAvatar(context, item.getAvatarUrl(), item.getFullName(), holder.binding.ivAvatar);

        holder.binding.btnApprove.setText(context.getString(R.string.human_btn_approve));
        holder.binding.btnReject.setText(context.getString(R.string.human_btn_reject));

        holder.binding.btnApprove.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 800);
            if (listener != null) listener.onApprove(item, holder.getBindingAdapterPosition());
        });

        holder.binding.btnReject.setOnClickListener(v -> {
            v.setEnabled(false);
            v.postDelayed(() -> v.setEnabled(true), 800);
            if (listener != null) listener.onReject(item, holder.getBindingAdapterPosition());
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

    private void loadAvatar(android.content.Context context, String avatarUrl, String fullName, com.google.android.material.imageview.ShapeableImageView imageView) {
        com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(context, avatarUrl, imageView);
    }
}