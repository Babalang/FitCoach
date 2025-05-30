package com.example.fitcoach.ui.home;
// ViewModel pour la page d'accueil de l'application FitCoach
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> stepCount = new MutableLiveData<>(0);
    private final MutableLiveData<Float> calories = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> distance = new MutableLiveData<>(0f);

    // Getters et setters pour les données de pas, calories et distance
    public LiveData<Integer> getStepCount() {
        return stepCount;
    }
    public void setStepCount(int steps) {
        stepCount.setValue(steps);
    }
    public void setCalories(float cal) {
        calories.setValue(cal);
    }
    public void setDistance(float dist) {
        distance.setValue(dist);
    }
    public LiveData<Float> getCalories() {
        return calories;
    }
    public LiveData<Float> getDistance() {
        return distance;
    }

    // Méthode pour incrémenter le nombre de pas
    public void incrementSteps(int value) {
        Integer current = stepCount.getValue();
        stepCount.setValue((current != null ? current : 0) + value);
    }
}
