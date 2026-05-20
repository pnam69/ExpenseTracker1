package com.example.expensetracker1.utils;

import java.util.Locale;

public class CurrencyUtils {
    public static String formatAmount(double amountVnd, PreferenceManager prefManager) {
        double rate = prefManager.getCurrencyRate();
        String symbol = prefManager.getCurrencySymbol();
        double converted = amountVnd * rate;
        
        if ("USD".equals(prefManager.getCurrency())) {
            return String.format(Locale.US, "%s%,.2f", symbol, converted);
        } else {
            return String.format(Locale.getDefault(), "%,.0f%s", converted, symbol);
        }
    }
}
