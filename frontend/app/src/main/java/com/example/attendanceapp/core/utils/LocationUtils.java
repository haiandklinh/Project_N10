package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationUtils {

    public interface AddressCallback {
        void onAddressFound(String address);
        void onError(String error);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void getAddressFromLocation(Context context, double lat, double lon, AddressCallback callback) {
        executor.execute(() -> {
            Geocoder geocoder = new Geocoder(context, new Locale("vi", "VN"));
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    
                    // Xây dựng chuỗi địa chỉ đầy đủ
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i));
                        if (i < address.getMaxAddressLineIndex()) sb.append(", ");
                    }
                    
                    String result = sb.toString();
                    mainHandler.post(() -> callback.onAddressFound(result));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy địa chỉ"));
                }
            } catch (IOException e) {
                mainHandler.post(() -> callback.onError("Lỗi kết nối: " + e.getMessage()));
            }
        });
    }
}