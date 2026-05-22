package com.example.expensetracker1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.widget.ArrayAdapter;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.ActivityAddTransactionBinding;
import com.example.expensetracker1.util.AppSettings;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_ID = "extra_id";

    private ActivityAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private long selectedDateMillis = System.currentTimeMillis();
    private int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        setupToolbar();
        setupCategoryDropdown();
        setupDatePicker();
        
        transactionId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (transactionId != -1) {
            binding.btnDelete.setVisibility(View.VISIBLE);
            loadTransactionData(transactionId);
        } else {
            applyPresetFromIntent();
        }

        binding.btnSave.setOnClickListener(v -> saveTransaction());
        binding.btnDelete.setOnClickListener(v -> deleteCurrentTransaction());
    }

    private void deleteCurrentTransaction() {
        if (transactionId == -1) return;
        
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Xoá giao dịch")
                .setMessage("Bạn có chắc chắn muốn xoá giao dịch này?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    viewModel.getTransactionById(transactionId).observe(this, t -> {
                        if (t != null) {
                            viewModel.delete(t);
                            Toast.makeText(this, "Đã xoá giao dịch", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void loadTransactionData(int id) {
        viewModel.getTransactionById(id).observe(this, transaction -> {
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
                
                selectedDateMillis = transaction.getDate();
                updateDateLabel();
            }
        });
    }

    private void setupDatePicker() {
        updateDateLabel();
        binding.etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDateMillis);
            
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                selectedDateMillis = calendar.getTimeInMillis();
                updateDateLabel();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void updateDateLabel() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        binding.etDate.setText(sdf.format(new java.util.Date(selectedDateMillis)));
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
        android.text.Editable amountEditable = binding.etAmount.getText();
        android.text.Editable titleEditable = binding.etTitle.getText();
        
        String amountStr = (amountEditable != null) ? amountEditable.toString().trim() : "";
        String title = (titleEditable != null) ? titleEditable.toString().trim() : "";
        String type = binding.toggleGroup.getCheckedButtonId() == R.id.btn_expense ? "EXPENSE" : "INCOME";

        if (amountStr.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double inputAmount = Double.parseDouble(amountStr);
            double rate = AppSettings.getExchangeRate(this);
            double amountVnd = inputAmount / rate; // Convert back to VND for base storage
            
            String category = binding.actvCategory.getText().toString().trim();
            
            Transaction transaction = new Transaction(transactionId == -1 ? 0 : transactionId, 
                    title, amountVnd, category, selectedDateMillis, "", type);
            
            if (transactionId == -1) {
                viewModel.insert(transaction);
            } else {
                viewModel.update(transaction);
            }
            
            Toast.makeText(this, getString(R.string.msg_saved_success, title), Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.msg_invalid_amount), Toast.LENGTH_SHORT).show();
        }
    }
}
