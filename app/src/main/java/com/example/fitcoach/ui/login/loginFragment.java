package com.example.fitcoach.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fitcoach.Datas.AppDataManager;
import com.example.fitcoach.R;

public class loginFragment extends Fragment {
    private long coords[];
    private AppDataManager appDataManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appDataManager = AppDataManager.getInstance(getContext());
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_infos, container, false);
        setHasOptionsMenu(true);
        int id = appDataManager.getCompteId();
        AppDataManager.Compte compte = appDataManager.getCompteById(id);
        EditText login = view.findViewById(R.id.input_login);
        login.setText(compte.getLogin());
        EditText email = view.findViewById(R.id.input_email);
        email.setText(compte.getEmail());
        EditText phone = view.findViewById(R.id.input_phone);
        phone.setText(compte.getTelephone());
        EditText age = view.findViewById(R.id.input_age);
        age.setText(String.valueOf(compte.getAge()));
        EditText weight = view.findViewById(R.id.input_weight);
        weight.setText(String.valueOf(compte.getPoids()));
        RadioGroup gender = view.findViewById(R.id.radio_group_gender);
        if (compte.getSexe().equals("Homme")) {
            gender.check(R.id.radio_male);
        } else {
            gender.check(R.id.radio_female);
        }
        EditText stepGoal = view.findViewById(R.id.input_step_goal);
        stepGoal.setText(String.valueOf(compte.getStepsObjective()));
        EditText calorieGoal = view.findViewById(R.id.input_calorie_goal);
        calorieGoal.setText(String.valueOf(compte.getCaloriesObjective()));
        EditText size = view.findViewById(R.id.input_size);
        size.setText(String.valueOf(compte.getTaille()));
        int selectedId = gender.getCheckedRadioButtonId();
        Button btn1 = view.findViewById(R.id.save_button);
        btn1.setOnClickListener(v -> {
            if(!login.getText().toString().isEmpty()
            && !email.getText().toString().isEmpty()
            && !phone.getText().toString().isEmpty()
            && !age.getText().toString().isEmpty()
            && !weight.getText().toString().isEmpty()
            && !stepGoal.getText().toString().isEmpty()
            && !calorieGoal.getText().toString().isEmpty()
           && !size.getText().toString().isEmpty() ){
                int selectedId1 = gender.getCheckedRadioButtonId();
                String sexe = "";
                if (selectedId1 == R.id.radio_male) {
                    sexe = "Homme";
                } else if (selectedId1 == R.id.radio_female) {
                    sexe = "Femme";
                }
                appDataManager.updateCompte(id,login.getText().toString(), email.getText().toString(), phone.getText().toString(), Integer.parseInt(age.getText().toString()), sexe, Integer.parseInt(stepGoal.getText().toString()), Integer.parseInt(calorieGoal.getText().toString()), Integer.parseInt(size.getText().toString()), Float.parseFloat(weight.getText().toString()));
                // Naviguer vers le fragment suivant avec les coordonnées
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.infos_to_home);
            } else {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Action de retour personnalisée (ou juste revenir)
            NavHostFragment.findNavController(this)
                    .navigate(R.id.infos_to_home);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
