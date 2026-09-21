package com.example.attendanceapp.ui.employee.home.payroll;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentPayrollBinding;

import java.util.ArrayList;
import java.util.List;

public class PayrollFragment extends Fragment {

    private FragmentPayrollBinding binding;

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

            // Giữ nguyên padding gốc từ XML, chỉ cộng thêm chiều cao Status Bar vào paddingTop
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
    }

    private void setupListeners() {
        binding.includeHeader.btnBack.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
    }

    private void setupRecyclerView() {
        List<PayrollAdapter.PayrollItem> list = new ArrayList<>();
        list.add(new PayrollAdapter.PayrollItem("Tháng 09/2026", "8/22 ngày công", null, "Đang tính", false));
        list.add(new PayrollAdapter.PayrollItem("Tháng 08/2026", "21/22 ngày công", "+0.5M thưởng", "14.82M đ", true));
        list.add(new PayrollAdapter.PayrollItem("Tháng 07/2026", "23/23 ngày công", "+1.5M thưởng", "16.50M đ", true));
        list.add(new PayrollAdapter.PayrollItem("Tháng 06/2026", "20/21 ngày công", null, "14.29M đ", true));

        PayrollAdapter adapter = new PayrollAdapter(list);
        binding.rvPayrollHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPayrollHistory.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}