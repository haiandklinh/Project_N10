package com.example.attendanceapp.data.remote;

import android.content.Context;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Mock server chạy trong app.
 * - Auth / Manager / Staff / Common: trả dữ liệu tĩnh từ assets/mock/*.json
 * - Shift + Work schedule: CÓ TRẠNG THÁI (giữ danh sách trong bộ nhớ). Thêm/sửa/xóa ca và xếp ca
 *   sẽ thay đổi dữ liệu thật sự, các lần GET sau trả về dữ liệu đã thay đổi (giống backend thật).
 * Dữ liệu chỉ reset khi app bị tắt hẳn (MockInterceptor là @Singleton).
 */
public class MockInterceptor implements Interceptor {

    private static final String TEST_OTP = "123456";
    private static final String TEST_REGISTERED_PHONE = "0987654321";
    private static final String PHONE_MANAGER = "0961474876";
    private static final String PHONE_EMPLOYEE = "0366494300";
    private static final String PASSWORD_MOCK = "111111";

    private final Context context;
    private final Map<String, JSONObject> mockDataMap = new HashMap<>();

    // ===== State có trạng thái (Đã cập nhật để lưu vĩnh viễn) =====
    private final List<JSONObject> shifts = new ArrayList<>();
    private final List<JSONObject> schedules = new ArrayList<>();
    private final Map<String, JSONObject> profiles = new HashMap<>();

    private static final String PREF_NAME = "mock_server_state";
    private static final String KEY_SHIFTS = "persisted_shifts";
    private static final String KEY_SCHEDULES = "persisted_schedules";
    private static final String KEY_PROFILES = "persisted_profiles";

    private static class MockResult {
        final int code;
        final String body;

        MockResult(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }

    public MockInterceptor(Context context) {
        this.context = context.getApplicationContext();
        loadAllMockFiles();
        loadPersistedState();
    }

