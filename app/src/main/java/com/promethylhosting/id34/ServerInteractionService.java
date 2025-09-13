package com.promethylhosting.id34;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class ServerInteractionService extends Service {
	
	private SharedPreferences prefs;
	private static Context context = null;
	private SQLiteAdapter sql = null;
	private static String LOG_TAG = "ID34";

	private static Boolean bRefresh = false;
	
	public ServerInteractionService() {
		// TODO Auto-generated constructor stub
        Log.d(LOG_TAG, "Service contstructor");

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
        Log.d(LOG_TAG, "ServerInteractionService BIND!");
		
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LOG_TAG, "Service onStartCommand");
		if (intent.hasExtra("bRefresh")) { bRefresh = true; }
		workerThread();
		return Service.START_STICKY;
	}

	public void workerThread() {
        context = getApplicationContext(); 
        
        prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
        
	    Thread thSyncServerToDb = new Thread() {
	        @Override
	        public void run() {
	            try {
	            	Thread.sleep(500);
	            	
	            	prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
	            	
	            	String lastUpdate = prefs.getString("LastUpdateSuccessDT", "1970-01-01 00:06:00");
	            	Log.i(LOG_TAG, "Service giant wakes up. Getting ready to update... " + lastUpdate);
	            	
	                sql = new SQLiteAdapter(context);
	                sql.openToWrite();
	                Boolean bError1 = sql.updateDBCats(lastUpdate);
	                Boolean bError2 = sql.updateDBIdeas(lastUpdate); // TEST THIS ~~~ /// TODO: 
	                sql.close();

	                Log.e(LOG_TAG, "Sync Ran: bErr1:" + bError1 + " bErr2:" + bError2);
	                
	                if (!bError1 & !bError2) {
	                	lastUpdate =  sql.getServerDateTime();
	                	if (lastUpdate.length()>8) { // only update with good date
	                		prefs.edit().putString("LastUpdateSuccessDT", sql.getServerDateTime()).commit();
	                	}

		                // allow refresh only if the update succeeded -- send intent back to activity here if it ran as a reuslt of a command...
		                if (bRefresh) {
		                	Log.i(LOG_TAG, "Sending refresh request to activity.");
		                	//TODO: Implement broadcast reciever in activity instead of launching a whole new activity
		                    Intent intentReturn = new Intent(ServerInteractionService.this, IdeaListActivity.class);
		                    intentReturn.putExtra("bRefresh", true);
		                    intentReturn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		                    startActivity(intentReturn);
		                }

	                } else {
		                prefs.edit().putString("LastUpdateFailDT", sql.getServerDateTime()).commit();
	                }
	                
	            } catch(Exception e) {
	                // do nothing
	            	Log.e(LOG_TAG, e.getMessage());
	            } 
	        
	        
	            stopServer();
	        }
	    };
	    thSyncServerToDb.start();

		
	}
	
    @Override
    public void onStart(Intent intent, int startId) { // in theory two of these should not able to run at the same time
        // TODO Auto-generated method stub
        
        Log.d(LOG_TAG, "ServerInteractionService started");
        super.onStart(intent, startId);
    }
    
    public void stopServer() { this.stopSelf(); }
    
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(LOG_TAG, "ServerInteractionService destroyed");
    }
}
