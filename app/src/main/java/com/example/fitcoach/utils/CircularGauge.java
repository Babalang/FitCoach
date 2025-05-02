package com.example.fitcoach.utils;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.fitcoach.R;

public class CircularGauge extends View {

    private int value = 0;
    private int total = 100;

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
        paint.setColor(ContextCompat.getColor(getContext(), R.color.stepGauge));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(100f);
        paint.setStrokeCap(Paint.Cap.ROUND);

        plein.setColor(ContextCompat.getColor(getContext(), R.color.gray));
        plein.setStyle(Paint.Style.STROKE);
        plein.setStrokeWidth(40f);
        plein.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        textPaint.setTextSize(60f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setValue(int value) {
        this.value = value;
        invalidate();
    }

    public void setTotal(int total) {
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
        int angle = (int)(((float)value /(float)total) * 220);
        canvas.drawArc(rect, -200, 220, false, plein);
        canvas.drawArc(rect, -200, angle, false, paint);
        canvas.drawText(value+"/"+total, rect.centerX(), rect.centerY(),textPaint);
    }
}