package com.example.iot_lab5_20212607.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.example.iot_lab5_20212607.HomeActivity;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import com.example.iot_lab5_20212607.R;

import java.util.Locale;

public class NotificationHelper {
    private static final String CHANNEL_MEALS_ID = "channel_meals";
    private static final String CHANNEL_MOTIVATION_ID = "channel_motivation";
    private static final String CHANNEL_CALORIES_ID = "channel_calories";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = context.getSystemService(NotificationManager.class);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para recordatorios de comidas
            NotificationChannel mealChannel = new NotificationChannel(
                    CHANNEL_MEALS_ID,
                    "Recordatorios de Comidas",
                    NotificationManager.IMPORTANCE_HIGH);
            mealChannel.setDescription("Recordatorios para registrar tus comidas");

            // Canal para motivación
            NotificationChannel motivationChannel = new NotificationChannel(
                    CHANNEL_MOTIVATION_ID,
                    "Mensajes Motivacionales",
                    NotificationManager.IMPORTANCE_DEFAULT);
            motivationChannel.setDescription("Mensajes motivacionales periódicos");

            // Canal para alertas de calorías
            NotificationChannel caloriesChannel = new NotificationChannel(
                    CHANNEL_CALORIES_ID,
                    "Alertas de Calorías",
                    NotificationManager.IMPORTANCE_HIGH);
            caloriesChannel.setDescription("Alertas y sugerencias sobre consumo de calorías");
            caloriesChannel.enableVibration(true);
            caloriesChannel.setVibrationPattern(new long[]{0, 500, 200, 500}); // Vibración

            notificationManager.createNotificationChannel(mealChannel);
            notificationManager.createNotificationChannel(motivationChannel);
            notificationManager.createNotificationChannel(caloriesChannel);
        }
    }

    public void showMealReminder(String mealType) {
        // Intent para abrir la actividad al tocar la notificación
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(context, CHANNEL_MEALS_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Hora de " + getMealTitle(mealType))
                .setContentText("¡No olvides registrar tu comida!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (checkNotificationPermission()) {
            notificationManager.notify(getMealNotificationId(mealType), notification);
        }
    }

    public void showMotivationalNotification(String message) {
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(context, CHANNEL_MOTIVATION_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("¡Motivación!")
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (checkNotificationPermission()) {
            notificationManager.notify(2, notification);
        }
    }

    public void showCaloriesAlert(int excess) {
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        // Calcular minutos de ejercicio sugeridos (aproximadamente 7.5 calorías por minuto caminando)
        int suggestedWalkingMinutes = (int) Math.ceil(excess / 7.5);

        // Calcular reducción sugerida para próximas comidas
        int suggestedReduction = (int) Math.ceil(excess / 3.0); // Dividir entre 3 comidas

        // Crear un estilo para múltiples líneas de texto
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle()
                .setBigContentTitle("¡Has excedido tu meta de calorías!")
                .bigText(String.format(Locale.getDefault(),
                        "Has consumido %d calorías extra.\n\n" +
                                "Sugerencias:\n" +
                                "• Camina %d minutos para quemar el exceso\n" +
                                "• Reduce %d calorías en tu próxima comida\n" +
                                "• Haz 30 minutos de ejercicio cardiovascular\n" +
                                "• Bebe más agua durante el día",
                        excess, suggestedWalkingMinutes, suggestedReduction));

        // Crear la notificación con el estilo expandido
        Notification notification = new Notification.Builder(context, CHANNEL_CALORIES_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("¡Has excedido tu meta de calorías!")
                .setContentText("Has consumido " + excess + " calorías extra")
                .setStyle(bigTextStyle)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (checkNotificationPermission()) {
            notificationManager.notify(3, notification);
        }

        // Notificación adicional para un ejercicio específico después de un breve delay
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            Notification exerciseNotification = new Notification.Builder(context, CHANNEL_CALORIES_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Sugerencia de Ejercicio")
                    .setContentText(String.format(Locale.getDefault(),
                            "Una caminata de %d minutos te ayudará a quemar el exceso de calorías",
                            suggestedWalkingMinutes))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            if (checkNotificationPermission()) {
                notificationManager.notify(4, exerciseNotification);
            }
        }, 3000); // 3 segundos de delay
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private String getMealTitle(String mealType) {
        switch (mealType) {
            case "breakfast": return "Desayuno";
            case "lunch": return "Almuerzo";
            case "dinner": return "Cena";
            default: return "";
        }
    }

    private int getMealNotificationId(String mealType) {
        switch (mealType) {
            case "breakfast": return 101;
            case "lunch": return 102;
            case "dinner": return 103;
            default: return 100;
        }
    }

    public void showNoMealsRegisteredAlert() {
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(context, CHANNEL_MEALS_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("¡Registra tus comidas!")
                .setContentText("No has registrado ninguna comida hoy")
                .setStyle(new Notification.BigTextStyle()
                        .bigText("Es importante registrar tus comidas para mantener un seguimiento adecuado de tu nutrición. " +
                                "Toca aquí para empezar a registrar tus comidas."))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (checkNotificationPermission()) {
            notificationManager.notify(105, notification);
        }
    }

}
