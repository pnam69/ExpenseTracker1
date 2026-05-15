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
        
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                this.allTransactions = transactions;
                filterTransactions(binding.etSearch.getText().toString());
            }
        });

        setupSearch();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTransactions(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allTransactions);
        } else {
            List<Transaction> filtered = allTransactions.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                            t.getCategory().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.updateData(filtered);
        }
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
