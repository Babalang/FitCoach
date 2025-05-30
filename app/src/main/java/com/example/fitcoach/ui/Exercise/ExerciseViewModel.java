package com.example.fitcoach.ui.Exercise;
// Classe pour le ViewModel de l'exercice
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ExerciseViewModel extends ViewModel {
    // MutableLiveData pour stocker les données de l'exercice avec des getters pour l'accès en lecture et des setters pour la mise à jour des données
    private final MutableLiveData<ArrayList<ExerciseStep>> _steps = new MutableLiveData<>();
    public LiveData<ArrayList<ExerciseStep>> getSteps() {
        return _steps;
    }

    private final MutableLiveData<Integer> _currentStep = new MutableLiveData<>();
    public LiveData<Integer> getCurrentStep() {
        return _currentStep;
    }

    private final MutableLiveData<Float> _distance = new MutableLiveData<>();
    public LiveData<Float> getDistance() {
        return _distance;
    }

    private final MutableLiveData<Float> _speed = new MutableLiveData<>();
    public LiveData<Float> getSpeed() {
        return _speed;
    }

    private final MutableLiveData<Long> _duration = new MutableLiveData<>();
    public LiveData<Long> getDuration() {
        return _duration;
    }

    private final MutableLiveData<Float> _calories = new MutableLiveData<>();
    public LiveData<Float> getCalories() {
        return _calories;
    }

    private final MutableLiveData<String> _sportType = new MutableLiveData<>();
    public LiveData<String> getSportType() {
        return _sportType;
    }

    private final MutableLiveData<String> _exerciseType = new MutableLiveData<>();
    public LiveData<String> getExerciseType() {
        return _exerciseType;
    }

    private final MutableLiveData<Boolean> _isChronoMode = new MutableLiveData<>();
    public LiveData<Boolean> getIsChronoMode() {
        return _isChronoMode;
    }

    private final MutableLiveData<Integer> _currentStepIndex = new MutableLiveData<>(0);
    public LiveData<Integer> getCurrentStepIndex() {
        return _currentStepIndex;
    }

    private final MutableLiveData<Boolean> _isPaused = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsPaused() {
        return _isPaused;
    }
    public void setIsPaused(boolean isPaused) {
        _isPaused.setValue(isPaused);
    }


    public void setSteps(ArrayList<ExerciseStep> stepList) {
        _steps.setValue(stepList);
    }

    public void setCurrentStep(int step) {
        _currentStep.setValue(step);
    }

    public void setDistance(float dist) {
        _distance.setValue(dist);
    }

    public void setSpeed(float currentSpeed) {
        _speed.setValue(currentSpeed);
    }

    public void setDuration(long totalDuration) {
        _duration.setValue(totalDuration);
    }

    public void setCalories(float cal) {
        _calories.setValue(cal);
    }

    public void setExerciseType(String type) {
        _exerciseType.setValue(type);
    }

    public void setSportType(String type) {
        _sportType.setValue(type);
    }

    public void setExerciseDetails(String sport, boolean isChrono) {
        _sportType.setValue(sport);
        _isChronoMode.setValue(isChrono);
    }

    public void setCurrentStepIndex(int index) {
        _currentStepIndex.setValue(index);

    }

    // Incremente l'inderex de l'étape actuelle si possible
    public void incrementStep() {
        Integer current = _currentStepIndex.getValue();
        if (current != null && _steps.getValue() != null && current + 1 < _steps.getValue().size()) {
           _currentStepIndex.setValue(current + 1);
        }
    }

    // Réinitialise les données de l'exercice
    public void resetExerciseData() {
        _steps.setValue(new ArrayList<>());
        _distance.setValue(0.0f);
        _speed.setValue(0.0f);
        _duration.setValue(0L);
        _calories.setValue(0.0f);
    }

}