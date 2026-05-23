package com.example.expensetracker1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.widget.EditText;
import android.text.InputType;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.FragmentDashboardBinding;
import com.example.expensetracker1.util.AppSettings;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        
        setupRecyclerView();
        
        // Cập nhật lời chào
        android.content.Context context = getContext();
        if (context != null) {
            String userName = AppSettings.getUserName(context);
            binding.tvWelcomeHeader.setText(getString(R.string.dashboard_greeting, userName));
        }
        
        // GIÁ TRỊ KHỞI TẠO: Ép hiển thị hạn mức ngay lập tức để không bị hiện 0đ
        updateAssistantUI(0.0, 0.0, 0.0);
        
        observeData();
        setupQuickAdd();
        
        binding.tvSeeAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToHistory();
            }
        });

        binding.cardEmergency.setOnClickListener(v -> showEmergencyGoalDialog());
        binding.cardDailyLimit.setOnClickListener(v -> showDailyLimitDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        android.content.Context context = getContext();
        if (binding != null && context != null) {
            String userName = AppSettings.getUserName(context);
            binding.tvWelcomeHeader.setText(getString(R.string.dashboard_greeting, userName));
        }
    }

    private void showDailyLimitDialog() {
        android.content.Context context = requireContext();
        double currentLimitVnd = AppSettings.getDailyLimit(context);
        double rate = AppSettings.getExchangeRate(context);
        double currentDisplayLimit = currentLimitVnd * rate;

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(R.string.settings_daily_limit);
        input.setText(String.format(java.util.Locale.US, "%.2f", currentDisplayLimit));

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
                            android.widget.Toast.makeText(context, R.string.msg_limit_positive, android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double limitVnd = inputLimit / rate;
                        AppSettings.setDailyLimit(context, limitVnd);
                        observeData(); // Refresh UI
                    } catch (NumberFormatException e) {
                        android.widget.Toast.makeText(context, R.string.msg_limit_invalid, android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEmergencyGoalDialog() {
        android.content.Context context = requireContext();
        double currentGoalVnd = AppSettings.getEmergencyGoal(context);
        double rate = AppSettings.getExchangeRate(context);
        double currentDisplayGoal = currentGoalVnd * rate;

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(getString(R.string.settings_emergency_goal));
        input.setText(String.format(java.util.Locale.US, "%.2f", currentDisplayGoal));

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
                            android.widget.Toast.makeText(context, "Mục tiêu phải lớn hơn 0", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double goalVnd = inputGoal / rate;
                        AppSettings.setEmergencyGoal(context, goalVnd);
                        observeData(); // Refresh UI
                    } catch (NumberFormatException e) {
                        android.widget.Toast.makeText(context, "Giá trị không hợp lệ", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void observeData() {
        long startOfDay = getStartOfDayMillis();
        long endOfDay = startOfDay + 24L * 60L * 60L * 1000L;
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();

        // Biến lưu trữ an toàn
        final double[] currentSpentToday = {0.0};
        final double[] currentSpentMonth = {0.0};
        final double[] currentTotalBalance = {0.0};

        viewModel.getTodayExpenses(startOfDay, endOfDay).observe(getViewLifecycleOwner(), today -> {
            currentSpentToday[0] = (today != null) ? today : 0.0;
            updateAssistantUI(currentSpentToday[0], currentSpentMonth[0], currentTotalBalance[0]);
        });

        viewModel.getMonthExpenses(startOfMonth).observe(getViewLifecycleOwner(), month -> {
            currentSpentMonth[0] = (month != null) ? month : 0.0;
            updateAssistantUI(currentSpentToday[0], currentSpentMonth[0], currentTotalBalance[0]);
        });

        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                double total = 0;
                for (Transaction t : transactions) {
                    if ("INCOME".equals(t.getType())) total += t.getAmount();
                    else total -= t.getAmount();
                }
                currentTotalBalance[0] = total;
                updateAssistantUI(currentSpentToday[0], currentSpentMonth[0], currentTotalBalance[0]);

                // Hiển thị 5 giao dịch gần nhất
                List<Transaction> recent = transactions.size() > 5 ? transactions.subList(0, 5) : transactions;
                adapter.updateData(recent);
            }
        });
    }

    private long getStartOfDayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void updateAssistantUI(double today, double month, double totalBalance) {
        // Đảm bảo Context vẫn tồn tại
        android.content.Context context = getContext();
        if (!isAdded() || context == null || binding == null) return;

        double dailyBudget = AppSettings.getDailyLimit(context);
        double remainingValue = dailyBudget - today;

        binding.tvDailyRemaining.setText(AppSettings.formatAmount(context, remainingValue));

        // TIẾN ĐỘ THÁNG
        double monthlyBudget = dailyBudget * 30.0;
        int percent = (monthlyBudget > 0) ? (int) ((month / monthlyBudget) * 100) : 0;
        if (percent > 100) percent = 100;

        binding.tvBudgetPercent.setText(String.format(java.util.Locale.US, "%d%%", percent));
        binding.progressBudget.setProgress(percent);

        // QUỸ DỰ PHÒNG
        double emergencyGoal = AppSettings.getEmergencyGoal(getContext());
        int emergencyPercent = (emergencyGoal > 0) ? (int) ((totalBalance / emergencyGoal) * 100) : 0;
        if (emergencyPercent < 0) emergencyPercent = 0;
        if (emergencyPercent > 100) emergencyPercent = 100;

        binding.progressEmergency.setProgress(emergencyPercent);
        binding.tvEmergencyProgress.setText(String.format("%s / %s",
                AppSettings.formatAmount(context, Math.max(0, totalBalance)),
                AppSettings.formatAmount(context, emergencyGoal)));

        if (remainingValue < 0) {
            binding.tvDailyRemaining.setTextColor(ContextCompat.getColor(context, R.color.error));
            binding.tvBudgetWarning.setText(getString(R.string.budget_over_limit));
            binding.tvBudgetWarning.setTextColor(ContextCompat.getColor(context, R.color.error));
            binding.tvSavingsMessage.setText(getString(R.string.budget_over_limit));
            binding.tvSavingsMessage.setTextColor(ContextCompat.getColor(context, R.color.error));
        } else if (percent > 85) {
            binding.tvDailyRemaining.setTextColor(ContextCompat.getColor(context, R.color.on_primary_container));
            binding.tvBudgetWarning.setText(getString(R.string.budget_warning_danger));
            binding.tvBudgetWarning.setTextColor(ContextCompat.getColor(context, R.color.error));
            binding.tvSavingsMessage.setText(getString(R.string.savings_message));
            binding.tvSavingsMessage.setTextColor(ContextCompat.getColor(context, R.color.on_primary_container));
        } else {
            binding.tvDailyRemaining.setTextColor(ContextCompat.getColor(context, R.color.on_primary_container));
            binding.tvBudgetWarning.setText(getString(R.string.budget_warning_safe));
            binding.tvBudgetWarning.setTextColor(ContextCompat.getColor(context, R.color.on_primary_container));
            binding.tvSavingsMessage.setText(getString(R.string.savings_message));
            binding.tvSavingsMessage.setTextColor(ContextCompat.getColor(context, R.color.on_primary_container));
        }
    }

    private void setupQuickAdd() {
        binding.chipMeal.setOnClickListener(v -> quickAdd("Ăn uống", 35000.0, "Ăn uống"));
        binding.chipMarket.setOnClickListener(v -> quickAdd("Đi chợ", 150000.0, "Sinh hoạt"));
        binding.chipFuel.setOnClickListener(v -> quickAdd("Đổ xăng", 50000.0, "Đi lại"));
        binding.chipBill.setOnClickListener(v -> quickAdd("Hoá đơn", 200000.0, "Hoá đơn"));
    }

    private void quickAdd(String title, double amount, String category) {
        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
        intent.putExtra(AddTransactionActivity.EXTRA_TITLE, title);
        intent.putExtra(AddTransactionActivity.EXTRA_AMOUNT, amount);
        intent.putExtra(AddTransactionActivity.EXTRA_CATEGORY, category);
        intent.putExtra(AddTransactionActivity.EXTRA_TYPE, "EXPENSE");
        startActivity(intent);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this::editTransaction);
        adapter.setOnItemLongClickListener(this::showDeleteConfirmation);
        
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(adapter);
    }

    private void editTransaction(Transaction transaction) {
        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
        intent.putExtra(AddTransactionActivity.EXTRA_ID, transaction.getId());
        startActivity(intent);
    }

    private void showDeleteConfirmation(Transaction transaction) {
        android.content.Context context = getContext();
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle("Xoá giao dịch")
                .setMessage("Bạn có chắc chắn muốn xoá '" + transaction.getTitle() + "'?")
                .setPositiveButton("Xoá", (dialog, which) -> viewModel.delete(transaction))
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
