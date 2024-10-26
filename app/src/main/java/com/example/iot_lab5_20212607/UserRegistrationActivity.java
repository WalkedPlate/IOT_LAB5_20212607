package com.example.iot_lab5_20212607;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.databinding.ActivityUserRegistrationBinding;
import com.example.iot_lab5_20212607.model.UserPreferences;

import java.util.HashMap;
import java.util.Map;

public class UserRegistrationActivity extends AppCompatActivity {

    private ActivityUserRegistrationBinding binding;
    private final Map<String, Double> activityMultipliers = new HashMap<>();
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        checkFirstTimeUser();

        askNotificationPermission();
        setupToolbar();
        setupDropdowns();
        setupActivityMultipliers();
        setupSaveButton();
    }

    private void checkFirstTimeUser() {
        if (databaseHelper.userExists()) {
            startHomeActivity();
            finish();
        }
    }


    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Registro de Usuario");
        }
    }

    private void setupActivityMultipliers() {
        activityMultipliers.put("Sedentario (poco o ningún ejercicio)", 1.2);
        activityMultipliers.put("Ligero (ejercicio 1-3 días/semana)", 1.375);
        activityMultipliers.put("Moderado (ejercicio 3-5 días/semana)", 1.55);
        activityMultipliers.put("Activo (ejercicio 6-7 días/semana)", 1.725);
        activityMultipliers.put("Muy activo (ejercicio intenso diario)", 1.9);
    }

    private void setupDropdowns() {
        // Configurar dropdown de género
        String[] genders = new String[]{"Masculino", "Femenino"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
        );
        binding.genderDropdown.setAdapter(genderAdapter);

        // Configurar dropdown de nivel de actividad
        String[] activityLevels = new String[]{
                "Sedentario (poco o ningún ejercicio)",
                "Ligero (ejercicio 1-3 días/semana)",
                "Moderado (ejercicio 3-5 días/semana)",
                "Activo (ejercicio 6-7 días/semana)",
                "Muy activo (ejercicio intenso diario)"
        };
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                activityLevels
        );
        binding.activityLevelDropdown.setAdapter(activityAdapter);

        // Hacer que los dropdowns no sean editables
        binding.genderDropdown.setKeyListener(null);
        binding.activityLevelDropdown.setKeyListener(null);

        // Mostrar el dropdown al hacer click
        binding.genderDropdown.setOnClickListener(v -> binding.genderDropdown.showDropDown());
        binding.activityLevelDropdown.setOnClickListener(v -> binding.activityLevelDropdown.showDropDown());
    }

    private void setupSaveButton() {
        binding.saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserProfile();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validar peso (en kilogramos)
        String weight = binding.weightInput.getText().toString();
        if (weight.isEmpty()) {
            binding.weightInput.setError("Ingrese su peso en kg");
            isValid = false;
        } else {
            float weightValue = Float.parseFloat(weight);
            if (weightValue < 30 || weightValue > 300) {
                binding.weightInput.setError("Ingrese un peso válido entre 30 y 300 kg");
                isValid = false;
            }
        }

        // Validar altura (en centímetros)
        String height = binding.heightInput.getText().toString();
        if (height.isEmpty()) {
            binding.heightInput.setError("Ingrese su altura en cm");
            isValid = false;
        } else {
            float heightValue = Float.parseFloat(height);
            if (heightValue < 100 || heightValue > 250) {
                binding.heightInput.setError("Ingrese una altura válida entre 100 y 250 cm");
                isValid = false;
            }
        }

        // Validar edad
        String age = binding.ageInput.getText().toString();
        if (age.isEmpty()) {
            binding.ageInput.setError("Ingrese su edad");
            isValid = false;
        } else {
            int ageValue = Integer.parseInt(age);
            if (ageValue < 15 || ageValue > 100) {
                binding.ageInput.setError("Ingrese una edad válida entre 15 y 100 años");
                isValid = false;
            }
        }


        return isValid;
    }

    private void saveUserProfile() {
        try {
            float weight = Float.parseFloat(binding.weightInput.getText().toString());
            float height = Float.parseFloat(binding.heightInput.getText().toString());
            int age = Integer.parseInt(binding.ageInput.getText().toString());
            String gender = binding.genderDropdown.getText().toString();
            String activityLevel = binding.activityLevelDropdown.getText().toString();

            double bmr = calculateBMR(weight, height, age, gender);
            double maintenanceCalories = calculateMaintenanceCalories(bmr, activityLevel);
            double targetCalories = calculateTargetCalories(maintenanceCalories);
            String weightGoal = getWeightGoalString();

            long result = databaseHelper.saveUserData(weight, height, age, gender,
                    activityLevel, targetCalories, weightGoal);

            if (result > 0) {
                startHomeActivity();
            } else {
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingrese valores válidos", Toast.LENGTH_SHORT).show();
        }
    }


    private String getWeightGoalString() {
        int selectedId = binding.weightGoalRadioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.goalLose) {
            return "LOSE";
        } else if (selectedId == R.id.goalGain) {
            return "GAIN";
        } else {
            return "MAINTAIN";
        }
    }

    private double calculateBMR(float weight, float height, int age, String gender) {
        if (gender.equals("Masculino")) {
            return 66 + (13.7 * weight) + (5 * height) - (6.8 * age);
        } else {
            return 655 + (9.6 * weight) + (1.8 * height) - (4.7 * age);
        }
    }

    private double calculateMaintenanceCalories(double bmr, String activityLevel) {
        Double multiplier = activityMultipliers.get(activityLevel);
        return bmr * (multiplier != null ? multiplier : 1.2);
    }

    private double calculateTargetCalories(double maintenanceCalories) {
        int selectedId = binding.weightGoalRadioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.goalLose) {
            return maintenanceCalories - 300; // Déficit para perder peso
        } else if (selectedId == R.id.goalGain) {
            return maintenanceCalories + 500; // Superávit para ganar peso
        } else {
            return maintenanceCalories; // Mantener peso
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}