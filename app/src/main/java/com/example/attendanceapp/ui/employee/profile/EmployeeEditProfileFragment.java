package com.example.attendanceapp.ui.employee.profile;

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
public class EmployeeEditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;

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
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Chỉnh sửa hồ sơ");
        binding.layoutToolbar.setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp()
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
        binding.btnChangePhoto.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Tính năng cập nhật ảnh FaceID", Toast.LENGTH_SHORT).show()
        );

        binding.rowBasicInfo.getRoot().setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeEditBasicInfoFragment)
        );

        binding.rowContactInfo.getRoot().setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeEditProfileFragment_to_employeeContactInfoFragment)
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}