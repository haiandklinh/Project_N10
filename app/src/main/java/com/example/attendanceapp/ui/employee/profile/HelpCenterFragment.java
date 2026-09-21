package com.example.attendanceapp.ui.employee.profile;

import android.content.Intent;
import android.net.Uri;
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
import com.example.attendanceapp.databinding.FragmentHelpCenterBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HelpCenterFragment extends Fragment {

    private FragmentHelpCenterBinding binding;

    public HelpCenterFragment() {
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
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Trung tâm trợ giúp");
        binding.layoutToolbar.setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp()
        );
    }
    private void setupViews(){
        binding.tvPhone.setText("0987654321");
        binding.tvEmail.setText("attendanceapp2026@gmail.com");
    }
    private void setupListeners() {
        // Categories
        binding.cardHelpAttendance.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_helpCenterFragment_to_checkInFragment));
        binding.cardHelpAccount.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_helpCenterFragment_to_editBasicInfoFragment));
        binding.cardHelpPayroll.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_helpCenterFragment_to_payrollFragment));
        binding.cardHelpRequests.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_helpCenterFragment_to_leaveRequestFragment));

        // Contact Actions
        binding.layoutCallSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0123456789"));
            startActivity(intent);
        });

        binding.layoutEmailSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:attendanceapp2026@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ ứng dụng AttendanceApp");
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}