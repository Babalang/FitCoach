package com.example.fitcoach.ui.login;
// Classe pour gérer l'activité de connexion et de création de compte
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.Serveur.ApiService;
import com.example.fitcoach.Serveur.RetrofitClient;
import com.example.fitcoach.Serveur.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class loginActivity extends AppCompatActivity {
    private AppDataManager appDataManager;
    private boolean bonLogin = false;

    // Méthode pour créer un compte sur le serveur et localement
    private void creerCompteServeur(String nom){
        ApiService apiService = RetrofitClient.getInstance();
        Call<User> call = apiService.create(nom);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    creerCompteLocal();
                } else {
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                creerCompteLocal();
            }
        });
    }

    // Méthode pour créer un compte localement
    private void creerCompteLocal(){
        EditText login = findViewById(R.id.input_login);
        EditText email = findViewById(R.id.input_email);
        EditText phone = findViewById(R.id.input_phone);
        EditText age = findViewById(R.id.input_age);
        EditText weight = findViewById(R.id.input_weight);
        EditText size = findViewById(R.id.input_size);
        RadioGroup genderGroup = findViewById(R.id.radio_group_gender);
        int selectedId = genderGroup.getCheckedRadioButtonId();
        String sexe = "";
        if (selectedId == R.id.radio_male) {
            sexe = "Homme";
        } else {
            sexe = "Femme";
        }
        EditText stepGoal = findViewById(R.id.input_step_goal);
        EditText calorieGoal = findViewById(R.id.input_calorie_goal);
        Button btn1 = findViewById(R.id.save_button);
        String finalSexe = sexe;
        int sizeVal = Integer.parseInt(size.getText().toString().trim());
        int ageVal = Integer.parseInt(age.getText().toString().trim());
        int stepGoalVal = Integer.parseInt(stepGoal.getText().toString().trim());
        int calorieGoalVal = Integer.parseInt(calorieGoal.getText().toString().trim());
        appDataManager.updateCompte(appDataManager.getCompteId(),
                login.getText().toString().trim(),
                email.getText().toString().trim(),
                phone.getText().toString().trim(),
                ageVal,
                finalSexe,
                stepGoalVal,
                calorieGoalVal,
                sizeVal,
                Float.parseFloat(weight.getText().toString().trim())
        );
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Méthode à la création de l'activité pour initialiser les éléments de l'interface utilisateur et gérer les clics
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appDataManager = AppDataManager.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_infos);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        EditText login = findViewById(R.id.input_login);
        EditText email = findViewById(R.id.input_email);
        EditText phone = findViewById(R.id.input_phone);
        EditText age = findViewById(R.id.input_age);
        EditText weight = findViewById(R.id.input_weight);
        RadioGroup genderGroup = findViewById(R.id.radio_group_gender);
        int selectedId = genderGroup.getCheckedRadioButtonId();
        String sexe = "";
        if (selectedId == R.id.radio_male) {
            sexe = "Homme";
        } else {
            sexe = "Femme";
        }
        EditText stepGoal = findViewById(R.id.input_step_goal);
        EditText calorieGoal = findViewById(R.id.input_calorie_goal);
        Button btn1 = findViewById(R.id.save_button);
        String finalSexe = sexe;
        btn1.setOnClickListener(v -> {
            try {
                creerCompteServeur(login.getText().toString().trim());
            } catch (NumberFormatException e) {
                Log.e("loginActivity", "Error parsing input fields", e);
            }
        });
    }
}
