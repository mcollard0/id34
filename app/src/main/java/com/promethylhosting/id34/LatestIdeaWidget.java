package com.promethylhosting.id34;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Latest Idea Widget - displays the most recent id34 idea
 * When tapped, launches the IdeaAddActivity to add a new idea
 */
public class LatestIdeaWidget extends AppWidgetProvider {
    
    private static final String LOG_TAG = "LatestIdeaWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(LOG_TAG, "onUpdate called for " + appWidgetIds.length + " widgets");
        
        // Update each widget instance
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.i(LOG_TAG, "Widget enabled - first instance added");
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        Log.i(LOG_TAG, "Widget disabled - last instance removed");
        super.onDisabled(context);
    }

    /**
     * Update a single widget instance
     */
    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.i(LOG_TAG, "Updating widget ID: " + appWidgetId);
        
        // Create the RemoteViews object for the widget layout
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_latest_idea);
        
        // Set click handler - launch IdeaAddActivity when widget is tapped
        Intent addIntent = new Intent(context, IdeaAddActivity.class);
        addIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            appWidgetId, // Use widget ID as request code to make each widget unique
            addIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
        
        // Load and display the latest idea in background
        new LoadLatestIdeaTask(context, appWidgetManager, appWidgetId, views).execute();
    }
    
    /**
     * Force update all widget instances
     */
    public static void updateAllWidgets(Context context) {
        Log.i(LOG_TAG, "Updating all widget instances");
        
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
            new android.content.ComponentName(context, LatestIdeaWidget.class)
        );
        
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    /**
     * AsyncTask to load the latest idea from database without blocking UI
     */
    private static class LoadLatestIdeaTask extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final AppWidgetManager appWidgetManager;
        private final int appWidgetId;
        private final RemoteViews views;
        
        public LoadLatestIdeaTask(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views) {
            this.context = context;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetId = appWidgetId;
            this.views = views;
        }
        
        @Override
        protected String doInBackground(Void... params) {
            try {
                // Query database for most recent idea
                SQLCipherAdapter sql = new SQLCipherAdapter(context);
                sql.openToRead();
                
                String latestIdea = sql.getMostRecentIdeaText();
                sql.close();
                
                if (latestIdea != null && !latestIdea.trim().isEmpty()) {
                    return latestIdea.trim();
                } else {
                    return "No ideas yet - tap to add your first one!";
                }
                
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error loading latest idea: " + e.getMessage());
                return "Error loading idea - tap to add new one";
            }
        }
        
        @Override
        protected void onPostExecute(String latestIdeaText) {
            try {
                // Update the widget text
                views.setTextViewText(R.id.widget_idea_text, latestIdeaText);
                
                // Apply the updated RemoteViews to the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
                
                Log.i(LOG_TAG, "Widget updated with latest idea: " + latestIdeaText.substring(0, Math.min(50, latestIdeaText.length())) + "...");
                
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error updating widget: " + e.getMessage());
            }
        }
    }
}