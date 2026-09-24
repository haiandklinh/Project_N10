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

import com.example.attendanceapp.databinding.FragmentContactInfoBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeContactInfoFragment extends Fragment {

    private FragmentContactInfoBinding binding;

    public EmployeeContactInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContactInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Thông tin liên hệ");
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> saveContactInfo(v));
    }

    private void saveContactInfo(View v) {
        String phone = binding.etPhoneNumber.getText() != null ? binding.etPhoneNumber.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";
        String emergencyName = binding.etEmergencyName.getText() != null ? binding.etEmergencyName.getText().toString().trim() : "";
        String emergencyRelation = binding.etEmergencyRelation.getText() != null ? binding.etEmergencyRelation.getText().toString().trim() : "";
        String emergencyPhone = binding.etEmergencyPhone.getText() != null ? binding.etEmergencyPhone.getText().toString().trim() : "";

        if (phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ Số điện thoại và Email", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Gọi ViewModel / Repository để cập nhật dữ liệu liên hệ lên Server / Room DB

        Toast.makeText(requireContext(), "Cập nhật thông tin liên hệ thành công!", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(v).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}