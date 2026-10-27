package com.example.attendanceapp.ui.main.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAuthLoginBinding;
import com.example.attendanceapp.domain.model.UserAccount;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.model.UserStatus;
import com.example.attendanceapp.ui.employee.EmployeeMainActivity;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentAuthLoginBinding binding;
    private LoginViewModel loginviewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAuthLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginviewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupDialogListeners();
        setupObservers();
        setupViews();
        setupTextChangeListeners();
        observeRegisteredPhone();
    }

    private void observeRegisteredPhone() {
        NavBackStackEntry currentEntry = NavHostFragment.findNavController(this).getCurrentBackStackEntry();
        if (currentEntry != null) {
            // 1. Tự động đăng nhập ngầm khi nhận được thông tin từ RegisterFragment (Auto-login)
            currentEntry.getSavedStateHandle()
                    .<String>getLiveData("auto_login_phone")
                    .observe(getViewLifecycleOwner(), autoPhone -> {
                        if (!TextUtils.isEmpty(autoPhone)) {
                            String autoPassword = currentEntry.getSavedStateHandle().get("auto_login_password");
                            currentEntry.getSavedStateHandle().remove("auto_login_phone");
                            currentEntry.getSavedStateHandle().remove("auto_login_password");

                            binding.edtPhone.setText(autoPhone);
                            if (!TextUtils.isEmpty(autoPassword)) {
                                binding.edtPassword.setText(autoPassword);
                                loginviewModel.login(autoPhone, autoPassword);
                            }
                        }
                    });

            // 2. Dự phòng khi chỉ nhận registered_phone
            currentEntry.getSavedStateHandle()
                    .<String>getLiveData("registered_phone")
                    .observe(getViewLifecycleOwner(), phone -> {
                        if (!TextUtils.isEmpty(phone)) {
                            binding.edtPhone.setText(phone);
                            binding.tilPhone.setError(null);
                            binding.edtPassword.requestFocus();

                            binding.edtPassword.postDelayed(() -> {
                                if (isAdded() && getContext() != null) {
                                    android.view.inputmethod.InputMethodManager imm =
                                            (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                                    if (imm != null) {
                                        imm.showSoftInput(binding.edtPassword, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                                    }
                                }
                            }, 200);

                            currentEntry.getSavedStateHandle().remove("registered_phone");
                        }
                    });
        }
    }

    private void setupObservers() {
        loginviewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        loginviewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof LoginState.Success) {
                UserAccount user = ((LoginState.Success) state).user;
                clearAllErrors();
                Toast.makeText(requireContext(), "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Kiểm tra luồng đăng nhập dựa trên trạng thái tài khoản
                if (user.isFirstLogin() || user.getRole() == null) {
                    showRoleSelectionDialog();
                } else if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
                    navigateToPendingApproval(user.getInvitationCode(), user.getCompanyName());
                } else {
                    navigateToRoleHome(user.getRole());
                }
            } else if (state instanceof LoginState.Error) {
                String errorMsg = ((LoginState.Error) state).message;
                showErrorOnInput(errorMsg);
            } else if (state instanceof LoginState.PhoneNotRegistered) {
                String phone = ((LoginState.PhoneNotRegistered) state).phone;
                showPhoneNotRegisteredDialog(phone);
            }
        });

        // Lắng nghe các sự kiện thiết lập Role từ ViewModel
        loginviewModel.getRoleSetupEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof LoginViewModel.RoleSetupEvent.ShowPersonalInfoForm) {
                PersonalInfoBottomSheet.newInstance()
                        .show(getChildFragmentManager(), PersonalInfoBottomSheet.TAG);

            } else if (event instanceof LoginViewModel.RoleSetupEvent.ShowCompanyNameForm) {
                CompanyNameBottomSheet.newInstance()
                        .show(getChildFragmentManager(), CompanyNameBottomSheet.TAG);

            } else if (event instanceof LoginViewModel.RoleSetupEvent.ShowInvitationCodeForm) {
                EnterInvitationCodeBottomSheet.newInstance()
                        .show(getChildFragmentManager(), EnterInvitationCodeBottomSheet.TAG);

            } else if (event instanceof LoginViewModel.RoleSetupEvent.NavigateToCompanySelection) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_loginFragment_to_staffCompanySelectionFragment);

            } else if (event instanceof LoginViewModel.RoleSetupEvent.NavigateToHome) {
                navigateToRoleHome(((LoginViewModel.RoleSetupEvent.NavigateToHome) event).role);

            } else if (event instanceof LoginViewModel.RoleSetupEvent.NavigateToPendingApproval) {
                LoginViewModel.RoleSetupEvent.NavigateToPendingApproval e = (LoginViewModel.RoleSetupEvent.NavigateToPendingApproval) event;
                navigateToPendingApproval(e.inviteCode, e.companyName);

            } else if (event instanceof LoginViewModel.RoleSetupEvent.ShowError) {
                Toast.makeText(requireContext(), ((LoginViewModel.RoleSetupEvent.ShowError) event).message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPhoneNotRegisteredDialog(String phone) {
        if (!isAdded() || getContext() == null) return;

        PhoneNotRegisteredDialog dialog = PhoneNotRegisteredDialog.newInstance(phone);
        dialog.setOnActionListener(phoneToRegister -> {
            // Chuyển sang màn hình Đăng ký và truyền số điện thoại đã nhập sang
            Bundle bundle = new Bundle();
            bundle.putString("phone_number", phoneToRegister);

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_loginFragment_to_registerFragment, bundle);
        });

        dialog.show(getChildFragmentManager(), PhoneNotRegisteredDialog.TAG);
    }

    //Hàm setupDialogListeners() này dùng để lắng nghe dữ liệu trả về từ các Popup / Dialog / BottomSheet
    // về lại LoginFragment bằng Fragment Result API (thông qua setFragmentResultListener).
    private void setupDialogListeners() {
        // 1. Nhận Role đã chọn
        getChildFragmentManager().setFragmentResultListener(
                RoleSelectionDialog.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String selectedRoleStr = result.getString(RoleSelectionDialog.BUNDLE_KEY_SELECTED_ROLE, "");
                    loginviewModel.onRoleSelected(UserRole.fromString(selectedRoleStr));
                }
        );

        // 2. Nhận Thông tin cá nhân
        getChildFragmentManager().setFragmentResultListener(
                PersonalInfoBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(PersonalInfoBottomSheet.RESULT_ACTION, "");
                    if (PersonalInfoBottomSheet.ACTION_SUBMITTED.equals(action)) {
                        String name = result.getString(PersonalInfoBottomSheet.BUNDLE_KEY_NAME, "");
                        String dob = result.getString(PersonalInfoBottomSheet.BUNDLE_KEY_DOB, "");
                        String gender = result.getString(PersonalInfoBottomSheet.BUNDLE_KEY_GENDER, "");
                        String avatarUri = result.getString(PersonalInfoBottomSheet.BUNDLE_KEY_AVATAR_URI, null);
                        loginviewModel.onPersonalInfoEntered(name, dob, gender, avatarUri);
                    }
                }
        );

        // 3. Nhận Tên công ty (Manager)
        getChildFragmentManager().setFragmentResultListener(
                CompanyNameBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(CompanyNameBottomSheet.RESULT_ACTION, "");
                    if (CompanyNameBottomSheet.ACTION_SUBMITTED.equals(action)) {
                        String companyName = result.getString(CompanyNameBottomSheet.BUNDLE_KEY_COMPANY_NAME, "");
                        loginviewModel.submitManagerCompany(companyName);
                    }
                }
        );

        // 4. Nhận Mã mời (Employee)
        getChildFragmentManager().setFragmentResultListener(
                EnterInvitationCodeBottomSheet.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String action = result.getString(EnterInvitationCodeBottomSheet.RESULT_ACTION, "");
                    if (EnterInvitationCodeBottomSheet.ACTION_SUBMITTED.equals(action)) {
                        String invitationCode = result.getString(EnterInvitationCodeBottomSheet.BUNDLE_KEY_CODE, "");
                        loginviewModel.submitEmployeeInvitationCode(invitationCode);
                    }
                }
        );
    }

    private void showRoleSelectionDialog() {
        RoleSelectionDialog.newInstance()
                .show(getChildFragmentManager(), RoleSelectionDialog.TAG);
    }

    private void navigateToPendingApproval(String inviteCode, String companyName) {
        Bundle bundle = new Bundle();
        bundle.putString("invite_code", inviteCode);
        bundle.putString("company_name", companyName);

        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_pendingApprovalFragment, bundle);
    }

    private void navigateToRoleHome(UserRole role) {
        if (!isAdded() || getContext() == null) return;

        if (role == UserRole.MANAGER) {
            Intent intent = new Intent(requireContext(), ManagerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        } else {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_loginFragment_to_staffCompanySelectionFragment);
        }
    }

    private void setupViews() {
        binding.btnLogin.setOnClickListener(v -> performLogin());
        binding.tvForgotPassword.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        );
        binding.tvSignUp.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment)
        );
    }

    private void setupTextChangeListeners() {
        binding.edtPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
            }
        });

        binding.edtPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
        });
    }

    private void performLogin() {
        clearAllErrors();

        String phone = binding.edtPhone.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError("Vui lòng nhập số điện thoại");
            binding.edtPhone.requestFocus();
            return;
        } else if (!phone.matches("^(0[0-9]{9}|\\+84[0-9]{9})$")) {
            binding.tilPhone.setError("Số điện thoại không đúng định dạng");
            binding.edtPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            // Nếu người dùng nhập mỗi SĐT mà không nhập mật khẩu, kiểm tra xem SĐT đã đăng ký chưa
            loginviewModel.checkPhoneRegistration(phone);
            return;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            binding.edtPassword.requestFocus();
            return;
        }

        loginviewModel.login(phone, password);
    }

    private void clearAllErrors() {
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
    }

    private void showErrorOnInput(String errorMsg) {
        clearAllErrors();

        if (errorMsg.contains("Số điện thoại") || errorMsg.contains("phone")) {
            binding.tilPhone.setError(errorMsg);
            if (binding.tilPhone.getEditText() != null) {
                binding.tilPhone.getEditText().requestFocus();
            }
        } else {
            binding.tilPassword.setError(errorMsg);
            if (binding.tilPassword.getEditText() != null) {
                binding.tilPassword.getEditText().requestFocus();
            }
            
            // Xổ bàn phím lên khi nhảy vào ô mật khẩu
            binding.edtPassword.postDelayed(() -> {
                if (isAdded() && getContext() != null) {
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(binding.edtPassword, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, 100);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
