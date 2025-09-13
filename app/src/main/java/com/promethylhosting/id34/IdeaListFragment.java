package com.promethylhosting.id34;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListFragment;
import android.widget.SimpleCursorAdapter;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.promethylhosting.id34.dummy.DummyContent;
import com.promethylhosting.id34.dummy.DummyContent.DummyItem;
import com.promethylhosting.id34.iserver.Iserver;


public class IdeaListFragment extends ListFragment {

	
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static String LOG_TAG = "Id34";
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private String mPhoneNumber;
    private static Context context;
    //private static ArrayAdapter aaHashTags;
    private SharedPreferences prefs;
    private static ProgressDialog progressDialog;
	protected static final int CONTEXTMENU_EDITITEM = 1; 
	protected static final int CONTEXTMENU_DELETEITEM = 0; 
    private static Boolean bSys_debug=false;
    private SQLCipherAdapter sql = null;

    MenuItem mnuAdd = null;
    MenuItem mnuRefresh = null; 
    
    public interface Callbacks {

        public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    public IdeaListFragment() {
    }

    @Override
    public void onDestroy() {
    	try {
			sql.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	super.onDestroy();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // cannot remove this until the phone number is removed from the url passed here
        try {
        	TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);    
        	mPhoneNumber = tMgr.getLine1Number();
        } catch (Exception e) {
        	//TODO use dialog to get phone number or email
        	Log.i(LOG_TAG, "Was unable to get the phone number.");
        }
        
        prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
        prefs.edit().putString("mPhoneNumber", mPhoneNumber).commit();
        mPhoneNumber = prefs.getString("mPhoneNumber", "");
        
        
        // automatic on GCM message
        //context.startService(new Intent(context, ServerInteractionService.class)); // run the updater by hand for now
        
        /*
	    Thread thUpdateDBCats = new Thread() {
	        @Override
	        public void run() {
	            try {
	            	
	            	prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
	            	
	            	String lastUpdate = prefs.getString("LastUpdateSuccessDT", "1970-01-01 00:06:00");
	            	
	            	
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
	                } else {
		                prefs.edit().putString("LastUpdateFailDT", sql.getServerDateTime()).commit();
	                }
	                
	            } catch(Exception e) {
	                // do nothing
	            	Log.e(LOG_TAG, e.getMessage());
	            } 
	        }
	    };
	    thUpdateDBCats.start();
        */
	    
	    //progressDialog = ProgressDialog.show(getActivity(), "", "Loading...");
	    /*
        DummyContent dcHashTags = new DummyContent();
        aaHashTags =new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                R.layout.simple_list_item_activated_1,
                R.id.text1,
                dcHashTags.ITEMS);
        */

        Cursor mCursor = loadDataFromDatabase();
        
        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        ListAdapter adapter = new SimpleCursorAdapter(context, // Context.
            //android.R.layout.two_line_list_item, 
            R.layout.fragment_idea_list,// Specify the row template
                                // to use (here, two
                                // columns bound to the
                                // two retrieved cursor
                                // rows).
            mCursor, // Pass in the cursor to bind to.
            // Array of cursor columns to bind to.
            new String[] { "cat" },
            // Parallel array of which template objects to bind to those
            // columns.
            new int[] { R.id.tvListItemName }, 0 ); // initialized with zero flags in order to run on seperate thread
        
        setListAdapter(adapter);
        
        //setListAdapter(aaHashTags);
        //getData(dcHashTags);
        //aaHashTags.notifyDataSetChanged();
        setHasOptionsMenu(true);
        //progressDialogCancel();
        //if (progressDialog != null) progressDialog.cancel();
    }
    
    

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        mnuAdd = menu.add("add");
        mnuAdd.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        mnuRefresh = menu.add("Refresh");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)   { 	
	   	if(item.getItemId() == mnuAdd.getItemId()) {
	   		Log.i(LOG_TAG, "Launch Add Activity...");
	   		
	   		startActivity(new Intent(context, IdeaAddActivity.class));
	   		return true;
	   	} else if ( item.getItemId() == mnuRefresh.getItemId()){
			context.startService(new Intent(context, ServerInteractionService.class)); // process data 
			return true;
	   		
	   	} else { 
	   		return false;
	   	}
   	}
    
    @Override
    public void onResume() {
    	super.onResume();
    	Intent i=getActivity().getIntent(); // needs to be tested TODO
  	    if (i.hasExtra("bRefresh")) {
  	    	onListChanged();
  	    }
        ListView listView1 = getListView(); 
        listView1.setOnCreateContextMenuListener(new OnCreateContextMenuListener() { 
			@Override 
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { 
			menu.setHeaderTitle("Action Menu");
			menu.add(0, CONTEXTMENU_EDITITEM, 0, "Edit this hashtag");
			menu.add(0, CONTEXTMENU_DELETEITEM, 1, "Delete this hashtag"); 
		} });
    
    } 
    
    public static void progressDialogCancel() { progressDialog.cancel(); }
    
