package com.promethylhosting.id34;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

import java.util.ArrayList;

public class IdeaListActivity extends FragmentActivity
        implements IdeaListFragment.Callbacks {

	private String mEmailAddress="";
	private String mPhoneNumber;
    private boolean mTwoPane;
    private static Context context;
    // this might not be right ... .check it out 
    private static String SENDER_ID = "**REDACTED**"; // GCM value -- See .secrets file for actual credentials
    private String LOG_TAG = "Id34";
    private SharedPreferences prefs ;
    private static final boolean bDebug = true;
    private static boolean bUse_Storage_Server = true; // Server / Local
    private static boolean bGCM_Enabled = true; // use cloud storage, set in code below
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = getApplicationContext();
        getUser();
        if (bUse_Storage_Server) { bGCM_Enabled=true; GCM_register(); }
        
        setContentView(R.layout.activity_idea_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.idea_detail_container) != null) {
            mTwoPane = true;
            ((IdeaListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.idea_list))
                    .setActivateOnItemClick(true);
            
            IdeaListFragment fragListItem = new IdeaListFragment();
            fragListItem.setArguments(getIntent().getExtras())            ;
            getSupportFragmentManager().beginTransaction().add(R.id.idea_list, fragListItem).commit();
        }
    }

    public boolean getUser() {
        // cannot remove this until the phone number is removed from the url passed here
        prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
        mPhoneNumber = prefs.getString("mPhoneNumber", "");
        mEmailAddress = prefs.getString("mEmailAddress", "");

        /* TODO: handle case with phone number changed -- do we trust the user? Should we bounce it off SMS? Or because we charge them, we just trust them?

         Or should we do it later when we get a phone number from the system?

         Phone number or other sensitive data should be SHA-1 if the RDBMS can handle it.
        */

        if (mPhoneNumber.length()==0) {
	        try {
	        	TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);    
	        	mPhoneNumber = tMgr.getLine1Number();
	        	Toast.makeText(context, mPhoneNumber, Toast.LENGTH_SHORT).show();
                prefs.edit().putString("mPhoneNumber", mPhoneNumber).apply();
                mPhoneNumber = prefs.getString("mPhoneNumber", "");
	        } catch (Exception e) {
	        	//TODO use dialog to get phone number
                // moving away from phone number, this can probably be skipped
                // email is handled below
	        }
	    }
        
        if (mEmailAddress.length()==0) {
            AccountManager accountManager = AccountManager.get(context); 
            Account[] accounts = accountManager.getAccountsByType("com.google");
            //Account account;
            if (accounts.length > 1) {
                getGoogleAccountFromMultiple(accounts);
            } else if (accounts.length == 1) {
                boolean bSuccess = saveGoogleAccount(accounts[0]);
                if (!bSuccess) { return false; }
            }
        }
    
        if (mPhoneNumber.length()==0 && mEmailAddress.length()==0) {
        	// TODO: TEST THIS BRANCH TO MAKE SURE ITR WORKS -- see if the phone number is required anymore
        	ToastOnUiThread("Unable to get a Google account. Please create/login your Google account and launch Id34 again...");
            return false;
        }

        return true;
    }

    ArrayList<String> gUsernameList = new ArrayList<String>();
    private boolean getGoogleAccountFromMultiple(Account[] accounts) {

        gUsernameList.clear();
        for (Account account : accounts) { gUsernameList.add(account.name); }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose you GMail account");

        ListView lv = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, gUsernameList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
                Log.d(LOG_TAG, gUsernameList.get(position) );
                //TODO: test this with multiple accounts, Andrew's tablet maybe
                boolean bSuccess = saveGoogleAccount(gUsernameList.get(position));
            }
        });
        builder.setView(lv);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        final Dialog dialog = builder.create();
        dialog.show();

        return true; // may not be actually true if the callback didn't happen on multiple accts
    }

    private boolean saveGoogleAccount(String account) { // overloaded to handle string or account
        try {
            prefs.edit().putString("mEmailAddress", mEmailAddress).apply();
            Log.i(LOG_TAG, "Google Account/Email: " + mEmailAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean saveGoogleAccount(Account account) { // save callback --0 // overloaded to handle string or account
        try {
            mEmailAddress = account.name;
            saveGoogleAccount(mEmailAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void GCM_reregister() { // do this if we don't have it in the server database ... if users gets sync..d... hrm. security problem. Only sync the master user.
    	// TODO: REMOVE
    	GCMRegistrar.unregister(this); // RMOVE ME!
    	GCM_register();
    }
    
    // register on the GCM cloud for updates
    public void GCM_register() {
    	Log.i(LOG_TAG, "GCM REgister");
    	if (bDebug) GCMRegistrar.checkDevice(this); // check to make sure device supports GCM, not emulator 
    	if (bDebug) GCMRegistrar.checkManifest(this); // checks to make sure manifest is set up properly.
    	
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
          GCMRegistrar.register(this, SENDER_ID);
        } else {
          Log.v(LOG_TAG, "Already registered " + regId );
        }

    }
    
    @Override
    public void onResume() {
    	    super.onResume(); 
    	  
    	
    	//getData();
    }
    
    public void ToastOnUiThread(String msg) {
    	if (context==null) return;
    	try {
    		runOnUiThread(new Runnable() {
    			@Override
    		    public void run() {
    		        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
    		    }
    		});
    		
    		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();	 // TODO: WTF?
    	}catch (Exception e) {}
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i(LOG_TAG, "Activity Menu item selected: " + item.getTitle());
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        
        if (item.getTitle() == "Refresh") {
        	Intent intent = new Intent(context, ServerInteractionService.class);
        	intent.putExtra("bRefresh", true);
        	context.startService(intent); // process data 
        	return true;
        } else if (item.getTitle() == "Google") {
        	// insert code here to google category? 
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(IdeaDetailFragment.ARG_ITEM_ID, id);
            IdeaDetailFragment fragment = new IdeaDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.idea_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, IdeaDetailActivity.class);
            detailIntent.putExtra(IdeaDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
    
}

