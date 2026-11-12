package com.example.attendanceapp.ui.main.auth.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.attendanceapp.core.utils.LocationUtils;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.BottomSheetAuthCompanyNameBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CompanyNameBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CompanyNameBottomSheet";
    public static final String REQUEST_KEY = "company_name_request_key";
    public static final String RESULT_ACTION = "result_action"; // SUBMITTED / CANCELED
    public static final String ACTION_SUBMITTED = "action_submitted";
    public static final String ACTION_CANCELED = "action_canceled";
    public static final String BUNDLE_KEY_COMPANY_NAME = "bundle_key_company_name";

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private BottomSheetAuthCompanyNameBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private Double latitude = null;
    private Double longitude = null;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), "Quyền truy cập vị trí bị từ chối", Toast.LENGTH_SHORT).show();
                }
            });

    public static CompanyNameBottomSheet newInstance() {
        return new CompanyNameBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAuthCompanyNameBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
    }

    private void setupViews() {

        binding.btnSubmit.setOnClickListener(v -> {
            String companyName = binding.edtCompanyName.getText().toString().trim();
            String location = binding.edtLocation.getText().toString().trim();

            if (TextUtils.isEmpty(companyName)) {
                binding.tilCompanyName.setError("Vui lòng nhập tên doanh nghiệp");
                return;
            }

            if (companyName.length() < 3) {
                binding.tilCompanyName.setError("Tên doanh nghiệp phải có ít nhất 3 ký tự");
                return;
            }

            if (latitude == null || longitude == null || TextUtils.isEmpty(location) ) {
                binding.tilLocation.setError("Vui lòng bấm biểu tượng GPS bên phải để lấy địa chỉ công ty");
                return;
            }

            binding.tilCompanyName.setError(null);
            binding.tilLocation.setError(null);

            // Lưu tọa độ và địa chỉ chi tiết vào SharedPreferences
            sharedPreferenceApp.saveCompanyLocation(latitude, longitude, location);

            // Gửi kết quả về Fragment cha
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_SUBMITTED);
            result.putString(BUNDLE_KEY_COMPANY_NAME, companyName);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_CANCELED);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);

            dismiss();
        });

        binding.edtCompanyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilCompanyName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.edtLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilLocation.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bấm vào icon bên phải để lấy địa chỉ hiện trực tiếp vào edt
        binding.tilLocation.setEndIconOnClickListener(v -> checkLocationPermissionAndGet());
    }

    private void checkLocationPermissionAndGet() {
        if (getContext() == null) return;

        // 1. Kiểm tra xem dịch vụ định vị (GPS) của thiết bị đã được bật hay chưa
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!isGpsEnabled) {
            Toast.makeText(requireContext(), "Vui lòng bật định vị (GPS) trên thiết bị", Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } catch (Exception ignored) {}
            return;
        }

        // 2. Kiểm tra quyền truy cập vị trí
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (binding == null || getContext() == null) return;

        binding.tilLocation.setError(null);
        binding.tilLocation.setHelperText("Đang lấy tọa độ GPS chính xác...");

        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
        cancellationTokenSource = new CancellationTokenSource();

        try {
            // Lấy tọa độ mới nhất và chính xác nhất thời điểm hiện tại
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            onLocationRetrieved(location);
                        } else {
                            // Fallback sang getLastLocation nếu thiết bị chưa kịp phản hồi
                            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), lastLoc -> {
                                if (lastLoc != null) {
                                    onLocationRetrieved(lastLoc);
                                } else {
                                    if (binding != null) {
                                        binding.tilLocation.setHelperText(null);
                                        binding.tilLocation.setError("Không lấy được vị trí GPS. Vui lòng thử lại.");
                                    }
                                    Toast.makeText(requireContext(), "Không thể lấy vị trí hiện tại. Hãy chắc chắn GPS đã bật.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(requireActivity(), e -> {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), lastLoc -> {
                            if (lastLoc != null) {
                                onLocationRetrieved(lastLoc);
                            } else {
                                if (binding != null) {
                                    binding.tilLocation.setHelperText(null);
                                    binding.tilLocation.setError("Lỗi lấy vị trí: " + e.getMessage());
                                }
                                Toast.makeText(requireContext(), "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        } catch (SecurityException e) {
            if (binding != null) binding.tilLocation.setHelperText(null);
            Toast.makeText(requireContext(), "Lỗi quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    private void onLocationRetrieved(@NonNull Location location) {
        if (binding == null || getContext() == null) return;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        binding.tilLocation.setHelperText("Đang xác định địa chỉ...");

        LocationUtils.getAddressFromLocation(requireContext(), latitude, longitude, new LocationUtils.AddressCallback() {
            @Override
            public void onAddressFound(String address) {
                if (binding != null) {
                    binding.tilLocation.setHelperText(null);
                    binding.tilLocation.setError(null);
                    // Địa chỉ hiện trực tiếp vào edtLocation
                    binding.edtLocation.setText(address);
                }
            }

            @Override
            public void onError(String error) {
                if (binding != null) {
                    binding.tilLocation.setHelperText(null);
                    binding.tilLocation.setError(null);
                    String coords = String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
                    binding.edtLocation.setText(coords);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
            cancellationTokenSource = null;
        }
        binding = null;
    }
}
