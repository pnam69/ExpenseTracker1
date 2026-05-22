package com.example.expensetracker1;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfileActivity.class);
            startActivity(intent);
        });

        binding.btnCurrency.setOnClickListener(v -> showCurrencyDialog());

        binding.btnLimit.setOnClickListener(v -> showDailyLimitDialog());

        binding.btnEmergencyGoal.setOnClickListener(v -> showEmergencyGoalDialog());

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
        binding.tvEmergencyGoalValue.setText(AppSettings.formatAmount(requireContext(), AppSettings.getEmergencyGoal(requireContext())));
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

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_currency_title)
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    AppSettings.setCurrency(requireContext(), labels[which], symbols[which]);
                    renderCurrentSettings();
                    Toast.makeText(requireContext(), getString(R.string.msg_currency_updated, labels[which]), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    requireActivity().recreate();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDailyLimitDialog() {
        Context context = requireContext();
        double currentLimitVnd = AppSettings.getDailyLimit(context);
        double rate = AppSettings.getExchangeRate(context);
        double currentDisplayLimit = currentLimitVnd * rate;

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.settings_daily_limit);
        input.setText(String.format(Locale.US, "%.2f", currentDisplayLimit));

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_limit_title)
                .setMessage(getString(R.string.dialog_limit_message, AppSettings.getCurrencySymbol(context)))
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    try {
                        String text = input.getText().toString().trim();
                        if (text.isEmpty()) return;
                        double inputLimit = Double.parseDouble(text);
                        if (inputLimit <= 0) {
                            Toast.makeText(context, R.string.msg_limit_positive, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double limitVnd = inputLimit / rate;
                        AppSettings.setDailyLimit(context, limitVnd);
                        renderCurrentSettings();
                        Toast.makeText(context, R.string.msg_limit_updated, Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, R.string.msg_limit_invalid, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEmergencyGoalDialog() {
        Context context = requireContext();
        double currentGoalVnd = AppSettings.getEmergencyGoal(context);
        double rate = AppSettings.getExchangeRate(context);
        double currentDisplayGoal = currentGoalVnd * rate;

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.settings_emergency_goal);
        input.setText(String.format(Locale.US, "%.2f", currentDisplayGoal));

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.settings_emergency_goal)
                .setMessage("Thiết lập mục tiêu quỹ dự phòng của bạn (" + AppSettings.getCurrencySymbol(context) + ")")
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    try {
                        String text = input.getText().toString().trim();
                        if (text.isEmpty()) return;
                        double inputGoal = Double.parseDouble(text);
                        if (inputGoal <= 0) {
                            Toast.makeText(context, "Mục tiêu phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double goalVnd = inputGoal / rate;
                        AppSettings.setEmergencyGoal(context, goalVnd);
                        renderCurrentSettings();
                        Toast.makeText(context, "Đã cập nhật mục tiêu", Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Giá trị không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showResetConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_message)
                .setPositiveButton(R.string.dialog_reset_positive, (dialog, which) -> {
                    viewModel.deleteAllTransactions(() -> {
                        Toast.makeText(requireContext(), R.string.msg_data_cleared, Toast.LENGTH_SHORT).show();
                        requireActivity().recreate();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
