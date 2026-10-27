package com.example.attendanceapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

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

    /**
     * Khi Quản lý duyệt nhân viên mới vào công ty:
     * Lưu thông tin nhân viên vào bộ nhớ để hiển thị ở tab Nhân sự và đếm tổng số nhân viên ở Home
     */
    public void addApprovedEmployee(String staffId, String fullName, String avatar, String position) {
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

        ManagerEmployeeItem newItem = new ManagerEmployeeItem(
                staffId,
                employeeCode,
                fullName != null ? fullName : "Nhân viên mới",
                avatar,
                pos,
                "Toàn thời gian",
                "",
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

    public List<ManagerEmployeeItem> getApprovedEmployees() {
        List<ManagerEmployeeItem> result = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_APPROVED_STAFFS, "[]");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                result.add(new ManagerEmployeeItem(
                        obj.getString("id"),
                        obj.getString("employeeCode"),
                        obj.getString("fullName"),
                        obj.optString("avatarUrl", null),
                        obj.getString("position"),
                        obj.optString("department", "Toàn thời gian"),
                        obj.optString("phoneNumber", ""),
                        obj.optString("startDate", ""),
                        EmployeeStatus.valueOf(obj.optString("status", "ACTIVE")),
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
