package com.example.attendanceapp.ui.manager.human;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.BottomSheetCompanyInvitationBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CompanyInvitationBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CompanyInvitationBottomSheet";

    private BottomSheetCompanyInvitationBinding binding;
    private ManagerHumanViewModel viewModel;

    public static CompanyInvitationBottomSheet newInstance() {
        return new CompanyInvitationBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetCompanyInvitationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Chia sẻ ViewModel cùng Activity để đồng bộ với toàn bộ màn hình
        viewModel = new ViewModelProvider(requireActivity()).get(ManagerHumanViewModel.class);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnCloseSheet.setOnClickListener(v -> dismiss());

        // Bấm Tạo mã công ty
        binding.btnGenerateCode.setOnClickListener(v -> {
            viewModel.generateInvitationCode(86399); // 23h 59m 59s (~24 giờ)
        });

        // Bấm Tạo mã mới khác
        binding.btnRegenerateCode.setOnClickListener(v -> {
            viewModel.generateInvitationCode(86399);
        });

        // Bấm Sao chép mã
        binding.btnCopyCode.setOnClickListener(v -> {
            String code = viewModel.invitationCode.getValue();
            if (code != null && !code.isEmpty()) {
                copyCodeToClipboard(code);
            }
        });

        // Bấm Chia sẻ mã
        binding.btnShareCode.setOnClickListener(v -> {
            String code = viewModel.invitationCode.getValue();
            if (code != null && !code.isEmpty()) {
                shareInvitationCode(code);
            }
        });
    }

    private void observeViewModel() {
        // Theo dõi mã mời hiện tại
        viewModel.invitationCode.observe(getViewLifecycleOwner(), code -> {
            if (code != null && !code.trim().isEmpty()) {
                showCodeDisplayState(code);
            } else {
                showPromptState();
            }
        });

        // Theo dõi trạng thái tạo mã
        viewModel.isGeneratingCode.observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            binding.pbPromptLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.pbCodeLoading.setVisibility(loading ? View.VISIBLE : View.GONE);

            binding.btnGenerateCode.setEnabled(!loading);
            binding.btnRegenerateCode.setEnabled(!loading);
            binding.btnCopyCode.setEnabled(!loading);
            binding.btnShareCode.setEnabled(!loading);
        });

        // Theo dõi lỗi
        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void showPromptState() {
        binding.layoutPromptState.setVisibility(View.VISIBLE);
        binding.layoutCodeState.setVisibility(View.GONE);
        binding.tvSheetHeader.setText("Thêm nhân sự mới");
    }

    private void showCodeDisplayState(String rawCode) {
        binding.layoutPromptState.setVisibility(View.GONE);
        binding.layoutCodeState.setVisibility(View.VISIBLE);
        binding.tvSheetHeader.setText("Mã mời tham gia công ty");

        // Format mã: cách nhóm 3 số nếu đủ 6 số
        String formattedCode = rawCode;
        if (rawCode != null && rawCode.length() == 6) {
            formattedCode = rawCode.substring(0, 3) + " " + rawCode.substring(3);
        }
        binding.tvDisplayCode.setText(formattedCode != null ? formattedCode : "------");
        binding.tvExpiryTime.setText("Mã có hiệu lực trong 24 giờ");
    }

    private void copyCodeToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Company Invitation Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Đã sao chép mã mời: " + code, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareInvitationCode(String code) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        String shareBody = "Mã mời tham gia doanh nghiệp trên Attendance App: " + code +
                "\n\nVui lòng tải ứng dụng Attendance App và nhập mã mời này tại màn hình Tham gia công ty để được xét duyệt (Mã có hiệu lực trong 24 giờ).";
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Mã mời tham gia công ty");
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sendIntent, "Chia sẻ mã mời"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
