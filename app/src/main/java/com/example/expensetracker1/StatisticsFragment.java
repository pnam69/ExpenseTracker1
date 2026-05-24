package com.example.expensetracker1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.FragmentStatisticsBinding;
import com.example.expensetracker1.util.AppSettings;
import com.example.expensetracker1.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private TransactionViewModel viewModel;
    private List<Transaction> allTransactions = new ArrayList<>();
    private long customStartMillis = -1;
    private long customEndMillis = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                this.allTransactions = transactions;
                applyFilters();
            }
        });

        binding.chipGroupTime.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_custom)) {
                showDateRangePicker();
            } else {
                applyFilters();
            }
        });
        binding.chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
    }

    private void applyFilters() {
        if (allTransactions == null || binding == null) return;

        int timeId = binding.chipGroupTime.getCheckedChipId();
        List<Transaction> filtered = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        
        if (timeId == R.id.chip_today) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfToday = calendar.getTimeInMillis();
            for (Transaction t : allTransactions) {
                if (t.getDate() >= startOfToday) filtered.add(t);
            }
        } else if (timeId == R.id.chip_this_month) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfMonth = calendar.getTimeInMillis();
            for (Transaction t : allTransactions) {
                if (t.getDate() >= startOfMonth) filtered.add(t);
            }
        } else if (timeId == R.id.chip_this_week) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startOfWeek = calendar.getTimeInMillis();
            for (Transaction t : allTransactions) {
                if (t.getDate() >= startOfWeek) filtered.add(t);
            }
        } else if (timeId == R.id.chip_custom) {
            if (customStartMillis != -1 && customEndMillis != -1) {
                for (Transaction t : allTransactions) {
                    if (t.getDate() >= customStartMillis && t.getDate() < customEndMillis) {
                        filtered.add(t);
                    }
                }
            } else {
                showDateRangePicker();
                return;
            }
        } else {
            filtered = allTransactions;
        }

        updateUI(filtered);
    }

    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.dialog_date_range_title)
                .build();
        
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                customStartMillis = selection.first;
                // Include the full selected end day with an exclusive upper bound
                customEndMillis = selection.second + 24L * 60L * 60L * 1000L;
                applyFilters();
            }
        });
        
        picker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void updateUI(List<Transaction> transactions) {
        android.content.Context context = getContext();
        if (transactions == null || context == null || binding == null) return;

        double income = 0;
        double expense = 0;
        Map<String, Double> categoryMap = new HashMap<>();
        
        int typeId = binding.chipGroupType.getCheckedChipId();
        String targetType = (typeId == R.id.chip_income_chart) ? "INCOME" : "EXPENSE";

        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
            
            if (targetType.equals(t.getType())) {
                Double currentAmount = categoryMap.getOrDefault(t.getCategory(), 0.0);
                if (currentAmount != null) {
                    categoryMap.put(t.getCategory(), currentAmount + t.getAmount());
                }
            }
        }

        binding.tvTotalIncome.setText(AppSettings.formatAmount(context, income));
        binding.tvTotalExpenses.setText(AppSettings.formatAmount(context, expense));

        setupPieChart(context, categoryMap, targetType);
        generateInsight(categoryMap, income, expense, targetType);
    }

    private void generateInsight(Map<String, Double> categoryMap, double income, double expense, String type) {
        if (categoryMap.isEmpty()) {
            binding.tvInsightText.setText(R.string.stats_no_data);
            return;
        }

        if ("EXPENSE".equals(type)) {
            String topCategory = "";
            double maxAmount = 0;
            for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                if (entry.getValue() > maxAmount) {
                    maxAmount = entry.getValue();
                    topCategory = entry.getKey();
                }
            }

            int percent = (expense > 0) ? (int) ((maxAmount / expense) * 100) : 0;
            String insight = getString(R.string.stats_insight_expense, percent, topCategory);
            binding.tvInsightText.setText(insight);
        } else {
            binding.tvInsightText.setText(R.string.stats_insight_income);
        }
    }

    private void setupPieChart(android.content.Context context, Map<String, Double> categoryMap, String type) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        
        int[] softColors = {
            android.graphics.Color.parseColor("#60A5FA"),
            android.graphics.Color.parseColor("#34D399"),
            android.graphics.Color.parseColor("#F87171"),
            android.graphics.Color.parseColor("#FBBF24"),
            android.graphics.Color.parseColor("#818CF8"),
            android.graphics.Color.parseColor("#A78BFA")
        };
        dataSet.setColors(softColors);
        
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.getDescription().setEnabled(false);
        
        int textColor = ContextCompat.getColor(context, R.color.text_primary);
        int holeColor = ContextCompat.getColor(context, R.color.surface);
        
        binding.pieChart.setCenterText("INCOME".equals(type)
                ? getString(R.string.stats_chart_income)
                : getString(R.string.stats_chart_expense));
        binding.pieChart.setCenterTextColor(textColor);
        binding.pieChart.setHoleColor(holeColor);
        binding.pieChart.getLegend().setTextColor(textColor);

        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
