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

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        
        setupRecyclerView();
        observeData();
        setupQuickAdd();
    }

    private void observeData() {
        long startOfDay = getStartOfDayMillis();
        long endOfDay = startOfDay + 24L * 60L * 60L * 1000L;

        viewModel.getTodayExpenses(startOfDay, endOfDay).observe(getViewLifecycleOwner(), todayExpenses -> {
            double spent = todayExpenses != null ? todayExpenses : 0.0;
            updateAssistantUI(spent);
        });

        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                // Show only last 5 for dashboard
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

    private void updateAssistantUI(double spentToday) {
        double dailyBudget = AppSettings.getDailyLimit(requireContext());
        double remainingValue = dailyBudget - spentToday;
        if (remainingValue < 0) remainingValue = 0;

        binding.tvDailyRemaining.setText(AppSettings.formatAmount(requireContext(), remainingValue));

        double monthlyLimit = dailyBudget * 30.0;
        int percent = (int) ((spentToday / monthlyLimit) * 100);
        if (percent > 100) percent = 100;

        binding.tvBudgetPercent.setText(percent + "%");
        binding.progressBudget.setProgress(percent);

        if (percent > 80) {
            binding.tvBudgetWarning.setText(getString(R.string.budget_warning_danger));
            binding.tvBudgetWarning.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        } else {
            binding.tvBudgetWarning.setText(getString(R.string.budget_warning_safe));
            binding.tvBudgetWarning.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
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
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