    public static void onListChanged() {
    	Log.e(LOG_TAG, "onListChanged fired, what do with it ?"); // TODO: ???
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState
                .containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
    	context = activity;
        
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        Cursor c= (Cursor) listView.getAdapter().getItem(position);
        c.moveToPosition(position);
        
        mCallbacks.onItemSelected(c.getString(0)); // needs testing, supply id, will query for it next iteration
        //mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
    
	@Override 
	public boolean onContextItemSelected(MenuItem aItem) { 
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo(); 
		/* Switch on the ID of the item, to get what the user selected. */
		
		switch (aItem.getItemId()) { 
			case CONTEXTMENU_EDITITEM:
				//menuInfo.position;
				//aaHashTags.notifyDataSetChanged();
				return true;  
			case CONTEXTMENU_DELETEITEM: 
				//menuInfo.position; 
				/* Remove it from the list. */ 
				//aaHashTags.notifyDataSetChanged();
				return true; /* true means: "we handled the event". */ 
		} 
		return false; 
	} 
    
	// does this need run on UI thread?
    public static void Toast(String msg) { Toast.makeText(context, msg, Toast.LENGTH_LONG).show(); }
    
    public Cursor loadDataFromDatabase() {
    	  
      		if (progressDialog != null) progressDialog = ProgressDialog.show(getActivity(), "", "Loading...");
      		try {
      			// Database migration disabled for minimal build
      			Log.i(LOG_TAG, "Database migration disabled - using direct SQLCipherAdapter initialization");
      			
      			// Use encrypted SQLCipherAdapter
      			sql = new SQLCipherAdapter(context);
      			sql.openToRead();
      			if (progressDialog != null) progressDialog.cancel();
      		} catch ( Exception e) {
      			Log.e(LOG_TAG, "loadDataFromDatabaseFailed: "  + e.getMessage());
      			return null;
      		}
      		return sql.queryCats();
    }
    
    public void getData(DummyContent dcHashTags) {
    	Log.i(LOG_TAG, "OFFLINE MODE: Loading data from local SQLCipher database");
    	// OFFLINE MODE: Load from local database instead of network
    	new getDataFromLocalDatabaseTask().execute(dcHashTags);
    }
    
    
    // NEW: AsyncTask that loads data from local SQLCipher database
    private class getDataFromLocalDatabaseTask extends AsyncTask<DummyContent, Void, Cursor> {
    	
    	DummyContent dcHashTags;
    	
    	@Override
    	protected Cursor doInBackground(DummyContent... params) {
    		Log.i(LOG_TAG, "OFFLINE MODE: Loading categories from SQLCipher database");
    		dcHashTags = params[0];
    		
    		try {
    			// Initialize database connection
    			if (sql == null) {
    				// Database migration disabled for minimal build
    				Log.i(LOG_TAG, "Database migration disabled - direct initialization");
    				
    			// Use encrypted SQLCipherAdapter
    			sql = new SQLCipherAdapter(context);
    			sql.openToRead();
    		}
    			
    			// Create demo item if database is empty
    			sql.createDemoItemIfEmpty();
    			
    			// Query categories from database
    			return sql.queryCats();
    			
    		} catch (Exception e) {
    			Log.e(LOG_TAG, "Error loading data from database: " + e.getMessage());
    			e.printStackTrace();
    			return null;
    		}
    	}
    	
    	protected void onPostExecute(Cursor cursor) {
    		Log.i(LOG_TAG, "OFFLINE MODE: Processing database results");
    		
    		if (cursor == null) {
    			Log.e(LOG_TAG, "Cursor is null - no data available");
    			progressDialogCancel();
    			return;
    		}
    		
    		try {
    			String cats[] = new String[500];
    			int i = 0;
    			
    			// Process cursor data
    			int nameIndex = cursor.getColumnIndex("name"); // KEY_CAT should map to "name"
    			if (nameIndex == -1) nameIndex = cursor.getColumnIndex("KEY_CAT");
    			if (nameIndex == -1) nameIndex = 1; // fallback to second column
    			
    			for (cursor.moveToFirst(); !cursor.isAfterLast() && i < 500; cursor.moveToNext()) {
    				String categoryName = cursor.getString(nameIndex);
    				if (categoryName != null && categoryName.length() > 0) {
    					Log.d(LOG_TAG, "Adding category: " + categoryName);
    					dcHashTags.addItem(new DummyItem(categoryName, categoryName, "false"));
    					cats[i] = categoryName;
    					i++;
    				}
    			}
    			
    			// Update Iserver hashtags
    			Iserver.setHashTags(Arrays.copyOf(cats, i));
    			Log.i(LOG_TAG, "OFFLINE MODE: Loaded " + i + " categories from database");
    			
    			// Refresh the UI
    			IdeaListFragment.onListChanged();
    			progressDialogCancel();
    			
    		} catch (Exception e) {
    			Log.e(LOG_TAG, "Error processing database cursor: " + e.getMessage());
    			e.printStackTrace();
    			progressDialogCancel();
    		} finally {
    			if (cursor != null) {
    				cursor.close();
    			}
    		}
    	}
    }
}
