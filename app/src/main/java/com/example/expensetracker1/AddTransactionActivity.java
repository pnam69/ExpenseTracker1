package com.example.expensetracker1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.widget.ArrayAdapter;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.ActivityAddTransactionBinding;
import com.example.expensetracker1.util.AppSettings;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_TYPE = "extra_type";

    private ActivityAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private int transactionId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        
        transactionId = getIntent().getIntExtra(EXTRA_ID, 0);

        setupToolbar();
        setupCategoryDropdown();
        
        if (transactionId != 0) {
            loadTransactionData();
            binding.btnSave.setText(R.string.btn_update);
            binding.btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            applyPresetFromIntent();
        }

        binding.btnSave.setOnClickListener(v -> saveTransaction());
        binding.btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    private void loadTransactionData() {
        observeOnce(viewModel.getTransactionById(transactionId), transaction -> {
            if (transaction != null) {
                binding.etTitle.setText(transaction.getTitle());
                
                double rate = AppSettings.getExchangeRate(this);
                double converted = transaction.getAmount() * rate;
                binding.etAmount.setText(formatAmount(converted));
                
                binding.actvCategory.setText(transaction.getCategory(), false);
                
                if ("INCOME".equalsIgnoreCase(transaction.getType())) {
                    binding.toggleGroup.check(R.id.btn_income);
                } else {
                    binding.toggleGroup.check(R.id.btn_expense);
                }
                
                binding.toolbar.setTitle(R.string.edit_transaction_title);
            }
        });
    }

    private void deleteTransaction() {
        observeOnce(viewModel.getTransactionById(transactionId), transaction -> {
            if (transaction != null) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_confirm)
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            viewModel.delete(transaction);
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.transaction_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.actvCategory.setAdapter(adapter);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void applyPresetFromIntent() {
        Intent intent = getIntent();

        String title = intent.getStringExtra(EXTRA_TITLE);
        if (title != null && !title.isEmpty()) {
            binding.etTitle.setText(title);
        }

        if (intent.hasExtra(EXTRA_AMOUNT)) {
            double amountVnd = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0);
            double rate = AppSettings.getExchangeRate(this);
            double converted = amountVnd * rate;
            binding.etAmount.setText(formatAmount(converted));
        }

        String category = intent.getStringExtra(EXTRA_CATEGORY);
        if (category != null && !category.isEmpty()) {
            binding.actvCategory.setText(category, false);
        }

        String type = intent.getStringExtra(EXTRA_TYPE);
        if ("INCOME".equalsIgnoreCase(type)) {
            binding.toggleGroup.check(R.id.btn_income);
        } else if ("EXPENSE".equalsIgnoreCase(type)) {
            binding.toggleGroup.check(R.id.btn_expense);
        }
    }

    private String formatAmount(double amount) {
        if (amount == Math.rint(amount)) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }

    private void saveTransaction() {
        CharSequence amountInput = binding.etAmount.getText();
        CharSequence titleInput = binding.etTitle.getText();

        String amountStr = (amountInput != null) ? amountInput.toString().trim() : "";
        String title = (titleInput != null) ? titleInput.toString().trim() : "";
        String type = binding.toggleGroup.getCheckedButtonId() == R.id.btn_expense ? "EXPENSE" : "INCOME";

        if (amountStr.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, R.string.msg_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double inputAmount = Double.parseDouble(amountStr);
            double rate = AppSettings.getExchangeRate(this);
            double amountVnd = inputAmount / rate; 
            
            CharSequence categoryInput = binding.actvCategory.getText();
            String category = (categoryInput != null) ? categoryInput.toString().trim() : "";
            
            if (transactionId != 0) {
                Transaction transaction = new Transaction(transactionId, title, amountVnd, category, System.currentTimeMillis(), "", type);
                viewModel.update(transaction);
                Toast.makeText(this, getString(R.string.msg_transaction_updated, title), Toast.LENGTH_SHORT).show();
            } else {
                Transaction transaction = new Transaction(0, title, amountVnd, category, System.currentTimeMillis(), "", type);
                viewModel.insert(transaction);
                Toast.makeText(this, getString(R.string.msg_saved_success, title), Toast.LENGTH_SHORT).show();

                // Send notification for new transaction
                if ("INCOME".equals(type)) {
                    NotificationHelper.sendIncomeNotification(this, amountVnd, title);
                } else {
                    notifyIfOverDailyLimit(amountVnd);
                }
            }
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.msg_invalid_amount, Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyIfOverDailyLimit(double newExpenseVnd) {
        final double dailyLimit = AppSettings.getDailyLimit(this);
        long startOfDay = getStartOfDayMillis();
        long endOfDay = startOfDay + 24L * 60L * 60L * 1000L;

        viewModel.getTodayExpensesValue(startOfDay, endOfDay, todayExpensesVnd -> {
            double remaining = dailyLimit - (todayExpensesVnd + newExpenseVnd);
            if (remaining <= 0) {
                NotificationHelper.checkAndSendBudgetNotification(this, remaining);
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

    private <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observe(this, new Observer<T>() {
            @Override
            public void onChanged(T value) {
                observer.onChanged(value);
                liveData.removeObserver(this);
            }
        });
    }
}
