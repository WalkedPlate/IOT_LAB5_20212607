package com.example.iot_lab5_20212607;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.adapter.ExerciseAdapter;
import com.example.iot_lab5_20212607.databinding.ActivityExerciseBinding;
import com.example.iot_lab5_20212607.model.Exercise;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ExerciseActivity extends AppCompatActivity {
    private ActivityExerciseBinding binding;
    private DatabaseHelper databaseHelper;
    private ExerciseAdapter exerciseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityExerciseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupRecyclerView();
        loadExercises();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(new ArrayList<>());
        binding.exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.exercisesRecyclerView.setAdapter(exerciseAdapter);
    }

    private void loadExercises() {
        // Obtener la fecha actual
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        // Cargar ejercicios de la base de datos
        List<Exercise> exercises = databaseHelper.getExercisesByDate(currentDate);
        exerciseAdapter.setExercises(exercises);

        // Actualizar la visibilidad del texto "sin ejercicios"
        binding.emptyExercisesText.setVisibility(
                exercises.isEmpty() ? View.VISIBLE : View.GONE
        );

        // Actualizar el total de calorías quemadas
        int totalCaloriesBurned = databaseHelper.getTotalCaloriesBurnedByDate(currentDate);
        binding.caloriesBurnedToday.setText(String.valueOf(totalCaloriesBurned));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExercises(); // Recargar datos cuando se vuelve a la actividad
    }
}