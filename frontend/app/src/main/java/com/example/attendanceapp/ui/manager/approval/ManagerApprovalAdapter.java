package com.example.attendanceapp.ui.manager.approval;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.ItemManagerApprovalRequestBinding;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;

import java.util.List;

public class ManagerApprovalAdapter extends RecyclerView.Adapter<ManagerApprovalAdapter.ViewHolder> {

    public interface OnActionListener {
        void onApprove(ManagerApprovalItem item, int position);
        void onReject(ManagerApprovalItem item, int position);
    }

    private final List<ManagerApprovalItem> list;
    private final OnActionListener listener;

    public ManagerApprovalAdapter(List<ManagerApprovalItem> list, OnActionListener listener) {
        this.list = new java.util.ArrayList<>(list);
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

        holder.binding.tvEmployeeName.setText(item.getEmployeeName());
        holder.binding.tvEmployeePosition.setText(item.getPosition());
        holder.binding.tvRequestType.setText(item.getType().getTitle());
        holder.binding.ivTypeIcon.setImageResource(item.getType().getIconResId());
        holder.binding.tvTargetDate.setText(item.getTargetDateLabel() +
                (item.getDetailLabel() != null && !item.getDetailLabel().isEmpty() ? " · " + item.getDetailLabel() : ""));
        holder.binding.tvReason.setText("Lý do: " + item.getReason());

        switch (item.getStatus()) {
            case ManagerApprovalItem.STATUS_APPROVED:
                holder.binding.chipStatus.setText("Đã duyệt");
                holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendancePresentContainer)));
                holder.binding.chipStatus.setTextColor(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendancePresent));
                holder.binding.layoutActions.setVisibility(View.GONE);
                break;
            case ManagerApprovalItem.STATUS_REJECTED:
                holder.binding.chipStatus.setText("Từ chối");
                holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendanceAbsentContainer)));
                holder.binding.chipStatus.setTextColor(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendanceAbsent));
                holder.binding.layoutActions.setVisibility(View.GONE);
                break;
            default: // PENDING
                holder.binding.chipStatus.setText("Chờ duyệt");
                holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendanceLateContainer)));
                holder.binding.chipStatus.setTextColor(
                        ColorUtil.getThemeColor(context, R.attr.colorAttendanceLate));
                holder.binding.layoutActions.setVisibility(View.VISIBLE);
                break;
        }

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
