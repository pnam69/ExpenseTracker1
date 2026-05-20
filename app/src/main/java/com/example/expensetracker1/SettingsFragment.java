package com.example.expensetracker1;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker1.databinding.FragmentSettingsBinding;
import com.example.expensetracker1.util.AppSettings;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        renderCurrentSettings();

        binding.btnProfile.setOnClickListener(v -> Toast.makeText(requireContext(), "Hồ sơ cá nhân đang được hoàn thiện", Toast.LENGTH_SHORT).show());

        binding.btnCurrency.setOnClickListener(v -> showCurrencyDialog());

        binding.btnLimit.setOnClickListener(v -> showDailyLimitDialog());

        binding.btnReset.setOnClickListener(v -> showResetConfirmation());

        binding.switchDarkMode.setChecked(AppSettings.isDarkModeEnabled(requireContext()));
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings.setDarkModeEnabled(requireContext(), isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Toast.makeText(requireContext(), isChecked ? "Đã bật Chế độ tối" : "Đã tắt Chế độ tối", Toast.LENGTH_SHORT).show();
        });
    }

    private void renderCurrentSettings() {
        binding.tvCurrencyValue.setText(AppSettings.getCurrencyLabel(requireContext()));
        binding.tvLimitValue.setText(String.format(
                Locale.getDefault(),
                "%s/ngày",
                AppSettings.formatAmount(requireContext(), AppSettings.getDailyLimit(requireContext()))
        ));
    }

    private void showCurrencyDialog() {
        String[] labels = {"Việt Nam Đồng (VND)", "US Dollar (USD)", "Euro (EUR)"};
        String[] symbols = {"đ", "$", "€"};
        String currentLabel = AppSettings.getCurrencyLabel(requireContext());
        int checkedIndex = 0;

        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(currentLabel)) {
                checkedIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn đơn vị tiền tệ")
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    AppSettings.setCurrency(requireContext(), labels[which], symbols[which]);
                    renderCurrentSettings();
                    Toast.makeText(requireContext(), "Đã cập nhật " + labels[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showDailyLimitDialog() {
        Context context = requireContext();
        double currentLimitVnd = AppSettings.getDailyLimit(context);
        double rate = AppSettings.getExchangeRate(context);
        double currentDisplayLimit = currentLimitVnd * rate;

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Nhập hạn mức/ngày");
        input.setText(String.format(Locale.US, "%.2f", currentDisplayLimit));

        new AlertDialog.Builder(context)
                .setTitle("Thiết lập hạn mức chi tiêu")
                .setMessage("Hạn mức này sẽ được dùng để tính số tiền còn lại trong ngày (" + AppSettings.getCurrencySymbol(context) + ").")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    try {
                        double inputLimit = Double.parseDouble(input.getText().toString().trim());
                        if (inputLimit <= 0) {
                            Toast.makeText(context, "Hạn mức phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double limitVnd = inputLimit / rate;
                        AppSettings.setDailyLimit(context, limitVnd);
                        renderCurrentSettings();
                        Toast.makeText(context, "Đã cập nhật hạn mức", Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Giá trị hạn mức không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá toàn bộ dữ liệu giao dịch không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xoá tất cả", (dialog, which) -> {
                    viewModel.deleteAllTransactions(() -> {
                        Toast.makeText(requireContext(), "Dữ liệu đã được xoá", Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
