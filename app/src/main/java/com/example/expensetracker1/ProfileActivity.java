package com.example.expensetracker1;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensetracker1.databinding.ActivityProfileBinding;
import com.example.expensetracker1.util.AppSettings;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        loadProfileData();

        binding.btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadProfileData() {
        String name = AppSettings.getUserName(this);
        String email = AppSettings.getUserEmail(this);

        binding.etName.setText(name);
        binding.etEmail.setText(email);
        binding.tvDisplayName.setText(name);
    }

    private void saveProfileData() {
        android.text.Editable nameEditable = binding.etName.getText();
        android.text.Editable emailEditable = binding.etEmail.getText();
        
        String name = (nameEditable != null) ? nameEditable.toString().trim() : "";
        String email = (emailEditable != null) ? emailEditable.toString().trim() : "";

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        AppSettings.setUserName(this, name);
        AppSettings.setUserEmail(this, email);

        binding.tvDisplayName.setText(name);
        Toast.makeText(this, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
        finish();
    }
}