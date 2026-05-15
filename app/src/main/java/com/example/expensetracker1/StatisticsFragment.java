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
import com.example.expensetracker1.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private TransactionViewModel viewModel;

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
        
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(List<Transaction> transactions) {
        if (transactions == null) return;

        double income = 0;
        double expense = 0;
        Map<String, Double> categoryMap = new HashMap<>();

        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
                Double currentAmount = categoryMap.getOrDefault(t.getCategory(), 0.0);
                if (currentAmount != null) {
                    categoryMap.put(t.getCategory(), currentAmount + t.getAmount());
                }
            }
        }

        binding.tvTotalIncome.setText(String.format(Locale.getDefault(), "%,.0fđ", income));
        binding.tvTotalExpenses.setText(String.format(Locale.getDefault(), "%,.0fđ", expense));

        setupPieChart(categoryMap);
    }

    private void setupPieChart(Map<String, Double> categoryMap) {
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
        binding.pieChart.setCenterText("Chi tiêu");
        binding.pieChart.animateY(1000);
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
