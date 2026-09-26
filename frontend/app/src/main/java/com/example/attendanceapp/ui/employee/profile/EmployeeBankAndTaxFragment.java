package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentBankTaxInfoBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeBankAndTaxFragment extends Fragment {

    private FragmentBankTaxInfoBinding binding;

    public EmployeeBankAndTaxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBankTaxInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadBankAndTaxData();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Ngân hàng & Thuế");
        binding.layoutToolbar.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void loadBankAndTaxData() {
        // TODO: Lấy dữ liệu thực tế từ BankTaxViewModel / Repository
        // Mẫu dữ liệu hiển thị chế độ xem (Read-only)
        binding.etBankName.setText("MB Bank (Ngân hàng Quân Đội)");
        binding.etBankAccountNumber.setText("999988886666");
        binding.etBankAccountHolder.setText("NGUYEN VIET SON");
        binding.etTaxCode.setText("8539201928");
        binding.etSocialInsuranceNumber.setText("0123456789");
    }

    private void setupListeners() {
        // Chuyển sang màn hình tạo Yêu cầu điều chỉnh thông tin (Ticket) gửi HR duyệt
        binding.btnRequestChangeBank.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_employeeBankAndTaxFragment_to_employeeCreateChangeRequestFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}