package com.example.attendanceapp.ui.employee.profile;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.attendanceapp.R;
import com.example.attendanceapp.databinding.FragmentPolicyBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EmployeePolicyFragment extends Fragment {

    private FragmentPolicyBinding binding;

    public EmployeePolicyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentPolicyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadPolicy();
    }

    private void setupToolbar() {
        binding.layoutToolbar.tvTitle.setText("Chính sách & Điều khoản");

        binding.layoutToolbar.ivBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void loadPolicy() {
        try {
            String policyContent = getStringFromRawFile(R.raw.privacy_policy);

            binding.tvPolicyContent.setText(
                    Html.fromHtml(
                            policyContent,
                            Html.FROM_HTML_MODE_LEGACY
                    )
            );

        } catch (Exception e) {
            binding.tvPolicyContent.setText(
                    "Không thể tải nội dung chính sách. Vui lòng thử lại sau."
            );
        }
    }

    private String getStringFromRawFile(int resourceId) throws Exception {
        java.io.InputStream inputStream =
                requireContext().getResources().openRawResource(resourceId);

        java.util.Scanner scanner =
                new java.util.Scanner(inputStream, "UTF-8").useDelimiter("\\A");

        String content = scanner.hasNext() ? scanner.next() : "";

        scanner.close();
        inputStream.close();

        return content;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}