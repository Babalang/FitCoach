package com.example.fitcoach.ui.Exercise;
// Classe pour modéliser une étape d'exercice
import java.io.Serializable;

public class ExerciseStep implements Serializable {
    private String name;
    private int duration;

    // Constructeur pour initialiser une étape d'exercice avec un nom et une durée
    public ExerciseStep(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    // Getters et setters pour les attributs de l'étape d'exercice
    public String getName() { return name; }
    public int getDuration() { return duration; }

    public void setName(String name) { this.name = name; }
    public void setDuration(int duration) { this.duration = duration; }
}
