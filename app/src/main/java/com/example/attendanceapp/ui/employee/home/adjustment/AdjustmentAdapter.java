package com.example.attendanceapp.ui.employee.home.adjustment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.attendanceapp.databinding.ItemAttendanceAdjustmentBinding;
import com.example.attendanceapp.domain.model.AttendanceAdjustment;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AdjustmentAdapter extends RecyclerView.Adapter<AdjustmentAdapter.ViewHolder> {

    private List<AttendanceAdjustment> list;

    public AdjustmentAdapter(List<AttendanceAdjustment> list) {
        this.list = list;
    }

    public void updateData(List<AttendanceAdjustment> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAttendanceAdjustmentBinding binding = ItemAttendanceAdjustmentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAttendanceAdjustmentBinding binding;

        public ViewHolder(ItemAttendanceAdjustmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AttendanceAdjustment item) {
            binding.tvReasonType.setText(item.getReasonType());
            binding.tvReasonDetail.setText(item.getReasonDetail());

            // Định dạng ngày yêu cầu
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
            binding.tvRequestDate.setText(item.getRequestDate().format(dateFormatter));

            // Định dạng ngày gửi
            binding.tvCreatedDate.setText("Gửi: " + item.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/M/yyyy")));

            // Check-in / Check-out Display
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            if (item.getCheckInTime() != null) {
                binding.containerCheckIn.setVisibility(View.VISIBLE);
                binding.tvCheckInTime.setText(item.getCheckInTime().format(timeFormatter));
            } else {
                binding.containerCheckIn.setVisibility(View.GONE);
            }

            if (item.getCheckOutTime() != null) {
                binding.containerCheckOut.setVisibility(View.VISIBLE);
                binding.tvCheckOutTime.setText(item.getCheckOutTime().format(timeFormatter));
            } else {
                binding.containerCheckOut.setVisibility(View.GONE);
            }

            // Status Badge
            switch (item.getStatus()) {
                case PENDING:
                    binding.tvStatus.setText("Chờ duyệt");
                    break;
                case APPROVED:
                    binding.tvStatus.setText("Đã duyệt");
                    break;
                case REJECTED:
                    binding.tvStatus.setText("Từ chối");
                    break;
            }
        }
    }
}