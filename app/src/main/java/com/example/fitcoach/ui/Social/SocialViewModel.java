package com.example.fitcoach.ui.Social;
// ViewModel pour la section sociale de l'application FitCoach
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SocialViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    // Constructeur pour initialiser le ViewModel avec un texte par défaut
    public SocialViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Social fragment");
    }

    // Méthode pour obtenir le texte en tant que LiveData
    public LiveData<String> getText() {
        return mText;
    }
}