package com.example.expensetracker1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.FragmentHistoryBinding;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private int currentSortOrder = 0; // 0: Newest, 1: Oldest, 2: High -> Low, 3: Low -> High

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
        
        binding.btnSortHistory.setOnClickListener(v -> showSortDialog());
    }

    private void showSortDialog() {
        String[] options = {"Mới nhất", "Cũ nhất", "Số tiền (Cao - Thấp)", "Số tiền (Thấp - Cao)"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sắp xếp giao dịch")
                .setItems(options, (dialog, which) -> {
                    sortTransactions(which);
                })
                .show();
    }

    private void sortTransactions(int which) {
        currentSortOrder = which;
        if (getContext() != null) {
            applyFilters();
        }
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

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long startOfMonth = calendar.getTimeInMillis();

        for (Transaction t : allTransactions) {
            boolean matchesSearch = query.isEmpty() || 
                    (t.getTitle() != null && t.getTitle().toLowerCase().contains(query)) ||
                    (t.getCategory() != null && t.getCategory().toLowerCase().contains(query));
            
            boolean matchesChip = true;
            if (checkedId == R.id.chip_expense) {
                matchesChip = "EXPENSE".equals(t.getType());
            } else if (checkedId == R.id.chip_income) {
                matchesChip = "INCOME".equals(t.getType());
            } else if (checkedId == R.id.chip_this_month) {
                matchesChip = t.getDate() >= startOfMonth;
            }
            
            if (matchesSearch && matchesChip) {
                filtered.add(t);
            }
        }

        // Apply Sorting
        filtered.sort((t1, t2) -> {
            switch (currentSortOrder) {
                case 1: // Oldest
                    return Long.compare(t1.getDate(), t2.getDate());
                case 2: // High -> Low
                    return Double.compare(t2.getAmount(), t1.getAmount());
                case 3: // Low -> High
                    return Double.compare(t1.getAmount(), t2.getAmount());
                default: // Newest
                    return Long.compare(t2.getDate(), t1.getDate());
            }
        });

        adapter.updateData(filtered);
        binding.layoutEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        adapter.setOnItemLongClickListener(this::showDeleteDialog);
        adapter.setOnItemClickListener(this::editTransaction);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        // Swipe to Delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Transaction transaction = adapter.getTransactionAt(position);
                showDeleteDialog(transaction);
                // Need to notify adapter to restore the item if user cancels, or just refresh
                adapter.notifyItemChanged(position);
            }
        }).attachToRecyclerView(binding.rvHistory);
    }

    private void editTransaction(Transaction transaction) {
        Intent intent = new Intent(requireContext(), AddTransactionActivity.class);
        intent.putExtra(AddTransactionActivity.EXTRA_ID, transaction.getId());
        startActivity(intent);
    }

    private void showDeleteDialog(Transaction transaction) {
        if (transaction == null || getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_msg))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.delete(transaction);
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
