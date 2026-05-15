package com.example.expensetracker1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.expensetracker1.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnProfile.setOnClickListener(v -> Toast.makeText(requireContext(), "Chức năng Hồ sơ đang phát triển", Toast.LENGTH_SHORT).show());
        
        binding.btnCurrency.setOnClickListener(v -> Toast.makeText(requireContext(), "Chức năng Tiền tệ đang phát triển", Toast.LENGTH_SHORT).show());

        binding.btnLimit.setOnClickListener(v -> Toast.makeText(requireContext(), "Chức năng Hạn mức đang phát triển", Toast.LENGTH_SHORT).show());

        binding.btnReset.setOnClickListener(v -> showResetConfirmation());
        
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String mode = isChecked ? "Đã bật Chế độ tối" : "Đã tắt Chế độ tối";
            Toast.makeText(requireContext(), mode, Toast.LENGTH_SHORT).show();
        });
    }

    private void showResetConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xoá")
                .setMessage("Bạn có chắc chắn muốn xoá toàn bộ dữ liệu giao dịch không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xoá tất cả", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Dữ liệu đã được xoá", Toast.LENGTH_SHORT).show();
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
