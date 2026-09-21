package com.example.attendanceapp.core.utils;

import android.os.Handler;
import android.os.Looper;

//Class IntervalTicker là một bộ đếm chu kỳ (Ticker/Timer)
// dùng trong Android để tự động thực thi một hành động (action)
// lặp đi lặp lại sau mỗi khoảng thời gian (intervalMs) cố định trên Main Thread (UI Thread).
public class IntervalTicker {

    //Đảm bảo công việc luôn được đưa về và thực thi trên Main Thread (UI Thread).
    // Điều này giúp bạn có thể cập nhật trực tiếp giao diện (như cập nhật đồng hồ,
    // cập nhật chữ) bên trong action mà không lo bị crash do sai luồng.
    private final Handler handler = new Handler(Looper.getMainLooper());

    //Khoảng thời gian giãn cách giữa các lần chạy (tính bằng milisecond - ms)
    private final long intervalMs;

    //Công việc/hành động cần thực hiện định kỳ (truyền vào dạng Lambda hoặc Anonymous Class).
    private final Runnable action;

    //Biến dùng để quản lý trạng thái vòng lặp và làm căn cứ để dừng vòng lặp sau này.
    private Runnable tickRunnable;

    public IntervalTicker(long intervalMs, Runnable action) {
        this.intervalMs = intervalMs;
        this.action = action;
    }

    public void start() {
        if (tickRunnable != null) return; // đã chạy rồi thì thôi
        tickRunnable = new Runnable() {
            @Override
            public void run() {
                action.run();
                handler.postDelayed(this, intervalMs);
            }
        };
        handler.post(tickRunnable);
    }

    public void stop() {
        if (tickRunnable != null) {
            //Hủy toàn bộ các tin nhắn/công việc đang chờ trong hàng đợi của Handler liên quan đến tickRunnable.
            handler.removeCallbacks(tickRunnable);
            //Dọn dẹp bộ nhớ và giải phóng trạng thái, cho phép gọi start() lại ở lần sau nếu cần.
            tickRunnable = null;
        }
    }
}