package com.example.iot_lab5_20212607;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iot_lab5_20212607.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private int consumedCalories = 1456;
    private int goalCalories = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();
        updateCaloriesProgress();



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


    private void updateCaloriesProgress() {
        int progress = (int) ((consumedCalories / (float) goalCalories) * 100);
        binding.caloriesProgress.setProgress(progress);
    }

}