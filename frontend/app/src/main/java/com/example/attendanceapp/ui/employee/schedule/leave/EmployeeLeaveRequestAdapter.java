package com.example.attendanceapp.ui.employee.schedule.leave;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendanceapp.databinding.ItemAttendanceAdjustmentBinding;
import com.example.attendanceapp.databinding.ItemLeaveRequestBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.LeaveRequest;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmployeeLeaveRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LEAVE_SWAP = 0;
    private static final int VIEW_TYPE_ADJUSTMENT = 1;

    private final List<LeaveRequest> list;

    public EmployeeLeaveRequestAdapter(List<LeaveRequest> list) {
        this.list = list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public void updateData(List<LeaveRequest> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        LeaveRequest item = list.get(position);
        if (item != null && item.getType() == ApprovalRequestType.ADJUSTMENT) {
            return VIEW_TYPE_ADJUSTMENT;
        }
        return VIEW_TYPE_LEAVE_SWAP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADJUSTMENT) {
            ItemAttendanceAdjustmentBinding binding = ItemAttendanceAdjustmentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new AdjustmentViewHolder(binding);
        } else {
            ItemLeaveRequestBinding binding = ItemLeaveRequestBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new LeaveViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LeaveRequest item = list.get(position);
        if (holder instanceof AdjustmentViewHolder) {
            ((AdjustmentViewHolder) holder).bind(item);
        } else if (holder instanceof LeaveViewHolder) {
            ((LeaveViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ===== VIEW HOLDER 1: LEAVE & SWAP (item_leave_request.xml) =====
    public static class LeaveViewHolder extends RecyclerView.ViewHolder {
        final ItemLeaveRequestBinding binding;

        public LeaveViewHolder(ItemLeaveRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LeaveRequest item) {
            Context context = itemView.getContext();

            // 1. Gán loại yêu cầu
            ApprovalRequestType type = item.getType();
            if (type != null) {
                binding.tvRequestType.setText(type.getTitle());
            } else {
                binding.tvRequestType.setText("Đơn đề nghị");
            }

            // 2. Gán ngày áp dụng và ca làm việc
            String dateStr = item.getTargetDate() != null ? item.getTargetDate().trim() : "";
            String formattedDate = formatDateDisplay(dateStr);
            if (item.getShiftId() != null && !item.getShiftId().trim().isEmpty() && !item.getShiftId().equalsIgnoreCase("null")) {
                formattedDate += " • " + item.getShiftId();
            }
            binding.tvTargetDate.setText("Ngày áp dụng: " + formattedDate);

            // 3. Gán lý do
            String reasonStr = item.getReason() != null ? item.getReason().trim() : "";
            if (!reasonStr.toLowerCase().startsWith("lý do:")) {
                binding.tvReason.setText("Lý do: " + reasonStr);
            } else {
                binding.tvReason.setText(reasonStr);
            }

            // 4. Badge đột xuất
            binding.tvEmergencyBadge.setVisibility(item.isEmergency() ? View.VISIBLE : View.GONE);

            // 5. Xử lý Chip trạng thái
            ApprovalStatus status = item.getStatus() != null ? item.getStatus() : ApprovalStatus.PENDING;
            binding.chipStatus.setText(status.getLabel());
            binding.chipStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, status.getContainerColorRes())
            ));
        }

        private String formatDateDisplay(String rawDate) {
            if (rawDate == null || rawDate.isEmpty()) return "";
            try {
                if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate date = LocalDate.parse(rawDate);
                    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
            } catch (Exception ignored) {}
            return rawDate;
        }
    }

    // ===== VIEW HOLDER 2: ADJUSTMENT (item_attendance_adjustment.xml) =====
    public static class AdjustmentViewHolder extends RecyclerView.ViewHolder {
        final ItemAttendanceAdjustmentBinding binding;

        public AdjustmentViewHolder(ItemAttendanceAdjustmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LeaveRequest item) {
            Context context = itemView.getContext();

            // 1. Parse loại lý do và lý do chi tiết từ reason
            String fullReason = item.getReason() != null ? item.getReason().trim() : "";
            String reasonTypeLabel = "Điều chỉnh chấm công";
            String reasonDetail = fullReason;

            if (fullReason.startsWith("[") && fullReason.contains("]")) {
                int closeIdx = fullReason.indexOf("]");
                reasonTypeLabel = fullReason.substring(1, closeIdx).trim();
                reasonDetail = fullReason.substring(closeIdx + 1).trim();
            }

            binding.tvReasonType.setText(reasonTypeLabel);
            binding.tvReasonDetail.setText(!reasonDetail.isEmpty() ? reasonDetail : "Không có mô tả chi tiết");

            // 2. Chip Trạng thái
            ApprovalStatus status = item.getStatus() != null ? item.getStatus() : ApprovalStatus.PENDING;
            binding.tvStatus.setText(status.getLabel());
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, status.getTextColorRes()));
            binding.tvStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    ContextCompat.getColor(context, status.getContainerColorRes())
            ));

            // 3. Ngày cần điều chỉnh
            String dateStr = item.getTargetDate() != null ? item.getTargetDate().trim() : "";
            binding.tvRequestDate.setText(formatDateWithDayOfWeek(dateStr));

            // 4. Ngày gửi đơn
            if (item.getCreatedAt() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                binding.tvCreatedDate.setText(sdf.format(new Date(item.getCreatedAt())));
                binding.tvSend.setVisibility(View.VISIBLE);
                binding.tvCreatedDate.setVisibility(View.VISIBLE);
            } else {
                binding.tvSend.setVisibility(View.GONE);
                binding.tvCreatedDate.setVisibility(View.GONE);
            }

            // 5. Hiển thị giờ Check-in / Check-out nếu có
            String inTime = item.getCheckInTime();
            String outTime = item.getCheckOutTime();
            boolean hasIn = inTime != null && !inTime.trim().isEmpty();
            boolean hasOut = outTime != null && !outTime.trim().isEmpty();

            if (hasIn) {
                binding.containerCheckIn.setVisibility(View.VISIBLE);
                binding.tvCheckInTime.setText(inTime);
            } else {
                binding.containerCheckIn.setVisibility(View.GONE);
            }

            if (hasOut) {
                binding.containerCheckOut.setVisibility(View.VISIBLE);
                binding.tvCheckOutTime.setText(outTime);
            } else {
                binding.containerCheckOut.setVisibility(View.GONE);
            }

            binding.dividerCheckInOut.setVisibility((hasIn && hasOut) ? View.VISIBLE : View.GONE);
            binding.layoutTimes.setVisibility((hasIn || hasOut) ? View.VISIBLE : View.GONE);
            binding.dividerTimeReason.setVisibility((hasIn || hasOut) ? View.VISIBLE : View.GONE);
        }

        private String formatDateWithDayOfWeek(String rawDate) {
            if (rawDate == null || rawDate.isEmpty()) return "";
            try {
                if (rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    LocalDate date = LocalDate.parse(rawDate);
                    return date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN")));
                }
            } catch (Exception ignored) {}
            return rawDate;
        }
    }
}
