package com.promethylhosting.id34;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.widget.CursorAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView.Tokenizer;
import android.widget.TextView;
import android.widget.Toast;

import com.promethylhosting.id34.iserver.Iserver;



public class IdeaAddActivity extends Activity {
	static SharedPreferences prefs=null;	
    protected static final int RESULT_SPEECH = 1; // result if speech was detected
	MultiAutoCompleteTextView multiAutoCompleteTextView1; 
	Context context;
	String LOG_TAG = "ID34";
	ArrayAdapter<String> aaStr;
	Boolean bSys_debug = false;
	private SQLCipherAdapter sql ;
	InputMethodManager inputMethodManager;
	
	// Edit mode support
	private boolean isEditMode = false;
	private String editingIdeaId = null;
	private String originalIdeaText = "";


	@Override
	public void onBackPressed() {
		Log.i(LOG_TAG,"Back Button pressed.");
		returnToIdeaList();
	}
	
	public void returnToIdeaList() {
		 runOnUiThread(new Runnable() {
						   public void run() {
							   getFragmentManager().popBackStackImmediate();
							   startActivity(new Intent(IdeaAddActivity.this, IdeaListActivity.class));
						   }
					   }
		 );
	}

	
    @Override
    public void onDestroy() {
    	try {
			sql.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(LOG_TAG, "onDestory fired.");

    	super.onDestroy();
	}

	@Override
	public void onResume () {
		super.onResume();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.i(LOG_TAG, "IdeaAddActivity.onCreate Fired");
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_idea_add);
	    
	    context = getApplicationContext();
        
		sql = new SQLCipherAdapter(context);
		sql.openToRead();
		
	    prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
	    int intAddActivityRuns = prefs.getInt("intAddActivityRuns", 0);
	    intAddActivityRuns++;
	    
	    SharedPreferences.Editor editor = prefs.edit();
        
        editor.putInt("intAddActivityRuns", intAddActivityRuns);
        editor.commit();
	    
	    multiAutoCompleteTextView1 = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView1);

	    // Check if we're in edit mode
	    checkEditMode();
	    
	    //if (bSys_debug) Log.i(LOG_TAG, Arrays.toString(Iserver.getHashTags()));
	    
	    
	    //REPLACE THIS WITH A DATABASE ARRAY ADAPTER
	    Cursor c = sql.getCursorOnCatsForAutoCompleteTextView();
	    Log.i(LOG_TAG, "Started cursor has " + c.getCount() + "rows");
	    String[] strArray = new String[c.getCount()];
	    c.moveToFirst();
	    
