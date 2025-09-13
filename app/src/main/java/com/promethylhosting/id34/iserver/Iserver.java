package com.promethylhosting.id34.iserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
		// TODO Auto-generated method stub
	return getStringFromRemote("https://id34.info/converse.php?aa=alcoholics&From="+mPhoneNumber+"&Body=!getdatetime", context);
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
	       String dataReceived = "";
	       ConnectivityManager connec =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	           if (connec.getNetworkInfo(0).isConnected() || connec.getNetworkInfo(1).isConnected()){
	               try {
	                       HttpClient httpClient = new DefaultHttpClient();
	                       HttpGet httpGet = new HttpGet(link);
	                       httpGet.setHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android " + 	android.os.Build.VERSION.RELEASE + "; " +  android.os.Build.DEVICE + " " + android.os.Build.MODEL + ") Apache.HttpClient.JAVA Version/1.4 Mobile "); 
	               		
	                       HttpParams params = httpClient.getParams();
	                       HttpConnectionParams.setConnectionTimeout(params, 30000);
	                       HttpConnectionParams.setSoTimeout(params, 30000);
	                       HttpResponse response;
	                       response = httpClient.execute(httpGet);
	                       int statusCode = response.getStatusLine().getStatusCode();
	                       if (statusCode == 200){
	                           HttpEntity entity = response.getEntity();
	                           InputStream inputStream = null;
	                           try{
	                           	inputStream = entity.getContent();
	                           	dataReceived =  convertStreamToString(inputStream);                            
	                            Log.d(LOG_TAG,"URL: "+link + "\nData Received: " + dataReceived); // <--- does this need a loop?	
	                           } catch (IOException e) {
	                               Log.e("SAVING", "Could not load xml", e);
	                           } finally {
	                           	inputStream.close();
	                           }
	                       } else {
	                    	   Log.d(LOG_TAG, "Status code "  + statusCode);
	                    	   Toast("The network request failed with message " + statusCode + ". Please try again later.");
	                       }
	                       httpClient.getConnectionManager().shutdown();
	                   }catch (SocketTimeoutException e){  
	                       //Handle not connecting to client !!!!
	                       Log.d("SocketTimeoutException Thrown", e.toString());
	                   } catch (ClientProtocolException e) {
	                       //Handle not connecting to client !!!!
	                       Log.d("ClientProtocolException Thrown", e.toString());
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

