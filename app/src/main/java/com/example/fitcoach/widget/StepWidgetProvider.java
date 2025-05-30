package com.example.fitcoach.widget;
// Classe pour le widget de compteur de pas
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.fitcoach.MainActivity;
import com.example.fitcoach.R;
import com.example.fitcoach.Services.StepCounterService;

public class StepWidgetProvider extends AppWidgetProvider {

    // Action pour mettre à jour le widget quand le service de compteur de pas envoie une mise à jour
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (StepCounterService.ACTION_STEP_COUNT_UPDATE.equals(intent.getAction())) {
            int stepCount = intent.getIntExtra(StepCounterService.EXTRA_STEP_COUNT, 0);
            updateWidget(context, stepCount);
        }
    }

    // Méthode appelée lors de la mise à jour du widget
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE);
        int steps = prefs.getInt("current_steps", 0);

        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_step);
            views.setTextViewText(R.id.steps_text, steps + " pas");
            views.setProgressBar(R.id.step_progress_bar, 10000, steps, false);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }


    // Méthode appelée pour mettre à jour le widget avec le nombre de pas
    private void updateWidget(Context context, int stepCount) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_step);
        views.setTextViewText(R.id.steps_text, stepCount+" pas");
        views.setProgressBar(R.id.step_progress_bar, 10000, stepCount, false);
        ComponentName thisWidget = new ComponentName(context, StepWidgetProvider.class);
        appWidgetManager.updateAppWidget(thisWidget, views);
    }
}
