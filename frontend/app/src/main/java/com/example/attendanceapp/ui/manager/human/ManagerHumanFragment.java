package com.example.attendanceapp.ui.manager.human;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentManagerHumanBinding;
import com.example.attendanceapp.ui.manager.ManagerMainActivity;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManagerHumanFragment extends Fragment {

    private FragmentManagerHumanBinding binding;
    private ManagerHumanViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagerHumanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ManagerHumanViewModel.class);

        setUpViews();
        setupViewPager();
        setupSearch();
        observeBadgeCount();
    }

    private void setUpViews() {
        applyWindowInsets();

        binding.toolbar.getMenu().clear();
        binding.toolbar.inflateMenu(R.menu.menu_manager_human);

        // Bắt sự kiện click các MenuItem trên Toolbar
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_search) {
                toggleSearchBar();
                return true;
            }
            return false;
        });

        // Bắt sự kiện Click cho ActionView custom (Join Requests)
        MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.action_join_requests);
        if (menuItem != null && menuItem.getActionView() != null) {
            menuItem.getActionView().setOnClickListener(v -> {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_managerHumanFragment_to_managerJoinRequestFragment);
            });
        }
    }

    /**
     * Bật/Tắt ẩn hiện thanh tìm kiếm
     */
    private void toggleSearchBar() {
        if (binding.tilSearch.getVisibility() == View.GONE) {
            binding.tilSearch.setVisibility(View.VISIBLE);
            binding.edtSearch.requestFocus();
            showKeyboard(binding.edtSearch);
        } else {
            binding.tilSearch.setVisibility(View.GONE);
            binding.edtSearch.setText(""); // Reset từ khóa khi đóng search
            hideKeyboard(binding.edtSearch);
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void updateBadgeCount(int count) {
        binding.toolbar.post(() -> {
            if (binding == null) return;

            MenuItem menuItem = binding.toolbar.getMenu().findItem(R.id.action_join_requests);
            if (menuItem == null) return;

            View actionView = menuItem.getActionView();
            if (actionView != null) {
                TextView tvBadge = actionView.findViewById(R.id.tv_badge_count);
                if (tvBadge != null) {
                    if (count > 0) {
                        tvBadge.setVisibility(View.VISIBLE);
                        tvBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                    } else {
                        tvBadge.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void observeBadgeCount() {
        viewModel.joinRequestCount.observe(getViewLifecycleOwner(), count -> {
            updateBadgeCount(count != null ? count : 0);
        });
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(new HumanPagerAdapter(this));

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText(R.string.human_tab_employees); break;
                case 1: tab.setText(R.string.human_tab_departments); break;
                case 2: tab.setText(R.string.human_tab_positions); break;
            }
        }).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateSearchHint(position);
                String currentQuery = binding.edtSearch.getText().toString();
                updateViewModelQuery(position, currentQuery);
            }
        });
    }

    private void updateSearchHint(int position) {
        int hintResId;
        switch (position) {
            case 0: hintResId = R.string.human_search_hint_employee; break;
            case 1: hintResId = R.string.human_search_hint_department; break;
            case 2: hintResId = R.string.human_search_hint_position; break;
            default: hintResId = R.string.action_search;
        }
        binding.tilSearch.setHint(getString(hintResId));
    }

    private void setupSearch() {
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateViewModelQuery(binding.viewPager.getCurrentItem(), s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateViewModelQuery(int position, String query) {
        switch (position) {
            case 0: viewModel.setEmployeeSearchQuery(query); break;
            case 1: viewModel.setDepartmentSearchQuery(query); break;
            case 2: viewModel.setPositionSearchQuery(query); break;
        }
    }

    private static class HumanPagerAdapter extends FragmentStateAdapter {
        public HumanPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return HumanTabFragment.newInstance(HumanTabFragment.TabType.EMPLOYEES);
                case 1: return HumanTabFragment.newInstance(HumanTabFragment.TabType.DEPARTMENTS);
                case 2: return HumanTabFragment.newInstance(HumanTabFragment.TabType.POSITIONS);
                default: throw new IllegalArgumentException("Invalid position");
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}