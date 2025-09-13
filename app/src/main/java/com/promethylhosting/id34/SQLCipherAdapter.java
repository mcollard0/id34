package com.promethylhosting.id34;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.promethylhosting.id34.iserver.Iserver;

public class SQLCipherAdapter {

	 public static final String MYDATABASE_NAME = "id34_id34";
	 public static final String MYDATABASE_TABLE_CATEGORY = "tblCategory";
	 public static final String MYDATABASE_TABLE_IDEA = "tblIdea";
	 public static final String MYDATABASE_TABLE_RESPONSES = "tblResponses"; // needed?
	 public static final int MYDATABASE_VERSION = 2;
	 public static final String KEY_ID = "id";
	 public static final String KEY_CAT = "cat";
	 public static final String KEY_CREATED = "created";
	 public static final String KEY_UPDATED = "updated";
	 public static final String KEY_REMINDER = "reminder";
	 public static final String KEY_UID = "uid";
	 public static final String KEY_DELETED = "deleted";
	 public static final String KEY_COMPLETED = "completed";
	 public static final String KEY_NAME = "name";
	 public static final String KEY_CID0 = "cid0";
	 public static final String KEY_CID1 = "cid1";
	 public static final String KEY_CID2 = "cid2";
	 public static final String KEY_CID3 = "cid3";
	 public static final String KEY_CID4 = "cid4";
	 public static final String KEY_NUM = "num";
	 private static final String LOG_TAG = "id34";
	 
	 // drop tables to upgrade.;)
	 private static final String SCRIPT_DROP_ALL_TABLES = "drop `tblCategory`; drop `tblIdea`; drop `tblResponse`\n";
	 //create table MY_DATABASE (ID integer primary key, Content text not null);
	 private static final String SCRIPT_CREATE_DATABASE_1 =
	  "CREATE TABLE `tblCategory` (\n" + 
	  "  `id` unsigned int(8) PRIMARY KEY NOT NULL,\n" + 
	  "  `uid` unsigned int(8) NOT NULL,\n" +
	  "  `updated` timestamp NOT NULL default '1970-01-01 06:00:00',\n" +
	  "  `num` unsigned int(8) NOT NULL,\n" + 
	  "  `cat` varchar(57) NOT NULL\n" + 
	  //"  KEY `cat` (`cat`,`num`)\n" + 
	  ") ;\n";
	 private static final String SCRIPT_CREATE_DATABASE_2 =
	  "CREATE TABLE `tblIdea` (\n" + 
	  "  `id` unsigned int(8) PRIMARY KEY NOT NULL,\n" + 
	  "  `uid` unsigned int(8) NOT NULL,\n" + 
	  "  `name` varchar(255) NOT NULL,\n" + 
	  "  `created` timestamp NOT NULL default CURRENT_TIMESTAMP,\n" +
	  "  `updated` timestamp NOT NULL default '1970-01-01 06:00:00',\n" +
	  "  `reminder` timestamp NOT NULL default '1970-01-01 06:00:00',\n" + 
	  "  `num` unsigned int(8) NOT NULL,\n" + 
	  "  `cid0` unsigned int(8) NOT NULL,\n" + 
	  "  `cid1` unsigned int(8) NOT NULL,\n" + 
	  "  `cid2` unsigned int(8) NOT NULL,\n" + 
	  "  `cid3` unsigned int(8) NOT NULL,\n" + 
	  "  `cid4` unsigned int(8) NOT NULL,\n" + 
	  "  `deleted` tinyint(1) NOT NULL default '0', \n" +
	  "  `completed` tinyint(1) NOT NULL default '0' \n" + 
	  //"  KEY `cid0` (`cid0`,`cid1`,`cid2`,`cid3`,`cid4`)\n" + // NO INDEXES!!!
	  "); \n" ;
	 
	 private static final String SCRIPT_CREATE_DATABASE_3 =
	  "create index cid0 on tblIdea (`cid0`,`cid1`,`cid2`,`cid3`,`cid4`);" +
	  "CREATE TABLE `tblResponses` (\n" + 
	  "  `msg` varchar(25) NOT NULL\n" + 
	  ");\n" ;
	 
	 private static SQLiteHelper sqLiteHelper;
	 private static SQLiteDatabase sqLiteDatabase;

	 private static Context context;
	 
	 public SQLiteAdapter(Context c){
	  context = c;
	  
	    Thread iserverinit = new Thread() {
	        @Override
	        public void run() {
	            try {
	            	Iserver.init(context);
	            } catch(Exception e) {
	                // do nothing
	            	Log.e(LOG_TAG, "ERROR: " + e.getMessage());
	            } 
	        }
	    };
	    iserverinit.start();
	 }
	 
	 public SQLiteAdapter openToRead() throws android.database.SQLException {
	  sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
	  sqLiteDatabase = sqLiteHelper.getReadableDatabase();
	  return this; 
	 }
	 
