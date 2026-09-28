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
import com.mapbox.bindgen.Expected;
import com.mapbox.bindgen.Value;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.mapbox.bindgen.Value;
import com.mapbox.common.MapboxOptions;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;

import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
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

    private static final int COLOR_CONDITION_OK =
            Color.rgb(76, 175, 80);

    private static final int COLOR_CONDITION_FAIL =
            Color.rgb(244, 67, 54);

    private static final long CONDITIONS_CHECK_INTERVAL_MS = 3000L;

    private static final long LOCATION_UPDATE_INTERVAL_MS = 2000L;
    private static final long LOCATION_UPDATE_MIN_INTERVAL_MS = 1000L;

    // =====================================================================
    // THUỘC TÍNH
    // =====================================================================

    private FragmentEmployeeCheckInBinding binding;

    // -- Bản đồ Mapbox --
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PointAnnotationManager pointAnnotationManager;

    // -- Vị trí --
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationValid = false;
    private boolean isGpsEnabled = false;
    private boolean isNetworkConnected = false;

    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // -- Đồng hồ & điều kiện --
    private final DateTimeFormatter clockFormatter =
            DateTimeFormatter.ofPattern("HH:mm");

    private IntervalTicker clockTicker;
    private IntervalTicker conditionsTicker;

    // -- Trạng thái chấm công --
    private CheckState currentState =
            CheckState.NOT_CHECKED_IN;

    public EmployeeCheckInFragment() {
    }

    // =====================================================================
    // 1. VÒNG ĐỜI FRAGMENT
    // =====================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapboxOptions.setAccessToken(
                getString(R.string.mapbox_access_token)
        );

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(
                        requireContext()
                );

        clockTicker = new IntervalTicker(
                1000,
                this::updateClockLabel
        );

        conditionsTicker = new IntervalTicker(
                CONDITIONS_CHECK_INTERVAL_MS,
                this::checkAndRenderConditions
        );

        registerPermissionLauncher();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentEmployeeCheckInBinding.inflate(
                inflater,
                container,
                false
        );

        setupMapboxMap();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupListeners();
        updateDateLabel();
        requestLocationPermissionIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (clockTicker != null) {
            clockTicker.start();
        }

        if (conditionsTicker != null) {
            conditionsTicker.start();
        }

        if (checkGpsEnabled()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (clockTicker != null) {
            clockTicker.stop();
        }

        if (conditionsTicker != null) {
            conditionsTicker.stop();
        }

        stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        stopLocationUpdates();

        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();
            pointAnnotationManager.onDestroy();
            pointAnnotationManager = null;
        }

        mapboxMap = null;
        mapView = null;
        binding = null;

        super.onDestroyView();
    }

    // =====================================================================
    // 2. THIẾT LẬP GIAO DIỆN
    // =====================================================================

    private void setupViews() {
        if (binding == null) {
            return;
        }

        binding.tagLocationValid.setText(
                getString(R.string.checkin_locating)
        );

        binding.cardWarningOutOfRange.setVisibility(
                View.GONE
        );

        binding.tvDistanceToOffice.setText(
                getString(R.string.checkin_determining_distance)
        );

        binding.tvLocationAddress.setText(
                getString(R.string.checkin_determining_location)
        );

        renderCheckButtonState();
        checkAndRenderConditions();
    }

    private void setupListeners() {
        if (binding == null) {
            return;
        }

        binding.btnCheckAction.setOnClickListener(
                v -> handleCheckActionClicked()
        );
    }

    // =====================================================================
    // 3. ĐỒNG HỒ & NGÀY
    // =====================================================================

    private void updateClockLabel() {
        if (binding == null) {
            return;
        }

        binding.tvClockBig.setText(
                DateTimeFormatUtils.formatTime(
                        LocalTime.now()
                )
        );
    }

    private void updateDateLabel() {
        if (binding == null) {
            return;
        }

        binding.tvDateToday.setText(
                DateTimeFormatUtils.formatTodayLabel(
                        LocalDate.now()
                )
        );
    }

    // =====================================================================
    // 4. MAPBOX
    // =====================================================================

    private void setupMapboxMap() {
        if (binding == null) {
            return;
        }

        mapView = binding.mapView;
        mapboxMap = mapView.getMapboxMap();

        CompassUtils.getCompass(mapView)
                .setEnabled(false);

        /*
         * Mapbox Maps SDK 11.x:
         *
         * Không dùng:
         * new Style.Builder().fromUri(...)
         *
         * Dùng trực tiếp:
         * loadStyle(Style.MAPBOX_STREETS, callback)
         */
        mapboxMap.loadStyle(
                Style.MAPBOX_STREETS,
                style -> {
                    if (mapboxMap == null) {
                        return;
                    }

                    mapboxMap.setCamera(
                            new CameraOptions.Builder()
                                    .center(
                                            Point.fromLngLat(
                                                    OFFICE_LNG,
                                                    OFFICE_LAT
                                            )
                                    )
                                    .zoom(17.0)
                                    .build()
                    );

                    drawOfficeRadius(style);
                    addOfficeMarker();
                }
        );
    }

    private void addOfficeMarker() {
        if (mapView == null) {
            return;
        }

        AnnotationPlugin annotationPlugin =
                mapView.getPlugin(
                        Plugin.MAPBOX_ANNOTATION_PLUGIN_ID
                );

        if (annotationPlugin == null) {
            return;
        }

        pointAnnotationManager =
                (PointAnnotationManager)
                        annotationPlugin.createAnnotationManager(
                                AnnotationType.PointAnnotation,
                                null
                        );

        pointAnnotationManager.deleteAll();

        Context context = getContext();

        if (context == null) {
            return;
        }

        Bitmap iconBitmap =
                bitmapFromDrawableRes(
                        context,
                        R.drawable.ic_location
                );

        PointAnnotationOptions options =
                new PointAnnotationOptions()
                        .withPoint(
                                Point.fromLngLat(
                                        OFFICE_LNG,
                                        OFFICE_LAT
                                )
                        );

        if (iconBitmap != null) {
            options.withIconImage(iconBitmap);
        }

        pointAnnotationManager.create(options);
    }

    private Bitmap bitmapFromDrawableRes(
            Context context,
            int resourceId
    ) {
        Drawable drawable =
                ContextCompat.getDrawable(
                        context,
                        resourceId
                );

        if (drawable == null) {
            return null;
        }

        int width =
                drawable.getIntrinsicWidth() > 0
                        ? drawable.getIntrinsicWidth()
                        : 64;

        int height =
                drawable.getIntrinsicHeight() > 0
                        ? drawable.getIntrinsicHeight()
                        : 64;

        Bitmap bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );

        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Vẽ vùng bán kính 50m quanh văn phòng.
     *
     * Mapbox 11.x không dùng:
     *
     * style.addSource(source);
     * style.addLayer(fillLayer);
     *
     * theo API cũ.
     *
     * Vì vậy sử dụng addStyleSource() và addStyleLayer()
     * với Value.
     */
    private void drawOfficeRadius(@NonNull Style style) {
        Polygon circlePolygon = createCirclePolygon(
                OFFICE_LAT,
                OFFICE_LNG,
                ALLOWED_RADIUS_METERS
        );

        Feature feature = Feature.fromGeometry(circlePolygon);
        FeatureCollection featureCollection =
                FeatureCollection.fromFeature(feature);

        String geoJson = featureCollection.toJson();

        // =========================================================
        // GeoJSON Source
        // =========================================================

        Value sourceProperties =
                Value.valueOf(
                        new java.util.HashMap<String, Value>() {{
                            put(
                                    "type",
                                    Value.valueOf("geojson")
                            );

                            put(
                                    "data",
                                    Value.fromJson(geoJson)
                                            .getValueOrElse(
                                                    error -> Value.nullValue()
                                            )
                            );
                        }}
                );

        style.addStyleSource(
                OFFICE_RADIUS_SOURCE,
                sourceProperties
        );

        // =========================================================
        // Fill Layer
        // =========================================================

        java.util.HashMap<String, Value> paint =
                new java.util.HashMap<>();

        paint.put(
                "fill-color",
                Value.valueOf("#4CAF50")
        );

        paint.put(
                "fill-opacity",
                Value.valueOf(0.4)
        );

        paint.put(
                "fill-outline-color",
                Value.valueOf("#2E7D32")
        );

        java.util.HashMap<String, Value> layer =
                new java.util.HashMap<>();

        layer.put(
                "id",
                Value.valueOf(OFFICE_RADIUS_LAYER)
        );

        layer.put(
                "type",
                Value.valueOf("fill")
        );

        layer.put(
                "source",
                Value.valueOf(OFFICE_RADIUS_SOURCE)
        );

        layer.put(
                "paint",
                Value.valueOf(paint)
        );

        style.addStyleLayer(
                Value.valueOf(layer),
                null
        );
    }
    private Polygon createCirclePolygon(
            double centerLat,
            double centerLng,
            double radiusMeters
    ) {
        List<Point> points =
                new ArrayList<>();

        int numberOfPoints = 64;

        double earthRadius = 6371000.0;

        double latRadians =
                Math.toRadians(centerLat);

        for (int i = 0;
             i <= numberOfPoints;
             i++) {

            double angle =
                    2 * Math.PI * i / numberOfPoints;

            double deltaLat =
                    (radiusMeters * Math.cos(angle))
                            / earthRadius;

            double deltaLng =
                    (radiusMeters * Math.sin(angle))
                            / (
                            earthRadius
                                    * Math.cos(latRadians)
                    );

            double pointLat =
                    centerLat
                            + Math.toDegrees(deltaLat);

            double pointLng =
                    centerLng
                            + Math.toDegrees(deltaLng);

            points.add(
                    Point.fromLngLat(
                            pointLng,
                            pointLat
                    )
            );
        }

        List<List<Point>> coordinates =
                new ArrayList<>();

        coordinates.add(points);

        return Polygon.fromLngLats(
                coordinates
        );
    }

    // =====================================================================
    // 5. QUYỀN VỊ TRÍ
    // =====================================================================

    private void registerPermissionLauncher() {
        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts
                                .RequestMultiplePermissions(),
                        result -> {

                            Boolean fineGranted =
                                    result.getOrDefault(
                                            Manifest.permission
                                                    .ACCESS_FINE_LOCATION,
                                            false
                                    );

                            Boolean coarseGranted =
                                    result.getOrDefault(
                                            Manifest.permission
                                                    .ACCESS_COARSE_LOCATION,
                                            false
                                    );

                            if (
                                    Boolean.TRUE.equals(
                                            fineGranted
                                    )
                                            ||
                                            Boolean.TRUE.equals(
                                                    coarseGranted
                                            )
                            ) {
                                startLocationUpdates();
                            } else {
                                showLocationPermissionDenied();
                            }
                        }
                );
    }

    private void requestLocationPermissionIfNeeded() {
        Context context = getContext();

        if (context == null) {
            return;
        }

        boolean fineGranted =
                hasPermission(
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                );

        boolean coarseGranted =
                hasPermission(
                        Manifest.permission
                                .ACCESS_COARSE_LOCATION
                );

        if (fineGranted || coarseGranted) {
            startLocationUpdates();
        } else {
            locationPermissionLauncher.launch(
                    new String[]{
                            Manifest.permission
                                    .ACCESS_FINE_LOCATION,
                            Manifest.permission
                                    .ACCESS_COARSE_LOCATION
                    }
            );
        }
    }

    private boolean hasPermission(
            String permission
    ) {
        Context context = getContext();

        if (context == null) {
            return false;
        }

        return ContextCompat.checkSelfPermission(
                context,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // =====================================================================
    // 6. FUSED LOCATION
    // =====================================================================

    private void startLocationUpdates() {
        if (!isAdded() || binding == null) {
            return;
        }

        if (!checkGpsEnabled()) {
            checkAndRenderConditions();
            return;
        }

        if (
                !hasPermission(
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                )
                        &&
                        !hasPermission(
                                Manifest.permission
                                        .ACCESS_COARSE_LOCATION
                        )
        ) {
            return;
        }

        if (locationCallback != null) {
            return;
        }

        LocationRequest locationRequest =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        LOCATION_UPDATE_INTERVAL_MS
                )
                        .setMinUpdateIntervalMillis(
                                LOCATION_UPDATE_MIN_INTERVAL_MS
                        )
                        .setWaitForAccurateLocation(false)
                        .build();

        locationCallback =
                new LocationCallback() {

                    @Override
                    public void onLocationResult(
                            @NonNull LocationResult locationResult
                    ) {
                        Location location =
                                locationResult
                                        .getLastLocation();

                        if (location != null) {
                            onLocationReceived(location);
                        }
                    }
                };

        try {

            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
            );

            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(
                            location -> {

                                if (location != null) {
                                    onLocationReceived(
                                            location
                                    );
                                }
                            }
                    );

        } catch (SecurityException e) {
            showLocationPermissionDenied();
        }
    }

    private void stopLocationUpdates() {
        if (
                fusedLocationClient != null
                        &&
                        locationCallback != null
        ) {
            fusedLocationClient.removeLocationUpdates(
                    locationCallback
            );
        }

        locationCallback = null;
    }

    private void onLocationReceived(
            @NonNull Location location
    ) {
        if (binding == null) {
            return;
        }

        currentLocation = location;

        float[] distanceResult =
                new float[1];

        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                OFFICE_LAT,
                OFFICE_LNG,
                distanceResult
        );

        float distanceMeters =
                distanceResult[0];

        isLocationValid =
                distanceMeters <= ALLOWED_RADIUS_METERS;

        if (distanceMeters < 1000) {

            binding.tvDistanceToOffice.setText(
                    getString(
                            R.string.checkin_distance_meters,
                            distanceMeters
                    )
            );

        } else {

            binding.tvDistanceToOffice.setText(
                    getString(
                            R.string.checkin_distance_kilometers,
                            distanceMeters / 1000f
                    )
            );
        }

        updateAddress(location);

        renderLocationValidChip();

        checkAndRenderConditions();
    }

    private void updateAddress(
            @NonNull Location location
    ) {
        if (binding == null) {
            return;
        }

        Context appContext =
                requireContext()
                        .getApplicationContext();

        new Thread(() -> {

            try {

                Geocoder geocoder =
                        new Geocoder(
                                appContext,
                                new Locale(
                                        "vi",
                                        "VN"
                                )
                        );

                List<android.location.Address>
                        addresses =
                        geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1
                        );

                String addressText =
                        appContext.getString(
                                R.string
                                        .checkin_current_location
                        );

                if (
                        addresses != null
                                &&
                                !addresses.isEmpty()
                ) {

                    android.location.Address address =
                            addresses.get(0);

                    StringBuilder builder =
                            new StringBuilder();

                    for (
                            int i = 0;
                            i <= address
                                    .getMaxAddressLineIndex();
                            i++
                    ) {

                        if (i > 0) {
                            builder.append(", ");
                        }

                        builder.append(
                                address.getAddressLine(i)
                        );
                    }

                    if (builder.length() > 0) {
                        addressText =
                                builder.toString();
                    }
                }

                String finalAddress =
                        addressText;

                if (
                        isAdded()
                                &&
                                binding != null
                ) {

                    requireActivity()
                            .runOnUiThread(() -> {

                                if (binding != null) {
                                    binding.tvLocationAddress
                                            .setText(
                                                    finalAddress
                                            );
                                }
                            });
                }

            } catch (
                    IOException
                    |
                    IllegalArgumentException e
            ) {

                if (
                        isAdded()
                                &&
                                binding != null
                ) {

                    requireActivity()
                            .runOnUiThread(() -> {

                                if (binding != null) {
                                    binding.tvLocationAddress
                                            .setText(
                                                    appContext
                                                            .getString(
                                                                    R.string
                                                                            .checkin_current_location
                                                            )
                                            );
                                }
                            });
                }
            }

        }).start();
    }

    private void renderLocationValidChip() {
        if (binding == null) {
            return;
        }

        binding.tagLocationValid.setText(
                isLocationValid
                        ? getString(
                        R.string
                                .checkin_location_valid
                )
                        : getString(
                        R.string
                                .checkin_location_invalid
                )
        );
    }

    private void showLocationPermissionDenied() {
        if (binding == null) {
            return;
        }

        binding.tvLocationAddress.setText(
                getString(
                        R.string.checkin_permission_denied
                )
        );

        binding.tvDistanceToOffice.setText(
                getString(
                        R.string.checkin_enable_location
                )
        );

        binding.tagLocationValid.setText(
                getString(
                        R.string.checkin_not_determined
                )
        );

        currentLocation = null;
        isLocationValid = false;

        checkAndRenderConditions();
    }

    // =====================================================================
    // 7. GPS & NETWORK
    // =====================================================================

    private boolean checkGpsEnabled() {
        if (!isAdded()) {
            return false;
        }

        Context context = getContext();

        if (context == null) {
            return false;
        }

        LocationManager manager =
                (LocationManager)
                        context.getSystemService(
                                Context.LOCATION_SERVICE
                        );

        if (manager == null) {
            return false;
        }

        try {
            return manager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
            );
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkNetworkConnected() {
        if (!isAdded()) {
            return false;
        }

        Context context = getContext();

        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager)
                        context.getSystemService(
                                Context.CONNECTIVITY_SERVICE
                        );

        if (connectivityManager == null) {
            return false;
        }

        Network network =
                connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(
                        network
                );

        return capabilities != null
                &&
                capabilities.hasCapability(
                        NetworkCapabilities
                                .NET_CAPABILITY_INTERNET
                )
                &&
                (
                        capabilities.hasTransport(
                                NetworkCapabilities
                                        .TRANSPORT_WIFI
                        )
                                ||
                                capabilities.hasTransport(
                                        NetworkCapabilities
                                                .TRANSPORT_CELLULAR
                                )
                );
    }

    // =====================================================================
    // 8. KIỂM TRA ĐIỀU KIỆN CHẤM CÔNG
    // =====================================================================

    private void checkAndRenderConditions() {
        if (binding == null) {
            return;
        }

        isGpsEnabled =
                checkGpsEnabled();

        /*
         * Không dùng:
         *
         * isLocationValid =
         *     (currentLocation != null)
         *     && isLocationValid;
         *
         * vì biểu thức này có thể giữ giá trị false
         * vĩnh viễn sau một lần false.
         *
         * Tính lại khoảng cách trực tiếp từ currentLocation.
         */
        if (currentLocation != null) {

            float[] distanceResult =
                    new float[1];

            Location.distanceBetween(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    OFFICE_LAT,
                    OFFICE_LNG,
                    distanceResult
            );

            isLocationValid =
                    distanceResult[0]
                            <= ALLOWED_RADIUS_METERS;

        } else {
            isLocationValid = false;
        }

        isNetworkConnected =
                checkNetworkConnected();

        renderConditionRow(
                binding.ivConditionGpsStatus,
                isGpsEnabled
        );

        renderConditionRow(
                binding.ivConditionRangeStatus,
                isLocationValid
        );

        renderConditionRow(
                binding.ivConditionNetworkStatus,
                isNetworkConnected
        );

        renderCheckButtonEnabled();
    }

    private void renderConditionRow(
            android.widget.ImageView iconView,
            boolean isOk
    ) {
        if (iconView == null) {
            return;
        }

        iconView.setImageResource(
                isOk
                        ? R.drawable.ic_check
                        : R.drawable.ic_close
        );

        iconView.setColorFilter(
                isOk
                        ? COLOR_CONDITION_OK
                        : COLOR_CONDITION_FAIL
        );
    }

    private boolean isAllConditionsMet() {
        return isGpsEnabled
                && isLocationValid
                && isNetworkConnected
                && currentLocation != null;
    }

    private String buildConditionsWarningMessage() {

        if (!isGpsEnabled) {
            return getString(
                    R.string.checkin_enable_gps
            );
        }

        if (currentLocation == null) {
            return getString(
                    R.string
                            .checkin_determining_your_location
            );
        }

        if (!isLocationValid) {
            return getString(
                    R.string
                            .checkin_outside_allowed_area
            );
        }

        if (!isNetworkConnected) {
            return getString(
                    R.string.checkin_check_network
            );
        }

        return getString(
                R.string.checkin_conditions_not_met
        );
    }

    // =====================================================================
    // 9. XỬ LÝ NÚT CHẤM CÔNG
    // =====================================================================

    private void handleCheckActionClicked() {

        if (!isAllConditionsMet()) {

            binding.cardWarningOutOfRange
                    .setVisibility(View.VISIBLE);

            binding.tvWarningOutOfRange.setText(
                    buildConditionsWarningMessage()
            );

            return;
        }

        binding.cardWarningOutOfRange
                .setVisibility(View.GONE);

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

        LocalTime time =
                LocalTime.now();

        currentState =
                CheckState.CHECKED_IN;

        binding.tvSummaryCheckInTime.setText(
                time.format(clockFormatter)
        );

        renderCheckButtonState();
    }

    private void performCheckOut() {

        LocalTime time =
                LocalTime.now();

        currentState =
                CheckState.CHECKED_OUT;

        binding.tvSummaryCheckOutTime.setText(
                time.format(clockFormatter)
        );

        renderCheckButtonState();
    }

    // =====================================================================
    // 10. TRẠNG THÁI NÚT
    // =====================================================================

    private void renderCheckButtonState() {
        if (binding == null) {
            return;
        }

        switch (currentState) {

            case NOT_CHECKED_IN:

                binding.btnCheckAction.setText(
                        getString(
                                R.string
                                        .checkin_btn_check_in
                        )
                );

                binding.btnCheckAction.setEnabled(
                        isAllConditionsMet()
                );

                break;

            case CHECKED_IN:

                binding.btnCheckAction.setText(
                        getString(
                                R.string
                                        .checkin_btn_check_out
                        )
                );

                binding.btnCheckAction.setEnabled(
                        isAllConditionsMet()
                );

                break;

            case CHECKED_OUT:

                binding.btnCheckAction.setText(
                        getString(
                                R.string
                                        .checkin_completed_today
                        )
                );

                binding.btnCheckAction.setEnabled(false);

                break;
        }
    }

    private void renderCheckButtonEnabled() {
        if (binding == null) {
            return;
        }

        if (
                currentState !=
                        CheckState.CHECKED_OUT
        ) {
            binding.btnCheckAction.setEnabled(
                    isAllConditionsMet()
            );
        }
    }
}