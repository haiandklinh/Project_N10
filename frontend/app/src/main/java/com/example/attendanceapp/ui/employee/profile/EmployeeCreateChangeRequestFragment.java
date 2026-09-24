package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentCreateChangeRequestBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeeCreateChangeRequestFragment extends Fragment {

    private FragmentCreateChangeRequestBinding binding;

    public EmployeeCreateChangeRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateChangeRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadUserInfo();
        setupDropdown();
        setupListeners();
    }

    private void setupToolbar() {
        binding.layoutToolbar.setTitle("Yêu cầu sửa thông tin");
        binding.layoutToolbar.setOnBackClickListener(() ->
                Navigation.findNavController(requireView()).navigateUp()
        );
    }

    private void loadUserInfo() {
        // Gán mã nhân viên do công ty cấp sẵn
        binding.etEmployeeCode.setText("NV-202601");
    }

    private void setupDropdown() {
        // Danh sách các mục nhân viên thường cần yêu cầu HR điều chỉnh khi bị nhập sai ban đầu
        String[] fields = {
                "Họ và tên",
                "Ngày tháng năm sinh",
                "Số CCCD / CMND",
                "Tài khoản ngân hàng (Nhận lương)",
                "Mã số thuế cá nhân"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, fields);
        binding.spinnerFieldType.setAdapter(adapter);

        // Hiển thị giá trị hiện tại tương ứng trên hệ thống
        binding.spinnerFieldType.setOnItemClickListener((parent, view, position, id) -> {
            String selected = fields[position];
            switch (selected) {
                case "Họ và tên":
                    binding.etOldValue.setText("Nguyễn Văn A");
                    break;
                case "Ngày tháng năm sinh":
                    binding.etOldValue.setText("01/01/1998");
                    break;
                case "Số CCCD / CMND":
                    binding.etOldValue.setText("012345678912");
                    break;
                case "Tài khoản ngân hàng (Nhận lương)":
                    binding.etOldValue.setText("123456789 - Vietcombank");
                    break;
                case "Mã số thuế cá nhân":
                    binding.etOldValue.setText("8539201928");
                    break;
                default:
                    binding.etOldValue.setText("Chưa cập nhật");
                    break;
            }
        });
    }

    private void setupListeners() {
        binding.btnUploadProof.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mở camera / Thư viện ảnh để chọn chứng từ đối soát", Toast.LENGTH_SHORT).show()
        );

        binding.btnSubmitRequest.setOnClickListener(v -> {
            String fieldType = binding.spinnerFieldType.getText().toString();
            String newValue = binding.etNewValue.getText().toString().trim();
            String reason = binding.etReason.getText() != null ? binding.etReason.getText().toString().trim() : "";

            if (fieldType.isEmpty() || newValue.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn loại thông tin và nhập giá trị đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reason.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng ghi rõ lý do yêu cầu điều chỉnh", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(requireContext(), "Đã gửi yêu cầu! HR/Quản lý sẽ duyệt thông tin cho bạn.", Toast.LENGTH_LONG).show();

            // Chuyển sang màn hình Lịch sử yêu cầu thay đổi
            Navigation.findNavController(v).navigate(R.id.action_employeeCreateChangeRequestFragment_to_employeeChangeRequestHistoryFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}