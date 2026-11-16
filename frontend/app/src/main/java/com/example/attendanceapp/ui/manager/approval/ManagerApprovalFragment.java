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
import com.example.attendanceapp.data.dto.response.BaseListResponse;
import com.example.attendanceapp.data.dto.response.manager.NotificationItemResponse;
import com.example.attendanceapp.data.local.HandledRequestManager;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.domain.model.LeaveRequest;
import com.example.attendanceapp.databinding.FragmentManagerApprovalBinding;
import com.example.attendanceapp.domain.model.ApprovalRequestType;
import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerApprovalItem;
import com.example.attendanceapp.domain.model.EmployeeWorkSchedule;
import com.example.attendanceapp.domain.repository.ManagerRepository;
import com.example.attendanceapp.domain.repository.NotificationRepository;
import com.example.attendanceapp.domain.repository.ScheduleRepository;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
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

    @Inject
    ScheduleRepository scheduleRepository;

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

    /**
     * Phân giải trạng thái duyệt từ dữ liệu server (status / content.status / notification_type).
     * Trả về null nếu server không cung cấp thông tin đủ để kết luận.
     */
    private ApprovalStatus resolveServerApprovalStatus(NotificationItemResponse item) {
        if (item == null) return null;
        String typeStr = item.getNotificationType() != null ? item.getNotificationType().toUpperCase(Locale.US) : "";
        Map<String, Object> content = item.getContent();

        // 1. content.status: backend thường trả kết quả duyệt trong payload content
        if (content != null && content.containsKey("status")) {
            Object val = content.get("status");
            if (val instanceof Boolean) {
                return ((Boolean) val) ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
            }
            if (val != null) {
                String s = val.toString().trim().toLowerCase(Locale.US);
                if ("true".equals(s) || "approved".equals(s) || "accept".equals(s) || "dong_y".equals(s)) {
                    return ApprovalStatus.APPROVED;
                }
                if ("false".equals(s) || "rejected".equals(s) || "reject".equals(s) || "tu_choi".equals(s)) {
                    return ApprovalStatus.REJECTED;
                }
                if ("pending".equals(s) || "cho_duyet".equals(s) || "active".equals(s)) {
                    return ApprovalStatus.PENDING;
                }
            }
        }

        // 2. status top-level của notification (backend có thể dùng active/deactive/...)
        String st = item.getStatus() != null ? item.getStatus().trim().toLowerCase(Locale.US) : "";
        if (!st.isEmpty()) {
            if (st.contains("approv") || st.contains("accept") || st.equals("deactive")
                    || st.equals("inactive") || st.equals("handled") || st.equals("done")) {
                return ApprovalStatus.APPROVED;
            }
            if (st.contains("reject") || st.contains("refuse") || st.contains("declin")) {
                return ApprovalStatus.REJECTED;
            }
            if (st.contains("pend") || st.equals("active") || st.equals("new")) {
                return ApprovalStatus.PENDING;
            }
        }

        // 3. Suy ra từ notification_type (APPROVED/REJECTED/RESPONSE)
        if (typeStr.contains("APPROVED") || typeStr.contains("_ACCEPT")) {
            return ApprovalStatus.APPROVED;
        }
        if (typeStr.contains("REJECTED") || typeStr.contains("_REFUSE")) {
            return ApprovalStatus.REJECTED;
        }
        if (typeStr.endsWith("_RESPONSE") || typeStr.contains("RESPONSE")) {
            String message = content != null && content.containsKey("message")
                    ? String.valueOf(content.get("message")).toLowerCase(Locale.US) : "";
            if (message.contains("từ chối") || message.contains("không chấp thuận") || message.contains("reject")) {
                return ApprovalStatus.REJECTED;
            }
            return ApprovalStatus.APPROVED;
        }
        return null;
    }

    private String extractWorkDay(Map<String, Object> content, String sendAt) {
        String workDay = "";
        if (content != null) {
            if (content.containsKey("work_day_off")) workDay = String.valueOf(content.get("work_day_off"));
            else if (content.containsKey("workday_off")) workDay = String.valueOf(content.get("workday_off"));
            else if (content.containsKey("workday")) workDay = String.valueOf(content.get("workday"));
            else if (content.containsKey("work_day")) workDay = String.valueOf(content.get("work_day"));
            else if (content.containsKey("date")) workDay = String.valueOf(content.get("date"));
            else if (content.containsKey("day_off")) workDay = String.valueOf(content.get("day_off"));
        }
        if (workDay.isEmpty()) {
            workDay = (sendAt != null && sendAt.length() >= 10) ? sendAt.substring(0, 10) : "";
        } else if (workDay.length() >= 10) {
            workDay = workDay.substring(0, 10);
        }
        return workDay;
    }

    private String extractCurShift(Map<String, Object> content) {
        if (content == null) return "";
        if (content.containsKey("id_work_shift_off")) return String.valueOf(content.get("id_work_shift_off"));
        if (content.containsKey("work_shift_off_id")) return String.valueOf(content.get("work_shift_off_id"));
        if (content.containsKey("id_work_shift_cur")) return String.valueOf(content.get("id_work_shift_cur"));
        if (content.containsKey("id_work_shift_current")) return String.valueOf(content.get("id_work_shift_current"));
        if (content.containsKey("current_shift_id")) return String.valueOf(content.get("current_shift_id"));
        if (content.containsKey("id_work_shift")) return String.valueOf(content.get("id_work_shift"));
        if (content.containsKey("id_shift")) return String.valueOf(content.get("id_shift"));
        return "";
    }

    private void loadApprovalRequests() {
        binding.swipeRefreshLayout.setRefreshing(true);

        Single<BaseListResponse<NotificationItemResponse>> notifSingle = managerRepository.getNotifications()
                .onErrorReturnItem(new BaseListResponse<>(new ArrayList<>()))
                .subscribeOn(Schedulers.io());

        Single<List<EmployeeWorkSchedule>> scheduleSingle = scheduleRepository.getEmployeeWorkSchedules()
                .onErrorReturnItem(new ArrayList<>())
                .subscribeOn(Schedulers.io());

        disposables.add(
                Single.zip(notifSingle, scheduleSingle, (response, scheduleList) -> {
                    // 1. Phân tích trạng thái lịch làm việc từ server
                    Map<String, String> scheduleStatusByStaffDate = new HashMap<>();
                    if (scheduleList != null) {
                        for (EmployeeWorkSchedule sch : scheduleList) {
                            if (sch != null && sch.getEmployeeId() != null && sch.getWorkDate() != null) {
                                String key = sch.getEmployeeId() + "_" + sch.getWorkDate();
                                if (sch.getStatus() != null) {
                                    scheduleStatusByStaffDate.put(key, sch.getStatus().toLowerCase(Locale.US));
                                }
                            }
                        }
                    }

                    // 2. Quét qua toàn bộ thông báo để tìm các thông báo phản hồi (RESPONSE / APPROVED / REJECTED)
                    Map<String, ApprovalStatus> responseStatusMap = new HashMap<>();
                    if (response != null && response.getData() != null) {
                        for (NotificationItemResponse item : response.getData()) {
                            if (item == null) continue;
                            ApprovalStatus inferred = resolveServerApprovalStatus(item);
                            if (inferred != null && inferred != ApprovalStatus.PENDING) {
                                String sId = item.getStaffId();
                                Map<String, Object> c = item.getContent();
                                String wDay = extractWorkDay(c, item.getSendAt());
                                String sShift = extractCurShift(c);
                                if (sId != null && !wDay.isEmpty()) {
                                    responseStatusMap.put(sId + "_" + wDay, inferred);
                                    if (!sShift.isEmpty()) {
                                        responseStatusMap.put(sId + "_" + wDay + "_" + sShift, inferred);
                                    }
                                }
                            }
                        }
                    }

                    LocalDate today = LocalDate.now();
                    Map<String, ManagerApprovalItem> deduplicatedItems = new LinkedHashMap<>();

                    if (response != null && response.getData() != null) {
                        for (NotificationItemResponse item : response.getData()) {
                            if (item == null) continue;
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
                                continue;
                            }

                            String senderName = item.getSendPerson() != null ? item.getSendPerson() : "Nhân sự";
                            Map<String, Object> content = item.getContent();
                            String curShift = extractCurShift(content);
                            if (curShift.isEmpty()) curShift = "shift_01";
                            String tgtShift = "shift_02";
                            if (content != null && content.containsKey("id_work_shift_change")) {
                                tgtShift = String.valueOf(content.get("id_work_shift_change"));
                            }
                            String workDay = extractWorkDay(content, item.getSendAt());
                            if (workDay.isEmpty()) {
                                workDay = today.toString();
                            }

                            String shiftName = "";
                            String targetShiftName = "";
                            String reasonText = detail;
                            if (content != null) {
                                if (content.containsKey("shift_name")) shiftName = String.valueOf(content.get("shift_name"));
                                else if (content.containsKey("name_shift")) shiftName = String.valueOf(content.get("name_shift"));
                                else if (content.containsKey("name_work_shift_off")) shiftName = String.valueOf(content.get("name_work_shift_off"));
                                else if (content.containsKey("name_shift_cur")) shiftName = String.valueOf(content.get("name_shift_cur"));

                                if (content.containsKey("name_shift_change")) targetShiftName = String.valueOf(content.get("name_shift_change"));

                                if (content.containsKey("reason")) reasonText = String.valueOf(content.get("reason"));
                                else if (content.containsKey("message")) reasonText = String.valueOf(content.get("message"));
                            }

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

                            // Phân giải trạng thái thông minh:
                            String requestKey = type.name() + "_" + (staffId != null ? staffId : "") + "_" + workDay + "_" + curShift;
                            ApprovalStatus serverStatus = resolveServerApprovalStatus(item);
                            ApprovalStatus savedStatus = handledRequestManager.getApprovalStatus(requestKey);
                            if (savedStatus == null) {
                                savedStatus = handledRequestManager.getApprovalStatus(type.name() + "_" + (staffId != null ? staffId : "") + "_" + workDay);
                            }

                            // 1. Kiểm tra ngày trong quá khứ
                            boolean isPastDate = false;
                            try {
                                LocalDate target = LocalDate.parse(workDay);
                                if (target.isBefore(today)) {
                                    isPastDate = true;
                                }
                            } catch (Exception ignored) {}

                            // 2. Kiểm tra thông báo phản hồi cặp đôi
                            ApprovalStatus pairedStatus = responseStatusMap.get((staffId != null ? staffId : "") + "_" + workDay + "_" + curShift);
                            if (pairedStatus == null) {
                                pairedStatus = responseStatusMap.get((staffId != null ? staffId : "") + "_" + workDay);
                            }

                            // 3. Kiểm tra trạng thái lịch làm việc thực tế từ backend
                            boolean scheduleIndicatesApproved = false;
                            if (staffId != null) {
                                String schSt = scheduleStatusByStaffDate.get(staffId + "_" + workDay);
                                if (schSt != null && (schSt.contains("leave") || schSt.contains("off") || schSt.contains("nghi"))) {
                                    scheduleIndicatesApproved = true;
                                }
                            }

                            ApprovalStatus itemStatus;
                            if (serverStatus != null && serverStatus != ApprovalStatus.PENDING) {
                                itemStatus = serverStatus;
                            } else if (pairedStatus != null) {
                                itemStatus = pairedStatus;
                            } else if (savedStatus != null) {
                                itemStatus = savedStatus;
                            } else if (scheduleIndicatesApproved) {
                                itemStatus = ApprovalStatus.APPROVED;
                            } else if (isPastDate) {
                                // Các đơn có ngày xin nghỉ trong quá khứ không thể là "Chờ duyệt"
                                itemStatus = ApprovalStatus.APPROVED;
                            } else {
                                itemStatus = ApprovalStatus.PENDING;
                            }

                            ManagerApprovalItem approvalItem = new ManagerApprovalItem(
                                    staffId != null ? staffId : "",
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

                            // Khử trùng lặp theo mã yêu cầu
                            String dedupeKey = type.name() + "_" + (staffId != null ? staffId : "") + "_" + workDay + "_" + curShift;
                            ManagerApprovalItem existing = deduplicatedItems.get(dedupeKey);
                            if (existing == null) {
                                deduplicatedItems.put(dedupeKey, approvalItem);
                            } else {
                                // Nếu đơn đã có trạng thái đã xử lý thì ưu tiên giữ trạng thái đã xử lý
                                if (existing.getStatus() == ApprovalStatus.PENDING && itemStatus != ApprovalStatus.PENDING) {
                                    deduplicatedItems.put(dedupeKey, approvalItem);
                                }
                            }
                        }
                    }

                    return new ArrayList<>(deduplicatedItems.values());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        items -> {
                            allRequests.clear();
                            allRequests.addAll(items);
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
                submitHandleRequest(item, true);
            }

            @Override
            public void onReject(ManagerApprovalItem item, int position) {
                submitHandleRequest(item, false);
            }

            private void submitHandleRequest(ManagerApprovalItem item, boolean approved) {
                String curShift = item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "";
                String tgtShift = item.getTargetShiftId() != null ? item.getTargetShiftId() : "";
                String workDay = item.getWorkDay() != null ? item.getWorkDay() : LocalDate.now().toString();

                io.reactivex.rxjava3.core.Completable call;
                if (item.getType() == ApprovalRequestType.SWAP) {
                    call = managerRepository.handleShiftChange(
                            new HandleShiftChangeRequest(item.getId(), curShift, tgtShift, workDay, approved,
                                    approved ? "Đồng ý đổi ca" : "Từ chối đổi ca"));
                } else if (item.getType() == ApprovalRequestType.LEAVE) {
                    call = managerRepository.handleLeaveRequest(
                            new HandleLeaveRequest(item.getId(), curShift, workDay, approved,
                                    approved ? "Đồng ý phê duyệt" : "Từ chối phê duyệt"));
                } else {
                    Toast.makeText(getContext(), "Loại yêu cầu này chưa hỗ trợ xử lý từ xa", Toast.LENGTH_SHORT).show();
                    return;
                }

                disposables.add(call
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> applyHandledResult(item, approved),
                                err -> {
                                    String msg = err != null && err.getMessage() != null ? err.getMessage() : "Lỗi không xác định";
                                    Toast.makeText(getContext(), "Xử lý thất bại: " + msg, Toast.LENGTH_LONG).show();
                                    loadApprovalRequests();
                                }
                        ));
            }

            private void applyHandledResult(ManagerApprovalItem item, boolean approved) {
                ApprovalStatus newStatus = approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
                item.setStatus(newStatus);

                String workDay = item.getWorkDay() != null ? item.getWorkDay() : LocalDate.now().toString();
                String requestKey = item.getType().name() + "_" + item.getId() + "_" + workDay + "_" + (item.getCurrentShiftId() != null ? item.getCurrentShiftId() : "");
                handledRequestManager.setApprovalStatus(requestKey, newStatus);
                if (approved && item.getType() == ApprovalRequestType.LEAVE) {
                    handledRequestManager.saveStaffLeave(item.getId(), workDay);
                }

                if (sharedPreferenceApp != null) {
                    if (item.getRequestId() != null && !item.getRequestId().isEmpty()) {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatusById(item.getRequestId(), newStatus);
                    } else {
                        sharedPreferenceApp.updateSubmittedLeaveRequestStatus(workDay, item.getType().name(), newStatus);
                    }
                }

                disposables.add(notificationRepository.deleteNotificationsByStaffId(item.getId())
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {}, err -> {}));

                Toast.makeText(getContext(), (approved ? "Đã duyệt yêu cầu của " : "Đã từ chối yêu cầu của ") + item.getEmployeeName(), Toast.LENGTH_SHORT).show();
                updatePendingCount();
                applyFilters();
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
