package com.example.expensetracker1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.FragmentStatisticsBinding;
import com.example.expensetracker1.util.AppSettings;
import com.example.expensetracker1.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private TransactionViewModel viewModel;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                this.allTransactions = transactions;
                applyFilters();
            }
        });

        binding.chipGroupTime.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
        binding.chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
    }

    private void applyFilters() {
        if (allTransactions == null) return;

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
        } else {
            filtered = allTransactions;
        }

        updateUI(filtered);
    }

    private void updateUI(List<Transaction> transactions) {
        if (transactions == null) return;

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

        binding.tvTotalIncome.setText(AppSettings.formatAmount(requireContext(), income));
        binding.tvTotalExpenses.setText(AppSettings.formatAmount(requireContext(), expense));

        setupPieChart(categoryMap, targetType);
    }

    private void setupPieChart(Map<String, Double> categoryMap, String type) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setCenterText("INCOME".equals(type) ? "Thu nhập" : "Chi tiêu");
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
