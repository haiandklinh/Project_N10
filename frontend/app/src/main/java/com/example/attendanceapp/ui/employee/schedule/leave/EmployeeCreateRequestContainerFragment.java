package com.example.attendanceapp.ui.employee.schedule.leave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.dto.response.staff.NotificationItemResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentEmployeeCreateRequestContainerBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.example.attendanceapp.domain.repository.StaffRepository;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeCreateRequestContainerFragment extends Fragment {

    private FragmentEmployeeCreateRequestContainerBinding binding;
    private EmployeeLeaveRequestAdapter adapter;

    private final List<LeaveRequest> allRequests = new ArrayList<>();
    private final List<LeaveRequest> displayList = new ArrayList<>();

    private int selectedStatusTab = 0; // 0: Tất cả, 1: Chờ duyệt, 2: Đã duyệt, 3: Từ chối
    @Nullable
    private ApprovalRequestType selectedTypeFilter = null; // null: Tất cả, ADJUSTMENT, SWAP, LEAVE

    @Inject
    StaffRepository staffRepository;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeCreateRequestContainerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();
        setupStatusTabs();
        setupTypeChips();
        setupSwipeRefresh();
        setupFab();
        readInitialArguments();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle("Quản lý đơn từ");
        binding.toolbarApp.setBackButtonVisible(true);
        binding.toolbarApp.setOnBackClickListener(() -> Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new EmployeeLeaveRequestAdapter(displayList);
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(adapter);
    }

    private void setupStatusTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedStatusTab = tab.getPosition();
                applyFilters();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupTypeChips() {
        binding.chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(binding.chipFilterAll.getId())) {
                selectedTypeFilter = null;
            } else if (checkedIds.contains(binding.chipFilterAdjustment.getId())) {
                selectedTypeFilter = ApprovalRequestType.ADJUSTMENT;
            } else if (checkedIds.contains(binding.chipFilterSwap.getId())) {
                selectedTypeFilter = ApprovalRequestType.SWAP;
            } else if (checkedIds.contains(binding.chipFilterLeave.getId())) {
                selectedTypeFilter = ApprovalRequestType.LEAVE;
            } else {
                selectedTypeFilter = null;
            }
            applyFilters();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void setupFab() {
        binding.fabCreateRequest.setOnClickListener(v -> {
            if (selectedTypeFilter == ApprovalRequestType.ADJUSTMENT) {
                Navigation.findNavController(v).navigate(R.id.action_container_to_adjustmentRequest);
            } else if (selectedTypeFilter == ApprovalRequestType.SWAP) {
                Navigation.findNavController(v).navigate(R.id.action_container_to_shiftExchange);
            } else if (selectedTypeFilter == ApprovalRequestType.LEAVE) {
                Navigation.findNavController(v).navigate(R.id.action_container_to_leaveRequest);
            } else {
                showCreateRequestChoiceDialog();
            }
        });
    }

    private void showCreateRequestChoiceDialog() {
        String[] options = {"Xin nghỉ phép", "Đổi ca làm việc", "Điều chỉnh chấm công"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn loại đơn cần tạo")
                .setItems(options, (dialog, which) -> {
                    if (binding == null) return;
                    View v = binding.getRoot();
                    switch (which) {
                        case 0:
                            Navigation.findNavController(v).navigate(R.id.action_container_to_leaveRequest);
                            break;
                        case 1:
                            Navigation.findNavController(v).navigate(R.id.action_container_to_shiftExchange);
                            break;
                        case 2:
                        default:
                            Navigation.findNavController(v).navigate(R.id.action_container_to_adjustmentRequest);
                            break;
                    }
                })
                .show();
    }

    private void readInitialArguments() {
        Bundle args = getArguments();
        if (args != null) {
            String filterType = args.getString("initial_filter_type");
            if ("ADJUSTMENT".equalsIgnoreCase(filterType)) {
                binding.chipFilterAdjustment.setChecked(true);
                selectedTypeFilter = ApprovalRequestType.ADJUSTMENT;
            } else if ("SWAP".equalsIgnoreCase(filterType)) {
                binding.chipFilterSwap.setChecked(true);
                selectedTypeFilter = ApprovalRequestType.SWAP;
            } else if ("LEAVE".equalsIgnoreCase(filterType)) {
                binding.chipFilterLeave.setChecked(true);
                selectedTypeFilter = ApprovalRequestType.LEAVE;
            }
        }
    }

    private void loadData() {
        binding.swipeRefreshLayout.setRefreshing(true);

        disposables.add(
                staffRepository.getNotifications()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (binding == null) return;
                                    allRequests.clear();
                                    List<LeaveRequest> remoteList = new ArrayList<>();

                                    if (response != null && response.getData() != null) {
                                        for (NotificationItemResponse notif : response.getData()) {
                                            LeaveRequest req = parseNotificationToLeaveRequest(notif);
                                            if (req != null) {
                                                remoteList.add(req);
                                            }
                                        }
                                    }

                                    List<LeaveRequest> merged = mergeWithLocalRequests(remoteList);
                                    allRequests.addAll(merged);

                                    applyFilters();
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                },
                                throwable -> {
                                    if (binding == null) return;
                                    allRequests.clear();
                                    if (sharedPreferenceApp != null) {
                                        allRequests.addAll(sharedPreferenceApp.getSubmittedLeaveRequests());
                                    }
                                    applyFilters();
                                    binding.swipeRefreshLayout.setRefreshing(false);
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Đã hiển thị đơn đã gửi offline: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
        );
    }

    private LeaveRequest parseNotificationToLeaveRequest(NotificationItemResponse item) {
        if (item == null) return null;
        String notifType = item.getNotificationType() != null ? item.getNotificationType().toUpperCase() : "";
        Map<String, Object> content = item.getContent();

        boolean isLeave = notifType.contains("LEAVE") || (content != null && (content.containsKey("work_day_off") || content.containsKey("id_work_shift_off")));
        boolean isSwap = notifType.contains("SHIFT") || notifType.contains("SWAP") || notifType.contains("WORK_SCHEDULE") || (content != null && (content.containsKey("id_work_shift_change") || content.containsKey("id_work_shift_cur")));
        boolean isAdjustment = notifType.contains("ADJUSTMENT") || (content != null && content.containsKey("check_in_time"));

        if (!isLeave && !isSwap && !isAdjustment) {
            return null;
        }

        ApprovalRequestType requestType;
        if (isLeave) {
            requestType = ApprovalRequestType.LEAVE;
        } else if (isSwap) {
            requestType = ApprovalRequestType.SWAP;
        } else {
            requestType = ApprovalRequestType.ADJUSTMENT;
        }

        // Trạng thái
        ApprovalStatus status = ApprovalStatus.PENDING;
        if (item.getStatus() != null) {
            String st = item.getStatus().trim().toLowerCase();
            if ("approved".equals(st) || "deactive".equals(st) || "accept".equals(st)) {
                status = ApprovalStatus.APPROVED;
            } else if ("rejected".equals(st) || "refuse".equals(st)) {
                status = ApprovalStatus.REJECTED;
            }
        }

        if (content != null && content.containsKey("status")) {
            Object val = content.get("status");
            if (val instanceof Boolean) {
                status = ((Boolean) val) ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
            } else if (val != null) {
                String strVal = val.toString().trim().toLowerCase();
                if ("true".equals(strVal) || "approved".equals(strVal) || "accept".equals(strVal) || "dong_y".equals(strVal)) {
                    status = ApprovalStatus.APPROVED;
                } else if ("false".equals(strVal) || "rejected".equals(strVal) || "reject".equals(strVal) || "tu_choi".equals(strVal)) {
                    status = ApprovalStatus.REJECTED;
                } else if ("pending".equals(strVal) || "cho_duyet".equals(strVal)) {
                    status = ApprovalStatus.PENDING;
                }
            }
        }

        if (notifType.contains("APPROVED")) {
            status = ApprovalStatus.APPROVED;
        } else if (notifType.contains("REJECTED")) {
            status = ApprovalStatus.REJECTED;
        } else if (notifType.endsWith("_RESPONSE")) {
            String message = content != null && content.containsKey("message") ? String.valueOf(content.get("message")).toLowerCase() : "";
            if (message.contains("từ chối") || message.contains("không chấp thuận")) {
                status = ApprovalStatus.REJECTED;
            } else {
                status = ApprovalStatus.APPROVED;
            }
        }

        // Ngày áp dụng
        String targetDate = "";
        if (content != null) {
            if (content.containsKey("work_day_off")) targetDate = String.valueOf(content.get("work_day_off"));
            else if (content.containsKey("workday")) targetDate = String.valueOf(content.get("workday"));
            else if (content.containsKey("work_day")) targetDate = String.valueOf(content.get("work_day"));
            else if (content.containsKey("date")) targetDate = String.valueOf(content.get("date"));
        }
        if (targetDate.isEmpty()) {
            targetDate = (item.getSendAt() != null && item.getSendAt().length() >= 10)
                    ? item.getSendAt().substring(0, 10)
                    : LocalDate.now().toString();
        }

        // Lý do
        String reason = "";
        if (content != null) {
            if (content.containsKey("reason")) reason = String.valueOf(content.get("reason"));
            else if (content.containsKey("message")) reason = String.valueOf(content.get("message"));
            else if (content.containsKey("note")) reason = String.valueOf(content.get("note"));
        }
        if (reason.isEmpty()) {
            reason = requestType.getTitle();
        }

        // Thông tin ca
        String shiftId = "";
        if (content != null) {
            if (content.containsKey("shift_name")) shiftId = String.valueOf(content.get("shift_name"));
            else if (content.containsKey("name_shift")) shiftId = String.valueOf(content.get("name_shift"));
            else if (content.containsKey("id_work_shift_off")) shiftId = String.valueOf(content.get("id_work_shift_off"));
            else if (content.containsKey("id_work_shift_cur")) shiftId = String.valueOf(content.get("id_work_shift_cur"));
        }

        // Giờ Check-in / Check-out nếu có
        String inTime = null;
        String outTime = null;
        if (content != null) {
            if (content.containsKey("check_in_time")) inTime = String.valueOf(content.get("check_in_time"));
            if (content.containsKey("check_out_time")) outTime = String.valueOf(content.get("check_out_time"));
        }

        boolean isEmergency = reason.contains("[ĐỘT XUẤT]") || reason.contains("[Khẩn cấp]") || (content != null && Boolean.TRUE.equals(content.get("is_emergency")));
        long createdAt = System.currentTimeMillis();
        String id = item.getId() != null ? item.getId() : UUID.randomUUID().toString();

        return new LeaveRequest(id, "staff", requestType, targetDate, shiftId, reason, isEmergency, status, createdAt, inTime, outTime);
    }

    private List<LeaveRequest> mergeWithLocalRequests(List<LeaveRequest> remoteList) {
        List<LeaveRequest> localList = sharedPreferenceApp != null ? sharedPreferenceApp.getSubmittedLeaveRequests() : new ArrayList<>();
        List<LeaveRequest> result = new ArrayList<>(remoteList);

        for (LeaveRequest local : localList) {
            boolean matched = false;
            for (LeaveRequest remote : remoteList) {
                if ((local.getId() != null && local.getId().equals(remote.getId())) ||
                        (local.getTargetDate() != null && local.getTargetDate().equals(remote.getTargetDate()) && local.getType() == remote.getType())) {
                    matched = true;
                    if (remote.getStatus() != local.getStatus() && sharedPreferenceApp != null) {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatus(local.getTargetDate(), local.getType() != null ? local.getType().name() : null, remote.getStatus());
                    }
                    break;
                }
            }
            if (!matched) {
                result.add(local);
            }
        }

        result.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        return result;
    }

    private void applyFilters() {
        displayList.clear();

        ApprovalStatus targetStatus = null;
        switch (selectedStatusTab) {
            case 1:
                targetStatus = ApprovalStatus.PENDING;
                break;
            case 2:
                targetStatus = ApprovalStatus.APPROVED;
                break;
            case 3:
                targetStatus = ApprovalStatus.REJECTED;
                break;
            case 0:
            default:
                targetStatus = null; // Tất cả trạng thái
                break;
        }

        for (LeaveRequest req : allRequests) {
            // Lọc theo trạng thái
            if (targetStatus != null && req.getStatus() != targetStatus) {
                continue;
            }
            // Lọc theo loại đơn
            if (selectedTypeFilter != null && req.getType() != selectedTypeFilter) {
                continue;
            }
            displayList.add(req);
        }

        adapter.updateData(displayList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (displayList.isEmpty()) {
            binding.layoutEmptyState.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvRequests.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.layoutEmpty.setVisibility(View.GONE);
            binding.rvRequests.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
