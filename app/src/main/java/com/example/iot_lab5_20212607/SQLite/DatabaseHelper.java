package com.example.iot_lab5_20212607.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.iot_lab5_20212607.model.Exercise;
import com.example.iot_lab5_20212607.model.Meal;
import com.example.iot_lab5_20212607.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CaloriesTrackerDB";
    private static final int DATABASE_VERSION = 1;

    // Tabla de frases motivacionales
    public static final String TABLE_MOTIVATIONAL_PHRASES = "motivational_phrases";
    public static final String COLUMN_PHRASE = "phrase";

    // Tabla de comidas
    public static final String TABLE_MEALS = "meals";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_DATE = "date";


    // Tabla de ejercicios
    public static final String TABLE_EXERCISES = "exercises";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CALORIES_BURNED = "calories_burned";

    // Tabla de datos de usuario
    public static final String TABLE_USER = "user";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_ACTIVITY_LEVEL = "activity_level";
    public static final String COLUMN_TARGET_CALORIES = "target_calories";
    public static final String COLUMN_WEIGHT_GOAL = "weight_goal";


    private static final String CREATE_TABLE_MEALS =
            "CREATE TABLE " + TABLE_MEALS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_TIME + " TEXT NOT NULL, " +
                    COLUMN_CALORIES + " INTEGER NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_EXERCISES =
            "CREATE TABLE " + TABLE_EXERCISES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_CALORIES_BURNED + " INTEGER NOT NULL, " +
                    COLUMN_TIME + " TEXT NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_WEIGHT + " REAL NOT NULL, " +
                    COLUMN_HEIGHT + " REAL NOT NULL, " +
                    COLUMN_AGE + " INTEGER NOT NULL, " +
                    COLUMN_GENDER + " TEXT NOT NULL, " +
                    COLUMN_ACTIVITY_LEVEL + " TEXT NOT NULL, " +
                    COLUMN_TARGET_CALORIES + " REAL NOT NULL, " +
                    COLUMN_WEIGHT_GOAL + " TEXT NOT NULL);";

    private static final String CREATE_TABLE_MOTIVATIONAL_PHRASES =
            "CREATE TABLE " + TABLE_MOTIVATIONAL_PHRASES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PHRASE + " TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_MEALS);
            db.execSQL(CREATE_TABLE_EXERCISES);
            db.execSQL(CREATE_TABLE_USER);
            db.execSQL(CREATE_TABLE_MOTIVATIONAL_PHRASES);
            insertDefaultPhrases(db);
        } catch (SQLiteException e) {
            Log.e("DatabaseHelper", "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOTIVATIONAL_PHRASES);
            onCreate(db);
        } catch (SQLiteException e) {
            Log.e("DatabaseHelper", "Error upgrading database: " + e.getMessage());
        }
    }

    // Agregar una comida
    public long addMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_TIME, meal.getTime());
        values.put(COLUMN_CALORIES, meal.getCalories());
        values.put(COLUMN_DATE, meal.getDate());

        long id = db.insert(TABLE_MEALS, null, values);
        db.close();
        return id;
    }

    // Obtener todas las comidas de un día específico
    public List<Meal> getMealsByDate(String date) {
        List<Meal> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_ID, COLUMN_NAME, COLUMN_TIME, COLUMN_CALORIES, COLUMN_DATE};
        String selection = COLUMN_DATE + " = ?";
        String[] selectionArgs = {date};
        String orderBy = COLUMN_TIME + " ASC";

        Cursor cursor = db.query(TABLE_MEALS, columns, selection, selectionArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                Meal meal = new Meal(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                );
                meals.add(meal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return meals;
    }

    // Obtener el total de calorías para un día específico
    public int getTotalCaloriesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_CALORIES + ") FROM " + TABLE_MEALS +
                " WHERE " + COLUMN_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{date});
        int totalCalories = 0;

        if (cursor.moveToFirst()) {
            totalCalories = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return totalCalories;
    }

    // Eliminar una comida
    public void deleteMeal(long mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEALS, COLUMN_ID + " = ?", new String[]{String.valueOf(mealId)});
        db.close();
    }


    // MÉTODOS para ejercicios
    public long addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, exercise.getName());
        values.put(COLUMN_DURATION, exercise.getDuration());
        values.put(COLUMN_CALORIES_BURNED, exercise.getCaloriesBurned());
        values.put(COLUMN_TIME, exercise.getTime());
        values.put(COLUMN_DATE, exercise.getDate());

        long id = db.insert(TABLE_EXERCISES, null, values);
        db.close();
        return id;
    }

    public List<Exercise> getExercisesByDate(String date) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID, COLUMN_NAME, COLUMN_DURATION,
                COLUMN_CALORIES_BURNED, COLUMN_TIME, COLUMN_DATE
        };
        String selection = COLUMN_DATE + " = ?";
        String[] selectionArgs = {date};
        String orderBy = COLUMN_TIME + " ASC";

        Cursor cursor = db.query(TABLE_EXERCISES, columns, selection, selectionArgs,
                null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES_BURNED)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                );
                exercises.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return exercises;
    }

    public int getTotalCaloriesBurnedByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_CALORIES_BURNED + ") FROM " + TABLE_EXERCISES +
                " WHERE " + COLUMN_DATE + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{date});
        int totalCaloriesBurned = 0;

        if (cursor.moveToFirst()) {
            totalCaloriesBurned = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return totalCaloriesBurned;
    }

    public void deleteExercise(long exerciseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISES, COLUMN_ID + " = ?",
                new String[]{String.valueOf(exerciseId)});
        db.close();
    }


    //MÉTODOS DE USUARIO

    // Método para guardar o actualizar datos del usuario
    public long saveUserData(float weight, float height, int age, String gender,
                             String activityLevel, double targetCalories, String weightGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_ACTIVITY_LEVEL, activityLevel);
        values.put(COLUMN_TARGET_CALORIES, targetCalories);
        values.put(COLUMN_WEIGHT_GOAL, weightGoal);

        // Primero intentamos actualizar si existe un usuario
        int rowsAffected = db.update(TABLE_USER, values, null, null);
        long result;

        if (rowsAffected == 0) {
            // Si no existe usuario, insertamos uno nuevo
            result = db.insert(TABLE_USER, null, values);
        } else {
            result = rowsAffected;
        }

        db.close();
        return result;
    }

    // Método para obtener los datos del usuario
    public User getUserData() {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(TABLE_USER, null, null, null,
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)),
                    cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY_LEVEL)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TARGET_CALORIES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT_GOAL))
            );
            cursor.close();
        }

        db.close();
        return user;
    }

    // Método para verificar si existe un usuario
    public boolean userExists() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, null, null,
                null, null, null);

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.getCount() > 0;
            cursor.close();
        }

        db.close();
        return exists;
    }

    private void insertDefaultPhrases(SQLiteDatabase db) {
        String[] defaultPhrases = {
                "¡Sigue así! Vas por buen camino",
                "Cada decisión saludable cuenta",
                "Tu esfuerzo vale la pena",
                "¡Mantén el buen trabajo!",
                "Pequeños cambios, grandes resultados"
        };

        for (String phrase : defaultPhrases) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PHRASE, phrase);
            db.insert(TABLE_MOTIVATIONAL_PHRASES, null, values);
        }
    }

    // Métodos CRUD para frases motivacionales
    public long addMotivationalPhrase(String phrase) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHRASE, phrase);
        return db.insert(TABLE_MOTIVATIONAL_PHRASES, null, values);
    }

    public void updateMotivationalPhrase(long id, String phrase) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHRASE, phrase);
        db.update(TABLE_MOTIVATIONAL_PHRASES, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteMotivationalPhrase(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MOTIVATIONAL_PHRASES,
                COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<String> getAllMotivationalPhrases() {
        List<String> phrases = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MOTIVATIONAL_PHRASES,
                new String[]{COLUMN_PHRASE},
                null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                phrases.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return phrases;
    }


    public boolean hasRegisteredMealsToday() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEALS,
                new String[]{COLUMN_ID},
                COLUMN_DATE + " = ?",
                new String[]{currentDate},
                null, null, null);

        boolean hasMeals = cursor.getCount() > 0;
        cursor.close();
        return hasMeals;
    }




    //ELIMINAR TODA LA DATA
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Eliminar todos los datos de las tablas
            db.delete(TABLE_USER, null, null);
            db.delete(TABLE_MEALS, null, null);
            db.delete(TABLE_EXERCISES, null, null);
        } catch (SQLiteException e) {
            Log.e("DatabaseHelper", "Error clearing data: " + e.getMessage());
        } finally {
            db.close();
        }
    }
}
