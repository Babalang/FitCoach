package com.example.fitcoach.ui.Exercise;

import java.io.Serializable;

public class ExerciseStep implements Serializable {
    private String name;
    private int duration;

    public ExerciseStep(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() { return name; }
    public int getDuration() { return duration; }

    public void setName(String name) { this.name = name; }
    public void setDuration(int duration) { this.duration = duration; }
}
