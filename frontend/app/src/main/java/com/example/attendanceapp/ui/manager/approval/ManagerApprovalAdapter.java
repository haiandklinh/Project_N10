package com.example.attendanceapp.ui.manager.approval;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemManagerApprovalRequestBinding;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;

import java.util.ArrayList;
import java.util.List;

public class ManagerApprovalAdapter extends RecyclerView.Adapter<ManagerApprovalAdapter.ViewHolder> {

    public interface OnActionListener {
        void onApprove(ManagerApprovalItem item, int position);
        void onReject(ManagerApprovalItem item, int position);
    }

    private final List<ManagerApprovalItem> list;
    private final OnActionListener listener;

    public ManagerApprovalAdapter(List<ManagerApprovalItem> list, OnActionListener listener) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
        this.listener = listener;
    }

    /** Thay toàn bộ dữ liệu hiển thị (dùng khi lọc theo trạng thái/loại yêu cầu). */
    public void updateData(List<ManagerApprovalItem> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemManagerApprovalRequestBinding binding = ItemManagerApprovalRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManagerApprovalItem item = list.get(position);
        Context context = holder.itemView.getContext();

        // 1. Gán thông tin nhân viên & yêu cầu
        holder.binding.tvEmployeeName.setText(item.getEmployeeName());
        holder.binding.tvEmployeePosition.setText(item.getPosition());
        holder.binding.tvRequestType.setText(item.getType().getTitle());
        holder.binding.ivTypeIcon.setImageResource(item.getType().getIconResId());

        String targetText = item.getTargetDateLabel() +
                (item.getDetailLabel() != null && !item.getDetailLabel().isEmpty() ? " · " + item.getDetailLabel() : "");
        holder.binding.tvTargetDate.setText(targetText);
        holder.binding.tvReason.setText("Lý do: " + item.getReason());

        // 2. Xử lý UI Trạng thái (Gọn gàng từ Enum ApprovalStatus)
        ApprovalStatus status = item.getStatus();
        holder.binding.chipStatus.setText(status.getLabel());
        holder.binding.chipStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(context, status.getContainerColorRes())
        ));

        // 3. Hiện nút duyệt/từ chối CHỈ KHI trạng thái là PENDING
        boolean isPending = status == ApprovalStatus.PENDING;
        holder.binding.layoutActions.setVisibility(isPending ? View.VISIBLE : View.GONE);

        // 4. Bắt sự kiện click
        holder.binding.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(item, holder.getBindingAdapterPosition());
        });
        holder.binding.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(item, holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemManagerApprovalRequestBinding binding;

        public ViewHolder(ItemManagerApprovalRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}