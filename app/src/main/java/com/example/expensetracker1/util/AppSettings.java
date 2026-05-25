package com.example.expensetracker1.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public final class AppSettings {

    private static final String PREFS_NAME = "expense_tracker_settings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_CURRENCY_LABEL = "currency_label";
    private static final String KEY_CURRENCY_SYMBOL = "currency_symbol";
    private static final String KEY_DAILY_LIMIT = "daily_limit";
    private static final String KEY_EMERGENCY_GOAL = "emergency_goal";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    public static final String DEFAULT_CURRENCY_LABEL = "Việt Nam Đồng (VND)";
    public static final String DEFAULT_CURRENCY_SYMBOL = "đ";
    public static final double DEFAULT_DAILY_LIMIT = 500000.0;
    public static final double DEFAULT_EMERGENCY_GOAL = 15000000.0;

    private AppSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isDarkModeEnabled(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public static boolean isNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public static String getCurrencyLabel(Context context) {
        return prefs(context).getString(KEY_CURRENCY_LABEL, DEFAULT_CURRENCY_LABEL);
    }

    public static String getCurrencySymbol(Context context) {
        return prefs(context).getString(KEY_CURRENCY_SYMBOL, DEFAULT_CURRENCY_SYMBOL);
    }

    public static void setCurrency(Context context, String label, String symbol) {
        prefs(context).edit()
                .putString(KEY_CURRENCY_LABEL, label)
                .putString(KEY_CURRENCY_SYMBOL, symbol)
                .apply();
    }

    public static double getDailyLimit(Context context) {
        try {
            String limitStr = prefs(context).getString(KEY_DAILY_LIMIT, String.valueOf(DEFAULT_DAILY_LIMIT));
            double limit = Double.parseDouble(limitStr);
            return limit > 0 ? limit : DEFAULT_DAILY_LIMIT;
        } catch (ClassCastException e) {
            long oldLimit = prefs(context).getLong(KEY_DAILY_LIMIT, (long) DEFAULT_DAILY_LIMIT);
            setDailyLimit(context, (double) oldLimit);
            return oldLimit > 0 ? (double) oldLimit : DEFAULT_DAILY_LIMIT;
        } catch (Exception e) {
            return DEFAULT_DAILY_LIMIT;
        }
    }

    public static void setDailyLimit(Context context, double dailyLimit) {
        double sanitizedLimit = Math.max(0.0, dailyLimit);
        prefs(context).edit().putString(KEY_DAILY_LIMIT, String.valueOf(sanitizedLimit)).apply();
    }

    public static double getEmergencyGoal(Context context) {
        try {
            String goalStr = prefs(context).getString(KEY_EMERGENCY_GOAL, String.valueOf(DEFAULT_EMERGENCY_GOAL));
            return Double.parseDouble(goalStr);
        } catch (ClassCastException e) {
            long oldGoal = prefs(context).getLong(KEY_EMERGENCY_GOAL, (long) DEFAULT_EMERGENCY_GOAL);
            setEmergencyGoal(context, (double) oldGoal);
            return (double) oldGoal;
        } catch (Exception e) {
            return DEFAULT_EMERGENCY_GOAL;
        }
    }

    public static void setEmergencyGoal(Context context, double goal) {
        prefs(context).edit().putString(KEY_EMERGENCY_GOAL, String.valueOf(goal)).apply();
    }

    public static String getUserName(Context context) {
        return prefs(context).getString(KEY_USER_NAME, "Người dùng");
    }

    public static void setUserName(Context context, String name) {
        prefs(context).edit().putString(KEY_USER_NAME, name).apply();
    }

    public static String getUserEmail(Context context) {
        return prefs(context).getString(KEY_USER_EMAIL, "user@example.com");
    }

    public static void setUserEmail(Context context, String email) {
        prefs(context).edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public static double getExchangeRate(Context context) {
        String symbol = getCurrencySymbol(context);
        if ("$".equals(symbol)) {
            return 1.0 / 25000.0; // 1 USD = 25,000 VND
        } else if ("€".equals(symbol)) {
            return 1.0 / 27000.0; // 1 EUR = 27,000 VND
        }
        return 1.0;
    }

    public static String formatAmount(Context context, double amountVnd) {
        double rate = getExchangeRate(context);
        double converted = amountVnd * rate;
        String symbol = getCurrencySymbol(context);

        if ("đ".equals(symbol) || "₫".equals(symbol)) {
            String formatted = String.format(Locale.getDefault(), "%,.0f", converted);
            return formatted + symbol;
        }

        String formatted = String.format(Locale.US, "%,.2f", converted);
        return symbol + formatted;
    }
    // === LƯU TRỮ GIỜ TỔNG KẾT ===
    private static final String KEY_SUMMARY_HOUR = "summary_hour";
    private static final String KEY_SUMMARY_MINUTE = "summary_minute";

    public static int getSummaryHour(Context context) {
        return prefs(context).getInt(KEY_SUMMARY_HOUR, 0); // Mặc định 0 giờ
    }

    public static int getSummaryMinute(Context context) {
        return prefs(context).getInt(KEY_SUMMARY_MINUTE, 0); // Mặc định 0 phút
    }

    public static void setSummaryTime(Context context, int hour, int minute) {
        prefs(context).edit()
                .putInt(KEY_SUMMARY_HOUR, hour)
                .putInt(KEY_SUMMARY_MINUTE, minute)
                .apply();
    }
}