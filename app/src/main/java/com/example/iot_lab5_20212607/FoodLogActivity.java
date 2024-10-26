package com.example.iot_lab5_20212607;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.iot_lab5_20212607.SQLite.DatabaseHelper;
import com.example.iot_lab5_20212607.adapter.CommonFoodAdapter;
import com.example.iot_lab5_20212607.adapter.MealAdapter;
import com.example.iot_lab5_20212607.databinding.ActivityFoodLogBinding;
import com.example.iot_lab5_20212607.model.CommonFood;
import com.example.iot_lab5_20212607.model.Meal;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoodLogActivity extends AppCompatActivity {
    private ActivityFoodLogBinding binding;
    private DatabaseHelper databaseHelper;
    private MealAdapter mealAdapter;
    private CommonFoodAdapter commonFoodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityFoodLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);

        setupToolbar();
        setupRecyclerViews();
        loadData();
        setupCommonFoods();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }


    private void setupRecyclerViews() {
        // Configurar RecyclerView de comidas
        binding.mealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealAdapter = new MealAdapter(new ArrayList<>());
        binding.mealsRecyclerView.setAdapter(mealAdapter);

    }

    private void setupCommonFoods() {
        List<CommonFood> commonFoods = Arrays.asList(
                new CommonFood("Arroz (1 taza)", 130),
                new CommonFood("Pollo (100g)", 165),
                new CommonFood("Ensalada César", 45),
                new CommonFood("Pan integral", 75),
                new CommonFood("Manzana", 60)
        );

        binding.commonFoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.commonFoodsRecyclerView.setAdapter(new CommonFoodAdapter(commonFoods));
    }


    private void loadData() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<Meal> meals = databaseHelper.getMealsByDate(currentDate);
        mealAdapter.setMeals(meals);

        // Mostrar u ocultar el texto de "no hay comidas"
        binding.emptyMealsText.setVisibility(meals.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}