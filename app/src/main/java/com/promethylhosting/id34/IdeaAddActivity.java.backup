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
import android.support.v4.widget.CursorAdapter;
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

	    //if (bSys_debug) Log.i(LOG_TAG, Arrays.toString(Iserver.getHashTags()));
	    
	    
	    //REPLACE THIS WITH A DATABASE ARRAY ADAPTER
	    Cursor c = sql.getCursorOnCatsForAutoCompleteTextView();
	    Log.i(LOG_TAG, "Started cursor has " + c.getCount() + "rows");
	    String[] strArray = new String[c.getCount()];
	    c.moveToFirst();
	    
	    for (int i=0;i<c.getCount();i++) {
	    	c.moveToNext();
	    	strArray[i]="#" + c.getString(1);
	    	Log.i(LOG_TAG, strArray[i]);
	    	i=i+1;
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
			// TODO Auto-generated method stub
			//Iserver Iserver = new Iserver(context); 
			Iserver.init(context);
			//Editable text = multiAutoCompleteTextView1.getText();
			String text="";
		     try {
					text = URLEncoder.encode(multiAutoCompleteTextView1.getText().toString(), "UTF-8").toString();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // this might fix some encoding issues
			
			Log.d(LOG_TAG, "AsyncTask : pulling data from url: " + text + "");
			String result  = Iserver.getStringFromRemote("Body=" + text);
			Toast(result);

			context.startService(new Intent(context, ServerInteractionService.class)); // process data

			if (text.contains("#twitter") || text.contains("#tweet"))  { postTwitter(text); }
			
			returnToIdeaList() ;
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
		/*
		* Consumer Key (API Key)	**REDACTED** (See .secrets file - manage keys and access tokens)
Callback URL	https://promethylhosting.com/mobile/id34/twittercallback.php
Sign in with Twitter	Yes
App-only authentication	https://api.twitter.com/oauth2/token
Request token URL	https://api.twitter.com/oauth/request_token
Authorize URL	https://api.twitter.com/oauth/authorize
Access token URL	https://api.twitter.com/oauth/access_token

Consumer Key (API Key)	**REDACTED**
Consumer Secret (API Secret)	**REDACTED**
		* */

	if (!bTwitterOAuthorized) { bTwitterMessagePending=true; sTwitterMessagePending = textUpdate; getTwitterOAuth(); return; }

	Thread t = new Thread() {
		public void run() {
			try {
					String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
					String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

					AccessToken accTok = new AccessToken(token,secret);
					Twitter twitter = new TwitterFactory().getInstance();
					twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
					twitter.setOAuthAccessToken(accTok);
					twitter.updateStatus(textUpdate);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	};
	t.start();



	}

	private boolean getTwitterOAuth() {
		@Override
		protected Void doInBackground(Void... params) {

			try {
				Log.i(TAG, "Retrieving request token from Google servers");
				final String url = provider.retrieveRequestToken(consumer, OAUTH_CALLBACK_URL);
				Log.i(TAG, "Popping a browser with the authorize URL : " + url);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
				context.startActivity(intent);
			} catch (Exception e) {
				Log.e(TAG, "Error during OAUth retrieve request token", e);
			}

			return null;
		}


	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(Constants.OAUTH_CALLBACK_SCHEME)) {
			Log.i(TAG, "Callback received : " + uri);
			Log.i(TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(this,consumer,provider,prefs).execute(uri);
			finish();
		}
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
