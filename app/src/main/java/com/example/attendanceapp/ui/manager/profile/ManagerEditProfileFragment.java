package com.example.attendanceapp.ui.manager.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentEditProfileBinding;
import com.example.attendanceapp.databinding.ItemProfileMenuRowBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerEditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;

    public ManagerEditProfileFragment() {
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
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Chỉnh sửa hồ sơ Quản lý");
        binding.layoutToolbar.setOnBackClickListener(() ->
            Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupMenuRows() {
        bindRow(binding.rowBasicInfo, R.drawable.ic_profile, "Thông tin cá nhân cơ bản");
        bindRow(binding.rowContactInfo, R.drawable.ic_phone, "Thông tin liên hệ");
        bindRow(binding.rowFinanceInfo, R.drawable.ic_money, "Ngân hàng & Thuế");
        bindRow(binding.rowJobInfo, R.drawable.ic_card_company, "Thông tin ban quản trị");
        bindRow(binding.rowPrivacySettings, R.drawable.ic_lock, "Cài đặt & Quyền riêng tư");
    }

    private void bindRow(ItemProfileMenuRowBinding row, int iconRes, String label) {
        row.ivRowIcon.setImageResource(iconRes);
        row.tvRowLabel.setText(label);
    }

    private void setupListeners() {
        binding.btnChangePhoto.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Tính năng cập nhật ảnh Quản lý", Toast.LENGTH_SHORT).show()
        );

        binding.rowBasicInfo.getRoot().setOnClickListener(v -> 
            Toast.makeText(getContext(), "Thông tin cá nhân cơ bản", Toast.LENGTH_SHORT).show()
        );

        binding.rowContactInfo.getRoot().setOnClickListener(v ->
            Toast.makeText(getContext(), "Thông tin liên hệ", Toast.LENGTH_SHORT).show()
        );

        binding.rowFinanceInfo.getRoot().setOnClickListener(v -> {
            Toast.makeText(getContext(), "Ngân hàng & Thuế", Toast.LENGTH_SHORT).show();
        });

        binding.rowJobInfo.getRoot().setOnClickListener(v -> 
            Toast.makeText(getContext(), "Thông tin công việc quản lý", Toast.LENGTH_SHORT).show()
        );

        binding.rowPrivacySettings.getRoot().setOnClickListener(v -> 
            Toast.makeText(getContext(), "Cài đặt & Quyền riêng tư", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}