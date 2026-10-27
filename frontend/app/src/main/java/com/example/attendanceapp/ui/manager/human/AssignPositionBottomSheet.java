package com.example.attendanceapp.ui.manager.human;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.BottomSheetAssignPositionBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

public class AssignPositionBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AssignPositionBottomSheet";

    private static final String ARG_STAFF_ID = "arg_staff_id";
    private static final String ARG_FULL_NAME = "arg_full_name";
    private static final String ARG_AVATAR_URL = "arg_avatar_url";

    public interface OnPositionConfirmedListener {
        void onPositionConfirmed(String staffId, String fullName, String avatarUrl, String position);
    }

    private BottomSheetAssignPositionBinding binding;
    private OnPositionConfirmedListener listener;

    public static AssignPositionBottomSheet newInstance(String staffId, String fullName, String avatarUrl, OnPositionConfirmedListener listener) {
        AssignPositionBottomSheet sheet = new AssignPositionBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STAFF_ID, staffId);
        args.putString(ARG_FULL_NAME, fullName);
        args.putString(ARG_AVATAR_URL, avatarUrl);
        sheet.setArguments(args);
        sheet.setOnPositionConfirmedListener(listener);
        return sheet;
    }

    public void setOnPositionConfirmedListener(OnPositionConfirmedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAssignPositionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String staffId = args.getString(ARG_STAFF_ID, "");
        String fullName = args.getString(ARG_FULL_NAME, "Nhân sự mới");
        String avatarUrl = args.getString(ARG_AVATAR_URL, null);

        binding.tvEmployeeName.setText(fullName);

        loadAvatar(requireContext(), avatarUrl, fullName, binding.ivEmployeeAvatar);

        setupChipListeners();

        binding.btnCloseSheet.setOnClickListener(v -> dismiss());
        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnConfirmApprove.setOnClickListener(v -> {
            binding.btnConfirmApprove.setEnabled(false);
            String position = binding.etPosition.getText() != null
                    ? binding.etPosition.getText().toString().trim()
                    : "";
            if (position.isEmpty()) {
                position = "Nhân viên";
            }

            if (listener != null) {
                listener.onPositionConfirmed(staffId, fullName, avatarUrl, position);
            }
            dismiss();
        });
    }

    private void setupChipListeners() {
        for (int i = 0; i < binding.chipGroupSuggestions.getChildCount(); i++) {
            View child = binding.chipGroupSuggestions.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    binding.etPosition.setText(chip.getText().toString());
                    if (binding.etPosition.getText() != null) {
                        binding.etPosition.setSelection(binding.etPosition.getText().length());
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadAvatar(android.content.Context context, String avatarUrl, String fullName, com.google.android.material.imageview.ShapeableImageView imageView) {
        com.example.attendanceapp.core.utils.AvatarLoader.loadAvatar(context, avatarUrl, imageView);
    }
}