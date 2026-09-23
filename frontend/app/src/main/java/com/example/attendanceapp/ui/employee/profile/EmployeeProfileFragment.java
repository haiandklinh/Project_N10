package com.example.attendanceapp.ui.employee.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.DarkModeAnimationHelper;
import com.example.attendanceapp.databinding.FragmentEmployeeProfileBinding;
import com.example.attendanceapp.databinding.ItemProfileMenuRowBinding;
import com.example.attendanceapp.databinding.ItemProfileSwitchRowBinding;
import com.example.attendanceapp.databinding.ItemThemeOptionBinding;
import com.example.attendanceapp.databinding.LayoutBottomSheetThemeBinding;
import com.example.attendanceapp.domain.usecase.auth.AuthCallback;
import com.example.attendanceapp.domain.usecase.auth.LogoutUseCase;
import com.example.attendanceapp.ui.main.auth.AuthActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeProfileFragment extends Fragment {

    private FragmentEmployeeProfileBinding binding;

    @Inject
    LogoutUseCase logoutUseCase;

    public EmployeeProfileFragment() {
        // Constructor rỗng mặc định
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutProfileHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            // Giữ nguyên padding gốc từ XML, chỉ cộng thêm chiều cao Status Bar vào paddingTop
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupViews();
        setupListeners();
        setupObservers();
    }

    private void setupViews() {
        // ----- Card tóm tắt chuyên cần -----
        binding.tvSummaryTitle.setText(getString(R.string.profile_attendance_summary));
        binding.tvSummaryRate.setText(getString(R.string.profile_attendance_rate));
        binding.tvSummaryDescription.setText(getString(R.string.profile_attendance_description));

        // ----- Nhóm "Tài khoản" -----
        bindMenuRow(binding.rowEditProfile, R.drawable.ic_profile, getString(R.string.profile_edit_profile));
        bindMenuRow(binding.rowChangePassword, R.drawable.ic_lock, getString(R.string.profile_change_password));
        bindMenuRow(binding.rowNotificationSettings, R.drawable.ic_notifications, getString(R.string.profile_notifications));

        // ----- Nhóm "Ứng dụng" -----
        bindMenuRow(binding.rowLanguage, R.drawable.ic_language, getString(R.string.profile_language));
        bindMenuRow(binding.rowThemes, R.drawable.ic_themes, getString(R.string.profile_theme));
        bindMenuRow(binding.rowHelpSupport, R.drawable.ic_help, getString(R.string.profile_help_support));
        bindMenuRow(binding.rowAboutApp, R.drawable.ic_warning, getString(R.string.profile_about_app));

        // ----- Dòng Chế độ tối -----
        ItemProfileSwitchRowBinding darkModeRow = binding.rowDarkMode;
        darkModeRow.tvRowLabel.setText(getString(R.string.profile_dark_mode));
        darkModeRow.ivRowIcon.setImageResource(R.drawable.ic_dark_mode);

        boolean isDarkModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        darkModeRow.switchRow.setChecked(isDarkModeOn);
    }

    private void bindMenuRow(ItemProfileMenuRowBinding row, int iconRes, String label) {
        row.ivRowIcon.setImageResource(iconRes);
        row.tvRowLabel.setText(label);
    }

    private void setupListeners() {
        // ----- Card tóm tắt chuyên cần -> Lịch sử -----
        binding.cardAttendanceSummary.setOnClickListener(v -> navigateToHistory());


        // ----- Nhóm "Tài khoản" -----
        binding.rowEditProfile.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeEditProfileFragment);
        });

        binding.rowChangePassword.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeChangePasswordFragment);
        });

        binding.rowNotificationSettings.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeNotificationFragment);
        });

        // ----- Nhóm "Ứng dụng" -----
        binding.rowLanguage.getRoot().setOnClickListener(v -> showLanguagePicker());

        binding.rowThemes.getRoot().setOnClickListener(v -> showThemePickerBottomSheet());

        binding.rowDarkMode.switchRow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                DarkModeAnimationHelper.startCloseCurtainAnimation(requireActivity(), isChecked);
            }
        });

        binding.rowHelpSupport.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeHelpCenterFragment);
        });

        binding.rowAboutApp.getRoot().setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeAboutFragment);
        });

        // ----- Đăng xuất -----
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    /**
     * Hiển thị BottomSheet chọn Theme giao diện
     */
    private void showThemePickerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutBottomSheetThemeBinding sheetBinding = LayoutBottomSheetThemeBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());

        SharedPreferences pref = requireContext().getSharedPreferences("SHARE_PREFERENCES_APP", Context.MODE_PRIVATE);

        // 1. Theme Bơ (Tone xanh bơ nhạt)
        setupThemeItem(sheetBinding.themeAvocado, "Màu Bơ", 0xFF7CB342, R.drawable.ic_avocado,
                v -> applyNewTheme(pref, dialog, "avocado"));

        // 2. Theme Dưa Hấu (Tone hồng dưa hấu nhạt)
        setupThemeItem(sheetBinding.themeWatermelon, "Dưa Hấu", 0xFFFF6B7A, R.drawable.ic_watermelon,
                v -> applyNewTheme(pref, dialog, "watermelon"));

        // 3. Theme Quả Cam (Tone vàng cam pastel nhạt)
        setupThemeItem(sheetBinding.themeOrange, "Quả Cam", 0xFFFFB74D, R.drawable.ic_orange,
                v -> applyNewTheme(pref, dialog, "orange"));

        // 4. Theme Hồng Cánh Sen (Tone hồng mận / hoa sen)
        setupThemeItem(sheetBinding.themePink, "Hồng Sen", 0xFFD81B60, R.drawable.ic_lotus,
                v -> applyNewTheme(pref, dialog, "pink"));

        // 5. Theme Quả Việt Quất (Tone xanh tím việt quất)
        setupThemeItem(sheetBinding.themeBlueberry, "Việt Quất", 0xFF3F51B5, R.drawable.ic_blueberry,
                v -> applyNewTheme(pref, dialog, "blueberry"));

        // 6. Theme Chocolate
        setupThemeItem(sheetBinding.themeChocolate, "Chocolate", 0xFF6D4C41, R.drawable.ic_chocolate,
                v -> applyNewTheme(pref, dialog, "chocolate"));

        dialog.show();
    }

    /**
     * Thiết lập dữ liệu item theme (Đã bỏ hoàn toàn stroke/viền)
     */
    private void setupThemeItem(ItemThemeOptionBinding itemBinding, String title, int colorHex,
                                int iconRes, View.OnClickListener listener) {
        itemBinding.tvThemeName.setText(title);
        itemBinding.cardThemeColor.setCardBackgroundColor(colorHex);
        itemBinding.ivThemeIcon.setImageResource(iconRes);

        // Bắt sự kiện click ở cả layout lẫn Card & Icon để tránh bị nuốt touch event
        itemBinding.btnSelectTheme.setOnClickListener(listener);
        itemBinding.cardThemeColor.setOnClickListener(listener);
        itemBinding.ivThemeIcon.setOnClickListener(listener);
    }

    private void applyNewTheme(SharedPreferences pref, BottomSheetDialog dialog, String themeKey) {
        // Lưu theme mới
        pref.edit().putString("app_theme", themeKey).commit(); // commit() giúp ghi dữ liệu tức thì
        dialog.dismiss();

        // Tải lại Activity để áp dụng theme mới
        requireActivity().recreate();
    }

    private void navigateToHistory() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_employeeProfileFragment_to_employeeHistoryFragment);
    }

    private void showLanguagePicker() {
        String[] languages = {getString(R.string.profile_language_vietnamese), getString(R.string.profile_language_english)};

        // Lấy ngôn ngữ hiện tại của App
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        String currentLanguageCode = currentLocales.isEmpty() ? "vi" : currentLocales.get(0).getLanguage();

        int selectedIndex = currentLanguageCode.equals("en") ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_select_language))
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    if (which == 0) {
                        setAppLocale("vi");
                    } else if (which == 1) {
                        setAppLocale("en");
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    /**
     * Thực hiện chuyển đổi ngôn ngữ ứng dụng
     */
    private void setAppLocale(String languageCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(languageCode);
        // API này tự động lưu và áp dụng ngôn ngữ mới cho toàn bộ App
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_logout_title))
                .setMessage(getString(R.string.profile_logout_message))
                .setPositiveButton(getString(R.string.profile_logout_title), (dialog, which) -> performLogout())
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void performLogout() {
        if (logoutUseCase != null) {
            logoutUseCase.execute(new AuthCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    navigateToLoginAndFinish();
                }

                @Override
                public void onError(String errorMessage) {
                    // Session local đã được clear trong AuthRepositoryImpl dù có lỗi
                    // (ví dụ lỗi gọi API invalidate token), nên vẫn điều hướng về Login
                    navigateToLoginAndFinish();
                }
            });
        } else {
            navigateToLoginAndFinish();
        }
    }

    private void navigateToLoginAndFinish() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void setupObservers() {
        // Observe LiveData/StateFlow từ ViewModel
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak khi Fragment bị onDestroyView
    }
}