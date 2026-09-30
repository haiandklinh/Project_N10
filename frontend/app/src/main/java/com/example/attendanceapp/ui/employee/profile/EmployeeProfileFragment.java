package com.example.attendanceapp.ui.employee.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.DarkModeAnimationHelper;
import com.example.attendanceapp.databinding.FragmentEmployeeProfileBinding;
import com.example.attendanceapp.databinding.ItemProfileMenuRowBinding;
import com.example.attendanceapp.databinding.ItemProfileSwitchRowBinding;
import com.example.attendanceapp.databinding.ItemThemeOptionBinding;
import com.example.attendanceapp.databinding.LayoutBottomSheetThemeBinding;
import com.example.attendanceapp.ui.main.auth.AuthActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeProfileFragment extends Fragment {

    private FragmentEmployeeProfileBinding binding;
    private EmployeeProfileViewModel viewModel;

    public EmployeeProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEmployeeProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EmployeeProfileViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutProfileHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m), v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        setupViews();
        setupListeners();
        observeViewModel();
    }

    private void setupViews() {

        // ----- Nhóm TÀI KHOẢN -----
        bindMenuRow(binding.rowEditProfile, R.drawable.ic_profile, getString(R.string.profile_edit_profile));
        bindMenuRow(binding.rowChangePassword, R.drawable.ic_lock, getString(R.string.profile_change_password));
        bindMenuRow(binding.rowNotificationSettings, R.drawable.ic_notifications, getString(R.string.profile_notifications));

        // ----- Nhóm ỨNG DỤNG -----
        bindMenuRow(binding.rowLanguage, R.drawable.ic_language, getString(R.string.profile_language));
        bindMenuRow(binding.rowThemes, R.drawable.ic_themes, getString(R.string.profile_theme));
        bindMenuRow(binding.rowHelpSupport, R.drawable.ic_help, getString(R.string.profile_help_support));
        bindMenuRow(binding.rowAboutApp, R.drawable.ic_warning, getString(R.string.profile_about_app));

        ItemProfileSwitchRowBinding darkModeRow = binding.rowDarkMode;
        darkModeRow.tvRowLabel.setText(getString(R.string.profile_dark_mode));
        darkModeRow.ivRowIcon.setImageResource(R.drawable.ic_dark_mode);
        darkModeRow.switchRow.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void bindMenuRow(ItemProfileMenuRowBinding row, int iconRes, String label) {
        row.ivRowIcon.setImageResource(iconRes);
        row.tvRowLabel.setText(label);
    }

    private void setupListeners() {
        //binding.rowShiftHistory.getRoot().setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeHistoryFragment));
        binding.rowEditProfile.getRoot().setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeEditProfileFragment));
        binding.rowChangePassword.getRoot().setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_employeeProfileFragment_to_employeeChangePasswordFragment));

        binding.rowLanguage.getRoot().setOnClickListener(v -> showLanguagePicker());
        binding.rowThemes.getRoot().setOnClickListener(v -> showThemePickerBottomSheet());
        binding.rowDarkMode.switchRow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) DarkModeAnimationHelper.startCloseCurtainAnimation(requireActivity(), isChecked);
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void observeViewModel() {
        viewModel.getUserAccount().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getProfile() != null) {
                updateUI(user);
            }
        });

        viewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                navigateToLoginAndFinish();
            }
        });
    }

    private void updateUI(com.example.attendanceapp.domain.model.UserAccount user) {
        com.example.attendanceapp.domain.model.UserProfile profile = user.getProfile();
        
        // Cập nhật tên
        binding.tvUserName.setText(profile.getFullName() != null ? profile.getFullName() : getString(R.string.profile_user_name));
        
        // Cập nhật tên công ty (hoặc chức vụ)
        binding.tvUserPosition.setText(profile.getCompanyName() != null ? profile.getCompanyName() : getString(R.string.profile_user_position));

        // Cập nhật ảnh đại diện
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(profile.getAvatarUrl())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(binding.ivAvatar);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_avatar);
        }
    }

    private void showThemePickerBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        LayoutBottomSheetThemeBinding sheetBinding = LayoutBottomSheetThemeBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheetBinding.getRoot());
        SharedPreferences pref = requireContext().getSharedPreferences("SHARE_PREFERENCES_APP", Context.MODE_PRIVATE);

        setupThemeItem(sheetBinding.themeAvocado, "Màu Bơ", 0xFF7CB342, R.drawable.ic_avocado, v -> applyNewTheme(pref, dialog, "avocado"));
        setupThemeItem(sheetBinding.themeWatermelon, "Dưa Hấu", 0xFFFF6B7A, R.drawable.ic_watermelon, v -> applyNewTheme(pref, dialog, "watermelon"));
        setupThemeItem(sheetBinding.themeOrange, "Quả Cam", 0xFFFFB74D, R.drawable.ic_orange, v -> applyNewTheme(pref, dialog, "orange"));
        setupThemeItem(sheetBinding.themePink, "Hồng Sen", 0xFFD81B60, R.drawable.ic_lotus, v -> applyNewTheme(pref, dialog, "pink"));
        setupThemeItem(sheetBinding.themeBlueberry, "Việt Quất", 0xFF3F51B5, R.drawable.ic_blueberry, v -> applyNewTheme(pref, dialog, "blueberry"));
        setupThemeItem(sheetBinding.themeChocolate, "Chocolate", 0xFF6D4C41, R.drawable.ic_chocolate, v -> applyNewTheme(pref, dialog, "chocolate"));
        dialog.show();
    }

    private void setupThemeItem(ItemThemeOptionBinding item, String title, int colorHex, int iconRes, View.OnClickListener listener) {
        item.tvThemeName.setText(title);
        item.cardThemeColor.setCardBackgroundColor(colorHex);
        item.ivThemeIcon.setImageResource(iconRes);
        item.btnSelectTheme.setOnClickListener(listener);
        item.cardThemeColor.setOnClickListener(listener);
        item.ivThemeIcon.setOnClickListener(listener);
    }

    private void applyNewTheme(SharedPreferences pref, BottomSheetDialog dialog, String themeKey) {
        pref.edit().putString("app_theme", themeKey).commit();
        dialog.dismiss();
        requireActivity().recreate();
    }

    private void showLanguagePicker() {
        String[] languages = {getString(R.string.profile_language_vietnamese), getString(R.string.profile_language_english)};
        int selectedIndex = AppCompatDelegate.getApplicationLocales().isEmpty() || !AppCompatDelegate.getApplicationLocales().get(0).getLanguage().equals("en") ? 0 : 1;
        new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.profile_select_language)).setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(which == 0 ? "vi" : "en"));
            dialog.dismiss();
        }).setNegativeButton(getString(R.string.action_cancel), null).show();
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(requireContext()).setTitle(getString(R.string.profile_logout_title)).setMessage(getString(R.string.profile_logout_message)).setPositiveButton(getString(R.string.profile_logout_title), (dialog, which) -> viewModel.logout()).setNegativeButton(getString(R.string.action_cancel), null).show();
    }

    private void navigateToLoginAndFinish() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
