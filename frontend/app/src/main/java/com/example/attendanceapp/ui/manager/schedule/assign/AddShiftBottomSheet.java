package com.example.attendanceapp.ui.manager.schedule.assign;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.attendanceapp.core.utils.DateTimeFormatUtils;
import com.example.attendanceapp.core.utils.LocationUtils;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.BottomSheetAddShiftBinding;
import com.example.attendanceapp.domain.model.ShiftOption;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

@AndroidEntryPoint
public class AddShiftBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "AddShiftBottomSheet_RequestKey";
    public static final String EXTRA_RESULT_SHIFT = "extra_result_shift";
    private static final String ARG_SHIFT_ITEM = "arg_shift_item";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private BottomSheetAddShiftBinding binding;
    private FusedLocationProviderClient locationClient;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private LocalTime startTime = LocalTime.of(8, 0);
    private LocalTime endTime = LocalTime.of(17, 30);
    private LocalTime allowedCheckInTime = LocalTime.of(7, 30);

    private Double latitude = null;
    private Double longitude = null;
    private boolean hasPickedLocation = false;
    private ShiftOption editingShift = null;

    public static AddShiftBottomSheet newInstance() {
        return new AddShiftBottomSheet();
    }

    public static AddShiftBottomSheet newInstanceForEdit(ShiftOption shift) {
        AddShiftBottomSheet fragment = new AddShiftBottomSheet();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SHIFT_ITEM, shift);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddShiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // 1. Kiểm tra chế độ chỉnh sửa hay tạo mới
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SHIFT_ITEM)) {
            editingShift = args.getParcelable(ARG_SHIFT_ITEM);
        }

        if (editingShift != null) {
            binding.tvTitle.setText("Chỉnh sửa ca làm việc");
            binding.edtShiftName.setText(editingShift.getTitle());
            latitude = editingShift.getLatitude();
            longitude = editingShift.getLongitude();

            if ((latitude == null || latitude == 0.0) && sharedPreferenceApp != null) {
                double savedLat = sharedPreferenceApp.getShiftLat(editingShift.getTitle());
                double savedLon = sharedPreferenceApp.getShiftLon(editingShift.getTitle());
                if (savedLat != 0.0 && savedLon != 0.0) {
                    latitude = savedLat;
                    longitude = savedLon;
                }
            }

            parseTimes(editingShift.getStartTime(), editingShift.getEndTime(), editingShift.getAllowedCheckInTime());

            double radius = editingShift.getCheckInRadius() != null ? editingShift.getCheckInRadius() : 100.0;
            if (radius <= 0 && sharedPreferenceApp != null) {
                float savedR = sharedPreferenceApp.getShiftRadius(editingShift.getTitle());
                if (savedR > 0) radius = savedR;
            }
            binding.edtCheckInRadius.setText(String.valueOf((int) radius));

            if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
                hasPickedLocation = true;
                String shiftAddress = sharedPreferenceApp != null ? sharedPreferenceApp.getShiftAddress(editingShift.getTitle()) : null;
                if (!TextUtils.isEmpty(shiftAddress)) {
                    binding.edtLocation.setText(shiftAddress);
                } else {
                    reverseGeocode(latitude, longitude);
                }
            } else {
                hasPickedLocation = false;
                binding.edtLocation.setText("");
                //binding.edtLocation.setHint("Bấm icon bên phải để lấy vị trí ca làm");
                binding.tilLocation.setHelperText("Bấm icon định vị để lấy vị trí GPS ca làm");
            }

            binding.btnSaveShift.setText("Cập nhật ca làm");
        } else {
            binding.tvTitle.setText("Tạo ca làm việc");
            latitude = null;
            longitude = null;
            hasPickedLocation = false;
            binding.edtLocation.setText("");
            //binding.edtLocation.setHint("Bấm icon bên phải để lấy vị trí ca làm");
            binding.tilLocation.setHelperText("Bấm icon định vị để lấy vị trí ca làm việc!");
            binding.edtCheckInRadius.setText("100");
            binding.btnSaveShift.setText("Tạo ca làm việc");
        }

        updateTimeText(binding.edtStartTime, startTime);
        updateTimeText(binding.edtEndTime, endTime);
        updateTimeText(binding.edtAllowedCheckInTime, allowedCheckInTime);

        // Lắng nghe sửa tay địa chỉ (cho phép quản lý sửa text hiển thị tùy ý mà không làm đổi lat/lon)
        binding.edtLocation.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLocation.setError(null);
            }
        });

        // Xóa thông báo lỗi khi nhập tên ca
        binding.edtShiftName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilShiftName.setError(null);
            }
        });

        binding.edtCheckInRadius.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilCheckInRadius.setError(null);
                if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
                    try {
                        double r = Double.parseDouble(s.toString().trim());
                        if (r > 0 && sharedPreferenceApp != null) {
                            String currentName = binding.edtShiftName.getText() != null ? binding.edtShiftName.getText().toString().trim() : "";
                            String currentAddr = binding.edtLocation.getText() != null ? binding.edtLocation.getText().toString().trim() : "";
                            sharedPreferenceApp.saveLatestShiftLocation(latitude, longitude, r, currentAddr);
                            if (!TextUtils.isEmpty(currentName)) {
                                sharedPreferenceApp.saveShiftLocation(currentName, latitude, longitude, r);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        });

        // Chọn giờ bắt đầu
        binding.edtStartTime.addTextChangedListener(new TimeInputWatcher(binding.tilStartTime, time -> startTime = time));
        View.OnClickListener startPicker = v -> showTimePicker(startTime, selectedTime -> {
            startTime = selectedTime;
            updateTimeText(binding.edtStartTime, startTime);
            binding.tilStartTime.setError(null);
            binding.tilEndTime.setError(null);
        });
        binding.edtStartTime.setOnClickListener(startPicker);
        binding.tilStartTime.setEndIconOnClickListener(startPicker);

        // Chọn giờ kết thúc
        binding.edtEndTime.addTextChangedListener(new TimeInputWatcher(binding.tilEndTime, time -> endTime = time));
        View.OnClickListener endPicker = v -> showTimePicker(endTime, selectedTime -> {
            endTime = selectedTime;
            updateTimeText(binding.edtEndTime, endTime);
            binding.tilEndTime.setError(null);
        });
        binding.edtEndTime.setOnClickListener(endPicker);
        binding.tilEndTime.setEndIconOnClickListener(endPicker);

        // Chọn giờ cho phép check-in
        binding.edtAllowedCheckInTime.addTextChangedListener(new TimeInputWatcher(binding.tilAllowedCheckInTime, time -> allowedCheckInTime = time));
        View.OnClickListener allowedCheckInPicker = v -> showTimePicker(allowedCheckInTime, selectedTime -> {
            allowedCheckInTime = selectedTime;
            updateTimeText(binding.edtAllowedCheckInTime, allowedCheckInTime);
            binding.tilAllowedCheckInTime.setError(null);
        });
        binding.edtAllowedCheckInTime.setOnClickListener(allowedCheckInPicker);
        binding.tilAllowedCheckInTime.setEndIconOnClickListener(allowedCheckInPicker);
        binding.tilAllowedCheckInTime.setStartIconOnClickListener(allowedCheckInPicker);

        // Nút lấy vị trí hiện tại GPS của Quản lý làm mốc ca làm việc
        binding.tilLocation.setEndIconOnClickListener(v -> fetchCurrentLocation());

        // Nút Lưu / Cập nhật
        binding.btnSaveShift.setOnClickListener(v -> handleSave());
    }

    private void handleSave() {
        String name = binding.edtShiftName.getText() != null ? binding.edtShiftName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            binding.tilShiftName.setError("Vui lòng nhập tên ca!");
            binding.edtShiftName.requestFocus();
            return;
        }

        if (!endTime.isAfter(startTime)) {
            binding.tilEndTime.setError("Giờ kết thúc phải lớn hơn giờ bắt đầu!");
            return;
        }

        if (allowedCheckInTime.isAfter(startTime)) {
            binding.tilAllowedCheckInTime.setError("Giờ cho phép check-in phải trước hoặc bằng giờ bắt đầu ca!");
            return;
        }

        String radiusStr = binding.edtCheckInRadius.getText() != null ? binding.edtCheckInRadius.getText().toString().trim() : "";
        double radius;
        if (!TextUtils.isEmpty(radiusStr)) {
            try {
                radius = Double.parseDouble(radiusStr);
                if (radius <= 0) {
                    binding.tilCheckInRadius.setError("Bán kính chấm công phải lớn hơn 0m!");
                    return;
                }
            } catch (NumberFormatException e) {
                binding.tilCheckInRadius.setError("Bán kính không hợp lệ!");
                return;
            }
        } else {
            binding.tilCheckInRadius.setError("Vui lòng nhập bán kính chấm công!");
            return;
        }

        // Bắt buộc quản lý phải bấm nút lấy vị trí GPS làm mốc cho ca làm
        if (!hasPickedLocation || latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
            binding.tilLocation.setError("Vui lòng bấm chọn vị trí ca làm!");
            binding.edtLocation.requestFocus();
            Toast.makeText(requireContext(), "Vui lòng bấm icon định vị để chọn vị trí ca làm!", Toast.LENGTH_SHORT).show();
            return;
        }

        String locationText = binding.edtLocation.getText() != null ? binding.edtLocation.getText().toString().trim() : "";
        if (TextUtils.isEmpty(locationText)) {
            binding.tilLocation.setError("Vui lòng nhập hoặc chọn vị trí ca làm!");
            binding.edtLocation.requestFocus();
            return;
        }

        String startTimeStr = DateTimeFormatUtils.formatTime(startTime);
        String endTimeStr = DateTimeFormatUtils.formatTime(endTime);
        String allowedCheckInStr = DateTimeFormatUtils.formatTime(allowedCheckInTime);

        // Giữ nguyên tọa độ GPS mà Quản lý đã lấy làm mốc, locationText là text địa chỉ hiển thị
        completeSaveShift(name, startTimeStr, endTimeStr, allowedCheckInStr, radius, locationText);
    }

    private void completeSaveShift(String name, String startTimeStr, String endTimeStr,
                                  String allowedCheckInStr, double radius, String locationText) {
        String id = editingShift != null ? editingShift.getId() : null;
        ShiftOption resultShift = new ShiftOption(
                id,
                name,
                startTimeStr,
                endTimeStr,
                latitude,
                longitude,
                allowedCheckInStr,
                radius
        );

        if (sharedPreferenceApp != null) {
            if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
                sharedPreferenceApp.saveShiftLocation(name, latitude, longitude, radius);
                sharedPreferenceApp.saveLatestShiftLocation(latitude, longitude, radius, locationText);
                sharedPreferenceApp.saveCompanyLocation(latitude, longitude, locationText);
            }
            if (!TextUtils.isEmpty(locationText)) {
                sharedPreferenceApp.saveShiftAddress(name, locationText);
            }
            if (!TextUtils.isEmpty(allowedCheckInStr)) {
                sharedPreferenceApp.saveShiftAllowedCheckInTime(name, allowedCheckInStr);
            }
        }

        Bundle result = new Bundle();
        result.putParcelable(EXTRA_RESULT_SHIFT, resultShift);

        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
        dismiss();
    }

    private void parseTimes(String startTimeStr, String endTimeStr, String allowedCheckInStr) {
        if (!TextUtils.isEmpty(startTimeStr)) {
            try {
                startTime = LocalTime.parse(startTimeStr);
            } catch (DateTimeParseException ignored) {}
        }
        if (!TextUtils.isEmpty(endTimeStr)) {
            try {
                endTime = LocalTime.parse(endTimeStr);
            } catch (DateTimeParseException ignored) {}
        }
        if (!TextUtils.isEmpty(allowedCheckInStr)) {
            try {
                allowedCheckInTime = LocalTime.parse(allowedCheckInStr);
            } catch (DateTimeParseException ignored) {}
        } else {
            allowedCheckInTime = startTime.minusMinutes(30);
        }
    }

    private interface TimePickerCallback {
        void onTimeSelected(LocalTime time);
    }

    private void showTimePicker(LocalTime currentTime, TimePickerCallback callback) {
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> callback.onTimeSelected(LocalTime.of(hourOfDay, minute)),
                currentTime.getHour(),
                currentTime.getMinute(),
                true
        ).show();
    }

    private void updateTimeText(android.widget.EditText editText, LocalTime time) {
        editText.setText(DateTimeFormatUtils.formatTime(time));
    }

    private void fetchCurrentLocation() {
        if (!isAdded() || getContext() == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        binding.tilLocation.setError(null);
        binding.tilLocation.setHelperText("Đang xác định vị trí GPS của bạn...");

        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        latitude = loc.getLatitude();
                        longitude = loc.getLongitude();
                        hasPickedLocation = true;
                        binding.tilLocation.setError(null);
                        reverseGeocode(latitude, longitude);
                    } else {
                        locationClient.getLastLocation()
                                .addOnSuccessListener(lastLoc -> {
                                    if (lastLoc != null) {
                                        latitude = lastLoc.getLatitude();
                                        longitude = lastLoc.getLongitude();
                                        hasPickedLocation = true;
                                        binding.tilLocation.setError(null);
                                        reverseGeocode(latitude, longitude);
                                    } else {
                                        if (binding != null) binding.tilLocation.setHelperText(null);
                                        Toast.makeText(requireContext(), "Không lấy được vị trí GPS. Vui lòng bật định vị trên thiết bị.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (binding != null) binding.tilLocation.setHelperText(null);
                                    Toast.makeText(requireContext(), "Không lấy được vị trí GPS", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null) binding.tilLocation.setHelperText(null);
                    Toast.makeText(requireContext(), "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private double getCurrentRadius() {
        if (binding != null && binding.edtCheckInRadius != null) {
            String rStr = binding.edtCheckInRadius.getText() != null ? binding.edtCheckInRadius.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(rStr)) {
                try {
                    double r = Double.parseDouble(rStr);
                    if (r > 0) return r;
                } catch (Exception ignored) {}
            }
        }
        return 100.0;
    }

    private void reverseGeocode(double lat, double lon) {
        if (!isAdded() || getContext() == null || binding == null) return;

        binding.tilLocation.setHelperText("Đang chuyển đổi sang địa chỉ...");

        LocationUtils.getAddressFromLocation(requireContext(), lat, lon, new LocationUtils.AddressCallback() {
            @Override
            public void onAddressFound(String address) {
                if (binding == null) return;
                binding.tilLocation.setHelperText("Bạn có thể chỉnh sửa tên địa chỉ bên trên nếu muốn.");
                binding.tilLocation.setError(null);
                binding.edtLocation.setText(address);
                hasPickedLocation = true;
                double currentRadius = getCurrentRadius();
                sharedPreferenceApp.saveCompanyLocation(lat, lon, address);
                sharedPreferenceApp.saveLatestShiftLocation(lat, lon, currentRadius, address);
                String currentName = binding.edtShiftName.getText() != null ? binding.edtShiftName.getText().toString().trim() : "";
                if (!TextUtils.isEmpty(currentName)) {
                    sharedPreferenceApp.saveShiftAddress(currentName, address);
                    sharedPreferenceApp.saveShiftLocation(currentName, lat, lon, currentRadius);
                }
                Toast.makeText(requireContext(), "Đã lấy vị trí ca làm việc!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                if (binding == null) return;
                binding.tilLocation.setHelperText("Bạn có thể chỉnh sửa tên địa chỉ bên trên nếu muốn.");
                binding.tilLocation.setError(null);
                String latLonStr = String.format(Locale.getDefault(), "%.6f, %.6f", lat, lon);
                binding.edtLocation.setText(latLonStr);
                hasPickedLocation = true;
                double currentRadius = getCurrentRadius();
                sharedPreferenceApp.saveCompanyLocation(lat, lon, latLonStr);
                sharedPreferenceApp.saveLatestShiftLocation(lat, lon, currentRadius, latLonStr);
                String currentName = binding.edtShiftName.getText() != null ? binding.edtShiftName.getText().toString().trim() : "";
                if (!TextUtils.isEmpty(currentName)) {
                    sharedPreferenceApp.saveShiftAddress(currentName, latLonStr);
                    sharedPreferenceApp.saveShiftLocation(currentName, lat, lon, currentRadius);
                }
                Toast.makeText(requireContext(), "Đã lấy tọa độ GPS ca làm việc!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        disposables.clear();
        super.onDestroyView();
        binding = null;
    }

    private static class TimeInputWatcher implements TextWatcher {
        private final com.google.android.material.textfield.TextInputLayout layout;
        private final java.util.function.Consumer<LocalTime> onValid;
        private final java.time.format.DateTimeFormatter[] formatters = new java.time.format.DateTimeFormatter[]{
                java.time.format.DateTimeFormatter.ofPattern("H:mm"),
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        };

        TimeInputWatcher(com.google.android.material.textfield.TextInputLayout layout,
                         java.util.function.Consumer<LocalTime> onValid) {
            this.layout = layout;
            this.onValid = onValid;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) {
            String txt = s.toString().trim();
            if (txt.isEmpty()) {
                layout.setError("Vui lòng nhập thời gian");
                return;
            }
            for (java.time.format.DateTimeFormatter f : formatters) {
                try {
                    LocalTime t = LocalTime.parse(txt, f);
                    onValid.accept(t);
                    layout.setError(null);
                    return;
                } catch (DateTimeParseException ignored) {}
            }
            layout.setError("Định dạng giờ không hợp lệ (ví dụ 1:15 hoặc 01:15)");
        }
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
