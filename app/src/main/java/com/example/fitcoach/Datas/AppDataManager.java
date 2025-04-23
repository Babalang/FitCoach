package com.example.fitcoach.Datas;

import android.content.Context;
import android.content.SharedPreferences;

public class AppDataManager {
    private static final String PREF_NAME = "fitcoach_data";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_EXERCISE = "exercise_minutes";
    private static final String KEY_CALORIES = "calories";

    private static AppDataManager instance;
    private final SharedPreferences prefs;

    private AppDataManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static AppDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppDataManager(context);
        }
        return instance;
    }

    public void saveSteps(int steps) {
        prefs.edit().putInt(KEY_STEPS, steps).apply();
    }

    public int getSteps() {
        return prefs.getInt(KEY_STEPS, 0);
    }

    public void saveExerciseMinutes(int minutes) {
        prefs.edit().putInt(KEY_EXERCISE, minutes).apply();
    }

    public int getExerciseMinutes() {
        return prefs.getInt(KEY_EXERCISE, 0);
    }

    public void saveCalories(int calories) {
        prefs.edit().putInt(KEY_CALORIES, calories).apply();
    }

    public int getCalories() {
        return prefs.getInt(KEY_CALORIES, 0);
    }
}
