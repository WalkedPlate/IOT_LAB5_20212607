package com.example.iot_lab5_20212607.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab5_20212607.R;
import com.example.iot_lab5_20212607.model.Meal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    private List<Meal> meals;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public MealAdapter(List<Meal> meals) {
        this.meals = meals;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.mealName.setText(meal.getName());

        // Convertir el formato de hora
        try {
            Date date = inputFormat.parse(meal.getTime());
            if (date != null) {
                holder.mealTime.setText(outputFormat.format(date));
            }
        } catch (ParseException e) {
            holder.mealTime.setText(meal.getTime()); // Fallback al formato original
        }

        holder.calories.setText(String.format(Locale.getDefault(), "%d kcal", meal.getCalories()));
    }

    @Override
    public int getItemCount() {
        return meals != null ? meals.size() : 0;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView mealName;
        TextView mealTime;
        TextView calories;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealName = itemView.findViewById(R.id.mealName);
            mealTime = itemView.findViewById(R.id.mealTime);
            calories = itemView.findViewById(R.id.calories);
        }
    }
}

