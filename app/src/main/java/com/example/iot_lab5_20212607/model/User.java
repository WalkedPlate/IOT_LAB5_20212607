package com.example.iot_lab5_20212607.model;

public class User {
    private float weight;
    private float height;
    private int age;
    private String gender;
    private String activityLevel;
    private double targetCalories;
    private String weightGoal;

    public User(float weight, float height, int age, String gender,
                String activityLevel, double targetCalories, String weightGoal) {
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.targetCalories = targetCalories;
        this.weightGoal = weightGoal;
    }

    // Getters
    public float getWeight() { return weight; }
    public float getHeight() { return height; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public String getActivityLevel() { return activityLevel; }
    public double getTargetCalories() { return targetCalories; }
    public String getWeightGoal() { return weightGoal; }
}
