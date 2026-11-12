package com.example.attendanceapp.ui.employee.profile;

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
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.AvatarLoader;
import com.example.attendanceapp.databinding.FragmentEditProfileBinding;
import com.example.attendanceapp.databinding.ItemProfileMenuRowBinding;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.usecase.profile.GetCurrentUserUseCase;
import com.example.attendanceapp.domain.usecase.profile.UpdateProfileUseCase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeeEditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;

    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;

    @Inject
    UpdateProfileUseCase updateProfileUseCase;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Uri tempCameraUri;

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

    public EmployeeEditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupMenuRows();
        setupListeners();
        loadCurrentAvatar();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Chỉnh sửa hồ sơ");
        binding.layoutToolbar.setOnBackClickListener(() ->
            Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupMenuRows() {
        // 1. Thông tin cá nhân cơ bản
        bindRow(binding.rowBasicInfo, R.drawable.ic_profile, "Thông tin cá nhân cơ bản");
        
        // 2. Thông tin liên hệ
        bindRow(binding.rowContactInfo, R.drawable.ic_phone, "Thông tin liên hệ");
        
        // 3. Thông tin ngân hàng & Thuế
        bindRow(binding.rowFinanceInfo, R.drawable.ic_money, "Ngân hàng & Thuế");
        
        // 4. Thông tin công việc
        bindRow(binding.rowJobInfo, R.drawable.ic_card_company, "Thông tin công việc");
        
        // 5. Cài đặt ứng dụng & Quyền riêng tư
        bindRow(binding.rowPrivacySettings, R.drawable.ic_lock, "Cài đặt & Quyền riêng tư");
    }

    private void bindRow(ItemProfileMenuRowBinding row, int iconRes, String label) {
        row.ivRowIcon.setImageResource(iconRes);
        row.tvRowLabel.setText(label);
    }

    private void setupListeners() {
        binding.btnChangePhoto.setOnClickListener(v -> showImageSourceDialog());
        binding.ivEditAvatar.setOnClickListener(v -> showImageSourceDialog());

        binding.rowBasicInfo.getRoot().setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeEditBasicInfoFragment)
        );

        binding.rowContactInfo.getRoot().setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeContactInfoFragment)
        );

        binding.rowFinanceInfo.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeBankAndTaxFragment);
        });

        binding.rowJobInfo.getRoot().setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeJobInfoFragment)
        );

        binding.rowPrivacySettings.getRoot().setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeSettingsFragment)
        );
    }

    private void loadCurrentAvatar() {
        disposables.add(getCurrentUserUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        user -> {
                            if (binding != null && user != null && user.getProfile() != null) {
                                AvatarLoader.loadAvatar(
                                        requireContext(),
                                        user.getProfile().getAvatarUrl(),
                                        user.getFullName(),
                                        binding.ivEditAvatar
                                );
                            }
                        },
                        throwable -> {}
                ));
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
        if (binding != null) {
            binding.ivEditAvatar.setImageURI(uri);
        }
        Toast.makeText(requireContext(), "Đang lưu ảnh đại diện...", Toast.LENGTH_SHORT).show();

        disposables.add(updateProfileUseCase.updateAvatar(uri, UserRole.EMPLOYEE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show(),
                        throwable -> Toast.makeText(requireContext(), "Lỗi: " + throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
