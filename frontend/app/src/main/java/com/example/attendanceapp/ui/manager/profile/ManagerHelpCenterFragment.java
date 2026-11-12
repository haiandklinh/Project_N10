package com.example.attendanceapp.ui.manager.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.core.utils.SupportUiHelper;
import com.example.attendanceapp.data.local.pref.SharedPreferenceApp;
import com.example.attendanceapp.databinding.FragmentManagerHelpCenterBinding;

import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHelpCenterFragment extends Fragment {

    @Inject
    SharedPreferenceApp sharedPreferenceApp;

    private FragmentManagerHelpCenterBinding binding;
    private String supportPhone;
    private String supportEmail;

    public ManagerHelpCenterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerHelpCenterBinding.inflate(inflater, container, false);
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
        binding.layoutToolbar.setTitle("Trung tâm trợ giúp (Quản lý)");
        binding.layoutToolbar.setOnBackClickListener(() -> {
            try {
                Navigation.findNavController(requireView()).navigateUp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
                        )
                )
        );
    }

    private void setupListeners() {
        binding.layoutCallSupport.setOnClickListener(v ->
                SupportUiHelper.dialPhone(requireContext(), supportPhone));

        binding.layoutEmailSupport.setOnClickListener(v ->
                SupportUiHelper.sendSupportEmail(
                        requireContext(),
                        supportEmail,
                        "Hỗ trợ ứng dụng AttendanceApp (Manager)"
                ));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
