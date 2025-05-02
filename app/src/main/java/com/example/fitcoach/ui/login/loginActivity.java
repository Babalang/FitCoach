package com.example.fitcoach.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;

public class loginActivity extends AppCompatActivity {
    private AppDataManager appDataManager;

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
        EditText gender = findViewById(R.id.input_gender);
        EditText stepGoal = findViewById(R.id.input_step_goal);
        EditText calorieGoal = findViewById(R.id.input_calorie_goal);
        Button btn1 = findViewById(R.id.save_button);
        btn1.setOnClickListener(v -> {
            try {
                int ageVal = Integer.parseInt(age.getText().toString().trim());
                int stepGoalVal = Integer.parseInt(stepGoal.getText().toString().trim());
                int calorieGoalVal = Integer.parseInt(calorieGoal.getText().toString().trim());

                appDataManager.updateCompte(appDataManager.getCompteId(),
                        login.getText().toString().trim(),
                        email.getText().toString().trim(),
                        phone.getText().toString().trim(),
                        ageVal,
                        gender.getText().toString().trim(),
                        stepGoalVal,
                        calorieGoalVal
                );

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Âge, objectif de pas ou de calories incorrect(s)", Toast.LENGTH_SHORT).show();
            }

        });
    }
}
