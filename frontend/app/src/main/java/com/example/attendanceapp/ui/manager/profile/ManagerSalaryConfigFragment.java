package com.example.attendanceapp.ui.manager.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerSalaryConfigBinding;

import java.text.DecimalFormat;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerSalaryConfigFragment extends Fragment {

    private FragmentManagerSalaryConfigBinding binding;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "SALARY_CONFIG_PREFS";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerSalaryConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupToolbar();
        loadSavedConfig();
        setupListeners();
        setupCurrencyFormatting();
    }

    private void setupToolbar() {
        binding.toolbarApp.setTitle(getString(R.string.salary_config_title));
        binding.toolbarApp.setOnBackClickListener(() -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void loadSavedConfig() {
        // Tải dữ liệu và định dạng hiển thị ngay lập tức
        setFormattedText(binding.etRateMorning, prefs.getString("rate_morning", "180000"));
        setFormattedText(binding.etRateAfternoon, prefs.getString("rate_afternoon", "180000"));
        setFormattedText(binding.etRateNight, prefs.getString("rate_night", "120000"));
        setFormattedText(binding.etRateFulltime, prefs.getString("rate_fulltime", "400000"));
        setFormattedText(binding.etPenaltyLate, prefs.getString("penalty_late", "30000"));
        binding.etWeekendMultiplier.setText(prefs.getString("weekend_multiplier", "1.5"));
    }

    private void setFormattedText(EditText et, String value) {
        try {
            double parsed = Double.parseDouble(value);
            et.setText(DecimalFormat.getNumberInstance(Locale.GERMANY).format(parsed));
        } catch (Exception e) {
            et.setText(value);
        }
    }

    private void setupListeners() {
        binding.btnSaveConfig.setOnClickListener(v -> {
            if (validateInputs()) {
                saveConfig();
            }
        });
    }

    private void setupCurrencyFormatting() {
        addCurrencyWatcher(binding.etRateMorning);
        addCurrencyWatcher(binding.etRateAfternoon);
        addCurrencyWatcher(binding.etRateNight);
        addCurrencyWatcher(binding.etRateFulltime);
        addCurrencyWatcher(binding.etPenaltyLate);
    }

    private void addCurrencyWatcher(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = DecimalFormat.getNumberInstance(Locale.GERMANY).format(parsed);
                            current = formatted;
                            editText.setText(formatted);
                            editText.setSelection(formatted.length());
                        } catch (NumberFormatException ignored) {}
                    }
                    editText.addTextChangedListener(this);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String errorEmpty = getString(R.string.salary_config_error_empty);

        EditText[] fields = {
                binding.etRateMorning, binding.etRateAfternoon, 
                binding.etRateNight, binding.etRateFulltime, 
                binding.etPenaltyLate
        };

        for (EditText field : fields) {
            if (getCleanValue(field).isEmpty()) {
                field.setError(errorEmpty);
                isValid = false;
            }
        }

        String multiplier = binding.etWeekendMultiplier.getText().toString().trim();
        if (multiplier.isEmpty()) {
            binding.etWeekendMultiplier.setError(errorEmpty);
            isValid = false;
        } else {
            try {
                double val = Double.parseDouble(multiplier);
                if (val < 1.0) {
                    binding.etWeekendMultiplier.setError("Hệ số phải >= 1.0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.etWeekendMultiplier.setError("Định dạng không hợp lệ");
                isValid = false;
            }
        }

        if (!isValid) {
            Toast.makeText(getContext(), "Vui lòng kiểm tra lại các thông tin nhập liệu", Toast.LENGTH_SHORT).show();
        }

        return isValid;
    }

    private String getCleanValue(EditText et) {
        return et.getText().toString().replaceAll("[.,]", "");
    }

    private void saveConfig() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("rate_morning", getCleanValue(binding.etRateMorning));
        editor.putString("rate_afternoon", getCleanValue(binding.etRateAfternoon));
        editor.putString("rate_night", getCleanValue(binding.etRateNight));
        editor.putString("rate_fulltime", getCleanValue(binding.etRateFulltime));
        editor.putString("penalty_late", getCleanValue(binding.etPenaltyLate));
        editor.putString("weekend_multiplier", binding.etWeekendMultiplier.getText().toString());
        
        if (editor.commit()) {
            Toast.makeText(getContext(), getString(R.string.salary_config_success), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
        } else {
            Toast.makeText(getContext(), "Lỗi hệ thống khi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
