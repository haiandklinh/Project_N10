package com.example.attendanceapp.ui.manager.human;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.databinding.FragmentManagerJoinRequestBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerJoinRequestFragment extends Fragment {

    private FragmentManagerJoinRequestBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerJoinRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadJoinRequests();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void loadJoinRequests() {
        // Tạm thời hiển thị trạng thái trống (Empty State) để đồng bộ hóa
        // Khi kết nối API thật, bạn sẽ đưa dữ liệu nhân sự chờ duyệt vào RecyclerView
        binding.rvJoinRequests.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
