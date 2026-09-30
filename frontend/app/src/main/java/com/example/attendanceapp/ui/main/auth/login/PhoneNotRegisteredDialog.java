package com.example.attendanceapp.ui.main.auth.login;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.attendanceapp.R;
import com.example.attendanceapp.core.utils.ColorUtil;
import com.example.attendanceapp.databinding.DialogAuthPhoneNotRegisteredBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhoneNotRegisteredDialog extends DialogFragment {

    public static final String TAG = "PhoneNotRegisteredDialog";
    private static final String ARG_PHONE = "arg_phone";
    
    public interface OnActionListener {
        void onContinue(String phone);
    }

    private DialogAuthPhoneNotRegisteredBinding binding;
    private OnActionListener listener;

    public static PhoneNotRegisteredDialog newInstance(String phone) {
        PhoneNotRegisteredDialog fragment = new PhoneNotRegisteredDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAuthPhoneNotRegisteredBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String phone = getArguments() != null ? getArguments().getString(ARG_PHONE, "") : "";

        String prefix = "Số điện thoại: ";
        String suffix = " chưa đăng ký tài khoản. Bấm tiếp tục để đăng ký.";
        String fullText = prefix + phone + suffix;

       SpannableStringBuilder spannable = new SpannableStringBuilder(fullText);

        int start = prefix.length();
        int end = start + phone.length();

        if (!phone.isEmpty()) {
            // Lấy màu colorPrimary trực tiếp bằng ColorUtil
            int primaryColor = ColorUtil.getThemeColor(requireContext(), R.attr.colorPrimary);

            // Đổi màu cho số điện thoại
            spannable.setSpan(new ForegroundColorSpan(primaryColor),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // In đậm số điện thoại
            spannable.setSpan(new StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        binding.tvDialogMessage.setText(spannable);

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnContinue.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinue(phone);
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) return;

        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int marginPx = (int) (24 * getResources().getDisplayMetrics().density);
        int width = getResources().getDisplayMetrics().widthPixels - (marginPx * 2);

        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
