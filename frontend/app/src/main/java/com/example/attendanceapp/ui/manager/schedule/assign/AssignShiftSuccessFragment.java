package com.example.attendanceapp.ui.manager.schedule.assign;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAssignShiftSuccessBinding;
import com.example.attendanceapp.domain.model.ManagerEmployeeItem;
import com.example.attendanceapp.domain.model.ShiftOption;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AssignShiftSuccessFragment extends Fragment {

    private FragmentAssignShiftSuccessBinding binding;
    private AssignShiftViewModel viewModel;
    private AssignedEmployeeSuccessAdapter employeeAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAssignShiftSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel dùng chung ở mức Activity để đồng bộ dữ liệu vừa xếp ca
        viewModel = new ViewModelProvider(requireActivity()).get(AssignShiftViewModel.class);

        applyWindowInsets();
        setupRecyclerView();
        setupActions();
        observeViewModel();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void setupRecyclerView() {
        employeeAdapter = new AssignedEmployeeSuccessAdapter(new ArrayList<>());
        binding.rvAssignedEmployees.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAssignedEmployees.setAdapter(employeeAdapter);
    }

    private void observeViewModel() {
        // 1. Theo dõi danh sách nhân viên vừa được xếp ca (Sửa lỗi chưa hiển thị)
        viewModel.getSelectedEmployeesLiveData().observe(getViewLifecycleOwner(), employees -> {
            if (employees != null) {
                employeeAdapter.updateData(employees);
                updateSummaryText(employees);
            }
        });

        // 2. Theo dõi kết quả xuất file
        viewModel.getExportSuccessUriLiveData().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                openExcelFile(uri);
                viewModel.clearExportEvent();
            }
        });

        // 3. Theo dõi trạng thái loading
        viewModel.getIsLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnExportExcel.setEnabled(!isLoading);
            if (isLoading) {
                binding.btnExportExcel.setText("Đang xử lý...");
            } else {
                binding.btnExportExcel.setText("Xuất ra bảng tính");
            }
        });

        // 4. Lỗi
        viewModel.getErrorMessageLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });
    }

    private void updateSummaryText(List<ManagerEmployeeItem> employees) {
        ShiftOption selectedShift = viewModel.getSelectedShiftLiveData().getValue();
        LocalDate selectedDate = viewModel.getSelectedDateLiveData().getValue();

        if (selectedDate == null) selectedDate = LocalDate.now();

        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi"));
        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String dateText = formattedDate + " (" + dayOfWeek + ")";

        String shiftTitle = selectedShift != null ? selectedShift.getTitle() : "Ca làm việc";
        String timeRange = selectedShift != null ? selectedShift.getTimeRange() : "";

        String desc = String.format(
                Locale.getDefault(),
                "Đã xếp ca cho %d nhân viên vào ngày\n%s - %s\n(%s).",
                employees.size(),
                dateText,
                shiftTitle,
                timeRange
        );

        binding.tvSuccessDesc.setText(desc);
    }

    private void setupActions() {
        // 1. Nút Xuất ra bảng tính Excel
        binding.btnExportExcel.setOnClickListener(v -> viewModel.exportToExcel());

        // 2. Quay lại màn Lịch: kết thúc luồng xếp ca, dọn state, pop toàn bộ stack của luồng
        binding.btnBackToSchedule.setOnClickListener(v -> {
            viewModel.resetAssignState();
            Navigation.findNavController(v).popBackStack(R.id.selectEmployeeFragment, true);
        });

        // 3. Xếp ca khác: quay về màn xếp ca, giữ danh sách nhân viên, xóa cờ thành công
        binding.btnAssignAnother.setOnClickListener(v -> {
            viewModel.clearAssignSuccessEvent();
            Navigation.findNavController(v).popBackStack(R.id.assignShiftFragment, false);
        });
    }

    private void openExcelFile(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng đọc file Excel", Toast.LENGTH_SHORT).show();
            // Fallback: Share file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(shareIntent, "Tải xuống bảng tính"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
