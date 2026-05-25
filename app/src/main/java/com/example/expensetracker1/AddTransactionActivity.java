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
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_TYPE = "extra_type";

    private ActivityAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private int transactionId = 0;
    private long transactionDate = 0;

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
        setupDatePicker();
        
        if (transactionId != 0) {
            loadTransactionData();
            binding.btnSave.setText(R.string.btn_update);
            binding.btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            transactionDate = System.currentTimeMillis();
            updateDateDisplay();
            applyPresetFromIntent();
        }

        binding.btnSave.setOnClickListener(v -> saveTransaction());
        binding.btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    private void setupDatePicker() {
        binding.etDate.setFocusable(false);
        binding.etDate.setLongClickable(false);
        binding.etDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.label_date)
                    .setSelection(transactionDate != 0 ? transactionDate : MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    transactionDate = selection;
                    updateDateDisplay();
                }
            });

            if (!getSupportFragmentManager().isStateSaved()) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        binding.etDate.setText(sdf.format(new Date(transactionDate)));
    }

    private void loadTransactionData() {
        viewModel.getTransactionById(transactionId).observe(this, transaction -> {
            if (transaction != null) {
                transactionDate = transaction.getDate();
                updateDateDisplay();
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
        String amountStr = String.valueOf(binding.etAmount.getText()).trim();
        String title = String.valueOf(binding.etTitle.getText()).trim();
        String type = binding.toggleGroup.getCheckedButtonId() == R.id.btn_expense ? "EXPENSE" : "INCOME";

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double inputAmount = Double.parseDouble(amountStr);
            double rate = AppSettings.getExchangeRate(this);
            double amountVnd = inputAmount / rate;

            String category = String.valueOf(binding.actvCategory.getText()).trim();
            if (title.isEmpty()) {
                title = category.isEmpty() ? "Khoản chi" : category;
            }

            long dateToUse = (transactionDate != 0) ? transactionDate : System.currentTimeMillis();
            
            if (transactionId != 0) {
                Transaction transaction = new Transaction(transactionId, title, amountVnd, category, dateToUse, "", type);
                viewModel.update(transaction);
                Toast.makeText(this, "Đã cập nhật: " + title, Toast.LENGTH_SHORT).show();
            } else {
                Transaction transaction = new Transaction(0, title, amountVnd, category, dateToUse, "", type);
                viewModel.insert(transaction);
                Toast.makeText(this, "Đã lưu: " + title, Toast.LENGTH_SHORT).show();
                
                // Keep teammate's notification logic but simplified
                if ("EXPENSE".equals(type)) {
                    double hanMucNgay = AppSettings.getDailyLimit(this);
                    viewModel.getTodayExpensesValue(getStartOfDayMillis(), getEndOfDayMillis(), today -> {
                        double remaining = hanMucNgay - (today + amountVnd);
                        NotificationHelper.checkAndSendBudgetNotification(this, remaining);
                    });
                } else {
                    NotificationHelper.sendIncomeNotification(this, amountVnd, title);
                }
            }
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    private long getStartOfDayMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDayMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23); cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59); cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }
}
