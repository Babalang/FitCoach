package com.example.fitcoach.Datas;
// AppDataManager.java
// Classe pour gérer les données de l'application FitCoach
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.fitcoach.ui.history.Exercise;

import java.util.ArrayList;
import java.util.List;

public class AppDataManager {
    private static final String TAG = "AppDataManager";
    private static final String DATABASE_NAME = "fitcoach_data";
    private static final int DATABASE_VERSION = 10;

    private static final String TABLE_COMPTE = "comptes";

    private static  final String TABLE_HISTORIQUE = "historique";

    private static final String TABLE_STEPCOUNTER = "stepcounter";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LOGIN = "login";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_TELEPHONE = "telephone";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_SEXE = "sexe";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_EXERCICE = "exercice";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_STEPS = "steps";
    private static final String COLUMN_BASE_STEPS = "base_steps";
    private static final String COLUMN_LAST_RESET_DATE = "last_reset_date";
    private static final String COLUMN_IS_COMPLETED = "is_completed";
    private static final String COLUMN_STEPS_OBJECTIVE = "steps_objective";
    private static final String COLUMN_CALORIES_OBJECTIVE = "calories_objective";
    private static final String COLUMN_TAILLE = "taille";
    private static final String COLUMN_POIDS = "poids";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_SPEED = "speed";
    private static final String COLUMN_REPETITION = "repetition";

    // Requêtes SQL pour créer les tables
    private static final String CREATE_TABLE_COMPTE =
            "CREATE TABLE " + TABLE_COMPTE + "("+
            COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_LOGIN+" TEXT, " +
            COLUMN_EMAIL+" TEXT, " +
            COLUMN_TELEPHONE+" TEXT, " +
            COLUMN_AGE+" INTEGER, " +
            COLUMN_POIDS+" FLOAT, " +
            COLUMN_TAILLE+" INTEGER, " +
            COLUMN_SEXE+" TEXT, "+
            COLUMN_IS_COMPLETED+" BOOLEAN, " +
            COLUMN_STEPS_OBJECTIVE+" INTEGER, " +
            COLUMN_CALORIES_OBJECTIVE+" INTEGER);";

    private static final String CREATE_TABLE_HISTORIQUE =
            "CREATE TABLE " + TABLE_HISTORIQUE + "("+
            COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_DATE+" TEXT, " +
            COLUMN_EXERCICE+" TEXT, " +
            COLUMN_CALORIES+" INTEGER, " +
            COLUMN_STEPS+" INTEGER, " +
            COLUMN_BASE_STEPS+" INTEGER, " +
            COLUMN_DISTANCE+" FLOAT, " +
            COLUMN_DURATION+" INTEGER,"+
            COLUMN_REPETITION+" INTEGER," +
            COLUMN_SPEED+" FLOAT);";

    private static final String CREATE_TABLE_STEPCOUNTER =
            "CREATE TABLE " + TABLE_STEPCOUNTER + "("+
            COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_LAST_RESET_DATE+" TEXT, " +
            COLUMN_STEPS+" INTEGER," +
            COLUMN_BASE_STEPS+" INTEGER,"+
            COLUMN_CALORIES+" FLOAT,"+
            COLUMN_DISTANCE+" FLOAT);";

    private static AppDataManager instance;
    private DataBaseHelper dHelper;
    private SQLiteDatabase db;


    private AppDataManager(){}

    // Méthodes pour obtenir l'instance unique de AppDataManager
    public static synchronized AppDataManager getInstance() {
        if(instance == null){
            instance = new AppDataManager();
        }
        return instance;
    }

