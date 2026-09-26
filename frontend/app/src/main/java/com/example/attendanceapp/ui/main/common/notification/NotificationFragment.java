package com.example.attendanceapp.ui.main.common.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.example.attendanceapp.databinding.FragmentNotificationBinding;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.NotificationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationFragment extends Fragment {

    FragmentNotificationBinding binding;
    NotificationAdapter notificationAdapter;

    // TODO: thay bằng dữ liệu thật từ ViewModel/Repository (Room, API, Firebase...)
    private final List<Notification> allNotifications = new ArrayList<>();

    public NotificationFragment() {
        // Constructor rỗng mặc định - BẮT BUỘC để hệ thống khôi phục Fragment khi cần
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
        setupObservers();

        loadNotifications();
    }

    private void setupViews() {
        binding.toolbarApp.setTitle("Thông báo");

        notificationAdapter = new NotificationAdapter(new ArrayList<>(), this::onNotificationClicked);

        binding.rvNotification.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvNotification.setAdapter(notificationAdapter);
        binding.rvNotification.setHasFixedSize(true);
    }

    private void setupListeners() {
        // Bấm nút back -> Nút quay lại của Activity/Fragment
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void setupObservers() {
        // TODO: nếu dùng ViewModel, observe LiveData/StateFlow danh sách thông báo ở đây,
        // ví dụ: viewModel.getNotifications().observe(getViewLifecycleOwner(), list -> {
        //     allNotifications.clear();
        //     allNotifications.addAll(list);
        //     renderList();
        // });
    }

    /**
     * TODO: thay bằng lời gọi Repository/API thật. Tạm dùng dữ liệu mẫu để test UI.
     */
    private void loadNotifications() {
        allNotifications.clear();
        allNotifications.addAll(getSampleNotifications());
        renderList();
    }

    private List<Notification> getSampleNotifications() {
        LocalDateTime now = LocalDateTime.now();

        return Arrays.asList(
                new Notification(
                        "1",
                        "Bảng lương tháng 8 đã được duyệt",
                        "Lương tháng 8/2026 của bạn đã được duyệt và sẽ chuyển khoản vào ngày 05/09.",
                        now.minusMinutes(5),
                        NotificationType.PAYROLL,
                        false),
                new Notification(
                        "2",
                        "Đơn nghỉ phép đã được duyệt",
                        "Quản lý đã duyệt đơn nghỉ phép ngày 10/09/2026 của bạn.",
                        now.minusHours(3),
                        NotificationType.LEAVE,
                        false),
                new Notification(
                        "3",
                        "Nhắc nhở chấm công",
                        "Bạn chưa check-out hôm qua. Vui lòng liên hệ quản lý nếu cần điều chỉnh.",
                        now.minusHours(20),
                        NotificationType.ATTENDANCE,
                        true),
                new Notification(
                        "4",
                        "Thay đổi ca làm việc",
                        "Ca làm của bạn ngày 03/09/2026 đã được đổi sang Ca chiều (14:00 - 22:00).",
                        now.minusDays(1),
                        NotificationType.SCHEDULE,
                        true),
                new Notification(
                        "5",
                        "Cập nhật hệ thống",
                        "Ứng dụng đã có phiên bản mới với nhiều cải tiến về hiệu năng.",
                        now.minusDays(4),
                        NotificationType.SYSTEM,
                        true)
        );
    }

    private void renderList() {
        notificationAdapter.updateData(new ArrayList<>(allNotifications));

        boolean isEmpty = allNotifications.isEmpty();
        binding.rvNotification.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void onNotificationClicked(Notification notification) {
        if (!notification.isRead()) {
            notification.setRead(true);
            // TODO: gọi Repository/API để đồng bộ trạng thái đã đọc lên server.
            renderList();
        }

        // TODO: điều hướng sang màn chi tiết tương ứng theo notification.getType(),
        // ví dụ PAYROLL -> màn chi tiết bảng lương, LEAVE -> màn chi tiết đơn nghỉ phép.
    }

    private void markAllAsRead() {
        for (Notification item : allNotifications) {
            item.setRead(true);
        }
        // TODO: gọi Repository/API để đồng bộ "đánh dấu tất cả đã đọc" lên server.
        renderList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}