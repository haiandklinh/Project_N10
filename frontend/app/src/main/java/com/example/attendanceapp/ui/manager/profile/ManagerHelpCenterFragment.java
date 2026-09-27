package com.example.attendanceapp.ui.manager.profile;

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
import com.example.attendanceapp.databinding.FragmentManagerHelpCenterBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHelpCenterFragment extends Fragment {

    private FragmentManagerHelpCenterBinding binding;

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

    private void setupListeners() {
        // Điều hướng theo các chức năng của Manager
        binding.cardHelpSchedule.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.managerScheduleFragment));
            
        binding.cardHelpApproval.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.managerApprovalFragment));
            
        binding.cardHelpPayroll.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.managerPayrollFragment));
            
        binding.cardHelpHuman.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.managerHumanFragment));

        // Các hành động liên hệ hỗ trợ
        binding.layoutCallSupport.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:0987654321"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show();
            }
        });

        binding.layoutEmailSupport.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:attendanceapp2026@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ ứng dụng AttendanceApp (Manager)");
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