    private void loadPersistedState() {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String savedShifts = prefs.getString(KEY_SHIFTS, null);
        String savedSchedules = prefs.getString(KEY_SCHEDULES, null);
        String savedProfiles = prefs.getString(KEY_PROFILES, null);

        try {
            if (savedShifts != null) {
                JSONArray arr = new JSONArray(savedShifts);
                for (int i = 0; i < arr.length(); i++) shifts.add(arr.getJSONObject(i));
            } else {
                // Nếu chưa có trong Prefs thì nạp từ file JSON mặc định
                JSONObject shiftFile = mockDataMap.get("mock_shifts.json");
                if (shiftFile != null && shiftFile.has("list_shifts")) {
                    JSONArray arr = shiftFile.getJSONArray("list_shifts");
                    for (int i = 0; i < arr.length(); i++) shifts.add(new JSONObject(arr.getJSONObject(i).toString()));
                }
            }

            if (savedSchedules != null) {
                JSONArray arr = new JSONArray(savedSchedules);
                for (int i = 0; i < arr.length(); i++) schedules.add(arr.getJSONObject(i));
            } else {
                // Nạp mặc định
                JSONObject scheduleFile = mockDataMap.get("mock_work_schedules.json");
                if (scheduleFile != null && scheduleFile.has("manager_get_all_schedules")) {
                    String today = java.time.LocalDate.now().toString();
                    JSONArray arr = scheduleFile.getJSONArray("manager_get_all_schedules");
                    for (int i = 0; i < arr.length(); i++) {
                        String json = arr.getJSONObject(i).toString().replace("2026-04-01", today);
                        schedules.add(new JSONObject(json));
                    }
                }
            }

            if (savedProfiles != null) {
                JSONObject obj = new JSONObject(savedProfiles);
                java.util.Iterator<String> keys = obj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    profiles.put(key, obj.getJSONObject(key));
                }
            }
            
            // Đảm bảo luôn có profile cho 2 số mock chính nếu chưa có trong persistence
            if (!profiles.containsKey(PHONE_MANAGER)) {
                JSONObject managerProfile = getMockDataAsObject("mock_manager.json", "profile_get_success");
                if (managerProfile != null) profiles.put(PHONE_MANAGER, managerProfile);
            }
            if (!profiles.containsKey(PHONE_EMPLOYEE)) {
                JSONObject employeeProfile = getMockDataAsObject("mock_staff.json", "profile_get_success");
                if (employeeProfile != null) profiles.put(PHONE_EMPLOYEE, employeeProfile);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getMockDataAsObject(String fileName, String key) throws JSONException {
        JSONObject fileJson = mockDataMap.get(fileName);
        if (fileJson != null && fileJson.has(key)) {
            return fileJson.getJSONObject(key);
        }
        return null;
    }

    private synchronized void saveState() {
        try {
            JSONArray shiftArr = new JSONArray();
            for (JSONObject s : shifts) shiftArr.put(s);

            JSONArray scheduleArr = new JSONArray();
            for (JSONObject s : schedules) scheduleArr.put(s);

            JSONObject profilesObj = new JSONObject();
            for (Map.Entry<String, JSONObject> entry : profiles.entrySet()) {
                profilesObj.put(entry.getKey(), entry.getValue());
            }

            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_SHIFTS, shiftArr.toString())
                    .putString(KEY_SCHEDULES, scheduleArr.toString())
                    .putString(KEY_PROFILES, profilesObj.toString())
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAllMockFiles() {
        String[] fileNames = {
                "mock_auth.json",
                "mock_manager.json",
                "mock_staff.json",
                "mock_shifts.json",
                "mock_work_schedules.json",
                "mock_common.json",
        };

        for (String fileName : fileNames) {
            try (InputStream is = context.getAssets().open("mock/" + fileName)) {
                int size = is.available();
                byte[] buffer = new byte[size];
                int read = is.read(buffer);
                if (read > 0) {
                    String jsonStr = new String(buffer, StandardCharsets.UTF_8);
                    mockDataMap.put(fileName, new JSONObject(jsonStr));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Nạp dữ liệu ban đầu cho ca làm và lịch làm việc từ file JSON (chỉ 1 lần). */
    private void initStatefulData() {
        try {
            JSONObject shiftFile = mockDataMap.get("mock_shifts.json");
            if (shiftFile != null && shiftFile.has("list_shifts")) {
                JSONArray arr = shiftFile.getJSONArray("list_shifts");
                for (int i = 0; i < arr.length(); i++) {
                    shifts.add(new JSONObject(arr.getJSONObject(i).toString()));
                }
            }

            JSONObject scheduleFile = mockDataMap.get("mock_work_schedules.json");
            if (scheduleFile != null && scheduleFile.has("manager_get_all_schedules")) {
                String today = LocalDate.now().toString();
                JSONArray arr = scheduleFile.getJSONArray("manager_get_all_schedules");
                for (int i = 0; i < arr.length(); i++) {
                    // Đổi ngày mẫu thành hôm nay để dữ liệu không bị lọc mất
                    String json = arr.getJSONObject(i).toString().replace("2026-04-01", today);
                    schedules.add(new JSONObject(json));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String uri = request.url().uri().getPath();
        String method = request.method();
        String requestBody = bodyToString(request);
        String authHeader = request.header("Authorization");

        String responseJson = null;
        int statusCode = 200;

        try {
            // ==================== AUTH MODULE ====================
            if (uri.contains("/check-phone") && "POST".equalsIgnoreCase(method)) {
                boolean exists = requestBody.contains(TEST_REGISTERED_PHONE)
                        || requestBody.contains(PHONE_MANAGER)
                        || requestBody.contains(PHONE_EMPLOYEE);
                responseJson = getMockData("mock_auth.json", exists ? "check_phone_exist" : "check_phone_not_exist");

            } else if (uri.contains("/login") && "POST".equalsIgnoreCase(method)) {
                JSONObject loginReq = new JSONObject(requestBody);
                String phone = str(loginReq, "phone_number", "phone");
                String password = loginReq.optString("password");

                if (phone.equals(PHONE_MANAGER) && password.equals(PASSWORD_MOCK)) {
                    responseJson = getMockData("mock_auth.json", "login_manager_success");
                } else if (phone.equals(PHONE_EMPLOYEE) && password.equals(PASSWORD_MOCK)) {
                    responseJson = getMockData("mock_auth.json", "login_employee_success");
                } else if (phone.equals(TEST_REGISTERED_PHONE) && password.equals(PASSWORD_MOCK)) {
                    responseJson = getMockData("mock_auth.json", "login_employee_success");
                } else {
                    responseJson = getMockData("mock_auth.json", "login_unauthorized");
                    statusCode = 401;
                }

                // Inject phone vào token để interceptor sau nhận diện được
                if (statusCode == 200 && responseJson != null) {
                    JSONObject res = new JSONObject(responseJson);
                    String token = res.optString("access_token");
                    res.put("access_token", token + "_" + phone);
                    responseJson = res.toString();
                }

            } else if (uri.contains("/register/send-otp") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_auth.json", "register_send_otp_success");

            } else if (uri.contains("/register/verify-otp") && "POST".equalsIgnoreCase(method)) {
                if (requestBody.contains(TEST_OTP)) {
                    responseJson = getMockData("mock_auth.json", "register_verify_otp_success");
                } else {
                    responseJson = getMockData("mock_auth.json", "register_verify_otp_failed");
                    statusCode = 401;
                }

            } else if (uri.contains("/register") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_auth.json", "register_success");

            } else if (uri.contains("/forgot-password/send-otp") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_auth.json", "forgot_password_send_otp_success");

            } else if (uri.contains("/forgot-password/verify-otp") && "POST".equalsIgnoreCase(method)) {
                if (requestBody.contains(TEST_OTP)) {
                    responseJson = getMockData("mock_auth.json", "forgot_password_verify_otp_success");
                } else {
                    responseJson = getMockData("mock_auth.json", "forgot_password_verify_otp_failed");
                    statusCode = 401;
                }

            } else if (uri.contains("/forgot-password/reset") && "PATCH".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_auth.json", "forgot_password_reset_success");

            } else if (uri.contains("/refresh-token") && "POST".equalsIgnoreCase(method)) {
                if (requestBody.contains("manager_refresh_token")) {
                    responseJson = getMockData("mock_auth.json", "refresh_token_manager_success");
                } else {
                    responseJson = getMockData("mock_auth.json", "refresh_token_employee_success");
                }

            } else if (uri.contains("/logout") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_auth.json", "logout_success");

                // ==================== MANAGER MODULE ====================
            } else if (uri.endsWith("/me") && "GET".equalsIgnoreCase(method)) {
                String phone = getPhoneFromToken(authHeader);
                if (profiles.containsKey(phone)) {
                    responseJson = profiles.get(phone).toString();
                } else {
                    statusCode = 404;
                    responseJson = "{\"message\":\"Chưa có hồ sơ\"}";
                }

            } else if (uri.contains("/company/invitation-code") && isManager(authHeader)) {
                if ("GET".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_manager.json", "get_invitation_code_success");
                } else if ("POST".equalsIgnoreCase(method) && uri.endsWith("/regenerate")) {
                    responseJson = getMockData("mock_manager.json", "regenerate_invitation_code_success");
                }

            } else if (uri.endsWith("/company/join-requests") && isManager(authHeader)) {
                if ("GET".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_manager.json", "get_join_requests_success");
                }

            } else if (uri.contains("/company/join-requests/") && "PATCH".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "handle_join_request_success");

            } else if (uri.endsWith("/profile")) {
                String phone = getPhoneFromToken(authHeader);
                if ("GET".equalsIgnoreCase(method)) {
                    if (profiles.containsKey(phone)) {
                        responseJson = profiles.get(phone).toString();
                    } else {
                        statusCode = 404;
                        responseJson = "{\"message\":\"Chưa có hồ sơ\"}";
                    }
                } else if ("POST".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
                    JSONObject req = new JSONObject(requestBody);
                    JSONObject profile = profiles.get(phone);
                    if (profile == null) profile = new JSONObject();

                    // Cập nhật các trường từ request
                    if (req.has("fullName")) profile.put("fullName", req.getString("fullName"));
                    if (req.has("date")) profile.put("date", req.getString("date"));
                    if (req.has("gender")) profile.put("gender", req.getString("gender"));
                    if (req.has("avatar")) profile.put("avatar", req.getString("avatar"));
                    if (req.has("companyName")) profile.put("companyName", req.getString("companyName"));

                    // Mặc định status nếu tạo mới
                    if (!profile.has("status")) profile.put("status", "APPROVED");

                    profiles.put(phone, profile);
                    saveState();

                    JSONObject res = new JSONObject(profile.toString());
                    res.put("message", "Cập nhật hồ sơ thành công");
                    responseJson = res.toString();
                }

            } else if (uri.endsWith("/otp") && "GET".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "get_otp_success");

            } else if (uri.endsWith("/work-schedules/change-request") && "PATCH".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "change_request_action_success");

            } else if (uri.contains("/work-schedules/change-request/") && "PATCH".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "change_request_action_success");

                // ==================== STAFF MODULE ====================
                // Đã gộp /profile vào block chung phía trên
            } else if (uri.endsWith("/company/join") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_staff.json", "company_request_success");

            } else if (uri.endsWith("/work-schedules-requests") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_staff.json", "work_schedule_change_request_success");

            } else if (uri.endsWith("/leave-requests") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_staff.json", "leave_request_success");

                // ==================== WORK SCHEDULE MODULE (CÓ TRẠNG THÁI) ====================
            } else if (uri.endsWith("/me/work-schedules") && "GET".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_work_schedules.json", "staff_get_my_schedules");

            } else if (uri.contains("/manager/work-schedules")) {
                MockResult result = handleManagerSchedules(method, uri, requestBody);
                responseJson = result.body;
                statusCode = result.code;

                // ==================== SHIFT MODULE (CÓ TRẠNG THÁI) ====================
            } else if (uri.contains("/shifts") || uri.contains("/shift")) {
                MockResult result = handleShifts(method, uri, requestBody);
                responseJson = result.body;
                statusCode = result.code;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            responseJson = "{\"message\":\"Mock server error: " + e.getMessage() + "\"}";
            statusCode = 400;
        }

        if (responseJson != null) {
            return new Response.Builder()
                    .code(statusCode)
                    .message(statusCode == 200 ? "OK" : "Error")
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .body(ResponseBody.create(
                            responseJson,
                            MediaType.parse("application/json")
                    ))
                    .build();
        }

        // Khi mock được bật, KHÔNG cho request đi ra backend thật
        // Trả về 404 cho endpoint chưa được implement trong mock
        return new Response.Builder()
                .code(404)
                .message("Not Found in Mock")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(ResponseBody.create(
                        "{\"message\":\"Endpoint chưa được implement trong mock data: " + uri + "\"}",
                        MediaType.parse("application/json")
                ))
                .build();
    }

    // =====================================================================
    //                              SHIFTS
    // =====================================================================

    private synchronized MockResult handleShifts(String method, String uri, String body) throws JSONException {
        String lastSegment = uri.substring(uri.lastIndexOf('/') + 1);
        boolean hasId = !lastSegment.isEmpty()
                && !"shifts".equalsIgnoreCase(lastSegment)
                && !"shift".equalsIgnoreCase(lastSegment);

        if ("GET".equalsIgnoreCase(method)) {
            if (hasId) {
                JSONObject found = findShift(lastSegment);
                return found != null ? ok(found.toString()) : notFound("Không tìm thấy ca làm");
            }
            JSONArray arr = new JSONArray();
            for (JSONObject s : shifts) arr.put(s);
            return ok(arr.toString());

        } else if ("POST".equalsIgnoreCase(method)) {
            JSONObject req = new JSONObject(body);
            JSONObject created = new JSONObject();
            created.put("shiftId", "shift_" + UUID.randomUUID().toString().substring(0, 8));
            created.put("shiftName", str(req, "shiftName", "shift_name", "name"));
            created.put("startTime", str(req, "startTime", "start_time"));
            created.put("endTime", str(req, "endTime", "end_time"));
            shifts.add(created);
            saveState(); // Lưu trạng thái
            return ok(created.toString());

        } else if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            if (!hasId) return null_();
            JSONObject existing = findShift(lastSegment);
            if (existing == null) return notFound("Không tìm thấy ca làm");

            JSONObject req = new JSONObject(body);
            String name = str(req, "shiftName", "shift_name", "name");
            String start = str(req, "startTime", "start_time");
            String end = str(req, "endTime", "end_time");
            if (name != null) existing.put("shiftName", name);
            if (start != null) existing.put("startTime", start);
            if (end != null) existing.put("endTime", end);

            // Đồng bộ thông tin ca đang gắn trong các lịch làm việc (như backend thật khi join bảng)
            for (JSONObject schedule : schedules) {
                JSONObject s = schedule.optJSONObject("shift");
                if (s != null && lastSegment.equals(s.optString("shift_id"))) {
                    schedule.put("shift", toScheduleShift(existing));
                }
            }
            saveState(); // Lưu trạng thái
            return ok(existing.toString());

        } else if ("DELETE".equalsIgnoreCase(method)) {
            if (!hasId) return null_();
            JSONObject existing = findShift(lastSegment);
            if (existing == null) return notFound("Không tìm thấy ca làm");
            shifts.remove(existing);

            // Các lịch đang dùng ca này trở thành "chưa xếp"
            for (JSONObject schedule : schedules) {
                JSONObject s = schedule.optJSONObject("shift");
                if (s != null && lastSegment.equals(s.optString("shift_id"))) {
                    schedule.put("shift", JSONObject.NULL);
                }
            }
            saveState(); // Lưu trạng thái
            return ok("{\"message\":\"Xóa ca làm thành công\"}");
        }

        return null_();
    }

    private JSONObject findShift(String shiftId) {
        for (JSONObject s : shifts) {
            if (shiftId.equals(s.optString("shiftId"))) return s;
        }
        return null;
    }

    /** Chuyển ca (camelCase) sang dạng lồng trong lịch (snake_case). */
    private JSONObject toScheduleShift(JSONObject shift) throws JSONException {
        JSONObject info = new JSONObject();
        info.put("shift_id", shift.optString("shiftId"));
        info.put("shift_name", shift.optString("shiftName"));
        info.put("start_time", shift.optString("startTime"));
        info.put("end_time", shift.optString("endTime"));
        return info;
    }

    // =====================================================================
    //                         MANAGER WORK SCHEDULES
    // =====================================================================

    private synchronized MockResult handleManagerSchedules(String method, String uri, String body) throws JSONException {
        if ("GET".equalsIgnoreCase(method)) {
            JSONArray arr = new JSONArray();
            for (JSONObject s : schedules) arr.put(s);
            return ok(arr.toString());
        }

        if ("POST".equalsIgnoreCase(method) && uri.contains("/assign")) {
            JSONObject req = new JSONObject(body);
            // Lấy danh sách ID nhân viên (ưu tiên snake_case từ DTO)
            JSONArray employeeIds = req.optJSONArray("employee_ids");
            if (employeeIds == null) {
                employeeIds = req.optJSONArray("employeeIds");
            }

            // Hỗ trợ fallback nếu client gửi single ID thay vì array
            if (employeeIds == null) {
                String singleId = str(req, "employee_id", "employeeId");
                if (singleId != null) {
                    employeeIds = new JSONArray();
                    employeeIds.put(singleId);
                }
            }

            String shiftId = str(req, "shift_id", "shiftId");
            String workDate = str(req, "work_date", "workDate");

            if (employeeIds == null || employeeIds.length() == 0 || workDate == null) {
                return new MockResult(400, "{\"message\":\"Thiếu thông tin xếp ca\"}");
            }

            JSONObject shift = (shiftId != null) ? findShift(shiftId) : null;
            if (shiftId != null && shift == null) return notFound("Không tìm thấy ca làm");

            JSONArray results = new JSONArray();

            for (int i = 0; i < employeeIds.length(); i++) {
                String empId = employeeIds.getString(i);

                // Tìm thông tin nhân viên từ các bản ghi đã có để giữ tính nhất quán (tên, avatar, chức vụ)
                String avatar = "";
                String status = "ACTIVE";
                String name = null;
                String position = null;

                JSONObject sameDay = null;
                for (JSONObject s : schedules) {
                    if (empId.equals(s.optString("employee_id"))) {
                        avatar = s.optString("avatar", avatar);
                        status = s.optString("status", status);
                        if (name == null) name = s.optString("name", null);
                        if (position == null) position = s.optString("position", null);
                        if (workDate.equals(s.optString("work_date"))) sameDay = s;
                    }
                }

                JSONObject result;
                if (sameDay != null) {
                    // Cập nhật ca hoặc hủy ca
                    sameDay.put("shift", shift != null ? toScheduleShift(shift) : JSONObject.NULL);
                    result = sameDay;
                } else {
                    // Tạo bản ghi mới (có thể chưa xếp ca ngay từ đầu)
                    result = new JSONObject();
                    result.put("employee_id", empId);
                    result.put("name", name != null ? name : "Nhân viên " + empId);
                    result.put("position", position != null ? position : "Nhân viên");
                    result.put("work_date", workDate);
                    result.put("avatar", avatar);
                    result.put("status", status);
                    result.put("shift", shift != null ? toScheduleShift(shift) : JSONObject.NULL);
                    schedules.add(result);
                }
                results.put(result);
            }

            saveState();
            return ok(results.toString());
        }

        return null_();
    }

    // =====================================================================
    //                                HELPERS
    // =====================================================================

    private static MockResult ok(String body) {
        return new MockResult(200, body);
    }

    private static MockResult notFound(String message) {
        return new MockResult(404, "{\"message\":\"" + message + "\"}");
    }

    /** Không xử lý -> body null để interceptor cho request đi tiếp. */
    private static MockResult null_() {
        return new MockResult(200, null);
    }

    /** Lấy giá trị chuỗi đầu tiên tồn tại trong các key cho trước. */
    private static String str(JSONObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.isNull(key)) {
                return obj.optString(key, null);
            }
        }
        return null;
    }

    private boolean isManager(String authHeader) {
        return authHeader != null && authHeader.contains("manager_access_token");
    }

    private String getPhoneFromToken(String authHeader) {
        if (authHeader == null || !authHeader.contains("_")) return PHONE_MANAGER; // Fallback
        String[] parts = authHeader.split("_");
        return parts[parts.length - 1];
    }

    private String getMockData(String fileName, String key) throws JSONException {
        JSONObject fileJson = mockDataMap.get(fileName);
        if (fileJson != null && fileJson.has(key)) {
            Object data = fileJson.get(key);
            return data.toString();
        }
        return null;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final okio.Buffer buffer = new okio.Buffer();
            if (copy.body() != null) {
                copy.body().writeTo(buffer);
                return buffer.readUtf8();
            }
            return "";
        } catch (final IOException e) {
            return "";
        }
    }
}
