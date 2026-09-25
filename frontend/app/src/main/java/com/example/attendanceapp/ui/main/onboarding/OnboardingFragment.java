package com.example.attendanceapp.ui.main.onboarding;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentAuthOnboardingBinding;
import com.example.attendanceapp.domain.model.OnboardingItem;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OnboardingFragment extends Fragment {

    private FragmentAuthOnboardingBinding binding;
    private OnboardingViewModel viewModel;
    private OnboardingPagerAdapter adapter;
    private List<OnboardingItem> onboardingItems;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null) return;
            int currentPosition = binding.viewPagerOnboarding.getCurrentItem();
            if (currentPosition == onboardingItems.size() - 1) {
                binding.viewPagerOnboarding.setCurrentItem(0, true);
            } else {
                binding.viewPagerOnboarding.setCurrentItem(currentPosition + 1, true);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthOnboardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);

        buildOnboardingItems();
        setupRecyclerView();
        setupViewPager();
        setupListeners();
    }

    private void buildOnboardingItems() {
        onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_camera,
                getString(R.string.onboarding_1_title),
                getString(R.string.onboarding_1_desc)
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_form,
                getString(R.string.onboarding_2_title),
                getString(R.string.onboarding_2_desc)
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_search,
                getString(R.string.onboarding_3_title),
                getString(R.string.onboarding_3_desc)
        ));
        onboardingItems.add(new OnboardingItem(
                R.drawable.ic_onboarding_mind,
                getString(R.string.onboarding_4_title),
                getString(R.string.onboarding_4_desc)
        ));
    }

    private void setupRecyclerView() {
        adapter = new OnboardingPagerAdapter(onboardingItems);
        binding.viewPagerOnboarding.setAdapter(adapter);
        binding.tabIndicator.setViewPager(binding.viewPagerOnboarding);
    }

    private void setupViewPager() {
        binding.viewPagerOnboarding.setOffscreenPageLimit(3);
        binding.viewPagerOnboarding.setClipToPadding(false);
        binding.viewPagerOnboarding.setClipChildren(false);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        binding.viewPagerOnboarding.setPageTransformer(transformer);

        binding.viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonState(position);
                handler.removeCallbacks(runnable);
                //handler.postDelayed(runnable, 5000);
            }
        });
    }

    private void setupListeners() {
        binding.btnSkip.setOnClickListener(v -> finishOnboarding());

        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPagerOnboarding.getCurrentItem();
            int lastIndex = onboardingItems.size() - 1;
            if (current < lastIndex) {
                binding.viewPagerOnboarding.setCurrentItem(current + 1, true);
            } else {
                finishOnboarding();
            }
        });
    }

    private void updateButtonState(int position) {
        boolean isLastPage = (position == onboardingItems.size() - 1);
        binding.btnNext.setText(isLastPage
                ? getString(R.string.action_get_started)
                : getString(R.string.action_next));
        binding.btnSkip.setVisibility(isLastPage ? View.INVISIBLE : View.VISIBLE);
    }

    private void finishOnboarding() {
        // Gọi ViewModel lưu trạng thái hoàn thành Onboarding qua Clean Architecture
        viewModel.completeOnboarding();

        // Điều hướng sang LoginFragment
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_onboardingFragment_to_loginFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
        binding.viewPagerOnboarding.setAdapter(null);
        binding = null;
    }
}