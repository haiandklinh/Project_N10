package com.example.attendanceapp.ui.manager.approval;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.core.notification.WebSocketNotificationManager;
import com.example.attendanceapp.data.dto.request.manager.HandleLeaveRequest;
import com.example.attendanceapp.data.dto.request.manager.HandleShiftChangeRequest;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.local.HandledRequestManager;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.example.attendanceapp.databinding.FragmentManagerApprovalBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerApprovalFragment extends Fragment {

    private FragmentManagerApprovalBinding binding;
    private ManagerApprovalViewModel viewModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    ManagerRepository managerRepository;

    @Inject
    HandledRequestManager handledRequestManager;

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    WebSocketNotificationManager webSocketManager;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private final List<ManagerApprovalItem> allRequests = new ArrayList<>();
    private ManagerApprovalAdapter adapter;

    private int selectedStatusTab = 0; // 0 = Chờ duyệt, 1 = Đã duyệt, 2 = Từ chối, 3 = Tất cả
    private ApprovalRequestType selectedType = null; // null = tất cả loại

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerApprovalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ManagerApprovalViewModel.class);

        applyWindowInsets();
        loadApprovalRequests();
        setupList();
        setupStatusTabs();
        setupTypeChips();
        setupSwipeRefresh();

        // Lắng nghe thông báo real-time qua WebSocket
        disposables.add(webSocketManager.observeIncomingNotifications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notification -> {
                    // Nếu là thông báo liên quan đến yêu cầu duyệt, tải lại danh sách
                    if (notification.getType() != null) {
                        loadApprovalRequests();
                    }
                }, throwable -> {}));
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private String formatDateDisplay(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";
        try {
            String clean = rawDate.trim();
            if (clean.length() >= 10) {
                clean = clean.substring(0, 10);
            }
            if (clean.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate date = LocalDate.parse(clean);
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
        } catch (Exception ignored) {}
        return rawDate;
    }

    private void loadApprovalRequests() {
        binding.swipeRefreshLayout.setRefreshing(true);
        disposables.add(
                managerRepository.getNotifications()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    allRequests.clear();
                                    if (response != null && response.getData() != null) {
                                        for (NotificationItemResponse item : response.getData()) {
                                            String staffId = item.getStaffId();

                                            String typeStr = item.getNotificationType();
                                            ApprovalRequestType type;
                                            String detail = "Yêu cầu từ nhân viên";
                                            if ("CHANGE_WORK_SHIFT".equalsIgnoreCase(typeStr) || "SHIFT_CHANGE_REQUEST".equalsIgnoreCase(typeStr)) {
                                                type = ApprovalRequestType.SWAP;
                                                detail = "Đổi ca làm việc";
                                            } else if ("CHANGE_LEAVE_REQUEST".equalsIgnoreCase(typeStr) || "LEAVE_REQUEST".equalsIgnoreCase(typeStr)
                                                    || (typeStr != null && (typeStr.contains("LEAVE") || typeStr.contains("OFF")))) {
                                                type = ApprovalRequestType.LEAVE;
                                                detail = "Nghỉ phép";
                                            } else if ("ATTENDANCE_ADJUSTMENT_REQUEST".equalsIgnoreCase(typeStr)) {
                                                type = ApprovalRequestType.ADJUSTMENT;
                                                detail = "Điều chỉnh chấm công";
                                            } else {
                                                // Bỏ qua JOIN_COMPANY_REQUEST và các thông báo khác (đã chuyển về ManagerJoinRequestFragment)
                                                continue;
                                            }

                                            String senderName = item.getSendPerson() != null ? item.getSendPerson() : "Nhân sự";

                                            // Trích xuất chi tiết ca làm và ngày làm việc từ content của thông báo
                                            Map<String, Object> content = item.getContent();
                                            String curShift = "shift_01";
                                            String tgtShift = "shift_02";
                                            String workDay = "";
                                            String reasonText = detail;
                                            String shiftName = "";
                                            String targetShiftName = "";

                                            if (content != null) {
                                                // 1. Trích xuất ngày
                                                if (content.containsKey("work_day_off")) workDay = String.valueOf(content.get("work_day_off"));
                                                else if (content.containsKey("workday_off")) workDay = String.valueOf(content.get("workday_off"));
                                                else if (content.containsKey("workday")) workDay = String.valueOf(content.get("workday"));
                                                else if (content.containsKey("work_day")) workDay = String.valueOf(content.get("work_day"));
                                                else if (content.containsKey("date")) workDay = String.valueOf(content.get("date"));
                                                else if (content.containsKey("day_off")) workDay = String.valueOf(content.get("day_off"));

                                                // 2. Trích xuất ca
                                                if (type == ApprovalRequestType.LEAVE) {
                                                    if (content.containsKey("id_work_shift_off")) curShift = String.valueOf(content.get("id_work_shift_off"));
                                                    else if (content.containsKey("work_shift_off_id")) curShift = String.valueOf(content.get("work_shift_off_id"));
                                                    else if (content.containsKey("id_work_shift")) curShift = String.valueOf(content.get("id_work_shift"));
                                                    else if (content.containsKey("id_shift")) curShift = String.valueOf(content.get("id_shift"));
                                                    else if (content.containsKey("id_work_shift_cur")) curShift = String.valueOf(content.get("id_work_shift_cur"));
                                                } else {
                                                    if (content.containsKey("id_work_shift_cur")) curShift = String.valueOf(content.get("id_work_shift_cur"));
                                                    else if (content.containsKey("id_work_shift_current")) curShift = String.valueOf(content.get("id_work_shift_current"));
                                                    else if (content.containsKey("current_shift_id")) curShift = String.valueOf(content.get("current_shift_id"));
                                                    else if (content.containsKey("id_work_shift_off")) curShift = String.valueOf(content.get("id_work_shift_off"));
                                                }

                                                if (content.containsKey("id_work_shift_change")) tgtShift = String.valueOf(content.get("id_work_shift_change"));
                                                else if (content.containsKey("target_shift_id")) tgtShift = String.valueOf(content.get("target_shift_id"));

                                                // 3. Trích xuất tên ca
                                                if (content.containsKey("shift_name")) shiftName = String.valueOf(content.get("shift_name"));
                                                else if (content.containsKey("name_shift")) shiftName = String.valueOf(content.get("name_shift"));
                                                else if (content.containsKey("name_work_shift_off")) shiftName = String.valueOf(content.get("name_work_shift_off"));
                                                else if (content.containsKey("name_shift_cur")) shiftName = String.valueOf(content.get("name_shift_cur"));

                                                if (content.containsKey("name_shift_change")) targetShiftName = String.valueOf(content.get("name_shift_change"));

                                                // 4. Lý do
                                                if (content.containsKey("reason")) reasonText = String.valueOf(content.get("reason"));
                                                else if (content.containsKey("message")) reasonText = String.valueOf(content.get("message"));
                                            }

                                            if (workDay.isEmpty()) {
                                                workDay = (item.getSendAt() != null && item.getSendAt().length() >= 10)
                                                        ? item.getSendAt().substring(0, 10)
                                                        : LocalDate.now().toString();
                                            } else if (workDay.length() >= 10) {
                                                workDay = workDay.substring(0, 10);
                                            }

                                            // Map tên ca thân thiện nếu chưa có
                                            if (shiftName.isEmpty()) {
                                                if ("shift_all".equalsIgnoreCase(curShift)) shiftName = "Cả ngày";
                                                else if ("shift_01".equalsIgnoreCase(curShift)) shiftName = "Ca sáng";
                                                else if ("shift_02".equalsIgnoreCase(curShift)) shiftName = "Ca chiều";
                                                else if ("shift_03".equalsIgnoreCase(curShift)) shiftName = "Ca hành chính";
                                            }

                                            if (type == ApprovalRequestType.LEAVE) {
                                                detail = !shiftName.isEmpty() ? "Nghỉ phép: " + shiftName : "Nghỉ phép";
                                            } else if (type == ApprovalRequestType.SWAP) {
                                                if (!shiftName.isEmpty() && !targetShiftName.isEmpty()) {
                                                    detail = "Đổi: " + shiftName + " ➔ " + targetShiftName;
                                                } else if (!shiftName.isEmpty()) {
                                                    detail = "Đổi ca: " + shiftName;
                                                } else {
                                                    detail = "Đổi ca làm việc";
                                                }
                                            }

                                            String dateDisplay = formatDateDisplay(workDay);

                                            // Xác định trạng thái của yêu cầu duyệt cụ thể
                                            String requestKey = type.name() + "_" + (staffId != null ? staffId : "") + "_" + workDay + "_" + (curShift != null ? curShift : "");
                                            ApprovalStatus savedStatus = handledRequestManager.getApprovalStatus(requestKey);
                                            ApprovalStatus itemStatus;
                                            if (savedStatus != null) {
                                                itemStatus = savedStatus;
                                            } else if ("approved".equalsIgnoreCase(item.getStatus()) || "accept".equalsIgnoreCase(item.getStatus())) {
                                                itemStatus = ApprovalStatus.APPROVED;
                                            } else if ("rejected".equalsIgnoreCase(item.getStatus()) || "refuse".equalsIgnoreCase(item.getStatus())) {
                                                itemStatus = ApprovalStatus.REJECTED;
                                            } else {
                                                // Mặc định luôn là CHỜ DUYỆT cho tới khi Quản lý duyệt hoặc từ chối
                                                itemStatus = ApprovalStatus.PENDING;
                                            }

                                            ManagerApprovalItem approvalItem = new ManagerApprovalItem(
                                                    item.getStaffId() != null ? item.getStaffId() : "",
                                                    senderName,
                                                    item.getSendPersonAvatar(),
                                                    "Nhân viên",
                                                    type,
                                                    dateDisplay,
                                                    detail,
                                                    reasonText,
                                                    itemStatus,
                                                    0,
                                                    curShift,
                                                    tgtShift,
                                                    workDay
                                            );
                                            approvalItem.setRequestId(item.getNotificationId());
                                            allRequests.add(approvalItem);
                                        }
                                    }

                                    mergeLocalSubmittedRequests();
                                    updatePendingCount();
                                    applyFilters();
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                },
                                error -> {
                                    allRequests.clear();
                                    mergeLocalSubmittedRequests();
                                    updatePendingCount();
                                    applyFilters();
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                }
                        )
        );
    }

    private void mergeLocalSubmittedRequests() {
        if (sharedPreferenceApp == null) return;
        List<LeaveRequest> localRequests = sharedPreferenceApp.getSubmittedLeaveRequests();
        if (localRequests == null || localRequests.isEmpty()) return;

        for (LeaveRequest req : localRequests) {
            if (req == null) continue;
            String reqDate = req.getTargetDate() != null ? req.getTargetDate().trim() : "";
            ApprovalRequestType reqType = req.getType() != null ? req.getType() : ApprovalRequestType.LEAVE;

            boolean exists = false;
            for (ManagerApprovalItem existing : allRequests) {
                if (existing == null || existing.getType() != reqType) continue;
                boolean sameDay = existing.getWorkDay() != null && existing.getWorkDay().trim().equals(reqDate);
                String existingReason = existing.getReason() != null ? existing.getReason().trim() : "";
                String localReason = req.getReason() != null ? req.getReason().trim() : "";
                if (sameDay && existingReason.equals(localReason)) {
                    exists = true;
                    break;
                }
            }
            if (exists) continue;

            String shiftId = req.getShiftId() != null ? req.getShiftId() : "shift_01";
            String requestKey = reqType.name() + "_" + (req.getUserId() != null ? req.getUserId() : "") + "_" + reqDate + "_" + shiftId;
            ApprovalStatus savedStatus = handledRequestManager.getApprovalStatus(requestKey);
            if (savedStatus == null) {
                savedStatus = handledRequestManager.getApprovalStatus(reqType.name() + "_" + (req.getUserId() != null ? req.getUserId() : "") + "_" + reqDate);
            }
            ApprovalStatus itemStatus = (savedStatus != null) ? savedStatus : (req.getStatus() != null ? req.getStatus() : ApprovalStatus.PENDING);

            String detail = "Yêu cầu từ nhân viên";
            if (reqType == ApprovalRequestType.SWAP) {
                detail = req.getShiftId() != null && !req.getShiftId().isEmpty() ? req.getShiftId() : "Đổi ca làm việc";
            } else if (reqType == ApprovalRequestType.LEAVE) {
                detail = req.getShiftId() != null && !req.getShiftId().isEmpty() ? "Nghỉ phép: " + req.getShiftId() : "Nghỉ phép";
            } else if (reqType == ApprovalRequestType.ADJUSTMENT) {
                detail = "Điều chỉnh chấm công";
                if (req.getCheckInTime() != null || req.getCheckOutTime() != null) {
                    detail += " (" + (req.getCheckInTime() != null ? req.getCheckInTime() : "--:--") + " - " + (req.getCheckOutTime() != null ? req.getCheckOutTime() : "--:--") + ")";
                }
            }

            ManagerApprovalItem localItem = new ManagerApprovalItem(
                    req.getUserId() != null ? req.getUserId() : (req.getId() != null ? req.getId() : ""),
                    "Nhân viên",
                    null,
                    "Nhân viên",
                    reqType,
                    formatDateDisplay(reqDate),
                    detail,
                    req.getReason() != null ? req.getReason() : "",
                    itemStatus,
                    req.getCreatedAt(),
                    shiftId,
                    null,
                    reqDate
            );
            localItem.setRequestId(req.getId());
            allRequests.add(localItem);
        }
    }

    private void setupList() {
        adapter = new ManagerApprovalAdapter(new ArrayList<>(), new ManagerApprovalAdapter.OnActionListener() {
            @Override
            public void onApprove(ManagerApprovalItem item, int position) {
                executeApproval(item);
            }

            private void executeApproval(ManagerApprovalItem item) {
                item.setStatus(ApprovalStatus.APPROVED);
                String requestKey = item.getType().name() + "_" + item.getId() + "_" + item.getWorkDay() + "_" + (item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "");
                handledRequestManager.setApprovalStatus(requestKey, ApprovalStatus.APPROVED);
                disposables.add(notificationRepository.deleteNotificationsByStaffId(item.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {}, err -> {}));

                Toast.makeText(getContext(), "Đã duyệt yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                updatePendingCount();
                applyFilters();

                String curShift = item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "shift_01";
                String tgtShift = item.getTargetShiftId() != null ? item.getTargetShiftId() : "shift_02";
                String workDay = item.getWorkDay() != null ? item.getWorkDay() : LocalDate.now().toString();

                if (sharedPreferenceApp != null) {
                    if (item.getRequestId() != null && !item.getRequestId().isEmpty()) {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatusById(item.getRequestId(), ApprovalStatus.APPROVED);
                    } else {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatus(workDay, item.getType().name(), ApprovalStatus.APPROVED);
                    }
                }

                if (item.getType() == ApprovalRequestType.SWAP) {
                    disposables.add(managerRepository.handleShiftChange(
                            new HandleShiftChangeRequest(item.getId(), curShift, tgtShift, workDay, true, "Đồng ý đổi ca")
                    ).subscribe(() -> {}, err -> {}));
                } else if (item.getType() == ApprovalRequestType.LEAVE) {
                    handledRequestManager.saveStaffLeave(item.getId(), workDay);
                    disposables.add(managerRepository.handleLeaveRequest(
                            new HandleLeaveRequest(item.getId(), curShift, workDay, true, "Đồng ý phê duyệt")
                    ).subscribe(() -> {}, err -> {}));
                } else if (item.getType() == ApprovalRequestType.ADJUSTMENT) {
                    // Logic xử lý điều chỉnh chấm công (nếu có API riêng)
                    Toast.makeText(getContext(), "Đang xử lý điều chỉnh chấm công...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReject(ManagerApprovalItem item, int position) {
                item.setStatus(ApprovalStatus.REJECTED);
                String requestKey = item.getType().name() + "_" + item.getId() + "_" + item.getWorkDay() + "_" + (item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "");
                handledRequestManager.setApprovalStatus(requestKey, ApprovalStatus.REJECTED);
                disposables.add(notificationRepository.deleteNotificationsByStaffId(item.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {}, err -> {}));

                Toast.makeText(getContext(), "Đã từ chối yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                updatePendingCount();
                applyFilters();

                String curShift = item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "shift_01";
                String tgtShift = item.getTargetShiftId() != null ? item.getTargetShiftId() : "shift_02";
                String workDay = item.getWorkDay() != null ? item.getWorkDay() : LocalDate.now().toString();

                if (sharedPreferenceApp != null) {
                    if (item.getRequestId() != null && !item.getRequestId().isEmpty()) {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatusById(item.getRequestId(), ApprovalStatus.REJECTED);
                    } else {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatus(workDay, item.getType().name(), ApprovalStatus.REJECTED);
                    }
                }

                if (item.getType() == ApprovalRequestType.SWAP) {
                    disposables.add(managerRepository.handleShiftChange(
                            new HandleShiftChangeRequest(item.getId(), curShift, tgtShift, workDay, false, "Từ chối đổi ca")
                    ).subscribe(() -> {}, err -> {}));
                } else if (item.getType() == ApprovalRequestType.LEAVE) {
                    disposables.add(managerRepository.handleLeaveRequest(
                            new HandleLeaveRequest(item.getId(), curShift, workDay, false, "Từ chối phê duyệt")
                    ).subscribe(() -> {}, err -> {}));
                }
            }
        });
        binding.rvApprovalRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvApprovalRequests.setAdapter(adapter);
    }

    private void setupStatusTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedStatusTab = tab.getPosition();
                applyFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupTypeChips() {
        binding.chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(binding.chipTypeSwap.getId())) {
                selectedType = ApprovalRequestType.SWAP;
            } else if (checkedIds.contains(binding.chipTypeLeave.getId())) {
                selectedType = ApprovalRequestType.LEAVE;
            } else if (checkedIds.contains(binding.chipTypeAdjustment.getId())) {
                selectedType = ApprovalRequestType.ADJUSTMENT;
            } else {
                selectedType = null;
            }
            applyFilters();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadApprovalRequests);
    }

    private void applyFilters() {
        List<ManagerApprovalItem> filtered = new ArrayList<>();
        for (ManagerApprovalItem item : allRequests) {
            boolean statusMatch;
            switch (selectedStatusTab) {
                case 0: // Chờ duyệt
                    statusMatch = item.getStatus() == ApprovalStatus.PENDING;
                    break;
                case 1: // Đã duyệt
                    statusMatch = item.getStatus() == ApprovalStatus.APPROVED;
                    break;
                case 2: // Từ chối
                    statusMatch = item.getStatus() == ApprovalStatus.REJECTED;
                    break;
                case 3: // Tất cả
                default:
                    statusMatch = true;
                    break;
            }

            boolean typeMatch = (selectedType == null) || (item.getType() == selectedType);

            if (statusMatch && typeMatch) {
                filtered.add(item);
            }
        }

        if (adapter != null) {
            adapter.updateData(filtered);
        }

        boolean isEmpty = filtered.isEmpty();
        binding.layoutEmptyState.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvApprovalRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updatePendingCount() {
        int count = 0;
        for (ManagerApprovalItem item : allRequests) {
            if (item.getStatus() == ApprovalStatus.PENDING) {
                count++;
            }
        }
        if (viewModel != null) {
            viewModel.setPendingApprovalCount(count);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadApprovalRequests();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
