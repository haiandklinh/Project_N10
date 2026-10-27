package com.example.attendanceapp.data.remote;

import com.example.attendanceapp.domain.exception.AppException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableTransformer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Đổi mọi lỗi của Retrofit/Rx thành AppException có message tiếng Việt.
 * Backend là FastAPI nên đọc được: detail (mảng 422 hoặc chuỗi), message, error_code.
 */
public final class ApiErrorMapper {

    private ApiErrorMapper() { }

    public static <T> SingleTransformer<T, T> forSingle(String defaultMessage) {
        return upstream -> upstream.onErrorResumeNext(
                t -> Single.<T>error(map(t, defaultMessage)));
    }

    public static CompletableTransformer forCompletable(String defaultMessage) {
        return upstream -> upstream.onErrorResumeNext(
                t -> Completable.error(map(t, defaultMessage)));
    }

    static Throwable map(Throwable t, String defaultMessage) {
        // Đã chuẩn hóa hoặc lỗi kiểm tra đầu vào do mình tự ném: giữ nguyên
        if (t instanceof AppException || t instanceof IllegalArgumentException) return t;
        if (t instanceof HttpException) return fromHttp((HttpException) t, defaultMessage);
        if (t instanceof IOException) {
            return new AppException("Lỗi mạng. Vui lòng kiểm tra kết nối", 0, null);
        }
        return new AppException(defaultMessage, 0, null);
    }

    private static AppException fromHttp(HttpException e, String defaultMessage) {
        String message = defaultMessage;
        String errorCode = null;
        try {
            ResponseBody body = e.response() != null ? e.response().errorBody() : null;
            if (body != null) {
                JSONObject json = new JSONObject(body.string());

                if (json.has("error_code") && !json.isNull("error_code")) {
                    errorCode = json.getString("error_code");
                }

                Object detail = json.opt("detail");
                if (detail instanceof JSONArray && ((JSONArray) detail).length() > 0) {
                    JSONObject first = ((JSONArray) detail).optJSONObject(0);
                    if (first != null && first.has("msg")) {
                        message = first.getString("msg");
                        // Dịch các lỗi mặc định từ Backend (FastAPI/Pydantic)
                        if ("field required".equalsIgnoreCase(message)) {
                            message = "Mã OTP không hợp lệ!";
                        }
                    }
                } else if (detail instanceof String) {
                    message = (String) detail;
                } else if (json.has("message") && !json.isNull("message")) {
                    message = json.getString("message");
                }
            }
        } catch (Exception ignored) {
            // Body không phải JSON: dùng message mặc định
        }

        // Chuẩn hóa một số thông báo lỗi tiếng Anh từ FastAPI Backend sang tiếng Việt thân thiện
        if (message != null) {
            if ("Invitation code is invalid or expired".equalsIgnoreCase(message)) {
                message = "Mã mời không hợp lệ hoặc đã hết hạn";
            } else if ("Not found Staff in System".equalsIgnoreCase(message)) {
                message = "Không tìm thấy hồ sơ nhân viên trong hệ thống";
            } else if ("Not found Manager".equalsIgnoreCase(message)) {
                message = "Không tìm thấy thông tin quản lý cho mã mời này";
            } else if (message.toLowerCase().contains("not approve")) {
                message = "Yêu cầu gia nhập chưa được quản lý phê duyệt";
            }
        }

        return new AppException(message, e.code(), errorCode);
    }
}
