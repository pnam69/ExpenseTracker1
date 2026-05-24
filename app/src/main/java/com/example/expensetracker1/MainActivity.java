package com.example.expensetracker1;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.expensetracker1.databinding.ActivityMainBinding;
import com.example.expensetracker1.util.AppSettings;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int currentNavItemId = R.id.navigation_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode before super.onCreate
        if (AppSettings.isDarkModeEnabled(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();

        if (savedInstanceState == null) {
            switchToFragment(R.id.navigation_dashboard);
            binding.bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
        } else {
            int restoredId = binding.bottomNavigation.getSelectedItemId();
            currentNavItemId = restoredId != 0 ? restoredId : R.id.navigation_dashboard;
            updateFabVisibility(currentNavItemId);
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentNavItemId) {
                return true;
            }

            currentNavItemId = id;
            updateFabVisibility(id);
            switchToFragment(id);
            return true;
        });

        binding.fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    private void updateFabVisibility(int menuItemId) {
        boolean showFab = menuItemId == R.id.navigation_dashboard || menuItemId == R.id.navigation_history;
        if (showFab) {
            binding.fabAddTransaction.show();
        } else {
            binding.fabAddTransaction.hide();
        }
    }

    private void switchToFragment(int menuItemId) {
        String tag = getTagForMenu(menuItemId);
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        if (target == null) {
            target = createFragmentForMenu(menuItemId);
        }

        Fragment current = getSupportFragmentManager().getPrimaryNavigationFragment();
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();

        if (current != null && current != target) {
            tx.hide(current);
        }

        if (target.isAdded()) {
            tx.show(target);
        } else {
            tx.add(R.id.nav_host_fragment, target, tag);
        }

        tx.setPrimaryNavigationFragment(target).commit();
    }

    private Fragment createFragmentForMenu(int menuItemId) {
        if (menuItemId == R.id.navigation_history) return new HistoryFragment();
        if (menuItemId == R.id.navigation_statistics) return new StatisticsFragment();
        if (menuItemId == R.id.navigation_settings) return new SettingsFragment();
        return new DashboardFragment();
    }

    private String getTagForMenu(int menuItemId) {
        if (menuItemId == R.id.navigation_history) return "history";
        if (menuItemId == R.id.navigation_statistics) return "statistics";
        if (menuItemId == R.id.navigation_settings) return "settings";
        return "dashboard";
    }

    public void navigateToHistory() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_history);
    }
}
