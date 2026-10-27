package com.example.attendanceapp.ui.employee.checkin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.core.utils.IntervalTicker;
import com.example.attendanceapp.core.utils.LocationUtils;
import com.example.attendanceapp.data.dto.request.attendance.CheckInRequest;
import com.example.attendanceapp.data.dto.request.attendance.CheckOutRequest;
import com.example.attendanceapp.data.dto.response.staff.StaffWorkScheduleResponse;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentEmployeeCheckInBinding;
import com.example.attendanceapp.domain.model.CheckState;
import com.example.attendanceapp.domain.repository.AttendanceRepository;
import com.example.attendanceapp.domain.repository.ShiftRepository;
import com.example.attendanceapp.domain.usecase.schedule.GetScheduleByDateUseCase;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeCheckInFragment extends Fragment {

    // =====================================================================
    // HẰNG SỐ & CẤU HÌNH
    // =====================================================================

    private static final float DEFAULT_RADIUS_METERS = 100f;
    private static final String SHIFT_RADIUS_SOURCE = "shift-radius-source";
    private static final String SHIFT_RADIUS_FILL_LAYER = "shift-radius-fill-layer";
    private static final String SHIFT_RADIUS_LINE_LAYER = "shift-radius-line-layer";

    private static final int COLOR_CONDITION_OK = Color.rgb(76, 175, 80);
    private static final int COLOR_CONDITION_FAIL = Color.rgb(244, 67, 54);

    private static final long CONDITIONS_CHECK_INTERVAL_MS = 3000L;
    private static final long LOCATION_UPDATE_INTERVAL_MS = 2000L;
    private static final long LOCATION_UPDATE_MIN_INTERVAL_MS = 1000L;

    // =====================================================================
    // DEPENDENCIES & STATE
    // =====================================================================

    private FragmentEmployeeCheckInBinding binding;

    @Inject
    GetScheduleByDateUseCase getScheduleByDateUseCase;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    @Inject
    AttendanceRepository attendanceRepository;

    @Inject
    ShiftRepository shiftRepository;

    private final CompositeDisposable disposables = new CompositeDisposable();

    // -- Thông tin ca làm việc hôm nay --
    private boolean hasTodayShift = false;
    private String currentWorkScheduleId = null;
    private String currentShiftName = null;
    private Double currentShiftLat = null;
    private Double currentShiftLng = null;
    private Float currentShiftRadius = null;

    // -- Cờ camera để chỉ zoom tự động 1 lần đầu --
    private boolean hasInitialCameraMoved = false;

    // -- Bản đồ Mapbox --
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PointAnnotationManager pointAnnotationManager;
    private boolean isMapStyleLoaded = false;

    // -- Vị trí --
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationValid = false;
    private boolean isGpsEnabled = false;
    private boolean isNetworkConnected = false;

    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // -- Đồng hồ & điều kiện --
    private final DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private IntervalTicker clockTicker;
    private IntervalTicker conditionsTicker;

    // -- Trạng thái chấm công --
    private CheckState currentState = CheckState.NOT_CHECKED_IN;

    public EmployeeCheckInFragment() {
    }

    // =====================================================================
    // 1. VÒNG ĐỜI FRAGMENT
    // =====================================================================

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = getString(R.string.mapbox_access_token);
        if (token != null && !token.isEmpty()) {
            MapboxOptions.setAccessToken(token);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        clockTicker = new IntervalTicker(1000, this::updateClockLabel);
        conditionsTicker = new IntervalTicker(CONDITIONS_CHECK_INTERVAL_MS, this::checkAndRenderConditions);

        registerPermissionLauncher();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentEmployeeCheckInBinding.inflate(inflater, container, false);
        setupMapboxMap();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupListeners();
        setupMapTouchHandling();
        updateDateLabel();
        requestLocationPermissionIfNeeded();
        loadTodaySchedule();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
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
        loadTodaySchedule();
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
    public void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onDestroyView() {
        stopLocationUpdates();

        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();
            pointAnnotationManager.onDestroy();
            pointAnnotationManager = null;
        }

        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }

        mapboxMap = null;
        binding = null;
        disposables.clear();

        super.onDestroyView();
    }

    // =====================================================================
    // 2. THIẾT LẬP GIAO DIỆN & SỰ KIỆN
    // =====================================================================

    private void setupViews() {
        if (binding == null) return;

        binding.tagLocationValid.setText(getString(R.string.checkin_locating));
        binding.cardWarningOutOfRange.setVisibility(View.GONE);
        binding.tvDistanceToOffice.setText(getString(R.string.checkin_determining_distance));
        binding.tvLocationAddress.setText(getString(R.string.checkin_determining_location));

        if (binding.tvConditionRange != null) {
            int radiusInt = Math.round(getTargetRadiusMeters());
            binding.tvConditionRange.setText(getString(R.string.checkin_within_range, radiusInt));
        }

        renderCheckButtonState();
        checkAndRenderConditions();
    }

    private void setupListeners() {
        if (binding == null) return;

        binding.btnCheckAction.setOnClickListener(v -> handleCheckActionClicked());

        if (binding.btnRecenterLocation != null) {
            // Click ngắn: ưu tiên zoom về vị trí user, nếu chưa có thì zoom về ca
            binding.btnRecenterLocation.setOnClickListener(v -> {
                if (mapboxMap == null) return;

                if (currentLocation != null) {
                    mapboxMap.setCamera(
                            new CameraOptions.Builder()
                                    .center(Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude()))
                                    .zoom(16.5)
                                    .build()
                    );
                    Toast.makeText(getContext(), "Đang hiển thị vị trí của bạn", Toast.LENGTH_SHORT).show();
                } else if (hasTodayShift && currentShiftLat != null && currentShiftLng != null) {
                    float radius = getTargetRadiusMeters();
                    mapboxMap.setCamera(
                            new CameraOptions.Builder()
                                    .center(Point.fromLngLat(currentShiftLng, currentShiftLat))
                                    .zoom(calculateOptimalZoom(radius, currentShiftLat))
                                    .build()
                    );
                    Toast.makeText(getContext(), "Đang hiển thị vị trí ca làm việc (" + (int) radius + "m)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Đang xác định vị trí...", Toast.LENGTH_SHORT).show();
                }
            });

            // Click dài: zoom về vị trí ca làm việc
            binding.btnRecenterLocation.setOnLongClickListener(v -> {
                if (mapboxMap != null && hasTodayShift && currentShiftLat != null && currentShiftLng != null) {
                    float radius = getTargetRadiusMeters();
                    mapboxMap.setCamera(
                            new CameraOptions.Builder()
                                    .center(Point.fromLngLat(currentShiftLng, currentShiftLat))
                                    .zoom(calculateOptimalZoom(radius, currentShiftLat))
                                    .build()
                    );
                    Toast.makeText(getContext(), "Đang hiển thị vị trí ca làm việc (" + (int) radius + "m)", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMapTouchHandling() {
        if (binding == null || binding.viewMapTouchInterceptor == null) return;
        binding.viewMapTouchInterceptor.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                if (binding != null && binding.scrollViewCheckin != null) {
                    binding.scrollViewCheckin.requestDisallowInterceptTouchEvent(true);
                }
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (binding != null && binding.scrollViewCheckin != null) {
                    binding.scrollViewCheckin.requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
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
    // 3. MAPBOX XỬ LÝ BẢN ĐỒ
    // =====================================================================

    private void setupMapboxMap() {
        if (binding == null) return;

        mapView = binding.mapView;
        mapboxMap = mapView.getMapboxMap();

        CompassUtils.getCompass(mapView).setEnabled(false);

        mapboxMap.loadStyle(
                Style.MAPBOX_STREETS,
                style -> {
                    if (mapboxMap == null) return;
                    isMapStyleLoaded = true;

                    initAnnotationManager();
                    renderShiftRadiusOnMap();
                    renderMarkers();

                    // Điều hướng camera lúc khởi tạo Style:
                    // 1. Nếu có ca làm việc hôm nay -> zoom về ca
                    // 2. Nếu không có ca làm việc hôm nay và đã có vị trí user -> zoom về user
                    if (!hasInitialCameraMoved) {
                        if (hasTodayShift && currentShiftLat != null && currentShiftLng != null) {
                            float radius = getTargetRadiusMeters();
                            mapboxMap.setCamera(
                                    new CameraOptions.Builder()
                                            .center(Point.fromLngLat(currentShiftLng, currentShiftLat))
                                            .zoom(calculateOptimalZoom(radius, currentShiftLat))
                                            .build()
                            );
                            hasInitialCameraMoved = true;
                        } else if (!hasTodayShift && currentLocation != null) {
                            mapboxMap.setCamera(
                                    new CameraOptions.Builder()
                                            .center(Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude()))
                                            .zoom(16.0)
                                            .build()
                            );
                            hasInitialCameraMoved = true;
                        }
                    }
                }
        );
    }

    private void initAnnotationManager() {
        if (mapView == null) return;
        AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
        if (annotationPlugin == null) return;

        pointAnnotationManager = (PointAnnotationManager)
                annotationPlugin.createAnnotationManager(AnnotationType.PointAnnotation, null);
    }

    /**
     * Hiển thị bán kính ca làm việc (Fill + Outline Line)
     */
    private void renderShiftRadiusOnMap() {
        if (mapboxMap == null || !isMapStyleLoaded) return;

        mapboxMap.getStyle(style -> {
            if (style == null) return;

            // Nếu hôm nay không có ca, xóa vùng bán kính
            if (!hasTodayShift || currentShiftLat == null || currentShiftLng == null) {
                removeShiftRadiusFromMap(style);
                return;
            }

            float radius = getTargetRadiusMeters();
            Polygon circlePolygon = createCirclePolygon(currentShiftLat, currentShiftLng, radius);
            Feature feature = Feature.fromGeometry(circlePolygon);
            FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
            String geoJson = featureCollection.toJson();

            if (style.styleSourceExists(SHIFT_RADIUS_SOURCE)) {
                // Cập nhật dữ liệu GeoJSON nếu source đã tồn tại
                style.setStyleSourceProperty(
                        SHIFT_RADIUS_SOURCE,
                        "data",
                        Value.fromJson(geoJson).getValueOrElse(err -> Value.nullValue())
                );
            } else {
                // Thêm GeoJSON Source mới
                Value sourceProperties = Value.valueOf(
                        new HashMap<String, Value>() {{
                            put("type", Value.valueOf("geojson"));
                            put("data", Value.fromJson(geoJson).getValueOrElse(err -> Value.nullValue()));
                        }}
                );
                style.addStyleSource(SHIFT_RADIUS_SOURCE, sourceProperties);

                // Thêm Fill Layer (vùng tròn bán kính mờ)
                HashMap<String, Value> fillPaint = new HashMap<>();
                fillPaint.put("fill-color", Value.valueOf("#4CAF50"));
                fillPaint.put("fill-opacity", Value.valueOf(0.25));

                HashMap<String, Value> fillLayer = new HashMap<>();
                fillLayer.put("id", Value.valueOf(SHIFT_RADIUS_FILL_LAYER));
                fillLayer.put("type", Value.valueOf("fill"));
                fillLayer.put("source", Value.valueOf(SHIFT_RADIUS_SOURCE));
                fillLayer.put("paint", Value.valueOf(fillPaint));
                style.addStyleLayer(Value.valueOf(fillLayer), null);

                // Thêm Line Layer (viền tròn rõ nét bao quanh bán kính)
                HashMap<String, Value> linePaint = new HashMap<>();
                linePaint.put("line-color", Value.valueOf("#2E7D32"));
                linePaint.put("line-width", Value.valueOf(2.0));
                linePaint.put("line-opacity", Value.valueOf(0.8));

                HashMap<String, Value> lineLayer = new HashMap<>();
                lineLayer.put("id", Value.valueOf(SHIFT_RADIUS_LINE_LAYER));
                lineLayer.put("type", Value.valueOf("line"));
                lineLayer.put("source", Value.valueOf(SHIFT_RADIUS_SOURCE));
                lineLayer.put("paint", Value.valueOf(linePaint));
                style.addStyleLayer(Value.valueOf(lineLayer), null);
            }
        });
    }

    private void removeShiftRadiusFromMap(@NonNull Style style) {
        if (style.styleLayerExists(SHIFT_RADIUS_LINE_LAYER)) {
            style.removeStyleLayer(SHIFT_RADIUS_LINE_LAYER);
        }
        if (style.styleLayerExists(SHIFT_RADIUS_FILL_LAYER)) {
            style.removeStyleLayer(SHIFT_RADIUS_FILL_LAYER);
        }
        if (style.styleSourceExists(SHIFT_RADIUS_SOURCE)) {
            style.removeStyleSource(SHIFT_RADIUS_SOURCE);
        }
    }

    /**
     * Cập nhật các điểm đánh dấu (Marker ca làm việc & Marker vị trí người dùng)
     */
    private void renderMarkers() {
        if (mapView == null || pointAnnotationManager == null) return;
        pointAnnotationManager.deleteAll();

        Context context = getContext();
        if (context == null) return;

        List<PointAnnotationOptions> optionsList = new ArrayList<>();

        // 1. Marker ca làm việc (chỉ hiện khi có ca)
        if (hasTodayShift && currentShiftLat != null && currentShiftLng != null) {
            Bitmap officeBitmap = bitmapFromDrawableRes(context, R.drawable.ic_location);
            PointAnnotationOptions officeOptions = new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(currentShiftLng, currentShiftLat));
            if (officeBitmap != null) {
                officeOptions.withIconImage(officeBitmap);
            }
            optionsList.add(officeOptions);
        }

        // 2. Marker vị trí người dùng (chấm tròn xanh GPS)
        if (currentLocation != null) {
            Bitmap userBitmap = createUserLocationBitmap(context);
            PointAnnotationOptions userOptions = new PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude()));
            if (userBitmap != null) {
                userOptions.withIconImage(userBitmap);
            }
            optionsList.add(userOptions);
        }

        if (!optionsList.isEmpty()) {
            pointAnnotationManager.create(optionsList);
        }
    }

    private Bitmap createUserLocationBitmap(Context context) {
        int size = (int) (22 * context.getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Vòng ngoài màu trắng
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Vòng trong màu xanh định vị
        paint.setColor(Color.parseColor("#1976D2"));
        canvas.drawCircle(size / 2f, size / 2f, size * 0.38f, paint);

        return bitmap;
    }

    private Bitmap bitmapFromDrawableRes(Context context, int resourceId) {
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        if (drawable == null) return null;

        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 64;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 64;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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

    /**
     * Tính zoom tối ưu để bán kính ca làm việc hiển thị trọn vẹn trong khung bản đồ
     */
    private double calculateOptimalZoom(float radiusMeters, double latitude) {
        if (radiusMeters <= 0) return 16.5;
        double metersPerPixel0 = 156543.03392 * Math.cos(Math.toRadians(latitude));
        double targetPixels = 380.0;
        double zoom = Math.log((metersPerPixel0 * targetPixels) / (2.0 * Math.max(radiusMeters, 25.0))) / Math.log(2.0);
        return Math.max(13.0, Math.min(18.5, zoom));
    }

    private float getTargetRadiusMeters() {
        if (currentShiftRadius != null && currentShiftRadius > 0) {
            return currentShiftRadius;
        }
        if (currentShiftName != null && sharedPreferenceApp != null) {
            float shiftRadius = sharedPreferenceApp.getShiftRadius(currentShiftName);
            if (shiftRadius > 0) {
                currentShiftRadius = shiftRadius;
                return shiftRadius;
            }
        }
        if (sharedPreferenceApp != null) {
            float latestRadius = sharedPreferenceApp.getLatestShiftRadius();
            if (latestRadius > 0) {
                return latestRadius;
            }
            if (sharedPreferenceApp.getCompanyRadius() > 0) {
                return (float) sharedPreferenceApp.getCompanyRadius();
            }
        }
        return DEFAULT_RADIUS_METERS;
    }

    // =====================================================================
    // 4. LẤY DỮ LIỆU CA LÀM VIỆC HÔM NAY
    // =====================================================================

    private void loadTodaySchedule() {
        if (getScheduleByDateUseCase == null) return;

        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        disposables.add(getScheduleByDateUseCase.execute(todayStr)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        schedules -> onScheduleLoaded(schedules, today),
                        throwable -> onScheduleLoadError(throwable.getMessage())
                ));
    }

    private void onScheduleLoaded(List<StaffWorkScheduleResponse> schedules, LocalDate today) {
        StaffWorkScheduleResponse.ShiftInfo matchedShift = null;

        if (schedules != null) {
            String todayStr = today.toString();
            List<StaffWorkScheduleResponse.ShiftInfo> todayCandidates = new ArrayList<>();

            for (StaffWorkScheduleResponse schedule : schedules) {
                if (schedule == null || schedule.getShifts() == null) continue;
                if (!todayStr.equals(schedule.getWorkDay())) continue;

                for (StaffWorkScheduleResponse.ShiftInfo s : schedule.getShifts()) {
                    if (s != null) {
                        todayCandidates.add(s);
                    }
                }
            }

            if (!todayCandidates.isEmpty()) {
                // Sắp xếp theo giờ bắt đầu
                todayCandidates.sort(Comparator.comparing(
                        s -> parseTime(s.getStartTime()),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));

                LocalTime now = LocalTime.now();
                for (StaffWorkScheduleResponse.ShiftInfo s : todayCandidates) {
                    LocalTime end = parseTime(s.getEndTime());
                    if (end == null || end.isAfter(now)) {
                        matchedShift = s;
                        break;
                    }
                }
                if (matchedShift == null) {
                    matchedShift = todayCandidates.get(todayCandidates.size() - 1);
                }
            }
        }

        if (matchedShift != null) {
            // Có ca làm việc hôm nay
            currentWorkScheduleId = matchedShift.getWorkScheduleId();
            currentShiftName = matchedShift.getShiftName();

            // Xác định tọa độ ca
            Double lat = matchedShift.getLatitude();
            Double lng = matchedShift.getLongitude();

            if (lat == null || lat == 0.0 || lng == null || lng == 0.0) {
                if (sharedPreferenceApp != null && currentShiftName != null) {
                    lat = sharedPreferenceApp.getShiftLat(currentShiftName);
                    lng = sharedPreferenceApp.getShiftLon(currentShiftName);
                }
            }
            if (lat == null || lat == 0.0 || lng == null || lng == 0.0) {
                if (sharedPreferenceApp != null) {
                    lat = sharedPreferenceApp.getLatestShiftLat();
                    lng = sharedPreferenceApp.getLatestShiftLon();
                    if (lat == 0.0 || lng == 0.0) {
                        lat = sharedPreferenceApp.getCompanyLat();
                        lng = sharedPreferenceApp.getCompanyLon();
                    }
                }
            }

            if (lat != null && lat != 0.0 && lng != null && lng != 0.0) {
                hasTodayShift = true;
                currentShiftLat = lat;
                currentShiftLng = lng;
            } else {
                hasTodayShift = false;
                currentShiftLat = null;
                currentShiftLng = null;
            }

            // Xác định bán kính
            Double radius = matchedShift.getCheckInRadius();
            if (radius != null && radius > 0) {
                currentShiftRadius = radius.floatValue();
                if (sharedPreferenceApp != null && currentShiftName != null) {
                    sharedPreferenceApp.saveShiftLocation(currentShiftName, lat != null ? lat : 0.0, lng != null ? lng : 0.0, currentShiftRadius);
                }
            } else {
                currentShiftRadius = getTargetRadiusMeters();
                if (matchedShift.getShiftId() != null && shiftRepository != null) {
                    disposables.add(shiftRepository.getShiftDetail(matchedShift.getShiftId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(detail -> {
                                if (detail != null && detail.getCheckInRadius() != null && detail.getCheckInRadius() > 0) {
                                    currentShiftRadius = detail.getCheckInRadius().floatValue();
                                    if (sharedPreferenceApp != null && currentShiftName != null) {
                                        sharedPreferenceApp.saveShiftLocation(currentShiftName, currentShiftLat != null ? currentShiftLat : 0.0, currentShiftLng != null ? currentShiftLng : 0.0, currentShiftRadius);
                                    }
                                    renderShiftRadiusOnMap();
                                    checkAndRenderConditions();
                                }
                            }, throwable -> {}));
                }
            }

            // Khôi phục trạng thái điểm danh trước đó từ SharedPreferences
            if (sharedPreferenceApp != null) {
                String todayStr = today.toString();
                CheckState savedState = sharedPreferenceApp.getShiftAttendanceState(currentWorkScheduleId, todayStr, currentShiftName);
                if (savedState != null) {
                    currentState = savedState;
                }
                String checkInTimeStr = sharedPreferenceApp.getShiftCheckInTime(currentWorkScheduleId, todayStr, currentShiftName);
                if (binding != null && checkInTimeStr != null && !checkInTimeStr.isEmpty()) {
                    binding.tvSummaryCheckInTime.setText(checkInTimeStr);
                }
                String checkOutTimeStr = sharedPreferenceApp.getShiftCheckOutTime(currentWorkScheduleId, todayStr, currentShiftName);
                if (binding != null && checkOutTimeStr != null && !checkOutTimeStr.isEmpty()) {
                    binding.tvSummaryCheckOutTime.setText(checkOutTimeStr);
                }
            }

            // Zoom về vị trí ca làm việc nếu có ca
            if (mapboxMap != null && !hasInitialCameraMoved && hasTodayShift) {
                float rad = getTargetRadiusMeters();
                mapboxMap.setCamera(
                        new CameraOptions.Builder()
                                .center(Point.fromLngLat(currentShiftLng, currentShiftLat))
                                .zoom(calculateOptimalZoom(rad, currentShiftLat))
                                .build()
                );
                hasInitialCameraMoved = true;
            }

        } else {
            // Không có ca làm việc hôm nay
            hasTodayShift = false;
            currentWorkScheduleId = null;
            currentShiftName = null;
            currentShiftLat = null;
            currentShiftLng = null;
            currentShiftRadius = null;

            // Zoom về vị trí người dùng nếu không có ca
            if (mapboxMap != null && !hasInitialCameraMoved && currentLocation != null) {
                mapboxMap.setCamera(
                        new CameraOptions.Builder()
                                .center(Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude()))
                                .zoom(16.0)
                                .build()
                );
                hasInitialCameraMoved = true;
            }
        }

        // Vẽ lại bán kính và Marker
        renderShiftRadiusOnMap();
        renderMarkers();
        checkAndRenderConditions();
        renderCheckButtonState();
    }

    private void onScheduleLoadError(String error) {
        // Fallback đọc cấu hình từ SharedPreferences nếu lỗi mạng
        if (sharedPreferenceApp != null) {
            double lat = sharedPreferenceApp.getLatestShiftLat();
            double lng = sharedPreferenceApp.getLatestShiftLon();
            if (lat == 0.0 || lng == 0.0) {
                lat = sharedPreferenceApp.getCompanyLat();
                lng = sharedPreferenceApp.getCompanyLon();
            }

            if (lat != 0.0 && lng != 0.0) {
                hasTodayShift = true;
                currentShiftLat = lat;
                currentShiftLng = lng;
                currentShiftRadius = getTargetRadiusMeters();

                if (mapboxMap != null && !hasInitialCameraMoved) {
                    mapboxMap.setCamera(
                            new CameraOptions.Builder()
                                    .center(Point.fromLngLat(currentShiftLng, currentShiftLat))
                                    .zoom(calculateOptimalZoom(currentShiftRadius, currentShiftLat))
                                    .build()
                    );
                    hasInitialCameraMoved = true;
                }
            } else {
                hasTodayShift = false;
            }
        }

        renderShiftRadiusOnMap();
        renderMarkers();
        checkAndRenderConditions();
        renderCheckButtonState();
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        try {
            return LocalTime.parse(timeStr.trim());
        } catch (Exception e) {
            try {
                if (timeStr.length() >= 5) {
                    return LocalTime.parse(timeStr.substring(0, 5));
                }
            } catch (Exception ignored) {}
            return null;
        }
    }

    // =====================================================================
    // 5. QUYỀN VỊ TRÍ & FUSED LOCATION
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
        Context context = getContext();
        if (context == null) return;

        boolean fineGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean coarseGranted = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineGranted || coarseGranted) {
            startLocationUpdates();
        } else {
            locationPermissionLauncher.launch(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }
            );
        }
    }

    private boolean hasPermission(String permission) {
        Context context = getContext();
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

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

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
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

        // Cập nhật marker người dùng trên bản đồ
        renderMarkers();

        // Nếu KHÔNG có ca làm việc hôm nay và chưa zoom camera -> zoom về vị trí user
        if (!hasTodayShift && !hasInitialCameraMoved && mapboxMap != null) {
            mapboxMap.setCamera(
                    new CameraOptions.Builder()
                            .center(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                            .zoom(16.0)
                            .build()
            );
            hasInitialCameraMoved = true;
        }

        // Tính khoảng cách
        if (hasTodayShift && currentShiftLat != null && currentShiftLng != null) {
            float[] distanceResult = new float[1];
            Location.distanceBetween(
                    location.getLatitude(),
                    location.getLongitude(),
                    currentShiftLat,
                    currentShiftLng,
                    distanceResult
            );

            float distanceMeters = distanceResult[0];
            float allowedRadius = getTargetRadiusMeters();
            isLocationValid = distanceMeters <= allowedRadius;

            if (distanceMeters < 1000) {
                binding.tvDistanceToOffice.setText(getString(R.string.checkin_distance_meters, distanceMeters));
            } else {
                binding.tvDistanceToOffice.setText(getString(R.string.checkin_distance_kilometers, distanceMeters / 1000f));
            }

            binding.tagLocationValid.setText(
                    isLocationValid ? getString(R.string.checkin_location_valid) : getString(R.string.checkin_location_invalid)
            );
        } else {
            isLocationValid = false;
            binding.tvDistanceToOffice.setText("Hôm nay bạn không có ca làm việc");
            binding.tagLocationValid.setText("Không có ca");
        }

        // Cập nhật địa chỉ văn bản
        Context context = getContext();
        if (context != null) {
            LocationUtils.getAddressFromLocation(
                    context,
                    location.getLatitude(),
                    location.getLongitude(),
                    new LocationUtils.AddressCallback() {
                        @Override
                        public void onAddressFound(String address) {
                            if (binding != null && address != null) {
                                binding.tvLocationAddress.setText(address);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (binding != null) {
                                binding.tvLocationAddress.setText(getString(R.string.checkin_current_location));
                            }
                        }
                    }
            );
        }

        checkAndRenderConditions();
    }

    private void showLocationPermissionDenied() {
        if (binding == null) return;

        binding.tvLocationAddress.setText(getString(R.string.checkin_permission_denied));
        binding.tvDistanceToOffice.setText(getString(R.string.checkin_enable_location));
        binding.tagLocationValid.setText(getString(R.string.checkin_not_determined));

        currentLocation = null;
        isLocationValid = false;

        checkAndRenderConditions();
    }

    // =====================================================================
    // 6. GPS & NETWORK
    // =====================================================================

    private boolean checkGpsEnabled() {
        if (!isAdded()) return false;
        Context context = getContext();
        if (context == null) return false;

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) return false;

        try {
            return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkNetworkConnected() {
        if (!isAdded()) return false;
        Context context = getContext();
        if (context == null) return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    // =====================================================================
    // 7. KIỂM TRA ĐIỀU KIỆN CHẤM CÔNG
    // =====================================================================

    private void checkAndRenderConditions() {
        if (binding == null) return;

        isGpsEnabled = checkGpsEnabled();
        isNetworkConnected = checkNetworkConnected();

        if (currentLocation != null && hasTodayShift && currentShiftLat != null && currentShiftLng != null) {
            float[] distanceResult = new float[1];
            Location.distanceBetween(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude(),
                    currentShiftLat,
                    currentShiftLng,
                    distanceResult
            );
            isLocationValid = distanceResult[0] <= getTargetRadiusMeters();
        } else {
            isLocationValid = false;
        }

        renderConditionRow(binding.ivConditionGpsStatus, isGpsEnabled);
        renderConditionRow(binding.ivConditionRangeStatus, isLocationValid);
        renderConditionRow(binding.ivConditionNetworkStatus, isNetworkConnected);

        if (binding.tvConditionRange != null) {
            int radiusInt = Math.round(getTargetRadiusMeters());
            binding.tvConditionRange.setText(getString(R.string.checkin_within_range, radiusInt));
        }

        renderCheckButtonEnabled();
    }

    private void renderConditionRow(ImageView iconView, boolean isOk) {
        if (iconView == null) return;
        iconView.setImageResource(isOk ? R.drawable.ic_check : R.drawable.ic_close);
        iconView.setColorFilter(isOk ? COLOR_CONDITION_OK : COLOR_CONDITION_FAIL);
    }

    private boolean isAllConditionsMet() {
        return hasTodayShift
                && isGpsEnabled
                && isLocationValid
                && isNetworkConnected
                && currentLocation != null;
    }

    private String buildConditionsWarningMessage() {
        if (!hasTodayShift) {
            return "Hôm nay bạn không có ca làm việc để chấm công";
        }
        if (!isGpsEnabled) {
            return getString(R.string.checkin_enable_gps);
        }
        if (currentLocation == null) {
            return getString(R.string.checkin_determining_your_location);
        }
        if (!isLocationValid) {
            return getString(R.string.checkin_outside_allowed_area);
        }
        if (!isNetworkConnected) {
            return getString(R.string.checkin_check_network);
        }
        return getString(R.string.checkin_conditions_not_met);
    }

    // =====================================================================
    // 8. XỬ LÝ CHẤM CÔNG (CHECK IN / CHECK OUT)
    // =====================================================================

    private void handleCheckActionClicked() {
        if (!hasTodayShift) {
            Toast.makeText(getContext(), "Không có ca làm việc hôm nay để chấm công!", Toast.LENGTH_SHORT).show();
            return;
        }

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
        if (attendanceRepository == null || binding == null) return;

        double lat = currentLocation != null ? currentLocation.getLatitude() : (currentShiftLat != null ? currentShiftLat : 0.0);
        double lng = currentLocation != null ? currentLocation.getLongitude() : (currentShiftLng != null ? currentShiftLng : 0.0);
        String nowIso = LocalDateTime.now().toString();
        String todayStr = LocalDate.now().toString();

        binding.btnCheckAction.setEnabled(false);

        disposables.add(attendanceRepository.checkIn(new CheckInRequest(currentWorkScheduleId, nowIso, lat, lng, todayStr))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            currentState = CheckState.CHECKED_IN;
                            String timeStr = LocalTime.now().format(clockFormatter);

                            if (sharedPreferenceApp != null && currentShiftName != null) {
                                sharedPreferenceApp.saveShiftAttendance(currentWorkScheduleId, todayStr, currentShiftName, currentState, timeStr);
                            }

                            if (binding != null) {
                                binding.tvSummaryCheckInTime.setText(timeStr);
                                renderCheckButtonState();
                            }
                            Toast.makeText(getContext(), res.getMessage() != null ? res.getMessage() : "Check-in thành công!", Toast.LENGTH_SHORT).show();
                        },
                        err -> {
                            if (binding != null) {
                                renderCheckButtonState();
                            }
                            Toast.makeText(getContext(), "Check-in thất bại: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    private void performCheckOut() {
        if (attendanceRepository == null || binding == null) return;

        double lat = currentLocation != null ? currentLocation.getLatitude() : (currentShiftLat != null ? currentShiftLat : 0.0);
        double lng = currentLocation != null ? currentLocation.getLongitude() : (currentShiftLng != null ? currentShiftLng : 0.0);
        String nowIso = LocalDateTime.now().toString();
        String todayStr = LocalDate.now().toString();

        binding.btnCheckAction.setEnabled(false);

        disposables.add(attendanceRepository.checkOut(new CheckOutRequest(currentWorkScheduleId, nowIso, lat, lng, todayStr))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        res -> {
                            currentState = CheckState.CHECKED_OUT;
                            String timeStr = LocalTime.now().format(clockFormatter);

                            if (sharedPreferenceApp != null && currentShiftName != null) {
                                sharedPreferenceApp.saveShiftAttendance(currentWorkScheduleId, todayStr, currentShiftName, currentState, timeStr);
                            }

                            if (binding != null) {
                                binding.tvSummaryCheckOutTime.setText(timeStr);
                                renderCheckButtonState();
                            }
                            Toast.makeText(getContext(), res.getMessage() != null ? res.getMessage() : "Check-out thành công!", Toast.LENGTH_SHORT).show();
                        },
                        err -> {
                            if (binding != null) {
                                renderCheckButtonState();
                            }
                            Toast.makeText(getContext(), "Check-out thất bại: " + err.getMessage(), Toast.LENGTH_LONG).show();
                        }
                ));
    }

    private void renderCheckButtonState() {
        if (binding == null) return;

        if (!hasTodayShift) {
            binding.btnCheckAction.setText("KHÔNG CÓ CA LÀM VIỆC");
            binding.btnCheckAction.setEnabled(false);
            return;
        }

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
        if (!hasTodayShift) {
            binding.btnCheckAction.setEnabled(false);
            return;
        }
        if (currentState != CheckState.CHECKED_OUT) {
            binding.btnCheckAction.setEnabled(isAllConditionsMet());
        }
    }
}
