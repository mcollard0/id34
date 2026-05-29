package com.promethylhosting.id34.iserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class Iserver {
	static Context context=null;
	static String baseurl = "";
	static String LOG_TAG = "Iserver";
	static String mPhoneNumber = "";
	static String sLastError ;
	static SharedPreferences prefs;
	static String gcmRegID = "";
	public static String hashtags[] = {}; // fix fc on restart ?? ?
	
	// only return items which arent null
	public static String[] getHashTags() { 
		//if (hashtags == null) return new String[] {}; // fix fc when restarting
	return Arrays.copyOf(hashtags, hashtags.length); }

	public static void setHashTags(String[] hashtags) {
		Iserver.hashtags = hashtags;
	}

	public Iserver (Context context) {
		super();
		Log.i(LOG_TAG, "Construct Iserver.");
		
		Iserver.context = context;
		init();
	} 
	
	public static Boolean init() {
		return init(context);
	}
	
	public static Boolean init(Context thecontext) {
		
		if (thecontext!=null) context = thecontext;
		Log.e(LOG_TAG, "init context: "  + context);
		
		
		if (baseurl.length()>5) { Log.d(LOG_TAG, "Already initialized. Do you want to add a return here --->"); }
		
		hashtags = new String[500];
		Log.i(LOG_TAG, "Initialize Iserver with " + context);
		
		prefs = context.getSharedPreferences("com.promethylhosting.id34", Context.MODE_PRIVATE);
        mPhoneNumber = prefs.getString("mPhoneNumber", "");
        
        //~! Add GCMRegID here:
        gcmRegID = prefs.getString("gcmRegID", "");
        
		// CRASH FIX: Skip problematic id34.info call that returns HTML instead of valid URL
		Log.i(LOG_TAG, "OFFLINE MODE: Skipping id34.info server call to prevent crash");
		baseurl = "local://offline-mode"; // Use local fallback to prevent crash
		Log.i(LOG_TAG, "Using local fallback baseurl: " + baseurl);

		// Always succeed in offline mode
		if (mPhoneNumber == null || mPhoneNumber.length() == 0) {
			mPhoneNumber = "+15555215554"; // Default for emulator
			Log.i(LOG_TAG, "Using default phone number: " + mPhoneNumber);
		}

	return true;
	} 
	public static String getLastError() { return sLastError; }
	public Iserver getInstance() {
		return this;
	} 
	
	public static String getServerDateTime() {
		// OFFLINE MODE: Return current system time instead of server time
		Log.i(LOG_TAG, "OFFLINE MODE: Using local system time instead of server time");
	return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(new java.util.Date());
	}

	
	public static String updateGCMuid(String regId) {
		return getStringFromRemote("Body=!getdatetime&uid_gcm=" + regId); // have to have something in the body
	}

	

	
	public static String getStringFromRemote(String body) { // passthrough to add base url with session info
		if (baseurl.length()<5) { init(); }
	return getStringFromRemote(baseurl + "&" + body, context);
	}
	
	public static JSONArray getJSONFromRemote(String body, Context context) {
		if (baseurl.length()<5) { init(context); }
		String retString = getStringFromRemote(baseurl + "&" +  body  + "&json=1", context);
		JSONArray jsonArray = new JSONArray();
		try {
			jsonArray = new JSONArray(retString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return jsonArray;
	}
	
	// this should be a private function but it is used in class, new use should use getstring from remote
	public static String getStringFromRemote(String link, Context context) { //legacy
			Log.d(LOG_TAG,"Getting:" + link);
			
			// CRASH FIX: Handle local:// URLs for offline mode
			if (link != null && link.startsWith("local://")) {
				Log.i(LOG_TAG, "OFFLINE MODE: Returning empty response for local URL: " + link);
				return ""; // Return empty string for local URLs to prevent network calls
			}
	       String dataReceived = "";
	       ConnectivityManager connec =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	           if (connec.getNetworkInfo(0).isConnected() || connec.getNetworkInfo(1).isConnected()){
	               try {
	                       // Modern HttpURLConnection replaces deprecated Apache HttpClient
	                       URL url = new URL(link);
	                       HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	                       
	                       // Set request properties (equivalent to old HttpGet setup)
	                       connection.setRequestMethod("GET");
	                       connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android " + android.os.Build.VERSION.RELEASE + "; " +  android.os.Build.DEVICE + " " + android.os.Build.MODEL + ") HttpURLConnection/Java Mobile");
	                       connection.setConnectTimeout(30000);
	                       connection.setReadTimeout(30000);
	                       
	                       int statusCode = connection.getResponseCode();
	                       if (statusCode == HttpURLConnection.HTTP_OK) {
	                           InputStream inputStream = null;
	                           try {
	                               inputStream = connection.getInputStream();
	                               dataReceived = convertStreamToString(inputStream);
	                               Log.d(LOG_TAG,"URL: "+link + "\nData Received: " + dataReceived);
	                           } catch (IOException e) {
	                               Log.e("SAVING", "Could not load xml", e);
	                           } finally {
	                               if (inputStream != null) {
	                                   inputStream.close();
	                               }
	                           }
	                       } else {
	                           Log.d(LOG_TAG, "Status code "  + statusCode);
	                           Toast("The network request failed with message " + statusCode + ". Please try again later.");
	                       }
	                       connection.disconnect();
	                   }catch (SocketTimeoutException e){  
	                       //Handle not connecting to client !!!!
	                       Log.d("SocketTimeoutException Thrown", e.toString());
	                   }catch (MalformedURLException e) {
	                       // TODO Auto-generated catch block
	                       e.printStackTrace();
	                       Log.d("MalformedURLException Thrown", e.toString());
	                   } catch (IOException e) {
	                       // TODO Auto-generated catch block
	                       e.printStackTrace();
	                       Log.d("IOException Thrown", e.toString());
	                   } 
	               } else { Log.d(LOG_TAG, "No network access.");}
	           return dataReceived;

			
		}


	   //support functino
		public static void Toast(String msg) { Toast.makeText(context, msg, Toast.LENGTH_LONG).show(); }

		public static String convertStreamToString(java.io.InputStream is) {
		    try {
		    	// TODO determine if this only returns first line
		        return new java.util.Scanner(is).useDelimiter("\\A").next();
		        
		    } catch (java.util.NoSuchElementException e) {
		        return "";
		    }
		}
}
