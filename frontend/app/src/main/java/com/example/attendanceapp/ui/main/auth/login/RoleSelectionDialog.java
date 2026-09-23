package com.example.attendanceapp.ui.main.auth.login;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.attendanceapp.databinding.DialogRoleSelectionBinding;
import com.example.attendanceapp.domain.model.UserRole;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RoleSelectionDialog extends DialogFragment {

    public static final String TAG = "RoleSelectionDialog";
    public static final String REQUEST_KEY = "role_selection_request_key";

    // Result Bundle Keys
    public static final String RESULT_ACTION = "result_action"; // SUBMITTED / CANCELED
    public static final String ACTION_SUBMITTED = "action_submitted";
    public static final String ACTION_CANCELED = "action_canceled";
    public static final String BUNDLE_KEY_ROLE = "bundle_key_role";
    public static final String BUNDLE_KEY_COMPANY_NAME = "bundle_key_company_name";
    public static final String BUNDLE_KEY_INVITATION_CODE = "bundle_key_invitation_code";

    private DialogRoleSelectionBinding binding;

    public static RoleSelectionDialog newInstance() {
        return new RoleSelectionDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogRoleSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupChildBottomSheetListeners();
        setupViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    /** Lắng nghe kết quả trả về từ các BottomSheet con (CompanyName & InvitationCode) */
    private void setupChildBottomSheetListeners() {
        // 1. Lắng nghe kết quả từ CompanyNameBottomSheet (dành cho Manager)
        getChildFragmentManager().setFragmentResultListener(
                CompanyNameBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(CompanyNameBottomSheet.RESULT_ACTION, "");
                    if (CompanyNameBottomSheet.ACTION_SUBMITTED.equals(action)) {
                        String companyName = result.getString(CompanyNameBottomSheet.BUNDLE_KEY_COMPANY_NAME, "");

                        // Gửi kết quả MANAGER kèm Tên công ty về Fragment/Activity cha
                        Bundle dialogResult = new Bundle();
                        dialogResult.putString(RESULT_ACTION, ACTION_SUBMITTED);
                        dialogResult.putString(BUNDLE_KEY_ROLE, UserRole.MANAGER.getValue());
                        dialogResult.putString(BUNDLE_KEY_COMPANY_NAME, companyName);
                        getParentFragmentManager().setFragmentResult(REQUEST_KEY, dialogResult);

                        dismiss();
                    }
                    // Nếu CANCELED thì không làm gì, Dialog giữ nguyên để người dùng chọn lại
                }
        );

        // 2. Lắng nghe kết quả từ EnterInvitationCodeBottomSheet (dành cho Employee)
        getChildFragmentManager().setFragmentResultListener(
                EnterInvitationCodeBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(EnterInvitationCodeBottomSheet.RESULT_ACTION, "");
                    if (EnterInvitationCodeBottomSheet.ACTION_SUBMITTED.equals(action)) {
                        String invitationCode = result.getString(EnterInvitationCodeBottomSheet.BUNDLE_KEY_CODE, "");

                        // Gửi kết quả EMPLOYEE kèm Mã mời về Fragment/Activity cha
                        Bundle dialogResult = new Bundle();
                        dialogResult.putString(RESULT_ACTION, ACTION_SUBMITTED);
                        dialogResult.putString(BUNDLE_KEY_ROLE, UserRole.EMPLOYEE.getValue());
                        dialogResult.putString(BUNDLE_KEY_INVITATION_CODE, invitationCode);
                        getParentFragmentManager().setFragmentResult(REQUEST_KEY, dialogResult);

                        dismiss();
                    }
                }
        );
    }

    private void setupViews() {
        // Mở BottomSheet nhập tên công ty khi bấm chọn Quản lý
        binding.cardManager.setOnClickListener(v ->
                CompanyNameBottomSheet.newInstance()
                        .show(getChildFragmentManager(), CompanyNameBottomSheet.TAG)
        );

        // Mở BottomSheet nhập mã mời khi bấm chọn Nhân viên
        binding.cardEmployee.setOnClickListener(v ->
                EnterInvitationCodeBottomSheet.newInstance()
                        .show(getChildFragmentManager(), EnterInvitationCodeBottomSheet.TAG)
        );

        // Bấm nút Hủy -> Báo về Fragment cha là Canceled
        binding.btnCancel.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(RESULT_ACTION, ACTION_CANCELED);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}