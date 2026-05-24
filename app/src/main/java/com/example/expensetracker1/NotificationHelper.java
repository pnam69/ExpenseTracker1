package com.example.expensetracker1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    // ĐỔI TÊN CHANNEL ID MỚI: Ép Android phải tạo lại kênh với độ ưu tiên cao nhất
    private static final String CHANNEL_ID = "expense_tracker_high_alert_channel";

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                CharSequence name = "Cảnh báo Tài chính";
                String description = "Kênh hiển thị thông báo thu chi và cảnh báo vượt hạn mức tức thời";

                // ĐÃ SỬA: Đẩy lên IMPORTANCE_HIGH để thông báo nổi lên màn hình ngay lập tức
                int importance = NotificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                channel.setVibrationPattern(new long[]{0, 250, 250, 250}); // Thêm hiệu ứng rung cho máy
                channel.enableVibration(true);

                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // 1. Phát thông báo khi người dùng thêm KHOẢN CHI
    public static void sendExpenseNotification(Context context, double soTienChi, String tenKhoanChi) {
        createNotificationChannel(context);

        String soTienChu = (soTienChi == (long) soTienChi) ? String.valueOf((long) soTienChi) : String.format("%,.0f", soTienChi);
        String title = "Đã ghi nhận khoản chi! 💸";
        String message = "Bạn vừa chi " + soTienChu + "đ cho \"" + tenKhoanChi + "\".";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Đẩy mức ưu tiên hiển thị lên cao
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(4, builder.build());
        }
    }

    // 2. ĐÃ SỬA: Phát thông báo CẢNH BÁO nếu hết tiền (bằng 0) hoặc âm tiền (vượt mốc)
    public static void checkAndSendBudgetNotification(Context context, double hanMucConLai) {
        createNotificationChannel(context);

        String title;
        String message;

        // Giải quyết sai số kiểu double: Nếu số tiền còn lại dao động trong khoảng từ -1đ đến 1đ thì coi như bằng 0
        if (Math.abs(hanMucConLai) < 1.0) {
            title = "Đã đạt giới hạn! 🚨";
            message = "Bạn đã chi tiêu chạm đúng mốc giới hạn của ngày hôm nay.";
        }
        // Nếu số tiền nhỏ hơn hẳn -1đ tức là đã bắt đầu âm tiền
        else if (hanMucConLai <= -1.0) {
            title = "Cảnh báo: Vượt hạn mức! 🛑";
            double soTienVuot = Math.abs(hanMucConLai);
            String soTienChu = (soTienVuot == (long) soTienVuot) ? String.valueOf((long) soTienVuot) : String.format("%,.0f", soTienVuot);
            message = "Bạn đã vượt mức " + soTienChu + "đ so với mốc giới hạn!";
        }
        // Vẫn còn tiền dương nhiều hơn 1đ thì thoát, không báo gì cả
        else {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Đẩy mức ưu tiên hiển thị lên cao
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(2, builder.build());
        }
    }

    // 3. Phát thông báo khi người dùng thêm KHOẢN THU
    public static void sendIncomeNotification(Context context, double soTienThu, String tenKhoanThu) {
        createNotificationChannel(context);

        String title = "Tiền về! 🎉";
        String soTienChu = (soTienThu == (long) soTienThu) ? String.valueOf((long) soTienThu) : String.format("%,.0f", soTienThu);
        String message = "Bạn vừa ghi nhận khoản thu " + soTienChu + "đ từ việc \"" + tenKhoanThu + "\".";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(3, builder.build());
        }
    }

    // 4. Thông báo tổng kết tài chính cuối ngày (WorkManager)
    public static void sendDailySummaryNotification(Context context, String title, String message) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(5, builder.build());
        }
    }
}