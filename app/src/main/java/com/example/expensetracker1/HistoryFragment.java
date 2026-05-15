package com.example.expensetracker1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.FragmentHistoryBinding;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                this.allTransactions = transactions;
                applyFilters();
            }
        });

        setupSearch();
        setupFilters();
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
    }

    private void setupSearch() {
        binding.ivClearSearch.setOnClickListener(v -> binding.etSearch.setText(""));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        String query = binding.etSearch.getText().toString().toLowerCase();
        int checkedId = binding.chipGroupFilters.getCheckedChipId();

        List<Transaction> filtered = new ArrayList<>();

        for (Transaction t : allTransactions) {

            boolean matchesSearch =
                    query.isEmpty() ||
                            (t.getTitle() != null &&
                                    t.getTitle().toLowerCase().contains(query)) ||
                            (t.getCategory() != null &&
                                    t.getCategory().toLowerCase().contains(query));

            boolean matchesType = true;

            if (checkedId == R.id.chip_expense) {
                matchesType = "EXPENSE".equals(t.getType());
            } else if (checkedId == R.id.chip_income) {
                matchesType = "INCOME".equals(t.getType());
            }

            if (matchesSearch && matchesType) {
                filtered.add(t);
            }
        }

        adapter.updateData(filtered);
        binding.layoutEmptyState.setVisibility(
                filtered.isEmpty() ? View.VISIBLE : View.GONE
        );

        adapter.updateData(filtered);
        binding.layoutEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        adapter.setOnItemLongClickListener(this::showDeleteDialog);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);
    }

    private void showDeleteDialog(Transaction transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá giao dịch")
                .setMessage("Bạn có chắc chắn muốn xoá '" + transaction.getTitle() + "'?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.delete(transaction);
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
