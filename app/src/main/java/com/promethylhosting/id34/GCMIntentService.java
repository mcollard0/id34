package com.promethylhosting.id34;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.promethylhosting.id34.iserver.Iserver;

public class GCMIntentService extends GCMBaseIntentService  {

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

    @Override
	public void onRegistered(Context context, String regId) {
		Log.e(LOG_TAG, "Registered... what now? Need to store " + regId) ;
		prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);	
        Editor editor = prefs.edit(); 
        editor.putString("gcmRegID", regId);
        Iserver.init(getApplicationContext()); // manual init so we know it has our context, possible race condition with master activity
        String strResult = Iserver.updateGCMuid(regId); // update GCM UID on mySQL Server.
        Log.e(LOG_TAG, "GCM register at server says: "+ strResult);
        editor.apply(); // safe replacement of commit()
	}

    @Override
	public void onUnregistered(Context context, String regId) {
		Log.e(LOG_TAG, "UN-Registered... Removing this regid from the settings: " + regId) ;
		prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);	
        Editor editor = prefs.edit(); 
        editor.putString("gcmRegID", "");
        editor.apply();
	}
	
    @Override
	public void onMessage(Context context, Intent intent) {
		Bundle b = intent.getExtras();
		String message ="";
		
		if (intent.hasExtra("message")) message = intent.getStringExtra("message");
		Log.e(LOG_TAG, "Got message " + message + ", starting service");
		
		context.startService(new Intent(context, ServerInteractionService.class)); // process data 
	}

    @Override
	public void onError(Context context, String errorId) {
    	Log.e(LOG_TAG, "GCM.ERROR: " + errorId + "!'");
    }
    
    @Override
	public boolean onRecoverableError(Context context, String errorId) { 
    	Log.w(LOG_TAG, "GCM.Recoverable ERROR: " + errorId + ".");
    	return false; 
    }
}
