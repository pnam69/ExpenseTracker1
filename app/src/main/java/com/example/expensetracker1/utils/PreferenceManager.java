package com.example.expensetracker1.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "finguard_prefs";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setCurrency(String currency) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply();
    }

    public String getCurrency() {
        return sharedPreferences.getString(KEY_CURRENCY, "VND");
    }

    public void setDarkMode(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }
    
    public double getCurrencyRate() {
        String currency = getCurrency();
        if ("USD".equals(currency)) {
            return 1.0 / 25000.0; // Mock rate: 1 USD = 25,000 VND
        }
        return 1.0;
    }
    
    public String getCurrencySymbol() {
        String currency = getCurrency();
        return "USD".equals(currency) ? "$" : "đ";
    }
}
