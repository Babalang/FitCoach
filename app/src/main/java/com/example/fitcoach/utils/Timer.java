package com.example.fitcoach.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.fitcoach.R;
// Classe pour afficher un minuteur avec des fonctionnalités de chronomètre et de compte à rebours
public class Timer extends View {
    // Interface pour écouter les événements de fin de minuteur
    public interface TimerListener {
        void onTimerFinished();
    }

    private boolean isRunning = false;
    private boolean isCountdown = false;
    private long totalTime = 0;
    private long elapsedTime = 0;
    private final Handler handler = new Handler();
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private TimerListener listener;

    // Constructeurs pour initialiser le minuteur
    public Timer(Context context) {
        super(context);
        init();
    }

    // Constructeur avec attributs personnalisés
    public Timer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Initialisation avec attributs personnalisés et style
    private void init() {
        progressPaint.setColor(ContextCompat.getColor(getContext(), R.color.stepGauge));
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(40f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(40f);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        textPaint.setTextSize(64f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    // Runnable pour mettre à jour le minuteur toutes les secondes
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                if (isCountdown) {
                    if (elapsedTime > 0) {
                        elapsedTime--;
                        invalidate();
                        handler.postDelayed(this, 1000);
                    } else {
                        stop();
                        if (listener != null) listener.onTimerFinished();
                    }
                } else {
                    elapsedTime++;
                    invalidate();
                    handler.postDelayed(this, 1000);
                }
            }
        }
    };

    // Méthode pour démarrer le minuteur
    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(timerRunnable);
        }
    }

    // Méthode pour arrêter le minuteur
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

    // Méthode pour réinitialiser le minuteur
    public void reset() {
        stop();
        elapsedTime = isCountdown ? totalTime : 0;
        invalidate();
    }

    // Méthode pour définir le minuteur en mode compte à rebours
    public void setCountdown(long seconds) {
        isCountdown = true;
        totalTime = seconds;
        elapsedTime = seconds;
        invalidate();
    }

    // Méthode pour définir le minuteur en mode chronomètre
    public void setChronoMode() {
        isCountdown = false;
        elapsedTime = 0;
        invalidate();
    }

    // Méthode pour définir un écouteur pour les événements de fin de minuteur
    public void setTimerListener(TimerListener listener) {
        this.listener = listener;
    }

    // Méthodes pour vérifier l'état du minuteur
    public boolean isRunning() {
        return isRunning;
    }

    // Méthode pour récupérer le temps écoulé au format "mm:ss"
    public String getElapsedTime() {
        long time = isCountdown ? elapsedTime : elapsedTime;
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    // Méthode pour définir le temps écoulé manuellement
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
        invalidate();
    }

    // Méthode pour dessiner le minuteur sur le canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - 40f;
        float centerX = width / 2f;
        float centerY = height / 2f;

        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        canvas.drawArc(rect, 0, 360, false, backgroundPaint);

        float sweepAngle;
        if (isCountdown && totalTime > 0) {
            sweepAngle = 360f * (1f - ((float) elapsedTime / totalTime));
        } else {
            sweepAngle = 360f * ((elapsedTime % 60f) / 60f);
        }

        SweepGradient sweepGradient = new SweepGradient(
                centerX,
                centerY,
                new int[]{Color.parseColor("#80FF4081"), Color.parseColor("#FF4081")},
                null
        );
        Matrix gradientMatrix = new Matrix();
        gradientMatrix.postRotate(-95, centerX, centerY);
        sweepGradient.setLocalMatrix(gradientMatrix);
        progressPaint.setShader(sweepGradient);

        canvas.drawArc(rect, -90, sweepAngle, false, progressPaint);
        canvas.drawText(getElapsedTime(), centerX, centerY + 20f, textPaint);
    }
}
