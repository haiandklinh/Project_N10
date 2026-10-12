package com.example.attendanceapp.ui.main.auth.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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
    private Double latitude = null;
    private Double longitude = null;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
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
        // Pre-fill if exists
        String savedAddress = sharedPreferenceApp.getCompanyAddress();
        if (savedAddress != null) {
            binding.edtLocation.setText(savedAddress);
            latitude = sharedPreferenceApp.getCompanyLat();
            longitude = sharedPreferenceApp.getCompanyLon();
        }

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

            if (latitude == null || longitude == null) {
                binding.tilLocation.setError("Vui lòng chọn vị trí trụ sở");
                return;
            }

            binding.tilCompanyName.setError(null);
            binding.tilLocation.setError(null);

            // Lưu vào SharedPreferences
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

        // Location selection
        binding.tilLocation.setEndIconOnClickListener(v -> checkLocationPermissionAndGet());
    }

    private void checkLocationPermissionAndGet() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    LocationUtils.getAddressFromLocation(requireContext(), latitude, longitude, new LocationUtils.AddressCallback() {
                        @Override
                        public void onAddressFound(String address) {
                            if (binding != null) {
                                binding.edtLocation.setText(address);
                                binding.tilLocation.setError(null);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (binding != null) updateLocationText();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Không thể lấy vị trí hiện tại. Hãy chắc chắn GPS đã bật.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Lỗi quyền truy cập vị trí", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocationText() {
        if (latitude != null && longitude != null) {
            binding.edtLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}