	 public SQLiteAdapter openToWrite() throws android.database.SQLException {
	  sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
	  sqLiteDatabase = sqLiteHelper.getWritableDatabase();
	  return this; 
	 }
	 
	 public void close(){
	  sqLiteHelper.close();
	 }
	
	 public String getCatIdFromCatName(String catname) {
		 String strRetVal = "-1";
		  try {
			Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_CATEGORY, new String[]{KEY_ID}, 
					  KEY_CAT + "=?", new String[]{catname}, null, null, null);
			  cursor.moveToFirst();
			  return String.valueOf(cursor.getInt(0)); // first one is the bingo
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return strRetVal;
		 
	 } 
	 
	 
	 public String getCatNameFromCatId(String catid) {
		 Log.i(LOG_TAG, "GetCatNameFromCatId(" + catid + ")");
		 String strRetVal = "Unknown";
		  try {
			Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_CATEGORY, new String[]{KEY_CAT}, 
					  KEY_ID + "=?", new String[]{catid}, null, null, null);
			  cursor.moveToFirst();
			  return cursor.getString(0); // first one is the bingo
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return strRetVal;
		 
	 }

	 
	 public Cursor getCursorOnCatsForAutoCompleteTextView() {
		 
		 Cursor cursor=null;
		 
		  try {
			cursor = sqLiteDatabase.query(MYDATABASE_TABLE_CATEGORY, new String[]{KEY_ID+" _ID", KEY_CAT}, null, null, null, null, null);
			  return cursor; // first one is the bingo
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return cursor;
		 
	 }

	 
	 
	 public long insertIdea(JSONObject jsonRow) throws JSONException {
		  ContentValues contentValues = new ContentValues();
		  contentValues.put(KEY_ID, jsonRow.getInt(KEY_ID));
		  contentValues.put(KEY_UID, jsonRow.getInt(KEY_UID));
		  contentValues.put(KEY_CID0, jsonRow.getInt(KEY_CID0));
		  contentValues.put(KEY_CID1, jsonRow.getInt(KEY_CID1));
		  contentValues.put(KEY_CID2, jsonRow.getInt(KEY_CID2));
		  contentValues.put(KEY_CID3, jsonRow.getInt(KEY_CID3));
		  contentValues.put(KEY_CID4, jsonRow.getInt(KEY_CID4));
		  contentValues.put(KEY_NAME, jsonRow.getString(KEY_NAME));
		  contentValues.put(KEY_NUM, jsonRow.getString(KEY_NUM));
		  contentValues.put(KEY_CREATED, jsonRow.getString(KEY_CREATED));
		  contentValues.put(KEY_UPDATED, jsonRow.getString(KEY_UPDATED));
		  contentValues.put(KEY_REMINDER, jsonRow.getString(KEY_REMINDER));
		  contentValues.put(KEY_DELETED, jsonRow.getBoolean(KEY_DELETED));
		  contentValues.put(KEY_COMPLETED, jsonRow.getBoolean(KEY_COMPLETED));
		  return sqLiteDatabase.replace(MYDATABASE_TABLE_IDEA, null, contentValues);
		  //return sqLiteDatabase.insertWithOnConflict(MYDATABASE_TABLE_IDEA, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
	 }
	
	 public long insertCat(JSONObject jsonRow) throws JSONException {
		  ContentValues contentValues = new ContentValues();
		  contentValues.put(KEY_ID, jsonRow.getInt(KEY_ID));
		  contentValues.put(KEY_UID, jsonRow.getInt(KEY_UID));
		  contentValues.put(KEY_UPDATED, jsonRow.getString(KEY_UPDATED));
		  contentValues.put(KEY_CAT, jsonRow.getString(KEY_CAT));
		  contentValues.put(KEY_NUM, jsonRow.getString(KEY_NUM));
		  //return sqLiteDatabase.insertWithOnConflict(MYDATABASE_TABLE_CATEGORY, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
		  return sqLiteDatabase.replace(MYDATABASE_TABLE_CATEGORY, null, contentValues);
		 
	 }
	 
		public Boolean updateDBCats(String dtSyncDate) { // TODO: MOVE THIS TO SERVCE
			// TODO: Implement timestamp to limit entries returned
			JSONArray jsonArray = Iserver.getJSONFromRemote("Body=hh&syncdate=" + URLEncoder.encode(dtSyncDate.toString()), context);
			JSONObject jsonRow = null;
			String cat = "";
			Boolean hasError= false;
			Log.e(LOG_TAG, "updateDBCats()" + dtSyncDate.toString() + " " +  jsonArray.length());
			
			for (int i =0; i< jsonArray.length(); i++) {
				
				try {
					jsonRow = jsonArray.getJSONObject(i);
					
					cat = jsonRow.getString("cat");
					Log.e(LOG_TAG, jsonRow.toString(4));
					Log.i(LOG_TAG, "Category: " + cat);
					insertCat(jsonRow);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(LOG_TAG, e.getMessage());
					e.printStackTrace();
					hasError=true;
				} 
				
			}
			return hasError;
		}

		public String getServerDateTime() {
			String strServerDT = Iserver.getServerDateTime();
			
			return strServerDT;
		}
		
		public Boolean updateDBIdeas(String dtSyncDate) { // TODO: MOVE THIS TO SERVCE
			// TODO: Implement timestamp to limit entries returned
			
			Boolean hasError= false;
			
			JSONArray jsonArray = Iserver.getJSONFromRemote("Einstein=plus&syncdate=" + URLEncoder.encode(dtSyncDate.toString()), context);
			JSONObject jsonRow = null;
			
			Log.e(LOG_TAG, "updateDBIdeas()" + dtSyncDate.toString() + " " +  jsonArray.length());
			
			for (int i =0; i< jsonArray.length(); i++) {
				
				try {
					jsonRow = jsonArray.getJSONObject(i);
					
					Log.e(LOG_TAG, jsonRow.toString(4));
					Log.i(LOG_TAG, "Category: " + jsonRow.getString("name"));
					insertIdea(jsonRow);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					Log.e(LOG_TAG, e.getMessage());
					e.printStackTrace();
					hasError=true;
				}
			}
			return hasError;
		}
	
		
	 
	 
	 public long insert(String content){
	  
	  ContentValues contentValues = new ContentValues();
	  contentValues.put(KEY_ID, content);
	  return sqLiteDatabase.insert(MYDATABASE_TABLE_CATEGORY, null, contentValues);
	 }
	 
	 public int deleteAll(){
	  return sqLiteDatabase.delete(MYDATABASE_TABLE_CATEGORY, null, null);
	 }
	 
	 public String queueAll(){
	  String[] columns = new String[]{KEY_ID};
	  Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_CATEGORY, columns, 
	    null, null, null, null, null);
	  String result = "";
	  
	  int index_CONTENT = cursor.getColumnIndex(KEY_ID);
	  for(cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()){
	   result = result + cursor.getString(index_CONTENT) + "\n";
	  }
	 
	  return result;
	 }
	 
