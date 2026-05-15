package com.example.expensetracker1;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.widget.ArrayAdapter;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.databinding.ActivityAddTransactionBinding;
import com.example.expensetracker1.viewmodel.TransactionViewModel;

public class AddTransactionActivity extends AppCompatActivity {

    private ActivityAddTransactionBinding binding;
    private TransactionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupToolbar();
        setupCategoryDropdown();

        binding.btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Ăn uống", "Đi lại", "Sinh hoạt", "Học tập", "Lương", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.actvCategory.setAdapter(adapter);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void saveTransaction() {
        String amountStr = binding.etAmount.getText().toString();
        String title = binding.etTitle.getText().toString();
        String type = binding.toggleGroup.getCheckedButtonId() == R.id.btn_expense ? "EXPENSE" : "INCOME";

        if (amountStr.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String category = binding.actvCategory.getText().toString();
            
            Transaction transaction = new Transaction(0, title, amount, category, System.currentTimeMillis(), "", type);
            viewModel.insert(transaction);
            
            Toast.makeText(this, "Đã lưu: " + title, Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
