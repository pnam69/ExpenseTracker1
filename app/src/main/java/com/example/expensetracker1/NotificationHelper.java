package com.example.expensetracker1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    public static void checkAndSendBudgetNotification(Context context, double hanMucConLai) {
        String title;
        String message;

        if (hanMucConLai == 0) {
            title = "Cảnh báo hết tiền! \uD83D\uDEA8";
            message = "Bạn đã tiêu sạch hạn mức của ngày hôm nay. Hãy dừng mua sắm nhé!";
        } else if (hanMucConLai < 0) {
            title = "Báo động đỏ: Vượt hạn mức! \uD83D\uDED1";
            message = "Bạn đã tiêu lố " + Math.abs(hanMucConLai) + "đ so với dự kiến rồi!";
        } else {
            return;
        }

        String CHANNEL_ID = "expense_tracker_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Thông báo hạn mức";
            String description = "Kênh cảnh báo khi chi tiêu quá tay";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(2, builder.build());
        }
    }

    public static void sendIncomeNotification(Context context, double soTienThu, String tenKhoanThu) {
        String title = "Tiền về! \uD83C\uDF89";
        String soTienChu = (soTienThu == (long) soTienThu) ? String.valueOf((long) soTienThu) : String.valueOf(soTienThu);
        String message = "Bạn vừa ghi nhận khoản thu " + soTienChu + "đ từ việc " + tenKhoanThu + ".";

        String CHANNEL_ID = "expense_tracker_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Thông báo thu chi";
            String description = "Kênh thông báo các biến động số dư";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(3, builder.build());
        }
    }
}