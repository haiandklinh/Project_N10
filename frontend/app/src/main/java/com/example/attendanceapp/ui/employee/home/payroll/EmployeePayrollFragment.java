package com.example.attendanceapp.ui.employee.home.payroll;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.dto.response.staff.StaffPayrollResponse;
import com.example.attendanceapp.databinding.FragmentPayrollBinding;
import com.example.attendanceapp.domain.repository.StaffRepository;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class EmployeePayrollFragment extends Fragment {

    private FragmentPayrollBinding binding;
    private EmployeePayrollAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    StaffRepository staffRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPayrollBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.includeHeader.layoutHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + getResources().getDimensionPixelSize(R.dimen.spacing_m),
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        setupListeners();
        setupRecyclerView();
        loadPayrollHistory();
    }

    private void setupListeners() {
        binding.includeHeader.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void setupRecyclerView() {
        adapter = new EmployeePayrollAdapter(new ArrayList<>());
        binding.rvPayrollHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPayrollHistory.setAdapter(adapter);
    }

    private void loadPayrollHistory() {
        if (staffRepository == null) return;
        disposables.add(
                staffRepository.getPayrollHistory()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                                        bindPayrollData(response.getData());
                                    } else {
                                        showEmptyState();
                                    }
                                },
                                error -> {
                                    Toast.makeText(getContext(), "Không thể tải lịch sử lương: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    showEmptyState();
                                }
                        )
        );
    }

    private void bindPayrollData(List<StaffPayrollResponse> list) {
        List<EmployeePayrollAdapter.PayrollItem> items = new ArrayList<>();
        DecimalFormat currencyDf = new DecimalFormat("#,###");

        double maxSalary = 0;
        double sumSalary = 0;
        double latestBase = 0;

        for (int i = 0; i < list.size(); i++) {
            StaffPayrollResponse p = list.get(i);
            double total = p.getTotalSalary();
            if (total > maxSalary) maxSalary = total;
            sumSalary += total;
            if (i == 0) latestBase = p.getBaseSalary();

            String monthStr = formatMonth(p.getTimeCreate());
            String workDaysStr = p.getNumberOfWorkShifts() + " ca làm";
            String amountStr = currencyDf.format((long) total) + " đ";

            boolean isPaid = "PAID".equalsIgnoreCase(p.getStatus()) 
                    || "CONFIRMED".equalsIgnoreCase(p.getStatus())
                    || "DONE".equalsIgnoreCase(p.getStatus());

            String statusNote = isPaid ? null : "Đang xử lý";

            items.add(new EmployeePayrollAdapter.PayrollItem(
                    monthStr,
                    workDaysStr,
                    statusNote,
                    amountStr,
                    isPaid,
                    p
            ));
        }

        adapter.updateData(items);

        // Cập nhật thống kê header
        double avgSalary = list.isEmpty() ? 0 : sumSalary / list.size();
        binding.includeHeader.tvStatBasic.setText(formatShortAmount(latestBase));
        binding.includeHeader.tvStatMax.setText(formatShortAmount(maxSalary));
        binding.includeHeader.tvStatAvg.setText(formatShortAmount(avgSalary));
    }

    private void showEmptyState() {
        if (adapter != null) {
            adapter.updateData(new ArrayList<>());
        }
        binding.includeHeader.tvStatBasic.setText("0đ");
        binding.includeHeader.tvStatMax.setText("0đ");
        binding.includeHeader.tvStatAvg.setText("0đ");
    }

    private String formatMonth(String timeCreate) {
        if (timeCreate == null || timeCreate.isEmpty()) return "Kỳ lương";
        try {
            // ví dụ timeCreate: "2026-10-25T14:30:00" hoặc "2026-10-25"
            String datePart = timeCreate.length() >= 7 ? timeCreate.substring(0, 7) : timeCreate;
            String[] parts = datePart.split("-");
            if (parts.length >= 2) {
                return "Tháng " + parts[1] + "/" + parts[0];
            }
        } catch (Exception ignored) {}
        return "Kỳ lương " + timeCreate;
    }

    private String formatShortAmount(double amount) {
        if (amount <= 0) return "0đ";
        if (amount >= 1_000_000) {
            return String.format(java.util.Locale.US, "%.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(java.util.Locale.US, "%.0fK", amount / 1_000);
        }
        return String.format(java.util.Locale.US, "%.0fđ", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
