package com.example.attendanceapp.data.remote;

import android.content.Context;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    private static final String TEST_OTP = "123456";
    private static final String TEST_REGISTERED_PHONE = "0987654321";
    private static final String PHONE_MANAGER = "0961474876";
    private static final String PHONE_EMPLOYEE = "0366494300";
    private static final String PASSWORD_MOCK = "111111";

    private final Context context;
    private final Map<String, JSONObject> mockDataMap = new HashMap<>();

    public MockInterceptor(Context context) {
        this.context = context.getApplicationContext();
        loadAllMockFiles();
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
                if (requestBody.contains(PHONE_MANAGER) && requestBody.contains(PASSWORD_MOCK)) {
                    responseJson = getMockData("mock_auth.json", "login_manager_success");
                } else if (requestBody.contains(PHONE_EMPLOYEE) && requestBody.contains(PASSWORD_MOCK)) {
                    responseJson = getMockData("mock_auth.json", "login_employee_success");
                } else if (requestBody.contains(TEST_REGISTERED_PHONE)) {
                    responseJson = getMockData("mock_auth.json", "login_employee_success");
                } else {
                    responseJson = getMockData("mock_auth.json", "login_unauthorized");
                    statusCode = 401;
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
                if (isManager(authHeader)) {
                    responseJson = getMockData("mock_manager.json", "profile_get_success");
                } else {
                    responseJson = getMockData("mock_staff.json", "profile_get_success");
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

            } else if (uri.endsWith("/profile") && isManager(authHeader)) {
                if ("GET".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_manager.json", "profile_get_success");
                } else if ("POST".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_manager.json", "profile_create_success");
                } else if ("PATCH".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_manager.json", "profile_update_success");
                }

            } else if (uri.endsWith("/otp") && "GET".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "get_otp_success");

            } else if (uri.endsWith("/work-schedules/change-request") && "PATCH".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "change_request_action_success");

            } else if (uri.contains("/work-schedules/change-request/") && "PATCH".equalsIgnoreCase(method) && isManager(authHeader)) {
                responseJson = getMockData("mock_manager.json", "change_request_action_success");

                // ==================== STAFF MODULE ====================
            } else if (uri.endsWith("/profile") || uri.endsWith("/me")) {
                if ("GET".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_staff.json", "profile_get_success");
                } else if ("POST".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_staff.json", "profile_create_success");
                } else if ("PATCH".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_staff.json", "profile_update_success");
                }

            } else if (uri.endsWith("/company/join") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_staff.json", "company_request_success");

            } else if (uri.endsWith("/work-schedules-requests") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_staff.json", "work_schedule_change_request_success");

            } else if (uri.endsWith("/leave-requests") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_staff.json", "leave_request_success");

                // ==================== WORK SCHEDULE MODULE ====================
            } else if (uri.endsWith("/me/work-schedules") && "GET".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_work_schedules.json", "staff_get_my_schedules");

            } else if (uri.contains("/manager/work-schedules")) {
                if ("GET".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_work_schedules.json", "manager_get_all_schedules");
                } else if ("POST".equalsIgnoreCase(method) && uri.contains("/assign")) {
                    responseJson = getMockData("mock_work_schedules.json", "assign_schedule_success");
                }

                // ==================== SHIFT MODULE ====================
            } else if (uri.contains("/shifts") || uri.contains("/shift")) {
                if ("GET".equalsIgnoreCase(method)) {
                    // Kiểm tra nếu URI có ID ở cuối (ví dụ: /shifts/shift_01)
                    String lastSegment = uri.substring(uri.lastIndexOf('/') + 1);
                    boolean isDetailRequest = !lastSegment.isEmpty()
                            && !"shifts".equalsIgnoreCase(lastSegment)
                            && !"shift".equalsIgnoreCase(lastSegment);

                    if (isDetailRequest) {
                        responseJson = getMockData("mock_shifts.json", "shift_detail");
                    } else {
                        responseJson = getMockData("mock_shifts.json", "list_shifts");
                    }
                } else if ("POST".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_shifts.json", "create_shift_success");
                } else if ("PUT".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_shifts.json", "update_shift_success");
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    responseJson = getMockData("mock_shifts.json", "delete_shift_success");
                }

                // ==================== COMMON MODULE ====================
            } else if (uri.endsWith("/uploads-avatar") && "POST".equalsIgnoreCase(method)) {
                responseJson = getMockData("mock_common.json", "upload_avatar_success");
            }

        } catch (JSONException e) {
            e.printStackTrace();
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

        return chain.proceed(request);
    }

    private boolean isManager(String authHeader) {
        return authHeader != null && (authHeader.contains("manager_access_token_string") || authHeader.contains("manager_access_token_refreshed"));
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