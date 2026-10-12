package com.example.attendanceapp.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import com.example.attendanceapp.domain.model.EmployeeStatus;
import com.google.android.material.chip.Chip;

public class EmployeeStatusUi {

    public static void bind(Chip chip, EmployeeStatus status) {
        if (status == null) return;
        
        Context context = chip.getContext();
        chip.setText(status.getLabel());
        
        int textColor = ContextCompat.getColor(context, status.getTextColorRes());
        int backgroundColor = ContextCompat.getColor(context, status.getContainerColorRes());
        
        chip.setTextColor(textColor);
        chip.setChipBackgroundColor(ColorStateList.valueOf(backgroundColor));
    }
}