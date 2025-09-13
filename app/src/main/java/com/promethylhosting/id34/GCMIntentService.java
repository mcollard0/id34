package com.promethylhosting.id34;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

// import com.google.android.gcm.GCMBaseIntentService; // Disabled for minimal build
import android.app.IntentService;
import android.content.Intent;
import com.promethylhosting.id34.iserver.Iserver;

public class GCMIntentService extends IntentService  {

	public static String LOG_TAG = "ID34"; 
	public static SharedPreferences prefs ;
	public static String sSenderID = "**REDACTED**"; // See .secrets file for actual credentials
	
	public GCMIntentService() {
		   super(sSenderID);
		   Log.d(LOG_TAG + "_GCMIntentService", "Google Cloud Messaging Class GCMIntentService created.");
	}
	
   public GCMIntentService(String senderId) {
	   super(sSenderID);
	   Log.d(LOG_TAG + "_GCMIntentService", "Google Cloud Messaging Class GCMIntentService created." + senderId);
    }

    // GCM functionality disabled for minimal build
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "GCMIntentService: GCM functionality disabled in minimal build");
        // Original GCM methods converted to no-ops for minimal build compatibility
    }
    
    // Legacy GCM methods - disabled for minimal build
    public void onRegistered(Context context, String regId) {
        Log.i(LOG_TAG, "GCM onRegistered - disabled");
    }
    
    public void onUnregistered(Context context, String regId) {
        Log.i(LOG_TAG, "GCM onUnregistered - disabled");
    }
    
    public void onMessage(Context context, Intent intent) {
        Log.i(LOG_TAG, "GCM onMessage - disabled");
    }
    
    public void onError(Context context, String errorId) {
        Log.i(LOG_TAG, "GCM onError - disabled");
    }
    
    public boolean onRecoverableError(Context context, String errorId) {
        Log.i(LOG_TAG, "GCM onRecoverableError - disabled");
        return false;
    }
}
