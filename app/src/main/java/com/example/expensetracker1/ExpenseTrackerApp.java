package com.example.expensetracker1;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.expensetracker1.util.AppSettings;

public class ExpenseTrackerApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		AppCompatDelegate.setDefaultNightMode(
				AppSettings.isDarkModeEnabled(this)
						? AppCompatDelegate.MODE_NIGHT_YES
						: AppCompatDelegate.MODE_NIGHT_NO
		);
	}
}

