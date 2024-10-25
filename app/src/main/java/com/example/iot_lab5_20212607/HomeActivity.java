package com.example.iot_lab5_20212607;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.adapter.AddExerciseDialog;
import com.example.iot_lab5_20212607.adapter.MealAdapter;
import com.example.iot_lab5_20212607.databinding.ActivityHomeBinding;
import com.example.iot_lab5_20212607.dialog.AddMealDialog;
import com.example.iot_lab5_20212607.model.Meal;
import com.example.iot_lab5_20212607.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private int goalCalories = 2000;
    private MealAdapter mealAdapter;
    private DatabaseHelper databaseHelper;
    private String currentDate;

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
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        setupWindowInsets();
        setupBottomNavigation();
        setupRecyclerView();
        setupButtons();
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


    private List<Meal> getMockMeals() {
        List<Meal> meals = new ArrayList<>();
        meals.add(new Meal("Desayuno", "8:00 AM", 350, "new Date()"));
        meals.add(new Meal("Almuerzo", "1:00 PM", 650,"a"));
        meals.add(new Meal("Merienda", "4:00 PM", 156,"a"));
        meals.add(new Meal("Cena", "8:00 PM", 300,"a"));
        return meals;
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


    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Recargar datos cuando se vuelve a la actividad
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}