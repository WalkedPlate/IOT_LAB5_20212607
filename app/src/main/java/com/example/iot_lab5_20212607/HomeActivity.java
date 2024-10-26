package com.example.iot_lab5_20212607;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.dialog.AddExerciseDialog;
import com.example.iot_lab5_20212607.adapter.MealAdapter;
import com.example.iot_lab5_20212607.databinding.ActivityHomeBinding;
import com.example.iot_lab5_20212607.dialog.AddMealDialog;
import com.example.iot_lab5_20212607.model.Meal;
import com.example.iot_lab5_20212607.model.User;
import com.example.iot_lab5_20212607.notification.NotificationHelper;
import com.example.iot_lab5_20212607.notification.NotificationReceiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private int goalCalories = 2000;
    private MealAdapter mealAdapter;
    private DatabaseHelper databaseHelper;
    private String currentDate;
    private NotificationHelper notificationHelper;

    private Handler handler;
    private Runnable mealCheckRunnable;
    private static final int CHECK_INTERVAL = 30000; // Cada 30 segundos xd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        handler = new Handler(Looper.getMainLooper());
        startPeriodicMealCheck();

        setupWindowInsets();
        setupBottomNavigation();
        setupRecyclerView();
        setupButtons();
        setupMealCheck();
        loadData();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void setupButtons() {
        // Botón para agregar comida
        binding.addFoodButton.setOnClickListener(v -> {
            AddMealDialog dialog = AddMealDialog.newInstance();
            dialog.setOnMealAddedListener(new AddMealDialog.OnMealAddedListener() {
                @Override
                public void onMealAdded() {
                    loadData(); // Recargar datos cuando se agrega una comida
                    checkCaloriesExcess(); // Verificar exceso después de agregar comida
                }
            });
            dialog.show(getSupportFragmentManager(), "AddMealDialog");
        });

        // Botón para agregar ejercicio
        binding.addExerciseButton.setOnClickListener(v -> {
            AddExerciseDialog dialog = AddExerciseDialog.newInstance();
            dialog.setOnExerciseAddedListener(new AddExerciseDialog.OnExerciseAddedListener() {
                @Override
                public void onExerciseAdded() {
                    loadData(); // Recargar datos cuando se agrega un ejercicio
                }
            });
            dialog.show(getSupportFragmentManager(), "AddExerciseDialog");
        });
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter(new ArrayList<>());
        binding.mealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.mealsRecyclerView.setAdapter(mealAdapter);
    }

    private void loadData() {
        try {
            // Obtener datos del usuario
            User user = databaseHelper.getUserData();
            if (user != null) {
                goalCalories = (int) user.getTargetCalories();
            }

            // Cargar comidas
            List<Meal> todayMeals = databaseHelper.getMealsByDate(currentDate);
            mealAdapter.setMeals(todayMeals);

            // Calcular calorías consumidas
            int consumedCalories = databaseHelper.getTotalCaloriesByDate(currentDate);

            // Calcular calorías quemadas
            int burnedCalories = 0;
            try {
                burnedCalories = databaseHelper.getTotalCaloriesBurnedByDate(currentDate);
            } catch (SQLiteException e) {
                // La tabla de ejercicios podría no existir aún
                Log.e("HomeActivity", "Error getting burned calories: " + e.getMessage());
                // Continuar con burnedCalories = 0
            }

            // Actualizar UI
            updateCaloriesDisplay(consumedCalories, burnedCalories);

        } catch (SQLiteException e) {
            Log.e("HomeActivity", "Database error: " + e.getMessage());
            Toast.makeText(this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCaloriesDisplay(int consumedCalories, int burnedCalories) {
        // Actualizar displays de calorías
        binding.consumedCalories.setText(String.format(Locale.getDefault(), "%,d", consumedCalories));
        binding.goalCalories.setText(String.format(Locale.getDefault(), "%,d", goalCalories));
        binding.burnedCalories.setText(String.format(Locale.getDefault(), "%,d", burnedCalories));

        // Calcular y mostrar calorías efectivas (consumidas - quemadas)
        int effectiveCalories = consumedCalories - burnedCalories;
        binding.circleEffectiveCalories.setText(String.format(Locale.getDefault(), "%,d", effectiveCalories));

        // Actualizar barra de progreso usando calorías efectivas
        int progress = (int) ((effectiveCalories / (float) goalCalories) * 100);
        // Asegurar que el progreso esté entre 0 y 100
        progress = Math.max(0, Math.min(100, progress));
        binding.caloriesProgress.setProgress(progress);
    }


    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                return false;
            }

            if (itemId == R.id.navigation_food_log) {
                startActivity(new Intent(this, FoodLogActivity.class));
                return false;
            }

            if (itemId == R.id.navigation_exercise) {
                startActivity(new Intent(this, ExerciseActivity.class));
                return false;
            }

            if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return false;
            }

            if (itemId == R.id.navigation_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return false;
            }

            return false;
        });
    }

    private void checkCaloriesExcess() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Obtener calorías consumidas y quemadas
        int consumedCalories = databaseHelper.getTotalCaloriesByDate(currentDate);
        int burnedCalories = databaseHelper.getTotalCaloriesBurnedByDate(currentDate);
        int effectiveCalories = consumedCalories - burnedCalories;

        // Obtener meta de calorías del usuario
        User user = databaseHelper.getUserData();
        if (user != null) {
            int goalCalories = (int) user.getTargetCalories();

            // Guardar el último estado de exceso para evitar notificaciones repetidas
            SharedPreferences prefs = getSharedPreferences("calories_prefs", MODE_PRIVATE);
            boolean wasExceeded = prefs.getBoolean("calories_exceeded_" + currentDate, false);

            // Verificar si excedió el límite y no se había notificado antes
            if (effectiveCalories > goalCalories && !wasExceeded) {
                int excess = effectiveCalories - goalCalories;
                notificationHelper.showCaloriesAlert(excess);

                // Guardar que ya se notificó el exceso hoy
                prefs.edit()
                        .putBoolean("calories_exceeded_" + currentDate, true)
                        .apply();
            }
        }
    }

    private void startPeriodicMealCheck() {
        mealCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Verificar si hay comidas registradas
                if (!databaseHelper.hasRegisteredMealsToday()) {
                    notificationHelper.showNoMealsRegisteredAlert();
                }
                // Programar la siguiente verificación
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };

        // Iniciar el ciclo de verificación
        handler.post(mealCheckRunnable);
    }


    private void setupMealCheck() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction("CHECK_MEALS");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                105,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Programar para verificar cada 4 horas después del mediodía
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                4 * 60 * 60 * 1000, // 4 horas
                pendingIntent
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Detener las verificaciones cuando la actividad no está visible
        if (handler != null && mealCheckRunnable != null) {
            handler.removeCallbacks(mealCheckRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        startPeriodicMealCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar recursos
        if (handler != null && mealCheckRunnable != null) {
            handler.removeCallbacks(mealCheckRunnable);
        }
        binding = null;
    }
}