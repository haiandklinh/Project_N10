package com.example.attendanceapp.ui.manager.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.LocationUtils;
import com.example.attendanceapp.databinding.FragmentManagerCompanySettingBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerCompanySettingsFragment extends Fragment {

    private FragmentManagerCompanySettingBinding binding;
    private ManagerCompanySettingsViewModel viewModel;
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
                    Toast.makeText(requireContext(), R.string.business_settings_location_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerCompanySettingBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerCompanySettingsViewModel.class);

        setupToolbar();
        setupObservers();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle(getString(R.string.business_settings_title));
        binding.layoutToolbar.setOnBackClickListener(() -> Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupObservers() {
        viewModel.getUserAccount().observe(getViewLifecycleOwner(), userAccount -> {
            if (userAccount != null) {
                binding.edtCompanyName.setText(userAccount.getCompanyName());
            }
        });

        viewModel.getCompanyLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                binding.edtLocation.setText(location);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSubmit.setEnabled(!isLoading);
            binding.btnSubmit.setText(isLoading ? getString(R.string.business_settings_updating) : getString(R.string.business_settings_update_btn));
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                String message = error;
                // Check if it's a resource key
                if (error.equals("business_settings_error_empty_name")) {
                    message = getString(R.string.business_settings_error_empty_name);
                } else if (error.equals("business_settings_error_no_account")) {
                    message = getString(R.string.business_settings_error_no_account);
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.business_settings_update_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void setupListeners() {
        binding.btnSubmit.setOnClickListener(v -> {
            String companyName = binding.edtCompanyName.getText().toString();
            String location = binding.edtLocation.getText().toString();
            viewModel.updateBusinessInfo(companyName, location);
        });

        binding.tilLocation.setEndIconOnClickListener(v -> checkLocationPermissionAndGet());
    }

    private void checkLocationPermissionAndGet() {
        if (getContext() == null) return;

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        if (!isGpsEnabled) {
            Toast.makeText(requireContext(), R.string.business_settings_enable_gps, Toast.LENGTH_LONG).show();
            try {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } catch (Exception ignored) {}
            return;
        }

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
        binding.tilLocation.setHelperText(getString(R.string.business_settings_locating));

        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
        cancellationTokenSource = new CancellationTokenSource();

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            onLocationRetrieved(location);
                        } else {
                            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), lastLoc -> {
                                if (lastLoc != null) {
                                    onLocationRetrieved(lastLoc);
                                } else {
                                    if (binding != null) {
                                        binding.tilLocation.setHelperText(null);
                                        binding.tilLocation.setError(getString(R.string.business_settings_location_error));
                                    }
                                }
                            });
                        }
                    })
                    .addOnFailureListener(requireActivity(), e -> {
                        if (binding != null) binding.tilLocation.setHelperText(null);
                        Toast.makeText(requireContext(), getString(R.string.business_settings_error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            if (binding != null) binding.tilLocation.setHelperText(null);
            Toast.makeText(requireContext(), R.string.business_settings_location_permission_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void onLocationRetrieved(@NonNull Location location) {
        if (binding == null || getContext() == null) return;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        binding.tilLocation.setHelperText(getString(R.string.business_settings_geocoding));

        LocationUtils.getAddressFromLocation(requireContext(), latitude, longitude, new LocationUtils.AddressCallback() {
            @Override
            public void onAddressFound(String address) {
                if (binding != null) {
                    binding.tilLocation.setHelperText(null);
                    binding.edtLocation.setText(address);
                }
            }

            @Override
            public void onError(String error) {
                if (binding != null) {
                    binding.tilLocation.setHelperText(null);
                    binding.edtLocation.setText(getString(R.string.business_settings_location_format, latitude, longitude));
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

