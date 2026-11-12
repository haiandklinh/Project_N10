package com.example.attendanceapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.attendanceapp.domain.model.ApprovalStatus;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

@Singleton
public class HandledRequestManager {

    private static final String PREF_NAME = "handled_requests_pref";
    private static final String KEY_HANDLED_IDS = "handled_request_ids";
    private static final String KEY_APPROVED_STAFFS = "approved_staffs_json";

    private final SharedPreferences prefs;
    private final BehaviorSubject<Integer> pendingRequestsChangedSubject = BehaviorSubject.createDefault(0);
    private final BehaviorSubject<List<ManagerEmployeeItem>> employeesSubject = BehaviorSubject.create();

    @Inject
    public HandledRequestManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.employeesSubject.onNext(getApprovedEmployees());
    }

    public Observable<Integer> observePendingRequestsChanged() {
        return pendingRequestsChangedSubject.hide();
    }

    public Observable<List<ManagerEmployeeItem>> observeEmployees() {
        return employeesSubject.hide();
    }

    /**
     * Kiểm tra xem yêu cầu đã được xử lý (Duyệt hoặc Từ chối) trước đó chưa
     */
    public boolean isHandled(String id) {
        if (id == null || id.isEmpty()) return false;
        Set<String> set = prefs.getStringSet(KEY_HANDLED_IDS, new HashSet<>());
        return set.contains(id);
    }

    /**
     * Đánh dấu yêu cầu đã được xử lý (tránh hiện lại)
     */
    public void markHandled(String id) {
        if (id == null || id.isEmpty()) return;
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_HANDLED_IDS, new HashSet<>()));
        set.add(id);
        prefs.edit().putStringSet(KEY_HANDLED_IDS, set).apply();
        pendingRequestsChangedSubject.onNext(set.size());
    }  

    /** Xóa toàn bộ danh sách ID đã xử lý (được gọi khi một request đã được duyệt/từ chối) */
    public void clearHandled() {
        prefs.edit().remove(KEY_HANDLED_IDS).apply();
        pendingRequestsChangedSubject.onNext(0);
    }

    private static final String KEY_APPROVAL_STATUS_PREFIX = "appr_status_";

    public void setApprovalStatus(String requestKey, ApprovalStatus status) {
        if (requestKey == null || status == null) return;
        prefs.edit().putString(KEY_APPROVAL_STATUS_PREFIX + requestKey, status.name()).apply();
    }

    @androidx.annotation.Nullable
    public ApprovalStatus getApprovalStatus(String requestKey) {
        if (requestKey == null) return null;
        String val = prefs.getString(KEY_APPROVAL_STATUS_PREFIX + requestKey, null);
        if (val == null) return null;
        try {
            return ApprovalStatus.valueOf(val);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lưu ảnh đại diện của nhân viên vào bộ nhớ
     */
    public void saveStaffAvatar(String staffId, String avatar) {
        if (staffId == null || avatar == null || avatar.trim().isEmpty()) return;
        String cleanAvatar = avatar.trim();
        if ("null".equalsIgnoreCase(cleanAvatar) || "none".equalsIgnoreCase(cleanAvatar)) return;
        prefs.edit().putString("staff_avatar_" + staffId, cleanAvatar).apply();
        List<ManagerEmployeeItem> list = getApprovedEmployees();
        boolean found = false;
        for (ManagerEmployeeItem item : list) {
            if (staffId.equals(item.getUserId())) {
                item.setAvatarUrl(cleanAvatar);
                found = true;
                break;
            }
        }
        if (found) {
            saveApprovedEmployees(list);
        }
    }

    public String getStaffAvatar(String staffId) {
        if (staffId == null) return "";
        String saved = prefs.getString("staff_avatar_" + staffId, "");
        if (!saved.isEmpty()) return saved;
        for (ManagerEmployeeItem item : getApprovedEmployees()) {
            if (staffId.equals(item.getUserId())) {
                return item.getAvatarUrl();
            }
        }
        return "";
    }

    /**
     * Lưu SĐT nhân viên vào bộ nhớ
     */
    public void saveStaffPhone(String staffId, String phone) {
        if (staffId == null || phone == null) return;
        prefs.edit().putString("staff_phone_" + staffId, phone.trim()).apply();
        List<ManagerEmployeeItem> list = getApprovedEmployees();
        boolean found = false;
        for (ManagerEmployeeItem item : list) {
            if (staffId.equals(item.getUserId())) {
                item.setPhoneNumber(phone.trim());
                found = true;
                break;
            }
        }
        if (found) {
            saveApprovedEmployees(list);
        }
    }

    public String getStaffPhone(String staffId) {
        if (staffId == null) return "";
        return prefs.getString("staff_phone_" + staffId, "");
    }

    /**
     * Lưu ngày vào làm của nhân viên
     */
    public void saveStaffJoinDate(String staffId, String joinDate) {
        if (staffId == null || joinDate == null) return;
        prefs.edit().putString("staff_join_date_" + staffId, joinDate.trim()).apply();
        List<ManagerEmployeeItem> list = getApprovedEmployees();
        boolean found = false;
        for (ManagerEmployeeItem item : list) {
            if (staffId.equals(item.getUserId())) {
                item.setJoinDateLabel(joinDate.trim());
                found = true;
                break;
            }
        }
        if (found) {
            saveApprovedEmployees(list);
        }
    }

    public String getStaffJoinDate(String staffId) {
        if (staffId == null) return "";
        return prefs.getString("staff_join_date_" + staffId, "");
    }

    /**
     * Cập nhật trạng thái nhân viên (Làm việc, Nghỉ phép, Nghỉ việc)
     */
    public void updateEmployeeStatus(String staffId, EmployeeStatus newStatus) {
        if (staffId == null || newStatus == null) return;
        prefs.edit().putString("staff_status_" + staffId, newStatus.name()).apply();
        List<ManagerEmployeeItem> list = getApprovedEmployees();
        boolean found = false;
        for (ManagerEmployeeItem item : list) {
            if (staffId.equals(item.getUserId())) {
                item.setStatus(newStatus);
                found = true;
                break;
            }
        }
        if (found) {
            saveApprovedEmployees(list);
        }
        employeesSubject.onNext(getApprovedEmployees());
    }

    public EmployeeStatus getEmployeeStatus(String staffId) {
        if (staffId == null) return null;
        String name = prefs.getString("staff_status_" + staffId, null);
        if (name != null) {
            try {
                return EmployeeStatus.valueOf(name);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Lưu ngày nghỉ phép của nhân viên khi Quản lý phê duyệt đơn nghỉ phép
     */
    public void saveStaffLeave(String staffId, String date) {
        if (staffId == null || date == null || date.trim().isEmpty()) return;
        String cleanDate = date.trim();
        if (cleanDate.length() >= 10) {
            cleanDate = cleanDate.substring(0, 10);
        }

        // 1. Lưu ngày nghỉ vào set
        String key = "staff_leaves_" + staffId;
        Set<String> set = new HashSet<>(prefs.getStringSet(key, new HashSet<>()));
        set.add(cleanDate);
        prefs.edit().putStringSet(key, set).apply();

        // 2. Tăng số ngày nghỉ trong tháng và cập nhật trạng thái nhân viên
        List<ManagerEmployeeItem> list = getApprovedEmployees();
        boolean found = false;
        for (ManagerEmployeeItem item : list) {
            if (staffId.equals(item.getUserId())) {
                item.setLeaveCountThisMonth(item.getLeaveCountThisMonth() + 1);
                item.setStatus(EmployeeStatus.ON_LEAVE);
                found = true;
                break;
            }
        }
        if (found) {
            saveApprovedEmployees(list);
        }
        updateEmployeeStatus(staffId, EmployeeStatus.ON_LEAVE);
    }

    /**
     * Kiểm tra nhân viên có được duyệt nghỉ phép vào ngày chỉ định hay không
     */
    public boolean isStaffOnLeave(String staffId, String date) {
        if (staffId == null || date == null || date.trim().isEmpty()) return false;
        String cleanDate = date.trim();
        if (cleanDate.length() >= 10) {
            cleanDate = cleanDate.substring(0, 10);
        }
        Set<String> set = prefs.getStringSet("staff_leaves_" + staffId, new HashSet<>());
        return set.contains(cleanDate);
    }

    /**
     * Khi Quản lý duyệt nhân viên mới vào công ty:
     * Lưu thông tin nhân viên vào bộ nhớ để hiển thị ở tab Nhân sự và đếm tổng số nhân viên ở Home
     */
    public void addApprovedEmployee(String staffId, String fullName, String avatar, String position, String phoneNumber) {
        markHandled(staffId);

        List<ManagerEmployeeItem> list = getApprovedEmployees();
        // Kiểm tra xem đã có chưa
        for (ManagerEmployeeItem item : list) {
            if (item.getUserId() != null && item.getUserId().equals(staffId)) return;
        }

        int codeNumber = list.size() + 1;
        String employeeCode = "NV" + String.format("%03d", codeNumber);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String pos = (position != null && !position.isEmpty()) ? position : "Nhân viên";
        String phone = (phoneNumber != null && !phoneNumber.trim().isEmpty())
                ? phoneNumber.trim()
                : getStaffPhone(staffId);

        String cleanAvatar = avatar;
        if (cleanAvatar == null || cleanAvatar.trim().isEmpty() || "null".equalsIgnoreCase(cleanAvatar) || "none".equalsIgnoreCase(cleanAvatar)) {
            cleanAvatar = getStaffAvatar(staffId);
        }

        if (cleanAvatar != null && !cleanAvatar.trim().isEmpty()) {
            saveStaffAvatar(staffId, cleanAvatar.trim());
        }

        if (!phone.isEmpty()) {
            saveStaffPhone(staffId, phone);
        }
        saveStaffJoinDate(staffId, today);

        ManagerEmployeeItem newItem = new ManagerEmployeeItem(
                staffId,
                employeeCode,
                fullName != null ? fullName : "Nhân viên mới",
                cleanAvatar != null ? cleanAvatar : "",
                pos,
                "Toàn thời gian",
                phone,
                today,
                EmployeeStatus.ACTIVE,
                0,
                0,
                0
        );

        list.add(newItem);
        saveApprovedEmployees(list);
        employeesSubject.onNext(list);
    }

    public void addApprovedEmployee(String staffId, String fullName, String avatar, String position) {
        addApprovedEmployee(staffId, fullName, avatar, position, "");
    }

    public List<ManagerEmployeeItem> getApprovedEmployees() {
        List<ManagerEmployeeItem> result = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_APPROVED_STAFFS, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.getString("id");

                String phone = obj.optString("phoneNumber", "");
                if (phone.isEmpty()) {
                    phone = prefs.getString("staff_phone_" + id, "");
                }

                String startDate = obj.optString("startDate", "");
                if (startDate.isEmpty()) {
                    startDate = prefs.getString("staff_join_date_" + id, "");
                }

                String avatarUrl = obj.optString("avatarUrl", "");
                if (avatarUrl.isEmpty() || "null".equalsIgnoreCase(avatarUrl) || "none".equalsIgnoreCase(avatarUrl)) {
                    avatarUrl = prefs.getString("staff_avatar_" + id, "");
                }

                String statusStr = obj.optString("status", "ACTIVE");
                String customStatus = prefs.getString("staff_status_" + id, null);
                if (customStatus != null) {
                    statusStr = customStatus;
                }
                EmployeeStatus status = EmployeeStatus.ACTIVE;
                try {
                    status = EmployeeStatus.valueOf(statusStr);
                } catch (Exception ignored) {
                }

                result.add(new ManagerEmployeeItem(
                        id,
                        obj.getString("employeeCode"),
                        obj.getString("fullName"),
                        avatarUrl,
                        obj.getString("position"),
                        obj.optString("department", "Toàn thời gian"),
                        phone,
                        startDate,
                        status,
                        obj.optInt("workDaysCount", 0),
                        obj.optInt("leaveDaysCount", 0),
                        obj.optLong("baseSalary", 0)
                ));
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    private void saveApprovedEmployees(List<ManagerEmployeeItem> list) {
        try {
            JSONArray arr = new JSONArray();
            for (ManagerEmployeeItem item : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.getUserId());
                obj.put("employeeCode", item.getEmployeeCode());
                obj.put("fullName", item.getFullName());
                obj.put("avatarUrl", item.getAvatarUrl());
                obj.put("position", item.getPosition());
                obj.put("department", item.getDepartment());
                obj.put("phoneNumber", item.getPhoneNumber());
                obj.put("startDate", item.getJoinDateLabel());
                obj.put("status", item.getStatus().name());
                obj.put("workDaysCount", item.getShiftCountThisMonth());
                obj.put("leaveDaysCount", item.getLeaveCountThisMonth());
                obj.put("baseSalary", item.getEstimatedSalaryThisMonth());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_APPROVED_STAFFS, arr.toString()).apply();
        } catch (Exception e) {
            // ignore
        }
    }

    public int getApprovedEmployeeCount() {
        return getApprovedEmployees().size();
    }

    /**
     * Xóa sạch dữ liệu cache cục bộ khi Quản lý đăng xuất hoặc chuyển tài khoản
     */
    public void clear() {
        prefs.edit()
                .remove(KEY_HANDLED_IDS)
                .remove(KEY_APPROVED_STAFFS)
                .apply();
        pendingRequestsChangedSubject.onNext(0);
        employeesSubject.onNext(new ArrayList<>());
    }
}
