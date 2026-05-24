package com.example.expensetracker1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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
        viewModel.getTransactionById(transactionId).observe(this, transaction -> {
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
        viewModel.getTransactionById(transactionId).observe(this, transaction -> {
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
        String[] categories = {
                "Ăn uống", "Đi lại", "Mua sắm", "Nhà cửa", "Hoá đơn",
                "Giải trí", "Sức khỏe", "Giáo dục", "Lương", "Thưởng", "Khác"
        };
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
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Đã cập nhật: " + title, Toast.LENGTH_SHORT).show();
            } else {
                Transaction transaction = new Transaction(0, title, amountVnd, category, System.currentTimeMillis(), "", type);
                viewModel.insert(transaction);
                Toast.makeText(this, "Đã lưu: " + title, Toast.LENGTH_SHORT).show();
                
                // Send notification for new transaction
                if ("INCOME".equals(type)) {
                    NotificationHelper.sendIncomeNotification(this, amountVnd, title);
                } else {
                    // type is "EXPENSE" - Check if budget exceeded and send budget notification
                    double dailyLimit = AppSettings.getDailyLimit(this);
                    double todayExpensesVnd = 0; // Will be fetched from ViewModel in real scenario
                    if (todayExpensesVnd >= dailyLimit) {
                        NotificationHelper.checkAndSendBudgetNotification(this, dailyLimit - (todayExpensesVnd + amountVnd));
                    }
                }
            }
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
