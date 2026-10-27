package com.example.attendanceapp.ui.manager.reward;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.data.dto.response.manager.StaffItemResponse;
import com.example.attendanceapp.databinding.FragmentManagerRewardPunishBinding;
import com.example.attendanceapp.domain.repository.ManagerRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class ManagerRewardPunishFragment extends Fragment {

    private FragmentManagerRewardPunishBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final List<String> employeeNames = new ArrayList<>();

    @Inject
    ManagerRepository managerRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerRewardPunishBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadEmployees();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.toolbarApp.setTitle("Khen thưởng / Phạt");
    }

    private void loadEmployees() {
        disposables.add(
                managerRepository.getStaffs()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    employeeNames.clear();
                                    if (response != null && response.getData() != null) {
                                        for (StaffItemResponse staff : response.getData()) {
                                            if (staff.getNameStaff() != null && !staff.getNameStaff().isEmpty()) {
                                                employeeNames.add(staff.getNameStaff());
                                            }
                                        }
                                    }
                                },
                                error -> {
                                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                        )
        );
    }

    private void setupListeners() {
        binding.tvSelectEmployee.setOnClickListener(v ->
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Chọn nhân sự")
                        .setItems(employeeNames.toArray(new String[0]), (d, i) ->
                                binding.tvSelectEmployee.setText(employeeNames.get(i)))
                        .show());

        binding.btnSubmit.setOnClickListener(v -> {
            String amount = binding.etAmount.getText().toString().trim();
            String reason = binding.etReason.getText().toString().trim();

            if (amount.isEmpty() || reason.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ số tiền và lý do", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = binding.rbReward.isChecked() ? "Thưởng" : "Phạt";
            Toast.makeText(getContext(), "Đã lưu quyết định " + type + " thành công!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
        binding = null;
    }
}
