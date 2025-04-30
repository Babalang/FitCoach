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

public class Timer extends View {
    private long elapsedTime = 0; // en secondes
    private boolean isRunning = false;
    private final Handler handler = new Handler();
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public Timer(Context context) {
        super(context);
        init();
    }

    public Timer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

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

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime++;
                invalidate();
                handler.postDelayed(this, 1000);
            }
        }
    };

    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(timerRunnable);
        }
    }

    public void stop() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

    public void reset() {
        stop();
        elapsedTime = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f - 40f;
        float centerX = width / 2f;
        float centerY = height / 2f;

        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Dessiner le fond
        canvas.drawArc(rect, 0, 360, false, backgroundPaint);

        // Calculer l'angle de progression basé sur le temps écoulé
        float sweepAngle = (360f * (elapsedTime % 60)) / 60;

        // Dégradé avec des couleurs transparentes
        SweepGradient sweepGradient = new SweepGradient(
                centerX,
                centerY,
                new int[]{
                        Color.parseColor("#80FF4081"),  // Couleur avec transparence en début
                        Color.parseColor("#FF4081")     // Couleur opaque en fin
                },
                null
        );

        // Rotation pour aligner le dégradé avec l'arc
        Matrix gradientMatrix = new Matrix();
        gradientMatrix.postRotate(-95, centerX, centerY); // Commencer à 12h
        sweepGradient.setLocalMatrix(gradientMatrix);

        progressPaint.setShader(sweepGradient);

        // Dessiner l'arc de progression avec le dégradé
        canvas.drawArc(rect, -90, sweepAngle, false, progressPaint);

        // Dessiner le temps au centre
        String timeText = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
        canvas.drawText(timeText, centerX, centerY + 20f, textPaint);
    }
    public boolean isRunning() {
        return isRunning;
    }

    public String getElapsedTime() {
        return String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60);
    }
}
