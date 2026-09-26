package com.example.attendanceapp.ui.manager.reward;

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
import com.example.attendanceapp.databinding.FragmentManagerRewardPunishBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerRewardPunishFragment extends Fragment {

    private FragmentManagerRewardPunishBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerRewardPunishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Khen thưởng / Phạt");
    }

    private void setupListeners() {
        binding.tvSelectEmployee.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở danh sách chọn nhân viên", Toast.LENGTH_SHORT).show();
        });

        binding.btnSubmit.setOnClickListener(v -> {
            String amount = binding.etAmount.getText().toString().trim();
            String reason = binding.etReason.getText().toString().trim();

            if (amount.isEmpty() || reason.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ số tiền và lý do", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = binding.rbReward.isChecked() ? "Thưởng" : "Phạt";
            Toast.makeText(getContext(), "Đã lưu quyết định " + type + " thành công!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
