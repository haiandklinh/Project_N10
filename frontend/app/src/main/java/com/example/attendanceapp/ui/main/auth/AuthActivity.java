package com.example.attendanceapp.ui.main.auth;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.ActivityAuthBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Phải gọi super.onCreate() ĐẦU TIÊN
        super.onCreate(savedInstanceState);

        // 2. Khởi tạo ViewModel SAU super.onCreate()
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 3. Apply Theme
        applyAppTheme(viewModel.getCurrentTheme().getValue());

        // 4. Setup UI & ViewBinding
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void applyAppTheme(String theme) {
        if (theme == null) theme = "avocado";
        switch (theme) {
            case "watermelon":
                setTheme(R.style.Theme_AttendanceApp_Watermelon);
                break;
            case "orange":
                setTheme(R.style.Theme_AttendanceApp_Orange);
                break;
            case "pink":
                setTheme(R.style.Theme_AttendanceApp_Pink);
                break;
            case "blueberry":
                setTheme(R.style.Theme_AttendanceApp_Blueberry);
                break;
            case "chocolate":
                setTheme(R.style.Theme_AttendanceApp_Chocolate);
                break;
            default:
                setTheme(R.style.Theme_AttendanceApp_Avocado);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}