    for (int i=0;i<c.getCount();i++) {
    	strArray[i]="#" + c.getString(1);
    	Log.i(LOG_TAG, strArray[i]);
    	if (i < c.getCount() - 1) {
    		c.moveToNext();
    	}
    }
	    aaStr = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, strArray);
	    
	    //multiAutoCompleteTextView1.setAdapter(aaStr);
	    //multiAutoCompleteTextView1.setTokenizer(new SpaceTokenizer());
	    
	    showKeyboard();
	}
	
	   @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		   
		   Log.i(LOG_TAG, "IdeaAddActivity.onActivityResult Fired" + requestCode + " " +   resultCode  + " " + data.getAction());
		   
	        super.onActivityResult(requestCode, resultCode, data);
	 
	        switch (requestCode) {
	        case RESULT_SPEECH: {
	            if (resultCode == RESULT_OK && null != data) {
	 
	                ArrayList<String> text = data
	                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	 
	                multiAutoCompleteTextView1.setText(multiAutoCompleteTextView1.getText() + text.get(0));
	                STT();
	                
	            }
	            break;
	        }
	 
	        }
	    }

	
	   public void STT (View v) { STT(); } // yay, ":rappers" are awesome.
	   
	   public void STT () {
		
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak your idea, when you are finished, stop.");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, "1");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
            //txtText.setText("");
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Oops! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

	public void Toast (String result) {
		final String rnResult = result;
		runOnUiThread(
				new Runnable() {
					public void run() {
						Toast.makeText(context, "" + rnResult, Toast.LENGTH_LONG).show();
					}
				});
			
	}
	
	public void SaveIdea (View v) {
		  new atSaveIdea().execute();
	}

	/**
	 * Check if we're in edit mode and pre-populate the text field
	 */
	private void checkEditMode() {
		Intent intent = getIntent();
		if (intent.hasExtra("EDIT_MODE") && intent.getBooleanExtra("EDIT_MODE", false)) {
			isEditMode = true;
			editingIdeaId = intent.getStringExtra(IdeaDetailFragment.ARG_ITEM_ID);
			
			Log.i(LOG_TAG, "Edit mode activated for idea ID: " + editingIdeaId);
			
			// Load the existing idea text from database
			try {
				originalIdeaText = sql.getIdeaNameFromId(Long.parseLong(editingIdeaId));
				if (originalIdeaText != null && !originalIdeaText.isEmpty()) {
					multiAutoCompleteTextView1.setText(originalIdeaText);
					multiAutoCompleteTextView1.setSelection(originalIdeaText.length()); // Place cursor at end
					Log.i(LOG_TAG, "Pre-populated text field with: " + originalIdeaText);
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error loading idea for editing: " + e.getMessage());
				Toast("Error loading idea for editing");
				isEditMode = false; // Fallback to add mode
			}
		}
	}

	public void showKeyboard() {
	findViewById(R.id.activity_idea_add).postDelayed(
			new Runnable() {
			    public void run() {
			    	try {
				        InputMethodManager inputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				        inputMethodManager.toggleSoftInputFromWindow(multiAutoCompleteTextView1.getApplicationWindowToken(),     InputMethodManager.SHOW_FORCED, 0);
				        multiAutoCompleteTextView1.requestFocus();
			    	} catch (Exception e) {} 
			    }
			},2500);
	}
	
	private class atSaveIdea extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// OFFLINE MODE: Save directly to local SQLCipher database
			Iserver.init(context);
			
			// Get the raw text (no URL encoding needed for local storage)
			String text = multiAutoCompleteTextView1.getText().toString().trim();
			
			if (text.isEmpty()) {
				Log.w(LOG_TAG, "Empty idea text, not saving");
				Toast("Please enter some text to save");
				return null;
			}
			
			Log.d(LOG_TAG, "OFFLINE MODE: Saving idea to local database: " + text);
			
			// Save to local SQLCipher database
			try {
				SQLCipherAdapter sql = new SQLCipherAdapter(context);
				
				if (isEditMode && editingIdeaId != null) {
					// UPDATE existing idea
					Log.i(LOG_TAG, "EDIT MODE: Updating existing idea ID: " + editingIdeaId);
					boolean updated = sql.updateIdeaById(Long.parseLong(editingIdeaId), text);
					
					if (updated) {
						Toast("Idea updated successfully!");
						Log.i(LOG_TAG, "EDIT MODE: Idea updated successfully for ID: " + editingIdeaId);
						
						// Update widget to show latest idea
						LatestIdeaWidget.updateAllWidgets(context);
					} else {
						Toast("Failed to update idea");
						Log.e(LOG_TAG, "Failed to update idea in database");
					}
				} else {
					// INSERT new idea
					Log.i(LOG_TAG, "ADD MODE: Saving new idea");
					long savedId = sql.saveIdeaLocal(text);
					
					if (savedId > 0) {
						Toast("Idea saved successfully! ID: " + savedId);
						Log.i(LOG_TAG, "ADD MODE: Idea saved successfully with ID: " + savedId);
						
						// Update widget to show latest idea
						LatestIdeaWidget.updateAllWidgets(context);
					} else {
						Toast("Failed to save idea");
						Log.e(LOG_TAG, "Failed to save idea to local database");
					}
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "Error saving idea: " + e.getMessage());
				Toast("Error saving idea: " + e.getMessage());
				e.printStackTrace();
			}

			// Handle Twitter posting if requested
			if (text.contains("#twitter") || text.contains("#tweet")) {
				postTwitter(text);
			}
			
			// Return to idea list to show updated data
			returnToIdeaList();
			return null;
		}
	}


public static final String CONSUMER_KEY = "**REDACTED**"; // See .secrets file for actual credentials
	public static final String CONSUMER_SECRET= "**REDACTED**"; // See .secrets file for actual credentials

	public static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";

	final public static String	CALLBACK_SCHEME = "x-latify-oauth-twitter";
	final public static String	CALLBACK_URL = CALLBACK_SCHEME + "://callback";
	final public static boolean bTwitterOAuthorized=false; // whether twitter has been authorized before this user

	private void postTwitter (final String textUpdate) {
		// Twitter functionality disabled for basic APK build
		Log.i(LOG_TAG, "Twitter posting not available in basic build: " + textUpdate);
		Toast("Twitter posting not available in basic build");
	}

	private boolean getTwitterOAuth() {
		// Twitter OAuth functionality disabled for basic APK build
		Log.i(LOG_TAG, "Twitter OAuth not available in basic build");
		return false;
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// OAuth callback handling disabled for basic APK build
	}


	public class SpaceTokenizer implements Tokenizer {

		public int findTokenStart(CharSequence text, int cursor) {
		int i = cursor;

		while (i > 0 && text.charAt(i - 1) != ' ') {
		    i--;
		}
		while (i < cursor && text.charAt(i) == ' ') {
		    i++;
		}

		return i;
		}

		public int findTokenEnd(CharSequence text, int cursor) {
		int i = cursor;
		int len = text.length();

		while (i < len) {
		    if (text.charAt(i) == ' ') {
		        return i;
		    } else {
		        i++;
		    }
		}

		return len;
		}

		public CharSequence terminateToken(CharSequence text) {
		int i = text.length();

		while (i > 0 && text.charAt(i - 1) == ' ') {
		    i--;
		}

		if (i > 0 && text.charAt(i - 1) == ' ') {
		    return text;
		} else {
		    if (text instanceof Spanned) {
		        SpannableString sp = new SpannableString(text + " ");
		        TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
		                Object.class, sp, 0);
		        return sp;
		    } else {
		        return text + " ";
		    }
		}
		}
		}

	
}
