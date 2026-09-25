package com.example.attendanceapp.data.remote;

import android.content.Context;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MockInterceptor implements Interceptor {

    // Test data cố định để giả lập các nhánh thành công/thất bại
    private static final String TEST_OTP = "123456";
    private static final String TEST_REGISTERED_PHONE = "0987654321"; // SĐT giả lập đã tồn tại
    private static final String TEST_VALID_PASSWORD = "123456"; // Mật khẩu đúng cho login test

    private final Context context;
    private JSONObject mockDataJsonObject;

    public MockInterceptor(Context context) {
        this.context = context.getApplicationContext();
        loadMockDataFile("mock/mock_data_auth.json");
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        String uri = request.url().uri().getPath();
        String requestBody = bodyToString(request);

        String responseJson = null;
        int statusCode = 200;

        if (mockDataJsonObject != null) {
            try {
                if (uri.endsWith("/check-phone")) {
                    boolean exists = requestBody.contains(TEST_REGISTERED_PHONE);
                    responseJson = getResponseData(exists ? "check_phone_exist" : "check_phone_not_exist");

                } else if (uri.endsWith("/login")) {
                    boolean valid = requestBody.contains(TEST_VALID_PASSWORD);
                    if (valid) {
                        responseJson = getResponseData("login_success");
                    } else {
                        responseJson = getResponseData("login_unauthorized");
                        statusCode = 401;
                    }

                } else if (uri.endsWith("/register/send-otp")) {
                    responseJson = getResponseData("register_send_otp_success");

                } else if (uri.endsWith("/register/verify-otp")) {
                    if (requestBody.contains(TEST_OTP)) {
                        responseJson = getResponseData("register_verify_otp_success");
                    } else {
                        responseJson = getResponseData("register_verify_otp_failed");
                        statusCode = 401;
                    }

                } else if (uri.endsWith("/register")) {
                    responseJson = getResponseData("register_success");

                } else if (uri.endsWith("/forgot-password/send-otp")) {
                    responseJson = getResponseData("forgot_password_send_otp_success");

                } else if (uri.endsWith("/forgot-password/verify-otp")) {
                    if (requestBody.contains(TEST_OTP)) {
                        responseJson = getResponseData("forgot_password_verify_otp_success");
                    } else {
                        responseJson = getResponseData("forgot_password_verify_otp_failed");
                        statusCode = 401;
                    }

                } else if (uri.endsWith("/forgot-password/reset")) {
                    responseJson = getResponseData("forgot_password_reset_success");

                } else if (uri.endsWith("/profile")) {
                    responseJson = getResponseData("profile_create_success");

                } else if (uri.endsWith("/refresh-token")) {
                    responseJson = getResponseData("refresh_token_success");

                } else if (uri.endsWith("/logout")) {
                    responseJson = getResponseData("logout_success");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

        // Không khớp endpoint nào thì cho request đi tiếp ra mạng thật
        return chain.proceed(request);
    }

    /** Đọc trực tiếp object theo key — mock giờ trả thẳng data, không còn bọc status/message/data */
    private String getResponseData(String key) throws JSONException {
        if (mockDataJsonObject.has(key)) {
            return mockDataJsonObject.getJSONObject(key).toString();
        }
        return null;
    }

    private void loadMockDataFile(String fileName) {
        try (InputStream is = context.getAssets().open(fileName)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            int read = is.read(buffer);
            if (read <= 0) return;
            String jsonStr = new String(buffer, StandardCharsets.UTF_8);
            this.mockDataJsonObject = new JSONObject(jsonStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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