package com.example.iot_lab5_20212607;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.iot_lab5_20212607.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupTimeButtons();
        setupMotivationalInterval();

    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTimeButtons() {
        binding.breakfastTimeButton.setOnClickListener(v -> showTimePickerDialog("breakfast"));
        binding.lunchTimeButton.setOnClickListener(v -> showTimePickerDialog("lunch"));
        binding.dinnerTimeButton.setOnClickListener(v -> showTimePickerDialog("dinner"));
    }

    private void setupMotivationalInterval() {
        binding.motivationalIntervalInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    updateMotivationalInterval(Integer.parseInt(s.toString()));
                }
            }
        });
    }

    private void showTimePickerDialog(String mealType) {
        // Implementar TimePickerDialog
    }

    private void updateMotivationalInterval(int minutes) {
        // Implementar actualización del intervalo
    }


}