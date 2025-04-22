package com.example.fitcoach.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.fonts.FontStyle;
import android.util.AttributeSet;
import android.view.View;

public class CircularGauge extends View {

    private float value = 0f;
    private float total = 100f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plein = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public CircularGauge(Context context) {
        super(context);
        init();
    }

    public CircularGauge(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularGauge(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(40f);
        paint.setStrokeCap(Paint.Cap.ROUND);

        plein.setColor(Color.GRAY);
        plein.setStyle(Paint.Style.STROKE);
        plein.setStrokeWidth(40f);
        plein.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setValue(float value) {
        this.value = value;
        invalidate();
    }

    public void setTotal(float total) {
        this.total = total;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = paint.getStrokeWidth() / 2;
        float size = Math.min(w, h) - paint.getStrokeWidth();
        rect.set(padding, padding, size + padding, size + padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float angle = (value / total) * 220f;
        canvas.drawArc(rect, -200f, 220f, false, plein);
        canvas.drawArc(rect, -200f, angle, false, paint);
        canvas.drawText(value+"/"+total, rect.centerX(), rect.centerY(),textPaint);
    }
}