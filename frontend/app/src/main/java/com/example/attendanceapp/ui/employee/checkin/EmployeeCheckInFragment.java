package com.example.attendanceapp.ui.employee.checkin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentEmployeeCheckInBinding;
import com.example.attendanceapp.domain.model.CheckState;

import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.core.utils.IntervalTicker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.mapbox.common.MapboxOptions;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.LayerUtils;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.sources.SourceUtils;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;

// Annotation imports cho Marker trong Mapbox SDK v11
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

// Compass & Gestures plugin imports
import com.mapbox.maps.plugin.compass.CompassUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeCheckInFragment extends Fragment {

    // =====================================================================
    // HẰNG SỐ & CẤU HÌNH
    // =====================================================================

    private static final double OFFICE_LAT = 21.028511;
    private static final double OFFICE_LNG = 105.804817;
    private static final float ALLOWED_RADIUS_METERS = 50f;

    private static final String OFFICE_RADIUS_SOURCE = "office-radius-source";
    private static final String OFFICE_RADIUS_LAYER = "office-radius-layer";

    private static final int COLOR_CONDITION_OK = Color.rgb(76, 175, 80);
    private static final int COLOR_CONDITION_FAIL = Color.rgb(244, 67, 54);
    private static final long CONDITIONS_CHECK_INTERVAL_MS = 3000L;

    // Cấu hình cập nhật vị trí cho FusedLocationProviderClient
    private static final long LOCATION_UPDATE_INTERVAL_MS = 2000L;
    private static final long LOCATION_UPDATE_MIN_INTERVAL_MS = 1000L;

    // =====================================================================
    // THUỘC TÍNH
    // =====================================================================

    private FragmentEmployeeCheckInBinding binding;

    // -- Bản đồ (Mapbox) --
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PointAnnotationManager pointAnnotationManager;

    // -- Vị trí (FusedLocationProviderClient) --
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationValid = false;
    private boolean isGpsEnabled = false;
    private boolean isNetworkConnected = false;

    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // -- Đồng hồ --
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private Runnable clockTickRunnable;
    private final DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("EEEE, dd 'tháng' MM, yyyy", new Locale("vi", "VN"));

    // -- Điều kiện chấm công (polling) --
    private final Handler conditionsHandler = new Handler(Looper.getMainLooper());
    private Runnable conditionsTickRunnable;

    // -- Trạng thái chấm công --
    private CheckState currentState = CheckState.NOT_CHECKED_IN;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    private IntervalTicker clockTicker;
    private IntervalTicker conditionsTicker;

    public EmployeeCheckInFragment() {
    }

    // =====================================================================
    // 1. VÒNG ĐỜI FRAGMENT (LIFECYCLE)
    // =====================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxOptions.setAccessToken(getString(R.string.mapbox_access_token));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        clockTicker = new IntervalTicker(1000, this::updateClockLabel);
        conditionsTicker = new IntervalTicker(CONDITIONS_CHECK_INTERVAL_MS, this::checkAndRenderConditions);
        registerPermissionLauncher();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeCheckInBinding.inflate(inflater, container, false);
        setupMapboxMap();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupListeners();
        updateDateLabel();
        requestLocationPermissionIfNeeded(); //bắt đầu xin quyền GPS
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        clockTicker.start();
        conditionsTicker.start();
        if (checkGpsEnabled()) startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        clockTicker.stop();
        conditionsTicker.stop();
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        stopClockTicking();
        stopConditionsPolling();
        stopLocationUpdates();

        if (pointAnnotationManager != null) {
            pointAnnotationManager.onDestroy();
            pointAnnotationManager = null;
        }

        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }

        binding = null;
        super.onDestroyView();
    }

    // =====================================================================
    // 2. THIẾT LẬP GIAO DIỆN BAN ĐẦU (INIT VIEWS & LISTENERS)
    // =====================================================================

    private void setupViews() {
        binding.tagLocationValid.setText(getString(R.string.checkin_locating));
        binding.cardWarningOutOfRange.setVisibility(View.GONE);
        binding.tvDistanceToOffice.setText(getString(R.string.checkin_determining_distance));
        binding.tvLocationAddress.setText(getString(R.string.checkin_determining_location));

        renderCheckButtonState();
        checkAndRenderConditions();
    }

    private void setupListeners() {
        binding.btnCheckAction.setOnClickListener(v -> handleCheckActionClicked());
    }

    // =====================================================================
    // 3. ĐỒNG HỒ & NGÀY THÁNG (CLOCK & DATE)
    // =====================================================================


    private void stopClockTicking() {
        if (clockTickRunnable != null) {
            clockHandler.removeCallbacks(clockTickRunnable);
            clockTickRunnable = null;
        }
    }

    private void updateClockLabel() {
        if (binding == null) return;
        binding.tvClockBig.setText(DateTimeFormatUtils.formatTime(LocalTime.now()));
    }

    private void updateDateLabel() {
        if (binding == null) return;
        binding.tvDateToday.setText(DateTimeFormatUtils.formatTodayLabel(LocalDate.now()));
    }


    // =====================================================================
    // 4. BẢN ĐỒ (MAPBOX MAP)
    // =====================================================================

    private void setupMapboxMap() {
        mapView = binding.mapView;
        mapboxMap = mapView.getMapboxMap();

        // Ẩn các UI control không cần thiết trên MapView
        CompassUtils.getCompass(mapView).setEnabled(false);

        mapboxMap.loadStyleUri(
                Style.MAPBOX_STREETS,
                style -> {
                    mapboxMap.setCamera(
                            new CameraOptions.Builder()
                                    .center(Point.fromLngLat(OFFICE_LNG, OFFICE_LAT))
                                    .zoom(17.0)
                                    .build()
                    );

                    drawOfficeRadius(style);
                    addOfficeMarker();
                }
        );
    }

    private void addOfficeMarker() {
        AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);

        if (annotationPlugin != null) {
            pointAnnotationManager = (PointAnnotationManager) annotationPlugin.createAnnotationManager(
                    AnnotationType.PointAnnotation,
                    null
            );

            pointAnnotationManager.deleteAll();

            Bitmap iconBitmap = bitmapFromDrawableRes(requireContext(), R.drawable.ic_location);

            PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(OFFICE_LNG, OFFICE_LAT));

            if (iconBitmap != null) {
                pointAnnotationOptions.withIconImage(iconBitmap);
            }

            pointAnnotationManager.create(pointAnnotationOptions);
        }
    }

    private Bitmap bitmapFromDrawableRes(Context context, int resourceId) {
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        if (drawable == null) return null;

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 64,
                drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 64,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void drawOfficeRadius(@NonNull Style style) {
        Polygon circlePolygon = createCirclePolygon(OFFICE_LAT, OFFICE_LNG, ALLOWED_RADIUS_METERS);
        Feature feature = Feature.fromGeometry(circlePolygon);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);

        GeoJsonSource source = new GeoJsonSource.Builder(OFFICE_RADIUS_SOURCE)
                .featureCollection(featureCollection)
                .build();

        SourceUtils.addSource(style, source);

        FillLayer fillLayer = new FillLayer(OFFICE_RADIUS_LAYER, OFFICE_RADIUS_SOURCE);
        fillLayer.fillColor(Color.rgb(76, 175, 80));
        fillLayer.fillOpacity(0.4);
        fillLayer.fillOutlineColor(Color.rgb(46, 125, 50));

        LayerUtils.addLayer(style, fillLayer);
    }

    private Polygon createCirclePolygon(double centerLat, double centerLng, double radiusMeters) {
        List<Point> points = new ArrayList<>();
        int numberOfPoints = 64;
        double earthRadius = 6371000.0;
        double latRadians = Math.toRadians(centerLat);

        for (int i = 0; i <= numberOfPoints; i++) {
            double angle = 2 * Math.PI * i / numberOfPoints;
            double deltaLat = (radiusMeters * Math.cos(angle)) / earthRadius;
            double deltaLng = (radiusMeters * Math.sin(angle)) / (earthRadius * Math.cos(latRadians));

            double pointLat = centerLat + Math.toDegrees(deltaLat);
            double pointLng = centerLng + Math.toDegrees(deltaLng);

            points.add(Point.fromLngLat(pointLng, pointLat));
        }

        List<List<Point>> coordinates = new ArrayList<>();
        coordinates.add(points);

        return Polygon.fromLngLats(coordinates);
    }

    // =====================================================================
    // 5. QUYỀN & VỊ TRÍ (LOCATION PERMISSION & FUSED LOCATION PROVIDER)
    // =====================================================================

    private void registerPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                        startLocationUpdates();
                    } else {
                        showLocationPermissionDenied();
                    }
                }
        );
    }

    private void requestLocationPermissionIfNeeded() {
        boolean fineGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean coarseGranted = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineGranted || coarseGranted) {
            startLocationUpdates();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Bắt đầu lắng nghe vị trí bằng FusedLocationProviderClient.
     * Ưu điểm so với LocationManager: tự động chọn nguồn vị trí tốt nhất
     * (GPS / mạng / cảm biến kết hợp), pin tối ưu hơn, và có API dựa trên
     * Task/Callback hiện đại hơn.
     */
    private void startLocationUpdates() {
        if (!isAdded() || binding == null) return;

        if (!checkGpsEnabled()) {
            checkAndRenderConditions();
            return;
        }

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return;
        }

        // Tránh đăng ký callback trùng lặp nếu đã có sẵn
        if (locationCallback != null) return;

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL_MS
        )
                .setMinUpdateIntervalMillis(LOCATION_UPDATE_MIN_INTERVAL_MS)
                .setWaitForAccurateLocation(false)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    onLocationReceived(location);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            // Lấy vị trí gần nhất đã biết để hiển thị ngay, không cần chờ update mới
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            onLocationReceived(location);
                        }
                    });
        } catch (SecurityException e) {
            showLocationPermissionDenied();
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        locationCallback = null;
    }

    private void onLocationReceived(@NonNull Location location) {
        if (binding == null) return;

        currentLocation = location;

        float[] distanceResult = new float[1];
        Location.distanceBetween(
                location.getLatitude(), location.getLongitude(),
                OFFICE_LAT, OFFICE_LNG,
                distanceResult
        );

        float distanceMeters = distanceResult[0];
        isLocationValid = distanceMeters <= ALLOWED_RADIUS_METERS;

        if (distanceMeters < 1000) {
            binding.tvDistanceToOffice.setText(getString(R.string.checkin_distance_meters, distanceMeters));
        } else {
            binding.tvDistanceToOffice.setText(getString(R.string.checkin_distance_kilometers, distanceMeters / 1000f));
        }

        updateAddress(location);
        renderLocationValidChip();
        checkAndRenderConditions();
    }

    private void updateAddress(@NonNull Location location) {
        if (binding == null) return;

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), new Locale("vi", "VN"));
                List<android.location.Address> addresses =
                        geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                String addressText = getString(R.string.checkin_current_location);
                if (addresses != null && !addresses.isEmpty()) {
                    android.location.Address address = addresses.get(0);
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        if (i > 0) builder.append(", ");
                        builder.append(address.getAddressLine(i));
                    }
                    if (builder.length() > 0) {
                        addressText = builder.toString();
                    }
                }

                String finalAddress = addressText;
                if (binding != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> binding.tvLocationAddress.setText(finalAddress));
                }
            } catch (IOException | IllegalArgumentException e) {
                if (binding != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> binding.tvLocationAddress.setText(getString(R.string.checkin_current_location)));
                }
            }
        }).start();
    }

    private void renderLocationValidChip() {
        if (binding == null) return;
        binding.tagLocationValid.setText(isLocationValid ? getString(R.string.checkin_location_valid) : getString(R.string.checkin_location_invalid));
    }

    private void showLocationPermissionDenied() {
        if (binding == null) return;

        binding.tvLocationAddress.setText(getString(R.string.checkin_permission_denied));
        binding.tvDistanceToOffice.setText(getString(R.string.checkin_enable_location));
        binding.tagLocationValid.setText(getString(R.string.checkin_not_determined));

        isLocationValid = false;
        checkAndRenderConditions();
    }

    // =====================================================================
    // 6. KIỂM TRA ĐIỀU KIỆN CHẤM CÔNG (GPS / MẠNG / BÁN KÍNH)
    // =====================================================================

    /**
     * Kiểm tra công tắc GPS phần cứng có đang bật hay không. FusedLocationProviderClient
     * không cho biết trực tiếp trạng thái này, nên vẫn dùng LocationManager chỉ để đọc
     * cờ isProviderEnabled cho mục đích hiển thị điều kiện trên UI.
     */
    private boolean checkGpsEnabled() {
        if (!isAdded()) return false;
        LocationManager manager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) return false;
        try {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkNetworkConnected() {
        if (!isAdded()) return false;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    private void checkAndRenderConditions() {
        if (binding == null) return;

        isGpsEnabled = checkGpsEnabled();
        isNetworkConnected = checkNetworkConnected();

        renderConditionRow(binding.ivConditionGpsStatus, isGpsEnabled);
        renderConditionRow(binding.ivConditionRangeStatus, isLocationValid);
        renderConditionRow(binding.ivConditionNetworkStatus, isNetworkConnected);

        renderCheckButtonEnabled();
    }

    private void renderConditionRow(android.widget.ImageView iconView, boolean isOk) {
        iconView.setImageResource(isOk ? R.drawable.ic_check : R.drawable.ic_close);
        iconView.setColorFilter(isOk ? COLOR_CONDITION_OK : COLOR_CONDITION_FAIL);
    }

    private void startConditionsPolling() {
        conditionsTickRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndRenderConditions();
                conditionsHandler.postDelayed(this, CONDITIONS_CHECK_INTERVAL_MS);
            }
        };
        conditionsHandler.post(conditionsTickRunnable);
    }

    private void stopConditionsPolling() {
        if (conditionsTickRunnable != null) {
            conditionsHandler.removeCallbacks(conditionsTickRunnable);
            conditionsTickRunnable = null;
        }
    }

    private boolean isAllConditionsMet() {
        return isGpsEnabled && isLocationValid && isNetworkConnected && currentLocation != null;
    }

    private String buildConditionsWarningMessage() {
        if (!isGpsEnabled) return getString(R.string.checkin_enable_gps);
        if (currentLocation == null) return getString(R.string.checkin_determining_your_location);
        if (!isLocationValid) return getString(R.string.checkin_outside_allowed_area);
        if (!isNetworkConnected) return getString(R.string.checkin_check_network);
        return getString(R.string.checkin_conditions_not_met);
    }

    // =====================================================================
    // 7. CHECK IN / CHECK OUT
    // =====================================================================

    private void handleCheckActionClicked() {
        if (!isAllConditionsMet()) {
            binding.cardWarningOutOfRange.setVisibility(View.VISIBLE);
            binding.tvWarningOutOfRange.setText(buildConditionsWarningMessage());
            return;
        }

        binding.cardWarningOutOfRange.setVisibility(View.GONE);

        switch (currentState) {
            case NOT_CHECKED_IN:
                performCheckIn();
                break;
            case CHECKED_IN:
                performCheckOut();
                break;
            case CHECKED_OUT:
                break;
        }
    }

    private void performCheckIn() {
        checkInTime = LocalTime.now();
        currentState = CheckState.CHECKED_IN;
        binding.tvSummaryCheckInTime.setText(checkInTime.format(clockFormatter));
        renderCheckButtonState();
    }

    private void performCheckOut() {
        checkOutTime = LocalTime.now();
        currentState = CheckState.CHECKED_OUT;
        binding.tvSummaryCheckOutTime.setText(checkOutTime.format(clockFormatter));
        renderCheckButtonState();
    }

    private void renderCheckButtonState() {
        if (binding == null) return;

        switch (currentState) {
            case NOT_CHECKED_IN:
                binding.btnCheckAction.setText(getString(R.string.checkin_btn_check_in));
                binding.btnCheckAction.setEnabled(isAllConditionsMet());
                break;
            case CHECKED_IN:
                binding.btnCheckAction.setText(getString(R.string.checkin_btn_check_out));
                binding.btnCheckAction.setEnabled(isAllConditionsMet());
                break;
            case CHECKED_OUT:
                binding.btnCheckAction.setText(getString(R.string.checkin_completed_today));
                binding.btnCheckAction.setEnabled(false);
                break;
        }
    }

    private void renderCheckButtonEnabled() {
        if (binding == null) return;
        if (currentState != CheckState.CHECKED_OUT) {
            binding.btnCheckAction.setEnabled(isAllConditionsMet());
        }
    }
}