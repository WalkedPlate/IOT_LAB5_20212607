package com.example.iot_lab5_20212607.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_lab5_20212607.R;
import com.example.iot_lab5_20212607.model.CommonFood;

import java.util.List;
import java.util.Locale;

public class CommonFoodAdapter extends RecyclerView.Adapter<CommonFoodAdapter.ViewHolder> {
    private final List<CommonFood> commonFoods;

    public CommonFoodAdapter(List<CommonFood> commonFoods) {
        this.commonFoods = commonFoods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_common_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommonFood food = commonFoods.get(position);
        holder.foodName.setText(food.getName());
        holder.calories.setText(String.format(Locale.getDefault(), "%d kcal", food.getCalories()));
    }

    @Override
    public int getItemCount() {
        return commonFoods.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView foodName;
        final TextView calories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.foodName);
            calories = itemView.findViewById(R.id.calories);
        }
    }
}