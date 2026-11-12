package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.SupportUiHelper;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentHelpCenterBinding;

import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeHelpCenterFragment extends Fragment {

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private FragmentHelpCenterBinding binding;
    private String supportPhone;
    private String supportEmail;

    public EmployeeHelpCenterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHelpCenterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupViews();
        setupFaqSearch();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Trung tâm trợ giúp");
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void setupViews() {
        supportPhone = SupportUiHelper.resolveOrDefault(
                sharedPreferenceApp != null ? sharedPreferenceApp.getCompanyPhone() : null,
                SupportUiHelper.DEFAULT_SUPPORT_PHONE
        );
        supportEmail = SupportUiHelper.resolveOrDefault(
                sharedPreferenceApp != null ? sharedPreferenceApp.getCompanyEmail() : null,
                SupportUiHelper.DEFAULT_SUPPORT_EMAIL
        );
        binding.tvPhone.setText(supportPhone);
        binding.tvEmail.setText(supportEmail);
    }

    private void setupFaqSearch() {
        SupportUiHelper.setupFaqSearchAndToggle(
                binding.etSearchHelp,
                binding.tvFaqEmpty,
                Arrays.asList(
                        new SupportUiHelper.FaqItem(
                                binding.cardFaq1,
                                binding.tvFaqAnswer1,
                                binding.tvFaqQuestion1.getText().toString(),
                                binding.tvFaqAnswer1.getText().toString()
                        ),
                        new SupportUiHelper.FaqItem(
                                binding.cardFaq2,
                                binding.tvFaqAnswer2,
                                binding.tvFaqQuestion2.getText().toString(),
                                binding.tvFaqAnswer2.getText().toString()
                        ),
                        new SupportUiHelper.FaqItem(
                                binding.cardFaq3,
                                binding.tvFaqAnswer3,
                                binding.tvFaqQuestion3.getText().toString(),
                                binding.tvFaqAnswer3.getText().toString()
                        )
                )
        );
    }

    private void setupListeners() {
        // Categories
        binding.cardHelpAttendance.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeHelpCenterFragment_to_employeeCheckInFragment));
        binding.cardHelpAccount.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeHelpCenterFragment_to_employeeEditBasicInfoFragment));
        binding.cardHelpPayroll.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeHelpCenterFragment_to_employeePayrollFragment));
        binding.cardHelpRequests.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeHelpCenterFragment_to_employeeLeaveRequestFragment));

        // Contact Actions (đồng bộ đúng số điện thoại & email đang hiển thị)
        binding.layoutCallSupport.setOnClickListener(v ->
                SupportUiHelper.dialPhone(requireContext(), supportPhone));

        binding.layoutEmailSupport.setOnClickListener(v ->
                SupportUiHelper.sendSupportEmail(
                        requireContext(),
                        supportEmail,
                        "Hỗ trợ ứng dụng AttendanceApp"
                ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}