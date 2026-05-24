package com.example.expensetracker1;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.expensetracker1.data.AppDatabase;
import com.example.expensetracker1.data.Transaction;
import com.example.expensetracker1.util.AppSettings;
import java.util.Calendar;
import java.util.List;

public class DailySummaryWorker extends Worker {

    public DailySummaryWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        double totalExpenses = 0.0;
        double totalIncome = 0.0;

        try {
            AppDatabase db = AppDatabase.getDatabase(context);
            List<Transaction> transactions = db.transactionDao().getTransactionsByTimeSync(startOfDay, endOfDay);

            if (transactions != null) {
                for (Transaction t : transactions) {
                    if ("EXPENSE".equals(t.getType())) {
                        totalExpenses += t.getAmount();
                    } else if ("INCOME".equals(t.getType())) {
                        totalIncome += t.getAmount();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }

        double dailyLimit = AppSettings.getDailyLimit(context);

        String title;
        StringBuilder message = new StringBuilder();

        if (totalExpenses <= dailyLimit) {
            title = "Tổng kết tài chính ngày hôm qua! 📊";
            double saved = dailyLimit - totalExpenses;
            message.append("Tổng thiệt hại: ").append(formatAmount(totalExpenses)).append("đ. ")
                    .append("Bạn đã tiết kiệm được ").append(formatAmount(saved)).append("đ so với hạn mức.");
        } else {
            title = "Tổng kết ngày: Vượt hạn mức! 🚨";
            double over = totalExpenses - dailyLimit;
            message.append("Tổng thiệt hại: ").append(formatAmount(totalExpenses)).append("đ. ")
                    .append("Bạn cần cố gắng hơn khi hôm nay đã quá so với hạn mức ").append(formatAmount(over)).append("đ.");
        }

        if (totalIncome > 0) {
            message.append(" Phần còn lại bạn đã kiếm được ").append(formatAmount(totalIncome)).append("đ.");
        }

        NotificationHelper.sendDailySummaryNotification(context, title, message.toString());

        DailySummaryScheduler.scheduleDailySummary(context);

        return Result.success();
    }

    private String formatAmount(double amount) {
        if (amount == (long) amount) {
            return String.valueOf((long) amount);
        }
        return String.valueOf(amount);
    }
}