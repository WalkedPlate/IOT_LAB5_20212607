package com.example.iot_lab5_20212607.model;

public class CommonFood {
    String name;
    int calories;
    private int iconResource;

    public CommonFood(String name, int calories) {
        this.name = name;
        this.calories = calories;
    }

    public CommonFood(String name, int calories, int iconResource) {
        this.name = name;
        this.calories = calories;
        this.iconResource = iconResource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
}
