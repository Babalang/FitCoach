package com.example.fitcoach.ui.history;
// ViewModel pour l'historique des exercices
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HistoryViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    // Constructeur pour initialiser le ViewModel avec un texte par défaut
    public HistoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Hello");
    }

    // Méthode pour obtenir le texte en tant que LiveData
    public LiveData<String> getText() {
        return mText;
    }
}