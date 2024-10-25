package com.example.iot_lab5_20212607;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.databinding.ActivityProfileBinding;
import com.example.iot_lab5_20212607.model.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        loadUserData();
        setupLogoutButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserData() {
        User user = databaseHelper.getUserData();
        if (user != null) {
            // Información Personal
            binding.weightValue.setText(String.format(Locale.getDefault(), "%.1f kg", user.getWeight()));
            binding.heightValue.setText(String.format(Locale.getDefault(), "%.1f cm", user.getHeight()));
            binding.ageValue.setText(String.format(Locale.getDefault(), "%d años", user.getAge()));
            binding.genderValue.setText(user.getGender());

            // Objetivos
            binding.activityLevelValue.setText(user.getActivityLevel());
            binding.targetCaloriesValue.setText(String.format(Locale.getDefault(), "%.0f kcal/día", user.getTargetCalories()));

            // Estado del objetivo
            String weightGoal = user.getWeightGoal();
            if ("LOSE".equals(weightGoal)) {
                binding.weightGoalValue.setText("Bajar de peso");
                binding.weightGoalIcon.setImageResource(R.drawable.ic_trending_down);
            } else if ("GAIN".equals(weightGoal)) {
                binding.weightGoalValue.setText("Aumentar peso");
                binding.weightGoalIcon.setImageResource(R.drawable.ic_trending_up);
            } else {
                binding.weightGoalValue.setText("Mantener peso");
                binding.weightGoalIcon.setImageResource(R.drawable.ic_trending_flat);
            }
        }
    }

    private void setupLogoutButton() {
        binding.logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión? Se eliminarán todos tus datos registrados.")
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        databaseHelper.clearAllData();
        Intent intent = new Intent(this, UserRegistrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}