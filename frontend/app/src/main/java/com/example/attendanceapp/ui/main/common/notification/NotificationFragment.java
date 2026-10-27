package com.example.attendanceapp.ui.main.common.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.attendanceapp.databinding.FragmentNotificationBinding;
import com.example.attendanceapp.domain.model.Notification;
import com.example.attendanceapp.domain.model.UserRole;
import com.example.attendanceapp.domain.repository.AppStateRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationFragment extends Fragment {

    @Inject
    AppStateRepository appStateRepository;

    private FragmentNotificationBinding binding;
    private NotificationAdapter notificationAdapter;
    private NotificationViewModel viewModel;

    public NotificationFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        setupViews();
        setupListeners();
        setupObservers();
    }

    private void setupViews() {
        binding.toolbarApp.setTitle("Hòm thư thông báo");

        notificationAdapter = new NotificationAdapter(new ArrayList<>(), this::onNotificationClicked);

        binding.rvNotification.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvNotification.setAdapter(notificationAdapter);
        binding.rvNotification.setHasFixedSize(true);
    }

    private void setupListeners() {
        binding.toolbarApp.setOnBackClickListener(() -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.btnMarkAllRead.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.markAllAsRead();
            }
        });
    }

    private void setupObservers() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), this::renderList);
    }

    private void renderList(List<Notification> list) {
        if (binding == null) return;
        List<Notification> safeList = (list != null) ? list : new ArrayList<>();
        notificationAdapter.updateData(new ArrayList<>(safeList));

        boolean isEmpty = safeList.isEmpty();
        binding.rvNotification.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.includeEmptyState.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void onNotificationClicked(Notification notification) {
        if (notification == null) return;
        if (!notification.isRead()) {
            viewModel.markAsRead(notification.getId());
        }
        UserRole role = (appStateRepository != null) ? appStateRepository.getUserRole() : null;
        NotificationDetailBottomSheet.newInstance(notification, role)
                .show(getChildFragmentManager(), NotificationDetailBottomSheet.TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
