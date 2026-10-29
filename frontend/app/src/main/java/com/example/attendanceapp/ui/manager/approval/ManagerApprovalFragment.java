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
import com.example.attendanceapp.databinding.FragmentManagerApprovalBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

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

        disposables.add(webSocketManager.observeIncomingNotifications()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notification -> loadApprovalRequests(), throwable -> {}));
    }
    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
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
                                            boolean isDeactive = "deactive".equalsIgnoreCase(item.getStatus());
                                            boolean isLocallyHandled = staffId != null && handledRequestManager.isHandled(staffId);
                                            ApprovalStatus itemStatus = (isDeactive || isLocallyHandled) 
                                                    ? ApprovalStatus.APPROVED 
                                                    : ApprovalStatus.PENDING;

                                            String typeStr = item.getNotificationType();
                                            ApprovalRequestType type;
                                            String detail = "Yêu cầu từ nhân viên";
                                            if ("CHANGE_WORK_SHIFT".equalsIgnoreCase(typeStr) || "SHIFT_CHANGE_REQUEST".equalsIgnoreCase(typeStr)) {
                                                type = ApprovalRequestType.SWAP;
                                                detail = "Đổi ca làm việc";
                                            } else if ("CHANGE_LEAVE_REQUEST".equalsIgnoreCase(typeStr) || "LEAVE_REQUEST".equalsIgnoreCase(typeStr)) {
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
                                            String dateLabel = item.getSendAt() != null ? item.getSendAt() : "Mới đây";

                                            // Trích xuất chi tiết ca làm và ngày làm việc từ content của thông báo
                                            Map<String, Object> content = item.getContent();
                                            String curShift = "shift_01";
                                            String tgtShift = "shift_02";
                                            String workDay = "";
                                            String reasonText = detail;

                                            if (content != null) {
                                                if (content.containsKey("id_work_shift_cur")) curShift = String.valueOf(content.get("id_work_shift_cur"));
                                                else if (content.containsKey("id_work_shift_current")) curShift = String.valueOf(content.get("id_work_shift_current"));
                                                else if (content.containsKey("current_shift_id")) curShift = String.valueOf(content.get("current_shift_id"));
                                                else if (content.containsKey("id_work_shift_off")) curShift = String.valueOf(content.get("id_work_shift_off"));

                                                if (content.containsKey("id_work_shift_change")) tgtShift = String.valueOf(content.get("id_work_shift_change"));
                                                else if (content.containsKey("target_shift_id")) tgtShift = String.valueOf(content.get("target_shift_id"));

                                                if (content.containsKey("workday")) workDay = String.valueOf(content.get("workday"));
                                                else if (content.containsKey("work_day")) workDay = String.valueOf(content.get("work_day"));
                                                else if (content.containsKey("date")) workDay = String.valueOf(content.get("date"));

                                                if (content.containsKey("reason")) reasonText = String.valueOf(content.get("reason"));
                                                else if (content.containsKey("message")) reasonText = String.valueOf(content.get("message"));
                                            }

                                            if (workDay.isEmpty()) {
                                                workDay = (item.getSendAt() != null && item.getSendAt().length() >= 10)
                                                        ? item.getSendAt().substring(0, 10)
                                                        : java.time.LocalDate.now().toString();
                                            }

                                            allRequests.add(new ManagerApprovalItem(
                                                    item.getStaffId() != null ? item.getStaffId() : "",
                                                    senderName,
                                                    item.getSendPersonAvatar(),
                                                    "Nhân viên",
                                                    type,
                                                    workDay,
                                                    detail,
                                                    reasonText,
                                                    itemStatus,
                                                    0,
                                                    curShift,
                                                    tgtShift,
                                                    workDay
                                            ));
                                        }
                                    }

                                    updatePendingCount();
                                    applyFilters();
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                },
                                error -> {
                                    allRequests.clear();
                                    updatePendingCount();
                                    applyFilters();
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                }
                        )
        );
    }

    private void setupList() {
        adapter = new ManagerApprovalAdapter(new ArrayList<>(), new ManagerApprovalAdapter.OnActionListener() {
            @Override
            public void onApprove(ManagerApprovalItem item, int position) {
                executeApproval(item);
            }

            private void executeApproval(ManagerApprovalItem item) {
                item.setStatus(ApprovalStatus.APPROVED);
                handledRequestManager.markHandled(item.getId());
                disposables.add(notificationRepository.deleteNotificationsByStaffId(item.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {}, err -> {}));

                Toast.makeText(getContext(), "Đã duyệt yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                updatePendingCount();
                applyFilters();

                String curShift = item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "shift_01";
                String tgtShift = item.getTargetShiftId() != null ? item.getTargetShiftId() : "shift_02";
                String workDay = item.getWorkDay() != null ? item.getWorkDay() : java.time.LocalDate.now().toString();

                if (item.getType() == ApprovalRequestType.SWAP) {
                    disposables.add(managerRepository.handleShiftChange(
                            new HandleShiftChangeRequest(item.getId(), curShift, tgtShift, workDay, true, "Đồng ý đổi ca")
                    ).subscribe(() -> {}, err -> {}));
                } else {
                    disposables.add(managerRepository.handleLeaveRequest(
                            new HandleLeaveRequest(item.getId(), curShift, workDay, true, "Đồng ý phê duyệt")
                    ).subscribe(() -> {}, err -> {}));
                }
            }

            @Override
            public void onReject(ManagerApprovalItem item, int position) {
                item.setStatus(ApprovalStatus.REJECTED);
                handledRequestManager.markHandled(item.getId());
                disposables.add(notificationRepository.deleteNotificationsByStaffId(item.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {}, err -> {}));

                Toast.makeText(getContext(), "Đã từ chối yêu cầu của " + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                updatePendingCount();
                applyFilters();

                String curShift = item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "shift_01";
                String tgtShift = item.getTargetShiftId() != null ? item.getTargetShiftId() : "shift_02";
                String workDay = item.getWorkDay() != null ? item.getWorkDay() : java.time.LocalDate.now().toString();

                if (item.getType() == ApprovalRequestType.SWAP) {
                    disposables.add(managerRepository.handleShiftChange(
                            new HandleShiftChangeRequest(item.getId(), curShift, tgtShift, workDay, false, "Từ chối đổi ca")
                    ).subscribe(() -> {}, err -> {}));
                } else {
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
            if (checkedIds.isEmpty()) {
                selectedType = null;
            } else if (checkedIds.get(0) == binding.chipTypeLeave.getId()) {
                selectedType = ApprovalRequestType.LEAVE;
            } else if (checkedIds.get(0) == binding.chipTypeAdjustment.getId()) {
                selectedType = ApprovalRequestType.ADJUSTMENT;
            } else if (checkedIds.get(0) == binding.chipTypeSwap.getId()) {
                selectedType = ApprovalRequestType.SWAP;
            } else {
                selectedType = null;
            }
            applyFilters();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadApprovalRequests();
        });
    }

    private void applyFilters() {
        List<ManagerApprovalItem> filtered = new ArrayList<>();
        for (ManagerApprovalItem item : allRequests) {
            boolean matchStatus;
            switch (selectedStatusTab) {
                case 1:
                    matchStatus = item.getStatus() == ApprovalStatus.APPROVED;
                    break;
                case 2:
                    matchStatus = item.getStatus() == ApprovalStatus.REJECTED;
                    break;
                case 3:
                    matchStatus = true;
                    break;
                default:
                    matchStatus = item.getStatus() == ApprovalStatus.PENDING;
                    break;
            }
            boolean matchType = (selectedType == null) || (item.getType() == selectedType);
            if (matchStatus && matchType) {
                filtered.add(item);
            }
        }

        adapter.updateData(filtered);

        boolean isEmpty = filtered.isEmpty();
        binding.rvApprovalRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
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
