package com.example.fitcoach.ui.Music;
// ViewModel pour la musique
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MusicViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    // Constructeur pour initialiser le ViewModel avec un texte par défaut
    public MusicViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Music fragment");
    }

    // Méthode pour obtenir le texte en tant que LiveData
    public LiveData<String> getText() {
        return mText;
    }
}