package com.example.attendanceapp.ui.manager.profile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.core.utils.AvatarLoader;
import com.example.attendanceapp.databinding.FragmentEditBasicInfoBinding;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserAccount;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerEditBasicInfoFragment extends Fragment {

    private FragmentEditBasicInfoBinding binding;
    private ManagerEditBasicInfoViewModel viewModel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Uri tempCameraUri;
    private Uri selectedAvatarUri;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selected = result.getData().getData();
                    if (selected != null) {
                        onAvatarSelected(selected);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && tempCameraUri != null) {
                    onAvatarSelected(tempCameraUri);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(requireContext(), "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                }
            });

    public ManagerEditBasicInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ManagerEditBasicInfoViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditBasicInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupObservers();
        setupEditableFields();
        setupListeners();
        binding.btnRequestChange.setVisibility(View.GONE);
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Thông tin cá nhân (Quản lý)");
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupObservers() {
        viewModel.getUserAccount().observe(getViewLifecycleOwner(), this::populateData);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSave.setEnabled(!isLoading);
        });
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateData(UserAccount user) {
        if (user == null) return;
        binding.etFullName.setText(user.getFullName());
        if (user.getDateOfBirth() != null) {
            binding.etDob.setText(user.getDateOfBirth().format(dateFormatter));
        }
        binding.etGender.setText(genderLabel(user.getGender()));
        binding.etPhone.setText(user.getPhoneNumber());
        AvatarLoader.loadAvatar(requireContext(), user.getAvatarUrl(), user.getFullName(), binding.ivEditAvatar);
        
        // Manager specific: Organization field (e.g. et_department or et_position reused for company)
        binding.etDepartment.setText(user.getCompanyName());
    }

    private void setupListeners() {
        binding.btnChangePhoto.setOnClickListener(v -> showImageSourceDialog());
        binding.ivEditAvatar.setOnClickListener(v -> showImageSourceDialog());

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String fullName = binding.etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(requireContext(), "Họ và tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        String dobStr = binding.etDob.getText().toString().trim();
        LocalDate dob = null;
        try {
            dob = LocalDate.parse(dobStr, dateFormatter);
        } catch (Exception ignored) {}
        if (dob == null) {
            Toast.makeText(requireContext(), "Ngày sinh không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Gender gender = parseGender(binding.etGender.getText().toString());
        if (gender == null) {
            Toast.makeText(requireContext(), "Giới tính không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String companyName = binding.etDepartment.getText().toString().trim();
        viewModel.updateProfile(fullName, dob, gender, selectedAvatarUri, companyName);
    }

    private void setupEditableFields() {
        binding.etDob.setShowSoftInputOnFocus(false);
        binding.etGender.setShowSoftInputOnFocus(false);
        binding.etDob.setOnClickListener(v -> showDatePicker());
        binding.etGender.setOnClickListener(v -> showGenderDialog());
    }

    private void showDatePicker() {
        Long initialSelection = null;
        try {
            LocalDate currentDob = LocalDate.parse(binding.etDob.getText().toString().trim(), dateFormatter);
            initialSelection = currentDob.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } catch (Exception ignored) {}

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày sinh")
                .setSelection(initialSelection != null ? initialSelection : MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate pickedDate = Instant.ofEpochMilli(selection).atZone(ZoneOffset.UTC).toLocalDate();
            binding.etDob.setText(pickedDate.format(dateFormatter));
        });
        datePicker.show(getParentFragmentManager(), "manager_edit_dob_picker");
    }

    private void showGenderDialog() {
        String[] options = {"Nam", "Nữ", "Khác"};
        int checkedItem = genderIndex(parseGender(binding.etGender.getText().toString()));
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Giới tính")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    setGenderSelection(which);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setGenderSelection(int which) {
        if (which == 0) {
            binding.etGender.setText("Nam");
        } else if (which == 1) {
            binding.etGender.setText("Nữ");
        } else {
            binding.etGender.setText("Khác");
        }
    }

    private int genderIndex(Gender gender) {
        if (gender == Gender.FEMALE) return 1;
        if (gender == Gender.OTHER) return 2;
        return 0;
    }

    private Gender parseGender(String value) {
        if (value == null) return null;
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("nam") || normalized.equals("male") || normalized.equals("m")) {
            return Gender.MALE;
        }
        if (normalized.equals("nữ") || normalized.equals("nu") || normalized.equals("female") || normalized.equals("f")) {
            return Gender.FEMALE;
        }
        if (normalized.equals("khác") || normalized.equals("khac") || normalized.equals("other") || normalized.equals("o")) {
            return Gender.OTHER;
        }
        try {
            return Gender.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String genderLabel(Gender gender) {
        if (gender == Gender.FEMALE) return "Nữ";
        if (gender == Gender.OTHER) return "Khác";
        if (gender == Gender.MALE) return "Nam";
        return "";
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thay đổi ảnh đại diện")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        tempCameraUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void onAvatarSelected(Uri uri) {
        selectedAvatarUri = uri;
        if (binding != null) {
            binding.ivEditAvatar.setImageURI(uri);
        }
        Toast.makeText(requireContext(), "Ảnh đại diện sẽ được lưu khi nhấn Lưu", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
