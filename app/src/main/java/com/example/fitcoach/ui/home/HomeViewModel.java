package com.example.fitcoach.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<Integer> stepCount = new MutableLiveData<>(0);

    public LiveData<Integer> getStepCount() {
        return stepCount;
    }

    public void setStepCount(int steps) {
        stepCount.setValue(steps);
    }

    public void incrementSteps(int value) {
        Integer current = stepCount.getValue();
        stepCount.setValue((current != null ? current : 0) + value);
    }
}
