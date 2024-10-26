package com.example.iot_lab5_20212607;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.adapter.MotivationalPhrasesAdapter;
import com.example.iot_lab5_20212607.databinding.ActivitySettingsBinding;
import com.example.iot_lab5_20212607.model.MotivationalPhrase;
import com.example.iot_lab5_20212607.notification.NotificationHelper;
import com.example.iot_lab5_20212607.notification.NotificationReceiver;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private NotificationHelper notificationHelper;
    private SharedPreferences preferences;
    private AlarmManager alarmManager;
    private MotivationalPhrasesAdapter phrasesAdapter;
    private List<MotivationalPhrase> phrasesList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        notificationHelper = new NotificationHelper(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        databaseHelper = new DatabaseHelper(this);

        askNotificationPermission();
        setupToolbar();
        setupTimeButtons();
        setupMotivationalInterval();
        loadSavedSettings();
        setupPhrasesRecyclerView();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void updateMotivationalInterval(int minutes) {
        // Guardar en preferencias
        preferences.edit()
                .putInt("motivational_interval", minutes)
                .apply();

        // Cancelar alarma anterior si existe
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction("MOTIVATIONAL_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                4, // Usar un requestCode diferente a los de las comidas
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);

        // Programar nueva alarma periódica
        Calendar calendar = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    minutes * 60 * 1000L, // Convertir minutos a milisegundos
                    pendingIntent
            );
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    minutes * 60 * 1000L,
                    pendingIntent
            );
        }

        Toast.makeText(this,
                "Notificaciones motivacionales configuradas cada " + minutes + " minutos",
                Toast.LENGTH_SHORT).show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                        new String[]{POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    private void setupTimeButtons() {
        binding.breakfastTimeButton.setOnClickListener(v -> showTimePickerDialog("breakfast"));
        binding.lunchTimeButton.setOnClickListener(v -> showTimePickerDialog("lunch"));
        binding.dinnerTimeButton.setOnClickListener(v -> showTimePickerDialog("dinner"));
    }

    private void showTimePickerDialog(String mealType) {
        int defaultHour = preferences.getInt(mealType + "_hour", 8);
        int defaultMinute = preferences.getInt(mealType + "_minute", 0);

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)  // Cambiar a formato 12h
                .setHour(defaultHour)
                .setMinute(defaultMinute)
                .setTitleText("Seleccionar hora para " + getMealTitle(mealType))  // Agregar título
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            updateMealTime(mealType, picker.getHour(), picker.getMinute());
        });

        picker.show(getSupportFragmentManager(), "timePicker");
    }


    private void updateMealTime(String mealType, int hour, int minute) {
        // Actualizar UI con formato 12h
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeText = sdf.format(calendar.getTime());

        switch (mealType) {
            case "breakfast":
                binding.breakfastTimeButton.setText(timeText);
                break;
            case "lunch":
                binding.lunchTimeButton.setText(timeText);
                break;
            case "dinner":
                binding.dinnerTimeButton.setText(timeText);
                break;
        }

        // Guardar preferencias
        preferences.edit()
                .putInt(mealType + "_hour", hour)
                .putInt(mealType + "_minute", minute)
                .apply();

        // Programar alarma
        scheduleMealAlarm(mealType, hour, minute);

        // Mostrar confirmación
        Toast.makeText(this,
                "Recordatorio configurado para las " + timeText,
                Toast.LENGTH_SHORT).show();
    }

    private void scheduleMealAlarm(String mealType, int hour, int minute) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("meal_type", mealType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                getMealRequestCode(mealType),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

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

    private void setupMotivationalInterval() {
        // Agregar botón para guardar el intervalo
        binding.saveMotivationalInterval.setOnClickListener(v -> {
            String intervalStr = binding.motivationalIntervalInput.getText().toString();
            if (!TextUtils.isEmpty(intervalStr)) {
                try {
                    int interval = Integer.parseInt(intervalStr);
                    if (interval > 0) {
                        updateMotivationalInterval(interval);
                    } else {
                        binding.motivationalIntervalInput.setError("El intervalo debe ser mayor a 0");
                    }
                } catch (NumberFormatException e) {
                    binding.motivationalIntervalInput.setError("Ingrese un número válido");
                }
            }
        });
    }

    private void loadSavedSettings() {
        // Cargar horarios guardados
        loadMealTime("breakfast", binding.breakfastTimeButton);
        loadMealTime("lunch", binding.lunchTimeButton);
        loadMealTime("dinner", binding.dinnerTimeButton);

        // Cargar intervalo motivacional
        int interval = preferences.getInt("motivational_interval", 60);
        binding.motivationalIntervalInput.setText(String.valueOf(interval));
    }

    private void loadMealTime(String mealType, MaterialButton button) {
        int hour = preferences.getInt(mealType + "_hour", 8);
        int minute = preferences.getInt(mealType + "_minute", 0);

        // Cargar con formato 12h
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timeText = sdf.format(calendar.getTime());

        button.setText(timeText);
    }

    private String getMealTitle(String mealType) {
        switch (mealType) {
            case "breakfast": return "Desayuno";
            case "lunch": return "Almuerzo";
            case "dinner": return "Cena";
            default: return "";
        }
    }

    //Frase motivacional
    private void setupPhrasesRecyclerView() {
        phrasesList = new ArrayList<>();
        phrasesAdapter = new MotivationalPhrasesAdapter(phrasesList, new MotivationalPhrasesAdapter.OnPhraseActionListener() {
            @Override
            public void onPhraseEdit(MotivationalPhrase phrase) {
                showPhraseDialog(phrase);
            }

            @Override
            public void onPhraseDelete(MotivationalPhrase phrase) {
                showDeleteConfirmation(phrase);
            }
        });

        binding.phrasesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.phrasesRecyclerView.setAdapter(phrasesAdapter);

        binding.addPhraseButton.setOnClickListener(v -> showPhraseDialog(null));

        loadPhrases();
    }

    private void showPhraseDialog(MotivationalPhrase phraseToEdit) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_phrase, null);
        TextView titleView = dialogView.findViewById(R.id.dialogTitle);
        TextInputEditText phraseInput = dialogView.findViewById(R.id.phraseInput);

        boolean isEdit = phraseToEdit != null;
        titleView.setText(isEdit ? "Editar frase" : "Nueva frase");
        if (isEdit) {
            phraseInput.setText(phraseToEdit.getPhrase());
        }

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Actualizar" : "Agregar", (dialog, which) -> {
                    String phrase = phraseInput.getText().toString().trim();
                    if (!phrase.isEmpty()) {
                        if (isEdit) {
                            phraseToEdit.setPhrase(phrase);
                            databaseHelper.updateMotivationalPhrase(phraseToEdit.getId(), phrase);
                            handlePhraseChange();
                        } else {
                            databaseHelper.addMotivationalPhrase(phrase);
                            handlePhraseChange();
                        }
                        loadPhrases();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDeleteConfirmation(MotivationalPhrase phrase) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar frase")
                .setMessage("¿Estás seguro de que quieres eliminar esta frase motivacional?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    databaseHelper.deleteMotivationalPhrase(phrase.getId());
                    handlePhraseChange();
                    loadPhrases();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadPhrases() {
        Cursor cursor = databaseHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_MOTIVATIONAL_PHRASES,
                new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_PHRASE},
                null, null, null, null, null);

        phrasesList.clear();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            String phrase = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHRASE));
            phrasesList.add(new MotivationalPhrase(id, phrase));
        }
        cursor.close();

        phrasesAdapter.notifyDataSetChanged();

        //binding.emptyPhrasesText.setVisibility(phrasesList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateMotivationalNotifications() {
        // Cancelar la alarma existente
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction("MOTIVATIONAL_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                4,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);

        // Reprogramar con el intervalo actual
        int minutes = preferences.getInt("motivational_interval", 60);
        Calendar calendar = Calendar.getInstance();
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                minutes * 60 * 1000L,
                pendingIntent
        );
    }

    private void handlePhraseChange() {
        loadPhrases(); // Recargar la lista
        updateMotivationalNotifications(); // Actualizar las notificaciones
    }



}