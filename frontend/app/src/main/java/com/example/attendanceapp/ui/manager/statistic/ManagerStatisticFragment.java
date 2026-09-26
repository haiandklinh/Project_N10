package com.example.attendanceapp.ui.manager.statistic;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.databinding.FragmentManagerStatisticBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerStatisticFragment extends Fragment {

    private FragmentManagerStatisticBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerStatisticBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupBarChart();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 15f)); // Thứ 2
        entries.add(new BarEntry(1, 20f)); // Thứ 3
        entries.add(new BarEntry(2, 5f));  // Thứ 4
        entries.add(new BarEntry(3, 8f));  // Thứ 5
        entries.add(new BarEntry(4, 12f)); // Thứ 6
        entries.add(new BarEntry(5, 2f));  // Thứ 7

        BarDataSet dataSet = new BarDataSet(entries, "Số lượt đi muộn");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        binding.barChartAttendance.setData(barData);

        // Cấu hình trục X
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7"};
        XAxis xAxis = binding.barChartAttendance.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        binding.barChartAttendance.getDescription().setEnabled(false);
        binding.barChartAttendance.animateY(1000);
        binding.barChartAttendance.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
