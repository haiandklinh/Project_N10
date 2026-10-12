package com.example.attendanceapp.ui.employee.profile;

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

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentEditBasicInfoBinding;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserAccount;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeEditBasicInfoFragment extends Fragment {

    private FragmentEditBasicInfoBinding binding;
    private EmployeeEditBasicInfoViewModel viewModel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public EmployeeEditBasicInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EmployeeEditBasicInfoViewModel.class);
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
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Thông tin cá nhân");
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupObservers() {
        viewModel.getUserAccount().observe(getViewLifecycleOwner(), this::populateData);
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSave.setEnabled(!isLoading);
            // Optionally show progress bar
        });
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Đã cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
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
        binding.etGender.setText(user.getGender() != null ? user.getGender().name() : "");
        binding.etEmail.setText(""); // Email is not in UserAccount flat getters, would need to fetch from another source or update model
        binding.etPhone.setText(user.getPhoneNumber());
        
        // Employee specifics (from profile/entity if available)
        // binding.etEmployeeId.setText(...);
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String dobStr = binding.etDob.getText().toString().trim();
            String genderStr = binding.etGender.getText().toString().trim();
            
            LocalDate dob = null;
            try {
                dob = LocalDate.parse(dobStr, dateFormatter);
            } catch (Exception ignored) {}

            Gender gender = null;
            try {
                gender = Gender.valueOf(genderStr.toUpperCase());
            } catch (Exception ignored) {}

            viewModel.updateProfile(fullName, dob, gender, null);
        });

        // Cần phê duyệt -> Chuyển qua màn hình tạo yêu cầu thay đổi thông tin bị khóa (Ticket)
        binding.btnRequestChange.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeEditBasicInfoFragment_to_employeeCreateChangeRequestFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}