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

import com.example.attendanceapp.databinding.FragmentEditBasicInfoBinding;
import com.example.attendanceapp.domain.model.Gender;
import com.example.attendanceapp.domain.model.UserAccount;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerEditBasicInfoFragment extends Fragment {

    private FragmentEditBasicInfoBinding binding;
    private ManagerEditBasicInfoViewModel viewModel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        setupListeners();
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
        binding.etGender.setText(user.getGender() != null ? user.getGender().name() : "");
        binding.etPhone.setText(user.getPhoneNumber());
        
        // Manager specific: Organization field (e.g. et_department or et_position reused for company)
        binding.etDepartment.setText(user.getCompanyName());
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText().toString().trim();
            String dobStr = binding.etDob.getText().toString().trim();
            String genderStr = binding.etGender.getText().toString().trim();
            String companyName = binding.etDepartment.getText().toString().trim();
            
            LocalDate dob = null;
            try {
                dob = LocalDate.parse(dobStr, dateFormatter);
            } catch (Exception ignored) {}

            Gender gender = null;
            try {
                gender = Gender.valueOf(genderStr.toUpperCase());
            } catch (Exception ignored) {}

            viewModel.updateProfile(fullName, dob, gender, null, companyName);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
