package com.example.iot_lab5_20212607.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREF_NAME = "UserData";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_AGE = "age";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_ACTIVITY_LEVEL = "activity_level";
    private static final String KEY_TARGET_CALORIES = "target_calories";
    private static final String KEY_WEIGHT_GOAL = "weight_goal";
    private static final String KEY_IS_FIRST_TIME = "is_first_time";

    private final SharedPreferences preferences;

    public UserPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserData(float weight, float height, int age, String gender,
                             String activityLevel, double targetCalories, String weightGoal) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_WEIGHT, weight);
        editor.putFloat(KEY_HEIGHT, height);
        editor.putInt(KEY_AGE, age);
        editor.putString(KEY_GENDER, gender);
        editor.putString(KEY_ACTIVITY_LEVEL, activityLevel);
        editor.putFloat(KEY_TARGET_CALORIES, (float) targetCalories);
        editor.putString(KEY_WEIGHT_GOAL, weightGoal);
        editor.putBoolean(KEY_IS_FIRST_TIME, false);
        editor.apply();
    }

    public boolean isFirstTime() {
        return preferences.getBoolean(KEY_IS_FIRST_TIME, true);
    }

    public float getWeight() {
        return preferences.getFloat(KEY_WEIGHT, 0f);
    }

    public float getHeight() {
        return preferences.getFloat(KEY_HEIGHT, 0f);
    }

    public int getAge() {
        return preferences.getInt(KEY_AGE, 0);
    }

    public String getGender() {
        return preferences.getString(KEY_GENDER, "");
    }

    public String getActivityLevel() {
        return preferences.getString(KEY_ACTIVITY_LEVEL, "");
    }

    public float getTargetCalories() {
        return preferences.getFloat(KEY_TARGET_CALORIES, 0f);
    }

    public String getWeightGoal() {
        return preferences.getString(KEY_WEIGHT_GOAL, "");
    }
}
