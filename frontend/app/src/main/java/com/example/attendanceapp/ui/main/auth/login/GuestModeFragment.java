package com.example.attendanceapp.ui.main.auth.login; // Hoặc package tương ứng của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentGuestModeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GuestModeFragment extends Fragment {

    private FragmentGuestModeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuestModeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Xử lý sự kiện khi bấm nút "Kiểm tra"
        binding.btnCheckStatus.setOnClickListener(v -> {
            // Hiển thị tiến trình loading giả lập hoặc gọi ViewModel kiểm tra trạng thái duyệt
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnCheckStatus.setEnabled(false);

            // Kiểm tra trạng thái duyệt (Ví dụ chuyển sang trang PendingApprovalFragment)
            v.postDelayed(() -> {
                if (isAdded() && binding != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCheckStatus.setEnabled(true);

                    // Điều hướng sang màn hình chờ phê duyệt để xem Manager đã duyệt chưa
                    Navigation.findNavController(v)
                            .navigate(R.id.action_guestModeFragment_to_pendingApprovalFragment);
                }
            }, 800); // Delay nhẹ 0.8s để tạo cảm giác load mượt mà
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak với ViewBinding
    }
}