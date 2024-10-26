package com.example.iot_lab5_20212607.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        DatabaseHelper databaseHelper = new DatabaseHelper(context);

        if ("MOTIVATIONAL_NOTIFICATION".equals(intent.getAction())) {
            // Manejar notificación motivacional usando frases de la base de datos
            String message = getRandomMotivationalMessage(databaseHelper);
            notificationHelper.showMotivationalNotification(message);
        } else if ("CHECK_MEALS".equals(intent.getAction())) {
            // Verificar si hay comidas registradas hoy
            if (!databaseHelper.hasRegisteredMealsToday()) {
                Calendar now = Calendar.getInstance();
                if (now.get(Calendar.HOUR_OF_DAY) >= 12) {
                    notificationHelper.showNoMealsRegisteredAlert();
                }
            }
        } else {
            // Manejar recordatorio de comida
            String mealType = intent.getStringExtra("meal_type");
            if (mealType != null) {
                notificationHelper.showMealReminder(mealType);
                scheduleTomorrowMealReminder(context, mealType);
            }
        }
    }


    private String getRandomMotivationalMessage(DatabaseHelper databaseHelper) {
        List<String> phrases = databaseHelper.getAllMotivationalPhrases();
        // Frase por default
        if (phrases.isEmpty()) {
            return "¡Mantén el buen trabajo!";
        }
        Random random = new Random();
        return phrases.get(random.nextInt(phrases.size()));
    }

    private void scheduleTomorrowMealReminder(Context context, String mealType) {
        Intent newIntent = new Intent(context, NotificationReceiver.class);
        newIntent.putExtra("meal_type", mealType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                getMealRequestCode(mealType),
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Obtener la hora programada de las preferencias
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        int hour = preferences.getInt(mealType + "_hour", 8);
        int minute = preferences.getInt(mealType + "_minute", 0);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    private int getMealRequestCode(String mealType) {
        switch (mealType) {
            case "breakfast": return 1;
            case "lunch": return 2;
            case "dinner": return 3;
            default: return 0;
        }
    }
}