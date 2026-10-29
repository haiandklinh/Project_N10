package com.example.attendanceapp.core.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.attendanceapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationUtils {

    private static final String TAG = "LocationUtils";
    private static final String DEFAULT_MAPBOX_TOKEN = "pk.eyJ1Ijoic29uYmFuZGExMSIsImEiOiJjbW51MThxejIwdHRyMnBwd2p2OHhvMzFxIn0.XsxPqOqD9upAvTzh5iI3ZA";

    public interface AddressCallback {
        void onAddressFound(String address);
        void onError(String error);
    }

    public interface CoordinatesCallback {
        void onCoordinatesFound(double latitude, double longitude);
        void onError(String error);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static volatile Handler mainHandler;

    private static Handler getMainHandler() {
        if (mainHandler == null) {
            synchronized (LocationUtils.class) {
                if (mainHandler == null) {
                    mainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mainHandler;
    }

    private static String getMapboxToken(Context context) {
        if (context != null) {
            try {
                String token = context.getString(R.string.mapbox_access_token);
                if (!TextUtils.isEmpty(token)) {
                    return token;
                }
            } catch (Exception ignored) {}
        }
        return DEFAULT_MAPBOX_TOKEN;
    }

    /**
     * Chuyển đổi tọa độ GPS (lat, lon) sang địa chỉ văn bản tiếng Việt.
     * Thử Android Geocoder trước, nếu thất bại tự động dùng Mapbox Geocoding API làm fallback.
     */
    public static void getAddressFromLocation(Context context, double lat, double lon, AddressCallback callback) {
        executor.execute(() -> {
            // 1. Thử Android Geocoder
            if (context != null) {
                try {
                    Geocoder geocoder = new Geocoder(context, new Locale("vi", "VN"));
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                            String line = address.getAddressLine(i);
                            if (!TextUtils.isEmpty(line)) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(line);
                            }
                        }
                        String result = sb.toString().trim();
                        if (!result.isEmpty()) {
                            String cleanResult = stripCountryOrLastSegment(result);
                            getMainHandler().post(() -> callback.onAddressFound(cleanResult));
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Android Geocoder reverse failed: " + e.getMessage());
                }
            }

            // 2. Fallback sang Mapbox Reverse Geocoding API
            String mapboxAddress = fetchMapboxReverseGeocode(context, lat, lon);
            if (!TextUtils.isEmpty(mapboxAddress)) {
                String cleanMapboxAddress = stripCountryOrLastSegment(mapboxAddress);
                getMainHandler().post(() -> callback.onAddressFound(cleanMapboxAddress));
                return;
            }

            // 3. Nếu cả 2 đều không được, trả về tọa độ dạng text
            String fallbackCoords = String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon);
            getMainHandler().post(() -> callback.onAddressFound(fallbackCoords));
        });
    }

    /**
     * Loại bỏ phần tử/miền cuối cùng trong chuỗi địa chỉ (thường là tên quốc gia như "Việt Nam", "Vietnam").
     * Ví dụ: "123 Đường Cầu Giấy, Quan Hoa, Cầu Giấy, Hà Nội, Việt Nam" -> "123 Đường Cầu Giấy, Quan Hoa, Cầu Giấy, Hà Nội"
     */
    public static String stripCountryOrLastSegment(String fullAddress) {
        if (fullAddress == null) return null;
        String trimmed = fullAddress.trim();
        // Loại bỏ dấu phẩy hoặc chấm thừa ở cuối chuỗi
        while (trimmed.endsWith(",") || trimmed.endsWith(".")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }
        int lastComma = trimmed.lastIndexOf(',');
        if (lastComma > 0) {
            String candidate = trimmed.substring(0, lastComma).trim();
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }
        return trimmed;
    }

    /**
     * Chuyển đổi tên địa chỉ văn bản sang tọa độ (latitude, longitude).
     * Hỗ trợ tự động nhận diện nếu người dùng nhập sẵn tọa độ ("lat, lon").
     * Thử Android Geocoder, nếu thất bại fallback sang Mapbox Geocoding API.
     */
    public static void getLocationFromAddress(Context context, String addressName, CoordinatesCallback callback) {
        if (TextUtils.isEmpty(addressName)) {
            getMainHandler().post(() -> callback.onError("Địa chỉ trống"));
            return;
        }

        executor.execute(() -> {
            String trimmed = addressName.trim();

            // 1. Kiểm tra nếu chuỗi nhập vào chính là tọa độ "lat, lon"
            String[] parts = trimmed.split("[,;\\s]+");
            if (parts.length == 2) {
                try {
                    double parsedLat = Double.parseDouble(parts[0]);
                    double parsedLon = Double.parseDouble(parts[1]);
                    if (parsedLat >= -90 && parsedLat <= 90 && parsedLon >= -180 && parsedLon <= 180) {
                        getMainHandler().post(() -> callback.onCoordinatesFound(parsedLat, parsedLon));
                        return;
                    }
                } catch (NumberFormatException ignored) {}
            }

            // 2. Thử Android Geocoder
            if (context != null) {
                try {
                    Geocoder geocoder = new Geocoder(context, new Locale("vi", "VN"));
                    List<Address> addresses = geocoder.getFromLocationName(trimmed, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();
                        if (lat != 0.0 || lon != 0.0) {
                            getMainHandler().post(() -> callback.onCoordinatesFound(lat, lon));
                            return;
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Android Geocoder forward failed: " + e.getMessage());
                }
            }

            // 3. Fallback sang Mapbox Forward Geocoding API
            double[] coords = fetchMapboxForwardGeocode(context, trimmed);
            if (coords != null && coords.length == 2) {
                getMainHandler().post(() -> callback.onCoordinatesFound(coords[0], coords[1]));
                return;
            }

            getMainHandler().post(() -> callback.onError("Không thể xác định tọa độ từ địa chỉ"));
        });
    }

    private static String fetchMapboxReverseGeocode(Context context, double lat, double lon) {
        try {
            String token = getMapboxToken(context);
            String urlStr = String.format(Locale.US,
                    "https://api.mapbox.com/geocoding/v5/mapbox.places/%.6f,%.6f.json?access_token=%s&language=vi",
                    lon, lat, token);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray features = json.optJSONArray("features");
                if (features != null && features.length() > 0) {
                    JSONObject first = features.getJSONObject(0);
                    return first.optString("place_name", "");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchMapboxReverseGeocode error: " + e.getMessage());
        }
        return null;
    }

    private static double[] fetchMapboxForwardGeocode(Context context, String address) {
        try {
            String token = getMapboxToken(context);
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = String.format(Locale.US,
                    "https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?country=vn&access_token=%s&language=vi",
                    encodedAddress, token);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());
                JSONArray features = json.optJSONArray("features");
                if (features != null && features.length() > 0) {
                    JSONObject first = features.getJSONObject(0);
                    JSONArray center = first.optJSONArray("center");
                    if (center != null && center.length() >= 2) {
                        double lon = center.getDouble(0);
                        double lat = center.getDouble(1);
                        return new double[]{lat, lon};
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "fetchMapboxForwardGeocode error: " + e.getMessage());
        }
        return null;
    }
}