    // Méthode pour obtenir l'instance unique de AppDataManager avec le contexte
    public static synchronized AppDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppDataManager();
            instance.init(context);
        }
        return instance;
    }

    // Méthode pour initialiser la base de données
    public void init(Context context){
        dHelper = new DataBaseHelper(context);
        db = dHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_STEPCOUNTER, null);
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count == 0) {
                insertStepCounter("", 0,0,0,0);
            }
        }
        cursor.close();
        Cursor cursor2 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COMPTE, null);
        if (cursor2.moveToFirst()) {
            int count = cursor2.getInt(0);
            if (count == 0) {
                insertCompte("", "", "", 0, "", 0, 0, false, 0, 0);
            }
        }
        cursor2.close();
    }

    // Classe interne pour gérer la base de données SQLite
    private static class DataBaseHelper extends SQLiteOpenHelper{
        DataBaseHelper(Context context){super(context, DATABASE_NAME, null, DATABASE_VERSION);}

        // Méthodes pour créer et mettre à jour la base de données
        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(CREATE_TABLE_COMPTE);
            db.execSQL(CREATE_TABLE_HISTORIQUE);
            db.execSQL(CREATE_TABLE_STEPCOUNTER);
            Log.d(TAG, "Création des tables effectuée");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPTE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORIQUE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPCOUNTER);
            onCreate(db);
        }
    }

    // Méthodes pour insérer, mettre à jour et supprimer des données dans la base de données
    public void insertCompte(String login, String email, String telephone, int age, String sexe, int stepsObjective, int caloriesObjective, boolean isCompleted, int taille, float poids){
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOGIN, login);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_TELEPHONE, telephone);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_SEXE, sexe);
        values.put(COLUMN_IS_COMPLETED, isCompleted);
        values.put(COLUMN_STEPS_OBJECTIVE, stepsObjective);
        values.put(COLUMN_CALORIES_OBJECTIVE, caloriesObjective);
        values.put(COLUMN_TAILLE, taille);
        values.put(COLUMN_POIDS, poids);
        db.insert(TABLE_COMPTE, null, values);
    }

    public void insertHistorique(String date, String exercice, int calories, int steps, float distance, long duration, int repetition, float speed) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_EXERCICE, exercice);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_BASE_STEPS, steps);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_REPETITION, repetition);
        values.put(COLUMN_SPEED, speed);
        db.insert(TABLE_HISTORIQUE, null, values);
    }

    public void insertStepCounter(String date, int steps, int baseSteps, float calories, float distance) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, 0);
        values.put(COLUMN_LAST_RESET_DATE, date);
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_BASE_STEPS, baseSteps);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_DISTANCE, distance);
        db.insert(TABLE_STEPCOUNTER, null, values);
    }


    public void updateCompte(int id, String login, String email, String telephone, int age, String sexe, int stepsObjective, int caloriesObjective,int size, float poids){
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOGIN, login);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_TELEPHONE, telephone);
        values.put(COLUMN_AGE, age);
        values.put(COLUMN_SEXE, sexe);
        values.put(COLUMN_IS_COMPLETED, true);
        values.put(COLUMN_STEPS_OBJECTIVE, stepsObjective);
        values.put(COLUMN_CALORIES_OBJECTIVE, caloriesObjective);
        values.put(COLUMN_TAILLE, size);
        values.put(COLUMN_POIDS, poids);

        Log.d(TAG, "Tentative mise à jour ID: " + id);

        int rows = db.update(TABLE_COMPTE, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "Mise à jour - lignes affectées: " + rows);
    }


    public void updateHistorique(int id, String date, String exercice, float calories, int steps, float distance, long duration, int repetition, float speed) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_EXERCICE, exercice);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_BASE_STEPS, steps);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_REPETITION, repetition);
        values.put(COLUMN_SPEED, speed);
        db.update(TABLE_HISTORIQUE, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
    public void updateStepCounter(int id, String date, int steps, int baseSteps, float calories, float distance) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STEPS, steps);
        values.put(COLUMN_BASE_STEPS, baseSteps);
        values.put(COLUMN_LAST_RESET_DATE, date);
        values.put(COLUMN_CALORIES, calories);
        values.put(COLUMN_DISTANCE, distance);
        db.update(TABLE_STEPCOUNTER, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteCompte(int id){
        db.delete(TABLE_COMPTE, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteHistorique(int id){
        db.delete(TABLE_HISTORIQUE, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
    public void deleteStepCounter(int id) {
        db.delete(TABLE_STEPCOUNTER, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Méthodes pour récupérer des données de la base de données
    // Récupération du nombre de pas
    public int getSteps(int id) {
        try{
            if(db == null || !db.isOpen()){
                db = dHelper.getWritableDatabase();
            }
            String query = "SELECT " + COLUMN_STEPS + " FROM " + TABLE_STEPCOUNTER + " WHERE " + COLUMN_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
            int steps = 0;
            if (cursor.moveToFirst()) {
                steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
            }
            cursor.close();
            return steps;
        } catch (Exception e){
            Log.e(TAG, "Erreur lors de l'ouverture de la base de données: " + e.getMessage());
            return 0;
        }

    }

    // Récupération des calories
    public float getCalories(int id) {
        SQLiteDatabase db = null;
        try {
            db = dHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e("AppDataManager", "Erreur d'accès à la base de données", e);
            return 0;
        }
        String query = "SELECT " + COLUMN_CALORIES + " FROM " + TABLE_STEPCOUNTER + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        float calories = 0;
        if (cursor.moveToFirst()) {
            calories = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
        }
        cursor.close();
        return calories;
    }

    // Récupération de la date du dernier reset
    public String getLastResetDate(int id) {
        String query = "SELECT " + COLUMN_LAST_RESET_DATE + " FROM " + TABLE_STEPCOUNTER + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        String lastResetDate = "";
        if (cursor.moveToFirst()) {
            lastResetDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_RESET_DATE));
        }
        cursor.close();
        return lastResetDate;
    }

    public int getBaseSteps(int id) {
        String query = "SELECT " + COLUMN_BASE_STEPS + " FROM " + TABLE_STEPCOUNTER + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        int baseSteps = -1;
        if (cursor.moveToFirst()) {
            baseSteps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BASE_STEPS));
        }
        cursor.close();
        return baseSteps;
    }

    // Récupération de la distance parcourue
    public float getDistance(int id) {
        String query = "SELECT " + COLUMN_DISTANCE + " FROM " + TABLE_STEPCOUNTER + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        float distance = 0;
        if (cursor.moveToFirst()) {
            distance = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE));
        }
        cursor.close();
        return distance;
    }

    // Récupération des informations du compte
    public boolean isCompleted(int id) {
        String query = "SELECT " + COLUMN_IS_COMPLETED + " FROM " + TABLE_COMPTE + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        boolean isCompleted = false;
        if (cursor.moveToFirst()) {
            isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1;
        }
        cursor.close();
        return isCompleted;
    }


    // Récupération des objectifs de pas et de calories
    public int getStepsObjective(int id) {
        String query = "SELECT " + COLUMN_STEPS_OBJECTIVE + " FROM " + TABLE_COMPTE + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        int stepsObjective = 0;
        if (cursor.moveToFirst()) {
            stepsObjective = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS_OBJECTIVE));
        }
        cursor.close();
        return stepsObjective;
    }

    public int getCaloriesObjective(int id){
        String query = "SELECT " + COLUMN_CALORIES_OBJECTIVE + " FROM " + TABLE_COMPTE + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        int caloriesObjective = 0;
        if (cursor.moveToFirst()) {
            caloriesObjective = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CALORIES_OBJECTIVE));
        }
        cursor.close();
        return caloriesObjective;
    }

    // Récupération de l'id du compte courant
    public int getCompteId() {
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_COMPTE + " LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        cursor.close();
        return id;
    }

    // Récupération de la liste des exercices dans l'historique
    public List<Exercise> getAllHistorique() {
        List<Exercise> exerciseList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_HISTORIQUE + " ORDER BY " + COLUMN_DATE + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String exercice = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXERCICE));
                float calories = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
                float distance = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DURATION));
                int repetition = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPETITION));
                float speed = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPEED));

                Exercise exercise = new Exercise(date, exercice, duration, steps, calories, distance, speed, repetition);
                exerciseList.add(exercise);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return exerciseList;
    }

    // Récupération du dernier exercice de l'historique
    public Exercise getLastHistorique() {
        String query = "SELECT * FROM " + TABLE_HISTORIQUE + " ORDER BY " + COLUMN_DATE + " DESC LIMIT 1";
        Cursor cursor = db.rawQuery(query, null);
        Exercise lastExercise = null;

        if (cursor.moveToFirst()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
            String exercice = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXERCICE));
            float calories = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_CALORIES));
            int steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
            float distance = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DURATION));
            int repetition = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPETITION));
            float speed = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SPEED));

            lastExercise = new Exercise(date, exercice, duration, steps, calories, distance, speed, repetition);
        }
        cursor.close();
        return lastExercise;
    }

    // Récupération du total de calories de l'historique
    public int getAllCalories(){
        String query = "SELECT SUM(" + COLUMN_CALORIES + ") FROM " + TABLE_HISTORIQUE;
        Cursor cursor = db.rawQuery(query, null);
        int totalCalories = 0;

        if (cursor.moveToFirst()) {
            totalCalories = cursor.getInt(0);
        }
        cursor.close();
        return totalCalories;
    }

    // Classe pour représenter les comptes
    public class Compte {
        private int id;
        private String login;
        private String email;
        private String telephone;
        private int age;
        private int poids;
        private int taille;
        private String sexe;
        private boolean isCompleted;
        private int stepsObjective;
        private int caloriesObjective;

        // Constructeur pour initialiser un compte
        public Compte(int id, String login, String email, String telephone, int age,int poids, int taille, String sexe, boolean isCompleted, int stepsObjective, int caloriesObjective) {
            this.id = id;
            this.login = login;
            this.email = email;
            this.telephone = telephone;
            this.age = age;
            this.poids = poids;
            this.taille = taille;
            this.sexe = sexe;
            this.isCompleted = isCompleted;
            this.stepsObjective = stepsObjective;
            this.caloriesObjective = caloriesObjective;
        }

        // Getters pour accéder aux attributs du compte
        public int getId() { return id; }
        public String getLogin() { return login; }
        public String getEmail() { return email; }
        public String getTelephone() { return telephone; }
        public int getAge() { return age; }
        public int getPoids() { return poids; }
        public int getTaille() { return taille; }
        public String getSexe() { return sexe; }
        public boolean isCompleted() { return isCompleted; }
        public int getStepsObjective() { return stepsObjective; }
        public int getCaloriesObjective() { return caloriesObjective; }
    }

    // Méthode pour récupérer un compte par son ID
    public Compte getCompteById(int id) {
        String query = "SELECT * FROM " + TABLE_COMPTE + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        Compte compte = null;

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
            int loginIndex = cursor.getColumnIndexOrThrow(COLUMN_LOGIN);
            int emailIndex = cursor.getColumnIndexOrThrow(COLUMN_EMAIL);
            int telephoneIndex = cursor.getColumnIndexOrThrow(COLUMN_TELEPHONE);
            int ageIndex = cursor.getColumnIndexOrThrow(COLUMN_AGE);
            int poidsIndex = cursor.getColumnIndexOrThrow(COLUMN_POIDS);
            int tailleIndex = cursor.getColumnIndexOrThrow(COLUMN_TAILLE);
            int sexeIndex = cursor.getColumnIndexOrThrow(COLUMN_SEXE);
            int isCompletedIndex = cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED);
            int stepsObjectiveIndex = cursor.getColumnIndexOrThrow(COLUMN_STEPS_OBJECTIVE);
            int caloriesObjectiveIndex = cursor.getColumnIndexOrThrow(COLUMN_CALORIES_OBJECTIVE);

            compte = new Compte(
                    cursor.getInt(idIndex),
                    cursor.getString(loginIndex),
                    cursor.getString(emailIndex),
                    cursor.getString(telephoneIndex),
                    cursor.getInt(ageIndex),
                    cursor.getInt(poidsIndex),
                    cursor.getInt(tailleIndex),
                    cursor.getString(sexeIndex),
                    cursor.getInt(isCompletedIndex) == 1,
                    cursor.getInt(stepsObjectiveIndex),
                    cursor.getInt(caloriesObjectiveIndex)
            );
        }

        cursor.close();
        return compte;
    }


}
