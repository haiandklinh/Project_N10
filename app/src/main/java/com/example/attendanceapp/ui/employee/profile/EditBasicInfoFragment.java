package com.example.attendanceapp.ui.employee.profile;

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
import com.example.attendanceapp.databinding.FragmentEditBasicInfoBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditBasicInfoFragment extends Fragment {

    private FragmentEditBasicInfoBinding binding;

    public EditBasicInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditBasicInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Thông tin cá nhân");
        binding.layoutToolbar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupListeners() {
        // Xử lý lưu thông tin liên hệ cho phép chỉnh sửa (Email & Số điện thoại)
        binding.btnSave.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";

            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin liên hệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Gọi ViewModel để cập nhật Email và SĐT lên Server / Local DB
            Toast.makeText(requireContext(), "Đã cập nhật thông tin liên hệ thành công", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });

        // Cần phê duyệt -> Chuyển qua màn hình tạo yêu cầu thay đổi thông tin bị khóa (Ticket)
        binding.btnRequestChange.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_editBasicInfoFragment_to_createChangeRequestFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}