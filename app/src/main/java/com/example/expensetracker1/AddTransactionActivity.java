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

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_AMOUNT = "extra_amount";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_TYPE = "extra_type";

    private ActivityAddTransactionBinding binding;
    private TransactionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        setupToolbar();
        setupCategoryDropdown();
        applyPresetFromIntent();

        binding.btnSave.setOnClickListener(v -> saveTransaction());
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
        String amountStr = binding.etAmount.getText().toString().trim();
        String title = binding.etTitle.getText().toString().trim();
        String type = binding.toggleGroup.getCheckedButtonId() == R.id.btn_expense ? "EXPENSE" : "INCOME";

        if (amountStr.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double inputAmount = Double.parseDouble(amountStr);
            double rate = AppSettings.getExchangeRate(this);
            double amountVnd = inputAmount / rate; // Convert back to VND for base storage
            
            String category = binding.actvCategory.getText().toString().trim();
            
            Transaction transaction = new Transaction(0, title, amountVnd, category, System.currentTimeMillis(), "", type);
            viewModel.insert(transaction);
            
            Toast.makeText(this, "Đã lưu: " + title, Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
