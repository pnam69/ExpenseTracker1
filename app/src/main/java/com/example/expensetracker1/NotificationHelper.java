package com.example.expensetracker1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.example.expensetracker1.util.AppSettings;

public class NotificationHelper {

    /**
     * Check if the app has permission to post notifications on Android 13+
     * Returns false only if we're on Android 13+ AND the permission is not granted
     */
    private static boolean shouldNotifyAboutPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Permission not required before Android 13
            return false; // Don't need to check, can proceed
        }
        // Android 13+ - check if permission is NOT granted
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Centralized constants for notifications
    private static final String CHANNEL_ID = "expense_tracker_channel";
    private static final int NOTIF_ID_BUDGET = 2;
    private static final int NOTIF_ID_INCOME = 3;

    private static void ensureChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel existing = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (existing == null) {
                CharSequence name = "Thông báo ExpenseTracker";
                String description = "Kênh thông báo của ứng dụng ExpenseTracker";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void checkAndSendBudgetNotification(Context context, double hanMucConLai) {
        // Skip if notification permission is required but not granted (Android 13+)
        if (shouldNotifyAboutPermission(context)) {
            return;
        }

        String title;
        String message;
        String timeStr = getCurrentTime();

        if (hanMucConLai == 0) {
            title = "Cảnh báo hết tiền! \uD83D\uDEA8";
            message = "Bạn đã tiêu sạch hạn mức của ngày hôm nay. Hãy dừng mua sắm nhé!\n " + timeStr;
        } else if (hanMucConLai < 0) {
            title = "Báo động đỏ: Vượt hạn mức! \uD83D\uDED1";
            String over = AppSettings.formatAmount(context, Math.abs(hanMucConLai));
            message = "Bạn đã tiêu lố " + over + " so với dự kiến rồi!\n " + timeStr;
        } else {
            return;
        }

        ensureChannel(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // PendingIntent to open MainActivity when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        if (notificationManager != null) {
            notificationManager.notify(NOTIF_ID_BUDGET, builder.build());
        }
    }

    public static void sendIncomeNotification(Context context, double soTienThu, String tenKhoanThu) {
        // Skip if notification permission is required but not granted (Android 13+)
        if (shouldNotifyAboutPermission(context)) {
            return;
        }

        String title = "Tiền về! \uD83C\uDF89";
        String timeStr = getCurrentTime();
        String amountStr = AppSettings.formatAmount(context, soTienThu);
        String message = "Bạn vừa ghi nhận khoản thu " + amountStr + " từ việc " + tenKhoanThu + ".\n " + timeStr;

        ensureChannel(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(1, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis());

        if (notificationManager != null) {
            notificationManager.notify(NOTIF_ID_INCOME, builder.build());
        }
    }
}