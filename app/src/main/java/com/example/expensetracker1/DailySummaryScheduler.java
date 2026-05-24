package com.example.expensetracker1;

import android.content.Context;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.expensetracker1.util.AppSettings;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DailySummaryScheduler {

    private static final String WORK_NAME = "DailySummaryWork";

    public static void scheduleDailySummary(Context context) {
        // Đã đổi thành đọc giờ động từ Settings thay vì số 0 cứng
        int targetHour = AppSettings.getSummaryHour(context);
        int targetMinute = AppSettings.getSummaryMinute(context);

        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();

        dueDate.set(Calendar.HOUR_OF_DAY, targetHour);
        dueDate.set(Calendar.MINUTE, targetMinute);
        dueDate.set(Calendar.SECOND, 0);
        dueDate.set(Calendar.MILLISECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        OneTimeWorkRequest summaryRequest = new OneTimeWorkRequest.Builder(DailySummaryWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                summaryRequest
        );
    }
}