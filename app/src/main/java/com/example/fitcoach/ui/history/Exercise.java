package com.example.fitcoach.ui.history;

public class Exercise {
    private String date;
    private String sport;
    private int duration;
    private int steps;

    public Exercise(String date, String sport, int duration, int steps) {
        this.date = date;
        this.sport = sport;
        this.duration = duration;
        this.steps = steps;
    }

    public String getDate() { return date; }
    public String getSport() { return sport; }
    public int getDuration() { return duration; }
    public int getSteps() { return steps; }
}