	 public class SQLiteHelper extends SQLiteOpenHelper {

	  public SQLiteHelper(Context context, String name,
	    CursorFactory factory, int version) {
	   super(context, name, factory, version);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase db) {
	   // TODO Auto-generated method stub
	   db.execSQL(SCRIPT_CREATE_DATABASE_1);
	   db.execSQL(SCRIPT_CREATE_DATABASE_2);
	   db.execSQL(SCRIPT_CREATE_DATABASE_3);
	   db.execSQL("create temp table search as select * from "  + MYDATABASE_TABLE_IDEA);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	   // TODO Auto-generated method stub
		  db.execSQL(SCRIPT_DROP_ALL_TABLES);

		  this.onCreate(db);

	  }

	 }

	public Cursor queryIdeasByCatName(String strCatName) {
		// Fixed SQL injection vulnerability - using parameterized queries
		  String[] columns = new String[]{KEY_ID + " _id",  KEY_NAME, KEY_COMPLETED, KEY_DELETED};
		  
		  String catid = getCatIdFromCatName(strCatName);
		  
		  Log.i(LOG_TAG, "Searching cat id #: " + catid + " (" + strCatName + ") " );
		  
		  String strDeleted01 = "0";
		  String strCompleted01 = "0";
		  
		  // Use parameterized query to prevent SQL injection
		  String selection = "(" + KEY_CID0 + " = ? OR " +  
						  KEY_CID1 + " = ? OR " + 
						  KEY_CID2 + " = ? OR " + 
						  KEY_CID3 + " = ? OR " + 
						  KEY_CID4 + " = ?) AND deleted = ?";
		  String[] selectionArgs = new String[]{catid, catid, catid, catid, catid, strDeleted01};
		  
		  return sqLiteDatabase.query(MYDATABASE_TABLE_IDEA, columns, 
				  selection, selectionArgs, null, null, null);
		
	}

	
	public Cursor queryCats() {
		// TODO Auto-generated method stub
		  String[] columns = new String[]{KEY_ID + " _id",  KEY_CAT};
		  
		  Log.i(LOG_TAG, "Searching for categories in database." );
		  
		  return sqLiteDatabase.query(MYDATABASE_TABLE_CATEGORY, columns, null, null, null, null, "lower(" + KEY_CAT + ")");
		
	}

	public String toggleCompleted(long id) {
		return Iserver.getStringFromRemote("Body=!complete&id=" + id);
	}

	public String toggleDeleted(long id) {
		return Iserver.getStringFromRemote("Body=!delete&id=" + id);
	}

	public String getIdeaNameFromId(final long intIdIdea) {
		 String strRetVal = "Unknown";
		 String strIdIdea = String.valueOf(intIdIdea);
		  try {
			Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE_IDEA, new String[]{KEY_NAME}, 
					  KEY_ID + "=?", new String[]{strIdIdea}, null, null, null);
			//if (cursor.getCount()=0) return "";
			cursor.moveToFirst();
			return cursor.getString(0); // first one is the bingo
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strRetVal;
	}

	
}
