package com.example.attendanceapp.ui.main.common.notification;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.BottomSheetNotificationDetailBinding;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationDetailBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "NotificationDetailBottomSheet";

    private static final String ARG_ID = "arg_id";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_TIME = "arg_time";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_ROLE = "arg_role";

    @Inject
    AppStateRepository appStateRepository;

    private BottomSheetNotificationDetailBinding binding;

    public static NotificationDetailBottomSheet newInstance(Notification notification, UserRole role) {
        NotificationDetailBottomSheet sheet = new NotificationDetailBottomSheet();
        Bundle args = new Bundle();
        if (notification != null) {
            args.putString(ARG_ID, notification.getId());
            args.putString(ARG_TITLE, notification.getTitle());
            args.putString(ARG_MESSAGE, notification.getMessage());
            args.putString(ARG_TYPE, notification.getType() != null ? notification.getType().name() : NotificationType.SYSTEM.name());
            if (notification.getCreatedAt() != null) {
                args.putString(ARG_TIME, notification.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")));
            } else {
                args.putString(ARG_TIME, "");
            }
        }
        if (role != null) {
            args.putString(ARG_ROLE, role.name());
        }
        sheet.setArguments(args);
        return sheet;
    }

    public static NotificationDetailBottomSheet newInstance(Notification notification) {
        return newInstance(notification, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNotificationDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String title = args.getString(ARG_TITLE, "");
        String message = args.getString(ARG_MESSAGE, "");
        String time = args.getString(ARG_TIME, "");
        String typeStr = args.getString(ARG_TYPE, NotificationType.SYSTEM.name());
        String roleStr = args.getString(ARG_ROLE, "");

        NotificationType type = NotificationType.SYSTEM;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (Exception ignored) {}

        UserRole role = null;
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                role = UserRole.valueOf(roleStr);
            } catch (Exception ignored) {}
        }
        if (role == null && appStateRepository != null) {
            role = appStateRepository.getUserRole();
        }

        binding.tvNotificationTitle.setText(title);
        binding.tvNotificationMessage.setText(message);
        binding.tvNotificationTime.setText(time);

        bindTypeInfo(type);
        setupActionButtons(type, title, message, role);

        binding.btnCloseDetail.setOnClickListener(v -> dismiss());
        binding.btnDismiss.setOnClickListener(v -> dismiss());
    }

    private void bindTypeInfo(NotificationType type) {
        binding.tvNotificationTypeBadge.setChipIconResource(getIconForType(type));
        binding.tvNotificationTypeBadge.setChipIconVisible(true);
        binding.tvNotificationTypeBadge.setText(type.getDisplayName());

        int bgColor = ColorUtil.getThemeColor(requireContext(), type.getBgAttrRes());
        int textColor = ColorUtil.getThemeColor(requireContext(), type.getTextColorAttrRes());

        binding.tvNotificationTypeBadge.setChipBackgroundColor(ColorStateList.valueOf(bgColor));
        binding.tvNotificationTypeBadge.setTextColor(textColor);
    }

    private int getIconForType(NotificationType type) {
        switch (type) {
            case PAYROLL: return R.drawable.ic_payroll;
            case LEAVE: return R.drawable.ic_remove;
            case SCHEDULE: return R.drawable.ic_schedule;
            case ATTENDANCE: return R.drawable.ic_history;
            default: return R.drawable.ic_notifications;
        }
    }

    private void setupActionButtons(NotificationType type, String title, String message, UserRole role) {
        String lowerTitle = title.toLowerCase();
        String lowerMessage = message.toLowerCase();

        if (role == UserRole.MANAGER) {
            boolean isJoinRequest = lowerTitle.contains("gia nhập") || lowerTitle.contains("tham gia")
                    || lowerMessage.contains("gia nhập") || lowerMessage.contains("tham gia");

            boolean isApprovalRequest = isJoinRequest
                    || type == NotificationType.LEAVE
                    || type == NotificationType.SCHEDULE
                    || lowerTitle.contains("đổi ca")
                    || lowerTitle.contains("nghỉ phép")
                    || lowerTitle.contains("điều chỉnh")
                    || lowerMessage.contains("đổi ca")
                    || lowerMessage.contains("nghỉ phép")
                    || lowerMessage.contains("điều chỉnh");

            if (isJoinRequest) {
                binding.btnActionButton.setVisibility(View.VISIBLE);
                binding.btnActionButton.setText("Xem đơn gia nhập");
                binding.btnActionButton.setIconResource(R.drawable.ic_profile);
                binding.btnActionButton.setOnClickListener(v -> {
                    dismiss();
                    navigateToDestination(R.id.action_notificationFragment_to_managerJoinRequestFragment);
                });
            } else if (isApprovalRequest) {
                binding.btnActionButton.setVisibility(View.VISIBLE);
                binding.btnActionButton.setText("Xem & Phê duyệt yêu cầu");
                binding.btnActionButton.setIconResource(R.drawable.ic_approval);
                binding.btnActionButton.setOnClickListener(v -> {
                    dismiss();
                    navigateToDestination(R.id.action_notificationFragment_to_managerApprovalFragment);
                });
            } else {
                binding.btnActionButton.setVisibility(View.GONE);
            }
        } else {
            // EMPLOYEE ROLE
            if (type == NotificationType.SCHEDULE || lowerTitle.contains("lịch làm") || lowerTitle.contains("ca làm")) {
                binding.btnActionButton.setVisibility(View.VISIBLE);
                binding.btnActionButton.setText("Xem lịch làm việc");
                binding.btnActionButton.setIconResource(R.drawable.ic_schedule);
                binding.btnActionButton.setOnClickListener(v -> {
                    dismiss();
                    navigateToDestination(R.id.action_notificationFragment_to_employeeScheduleFragment);
                });
            } else if (type == NotificationType.ATTENDANCE || lowerTitle.contains("chấm công")) {
                if (lowerTitle.contains("điều chỉnh") || lowerMessage.contains("điều chỉnh")) {
                    binding.btnActionButton.setVisibility(View.VISIBLE);
                    binding.btnActionButton.setText("Xem điều chỉnh chấm công");
                    binding.btnActionButton.setIconResource(R.drawable.ic_history);
                    binding.btnActionButton.setOnClickListener(v -> {
                        dismiss();
                        navigateToDestination(R.id.action_notificationFragment_to_employeeAdjustmentFragment);
                    });
                } else {
                    binding.btnActionButton.setVisibility(View.VISIBLE);
                    binding.btnActionButton.setText("Chấm công ngay");
                    binding.btnActionButton.setIconResource(R.drawable.ic_fingerprint);
                    binding.btnActionButton.setOnClickListener(v -> {
                        dismiss();
                        navigateToDestination(R.id.action_notificationFragment_to_employeeCheckInFragment);
                    });
                }
            } else if (type == NotificationType.LEAVE || lowerTitle.contains("nghỉ phép") || lowerMessage.contains("nghỉ phép")) {
                binding.btnActionButton.setVisibility(View.VISIBLE);
                binding.btnActionButton.setText("Xem lịch sử nghỉ phép");
                binding.btnActionButton.setIconResource(R.drawable.ic_remove);
                binding.btnActionButton.setOnClickListener(v -> {
                    dismiss();
                    navigateToDestination(R.id.action_notificationFragment_to_employeeLeaveHistoryFragment);
                });
            } else if (type == NotificationType.PAYROLL || lowerTitle.contains("lương") || lowerMessage.contains("lương")) {
                binding.btnActionButton.setVisibility(View.VISIBLE);
                binding.btnActionButton.setText("Xem bảng lương");
                binding.btnActionButton.setIconResource(R.drawable.ic_payroll);
                binding.btnActionButton.setOnClickListener(v -> {
                    dismiss();
                    navigateToDestination(R.id.action_notificationFragment_to_employeePayrollFragment);
                });
            } else {
                binding.btnActionButton.setVisibility(View.GONE);
            }
        }
    }

    private void navigateToDestination(int actionId) {
        try {
            NavController navController;
            if (getParentFragment() != null) {
                navController = NavHostFragment.findNavController(getParentFragment());
            } else {
                navController = NavHostFragment.findNavController(this);
            }
            navController.navigate(actionId);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
