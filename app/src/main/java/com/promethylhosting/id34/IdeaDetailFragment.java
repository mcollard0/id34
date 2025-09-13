package com.promethylhosting.id34;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.promethylhosting.id34.iserver.Iserver;

public class IdeaDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    public static Context context ;
    //DummyContent.DummyItem mItem;
    String mItem;
    private String mItemId =""; 
    final static String LOG_TAG="id34";
    //TextView TextView1;
    ListView ListView1;
    private static ProgressDialog progressDialog;
    //private static ArrayAdapter aaIdeas;
    //private static String saIdeas[];
    private static Boolean bDebug=false;
    private SQLCipherAdapter sql ;
    
	protected static final int CONTEXTMENU_COMPLETEITEM = 2;
	protected static final int CONTEXTMENU_EDITITEM = 1; 
	protected static final int CONTEXTMENU_DELETEITEM = 0; 
	protected static final int CONTEXTMENU_GOOGLEITEM = 3;
    
    public IdeaDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// CANNOT use context in this class
        super.onCreate(savedInstanceState);
        
        context = getActivity();
		sql = new SQLCipherAdapter(context);
		sql.openToRead();
		
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            //mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        	mItemId = getArguments().getString(ARG_ITEM_ID);
        	mItem = sql.getCatNameFromCatId(mItemId);
        }
        
        setHasOptionsMenu(true);
        //loadIdeas(); // removed call to server, using database internal
    }

    public void onStart() {
    	super.onStart();
    	context = getActivity();
    	
    }
    
    public void onResume() { // do I want to do it this way, or send an invalidation message?
    	super.onResume();
    	context = getActivity();
    	
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
    
    private Cursor loadIdeasFromDatabase() {
    		if (progressDialog != null) progressDialog = ProgressDialog.show(getActivity(), "", "Loading...");

    		if (progressDialog != null) progressDialog.cancel();
    		Log.d(LOG_TAG , "Loading ideas on " + mItem.toString());
    		return sql.queryIdeasByCatName(mItem.toString());
    }
    
    public void loadIdeas() {
    	if (mItem == null) { Toast("Item is empty, network down?"); return; } // possible if we get a bad load, no network? 
        Log.i(LOG_TAG, "Query server for " + mItem.toString());
        if (progressDialog != null) progressDialog = ProgressDialog.show(getActivity(), "", "Loading...");
    	new getStringFromRemoteTask().execute(mItem.toString(), context);
    }
    

    MenuItem mnuAdd = null;
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        mnuAdd = menu.add("add");
        mnuAdd.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)   { 	
	   	if(item.getItemId() == mnuAdd.getItemId()) {
	   		Log.i(LOG_TAG, "Add ID34..");
	   		startActivity(new Intent(context, IdeaAddActivity.class));
	   		return true;
	   	} else { 
	   		return false;
	   	}
   	}
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_idea_detail, container, false);
        
        ListView1 = ((ListView) rootView.findViewById(R.id.lv_idea_detail));
                
        Cursor mCursor = loadIdeasFromDatabase();
        
        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        
        ListAdapter adapter= new IDFListViewAdapter(context, mCursor, R.layout.fragment_idea_list, R.id.tvListItemName, "name");
        
        /*ListAdapter adapter_OLD_NEEDS_DELETIONS = new SimpleCursorAdapter(context, // Context.
            //android.R.layout.two_line_list_item, 
            R.layout.fragment_idea_list,// Specify the row template
                                // to use (here, two
                                // columns bound to the
                                // two retrieved cursor
                                // rows).
            mCursor, // Pass in the cursor to bind to.
            // Array of cursor columns to bind to.
            new String[] { "name" },
            // Parallel array of which template objects to bind to those
            // columns.
            new int[] { R.id.tvListItemName },0);
         */
        
        ListView1.setAdapter(adapter); // Bind to our new adapter.
        
        ListView1.setOnCreateContextMenuListener(new OnCreateContextMenuListener() { 
 			@Override 
 			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { 
 			menu.setHeaderTitle("Action Menu");
 			menu.add(0, CONTEXTMENU_GOOGLEITEM, 5, "Google this idea");
 			menu.add(0, CONTEXTMENU_EDITITEM, 10, "Edit this idea...");
 			menu.add(0, CONTEXTMENU_COMPLETEITEM, 20, "Complete this idea");
 			menu.add(0, CONTEXTMENU_DELETEITEM, 30, "Delete this idea");
 			
 			//menu.add(0, CONTEXTMENU_DELETEITEM, 2, "" + v.toString());
 			
 		} });

        ListView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long longTblIdea_Id) {
					//String ideaClicked = (String) ListView1.getAdapter().getItem(position);
					Log.i(LOG_TAG,"Clicked item " + position + "  SHould be database ID: "  + longTblIdea_Id);
			}
        });
        
        //TODO: REMOVE!!!! ThiS SHOULD BE AUTOMATED WITH GCM
        //context.startService(new Intent(context, ServerInteractionService.class)); // manually sync data from server 
        return rootView;
    }
    
	@Override 
	public boolean onContextItemSelected(final MenuItem aItem) {
		final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		
		/* Switch on the ID of the item, to get what the user selected. */
		//Cursor strIdeaContexted=null;
		//Cursor strIdeaContexted = (Cursor) ListView1.getAdapter().getItem(menuInfo.position);
		final String response = "";
		
		switch (aItem.getItemId()) { 
			case CONTEXTMENU_GOOGLEITEM:
				
				
				Thread thGetNameAndSearch = new Thread() {
			        @Override
			        public void run() {
			            try {
			            	String response = sql.getIdeaNameFromId(menuInfo.id);
							Log.i(LOG_TAG, "Google search on " + menuInfo.id + " " + response) ;
							
							Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
							intent.putExtra(SearchManager.QUERY, response ); // query contains search string
							startActivity(intent);

			            } catch(Exception e) {
			                // do nothing
			            	Log.e(LOG_TAG, "Error Posting message:  " + e.getMessage());
			            } 
			        }
			    }; thGetNameAndSearch.start();
				
			return true;
			
			case CONTEXTMENU_EDITITEM:
				/* Get the selected item out of the Adapter by its position. */
				Log.i(LOG_TAG, "Edit request on " + menuInfo.id) ;
				return true; /* true means: "we handled the event". */ 
			case CONTEXTMENU_COMPLETEITEM:
			    Thread thUpdateServerComplete = new Thread() {
			        @Override
			        public void run() {
			            try {
			            	String response = sql.toggleCompleted(menuInfo.id);
							Log.i(LOG_TAG, "Mark item Complete request on " + menuInfo.id + " " + response) ;
			            } catch(Exception e) {
			                // do nothing
			            	Log.e(LOG_TAG, "Error Posting message:  " + e.getMessage());
			            } 
			        }
			    };
			    thUpdateServerComplete.start();
				return true; /* true means: "we handled the event". */ 
			case CONTEXTMENU_DELETEITEM:
			    Thread thUpdateServerDelete = new Thread() {
			        @Override
			        public void run() {
			            try {
			            	String response = sql.toggleCompleted(menuInfo.id);
			            	Log.i(LOG_TAG, "Delete request on " + menuInfo.id + " " + response) ;
			            } catch(Exception e) {
			                // do nothing
			            	Log.e(LOG_TAG, "Error Posting message:  " + e.getMessage());
			            } 
			        }
			    };
			    thUpdateServerDelete.start();
			    return true; /* true means: "we handled the event". */ 
		} 
		return false; 
	} 
	
	
    public void Toast(String msg) {
    	if (context==null) return;
    	try {
    		/*IdeaDetailActivity.this.runOnUiThread(new Runnable() {
    			@Override
    		    public void run() {
    		        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
    		    }
    		});
    		*/
    		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();	
    	}catch (Exception e) {}
    }

    
    // OLDER STUFF:
    
	
    public void updateView(String[] result) {
    	//aaIdeas = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, result);
    	//Log.d(LOG_TAG, "updateView(" + aaIdeas.getCount()+")");
    	 //ListView1.setAdapter(aaIdeas);
    	//TextView1.setText(mItem.content + "\n\n"+ result);
    	progressDialogCancel();
    }
    
    public static void progressDialogCancel() { try {progressDialog.cancel(); } catch (Exception e) {} } 
    
    
    // about to be deprecated
    private class getStringFromRemoteTask extends AsyncTask<Object, Void, String> {
    	
    	@Override
    	protected String doInBackground(Object... params) {
    		
    		String hashtag = (String)params[0];
    		Context context = (Context)params[1];
    		 
    		Iserver.init(context);
    		
    		Log.d(LOG_TAG, "AsyncTask : pulling data from url: " + params[0] + "");
    		String result  = Iserver.getStringFromRemote("hash=" + params[0]);
    		
    		return result;
    	}
    	protected void onPostExecute (String result) {
    			if (bDebug) Log.d(LOG_TAG, result);
    			BufferedReader br = new BufferedReader(new StringReader(result));
    			String line;
    			//String result2="";
    			Log.i(LOG_TAG,"Initializing buffer");
    			String strBuffer[]= new String[1500];
				int intLineNumber = 0;
    				
    			try {
    				Log.i(LOG_TAG,"Processing downloaded data");
    				while ((line = br.readLine()) != null)  {
    				   Log.i(LOG_TAG, intLineNumber + " " + line);
    				   strBuffer[intLineNumber] = line;
    				   intLineNumber++;
    				   //DummyContent.addItem(new DummyItem("1", line));;
    				   //result2 = result2 + line + "\r\n\r\n";
    				}// IdeaListFragment.onListChanged();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} finally {
    				Log.e(LOG_TAG, "Updting textview");	
    				updateView(Arrays.copyOf(strBuffer, intLineNumber)); // does this trim?
    			}
    	}   
    }
    
    
    public class IDFListViewAdapter extends CursorAdapter {
    	
    	String column="";
    	int intTextViewName ;
    	int layout; 
    	
    	public IDFListViewAdapter(Context context, Cursor c, int layout, int intTextViewName, String column) {
    		super(context, c);
    		this.column=column;
    		this.intTextViewName = intTextViewName;
    		this.layout = layout;
    		
    	}
    	
    	@Override
    	public void bindView(View view, Context context, Cursor cursor) {
    		
    		Log.i(LOG_TAG, cursor.getString(cursor.getColumnIndex(column)) +  " Deleted: " + cursor.getInt(cursor.getColumnIndex("deleted")) + " Completed: " + cursor.getInt(cursor.getColumnIndex("completed")));
    		
    		TextView tvName = (TextView)view.findViewById(intTextViewName);
    		tvName.setText(cursor.getString(cursor.getColumnIndex(column)));
    		
    		if (cursor.getInt(cursor.getColumnIndex("completed")) == 1) { tvName.setTextColor(0xffbdbdbd);; } else {tvName.setTextColor(0xff000000);}
    		if (cursor.getInt(cursor.getColumnIndex("deleted")) == 1) { tvName.setPaintFlags(tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); } else { tvName.setPaintFlags(tvName.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));  /* TODO: FIX HERE LINE NEXT LINE*/ }
    		
    	}
     
    	@Override
    	public View newView(Context context, Cursor cursor, ViewGroup parent) {
    		LayoutInflater inflater = LayoutInflater.from(context);
    		View v = inflater.inflate(layout, parent, false);
    		bindView(v, context, cursor);
    		return v;
    	}
    }
    
}

