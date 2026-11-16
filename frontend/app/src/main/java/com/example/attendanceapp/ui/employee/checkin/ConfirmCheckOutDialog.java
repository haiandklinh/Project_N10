package com.example.attendanceapp.ui.employee.checkin;

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
import com.example.attendanceapp.databinding.DialogConfirmCheckoutBinding;

import java.util.Locale;

public class ConfirmCheckOutDialog extends DialogFragment {

    public static final String TAG = "ConfirmCheckOutDialog";
    private static final String ARG_HOURS = "arg_hours";
    private static final String ARG_MINUTES = "arg_minutes";

    public interface OnConfirmListener {
        void onConfirm();
    }

    private DialogConfirmCheckoutBinding binding;
    private OnConfirmListener onConfirmListener;

    public static ConfirmCheckOutDialog newInstance(long hours, long minutes) {
        ConfirmCheckOutDialog dialog = new ConfirmCheckOutDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_HOURS, hours);
        args.putLong(ARG_MINUTES, minutes);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        this.onConfirmListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogConfirmCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long hours = getArguments() != null ? getArguments().getLong(ARG_HOURS, 0) : 0;
        long minutes = getArguments() != null ? getArguments().getLong(ARG_MINUTES, 0) : 0;

        boolean isEn = Locale.getDefault().getLanguage().equals("en");
        String timeStr = isEn
                ? String.format(Locale.getDefault(), "%02d hours : %02d minutes", hours, minutes)
                : String.format(Locale.getDefault(), "%02d giờ : %02d phút", hours, minutes);

        String prefix = isEn ? "You have worked for " : "Bạn đã làm được ";
        String suffix = isEn
                ? ".\nAre you sure you want to end your shift?"
                : ".\nBạn có chắc chắn muốn kết thúc ca làm việc?";
        String fullText = prefix + timeStr + suffix;

        SpannableStringBuilder spannable = new SpannableStringBuilder(fullText);
        int start = prefix.length();
        int end = start + timeStr.length();

        int primaryColor = ColorUtil.getThemeColor(requireContext(), R.attr.colorPrimary);
        spannable.setSpan(new ForegroundColorSpan(primaryColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvDialogMessage.setText(spannable);

        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.btnConfirm.setOnClickListener(v -> {
            dismiss();
            if (onConfirmListener != null) {
                onConfirmListener.onConfirm();
            }
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
