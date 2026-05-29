package pro.michaelcollard.id34.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class IdeasDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ideas.db";
    public static final int DB_VERSION = 1;
    private final Context context;

    public IdeasDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createSchema(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createSchema(db);
        maybeMigrateLegacyRows(db);
    }

    private void createSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS ideas (id TEXT PRIMARY KEY, content TEXT NOT NULL, created_at TEXT NOT NULL, updated_at TEXT NOT NULL, deleted INTEGER DEFAULT 0)");
        try {
            db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS ideas_fts USING fts5(id, content)");
        } catch (SQLiteException ignored) {
            // FTS5 may be unavailable on some device SQLite builds; repository falls back to LIKE search.
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS backups_registry (filename TEXT PRIMARY KEY, timestamp TEXT NOT NULL, size TEXT NOT NULL, idea_count INTEGER NOT NULL)");
    }

    private void maybeMigrateLegacyRows(SQLiteDatabase db) {
        if (!tableExists(db, "tblIdea")) {
            return;
        }
        Cursor countCursor = db.rawQuery("SELECT COUNT(1) FROM ideas", null);
        int newCount = 0;
        if (countCursor.moveToFirst()) {
            newCount = countCursor.getInt(0);
        }
        countCursor.close();
        if (newCount > 0) {
            return;
        }

        Cursor cursor = db.rawQuery("SELECT id, name, created, updated, deleted FROM tblIdea", null);
        while (cursor.moveToNext()) {
            String legacyName = cursor.getString(1);
            String created = normalizeDate(cursor.getString(2));
            String updated = normalizeDate(cursor.getString(3));
            int deleted = cursor.getInt(4);
            String id = "idea_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("content", TextUtils.isEmpty(legacyName) ? "" : legacyName);
            values.put("created_at", created);
            values.put("updated_at", updated);
            values.put("deleted", deleted);
            db.insertWithOnConflict("ideas", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        cursor.close();
        try {
            db.execSQL("INSERT INTO ideas_fts(rowid, id, content) SELECT rowid, id, content FROM ideas WHERE deleted = 0");
        } catch (SQLiteException ignored) {
            // ideas_fts absent when FTS5 is unsupported.
        }
    }

    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private String normalizeDate(String maybeDate) {
        if (TextUtils.isEmpty(maybeDate)) {
            return isoNow();
        }
        if (maybeDate.endsWith("Z") && maybeDate.contains("T")) {
            return maybeDate;
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsed = in.parse(maybeDate);
            SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            out.setTimeZone(TimeZone.getTimeZone("UTC"));
            return out.format(parsed);
        } catch (ParseException e) {
            return isoNow();
        }
    }

    public static String isoNow() {
        SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        out.setTimeZone(TimeZone.getTimeZone("UTC"));
        return out.format(new Date());
    }
}
