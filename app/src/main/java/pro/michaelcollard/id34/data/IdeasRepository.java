package pro.michaelcollard.id34.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IdeasRepository {
    private final IdeasDatabaseHelper helper;

    public IdeasRepository(Context context) {
        this.helper = new IdeasDatabaseHelper(context);
    }

    public Idea addIdea(String content) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String now = IdeasDatabaseHelper.isoNow();
        String id = "idea_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("content", content);
        values.put("created_at", now);
        values.put("updated_at", now);
        values.put("deleted", 0);
        db.insert("ideas", null, values);
        rebuildFtsIndex(db);
        return new Idea(id, content, now, now, 0);
    }

    public void updateIdea(String id, String content) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", content);
        values.put("updated_at", IdeasDatabaseHelper.isoNow());
        db.update("ideas", values, "id = ?", new String[]{id});
        rebuildFtsIndex(db);
    }

    public void softDeleteIdea(String id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("deleted", 1);
        values.put("updated_at", IdeasDatabaseHelper.isoNow());
        db.update("ideas", values, "id = ?", new String[]{id});
        rebuildFtsIndex(db);
    }

    public List<Idea> listActiveIdeas() {
        return queryIdeas("SELECT id, content, created_at, updated_at, deleted FROM ideas WHERE deleted = 0 ORDER BY updated_at DESC", null);
    }

    public List<Idea> searchIdeas(String query) {
        String ftsSql = "SELECT i.id, i.content, i.created_at, i.updated_at, i.deleted FROM ideas_fts f JOIN ideas i ON i.id = f.id WHERE ideas_fts MATCH ? AND i.deleted = 0 ORDER BY i.updated_at DESC LIMIT 100";
        try {
            List<Idea> fts = queryIdeas(ftsSql, new String[]{query + "*"});
            if (!fts.isEmpty()) {
                return fts;
            }
        } catch (SQLiteException ignored) {
            // ideas_fts unavailable when device SQLite does not include FTS5.
        }
        String likeSql = "SELECT id, content, created_at, updated_at, deleted FROM ideas WHERE deleted = 0 AND content LIKE ? ORDER BY updated_at DESC LIMIT 100";
        return queryIdeas(likeSql, new String[]{"%" + query + "%"});
    }

    public List<HeatWord> computeHeatmap() {
        return HeatmapUtils.computeHeatmap(listActiveIdeas());
    }

    public int getActiveIdeaCount() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(1) FROM ideas WHERE deleted = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void saveBackupRegistryEntry(String filename, String timestamp, String size, int ideaCount) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("filename", filename);
        values.put("timestamp", timestamp);
        values.put("size", size);
        values.put("idea_count", ideaCount);
        db.insertWithOnConflict("backups_registry", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<String> listBackupRegistryFilenames() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<String> rows = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT filename FROM backups_registry ORDER BY timestamp DESC", null);
        while (cursor.moveToNext()) {
            rows.add(cursor.getString(0));
        }
        cursor.close();
        return rows;
    }

    private List<Idea> queryIdeas(String sql, String[] args) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        List<Idea> ideas = new ArrayList<>();
        while (cursor.moveToNext()) {
            ideas.add(new Idea(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
        }
        cursor.close();
        return ideas;
    }

    private void rebuildFtsIndex(SQLiteDatabase db) {
        try {
            db.execSQL("DELETE FROM ideas_fts");
            db.execSQL("INSERT INTO ideas_fts(id, content) SELECT id, content FROM ideas WHERE deleted = 0");
        } catch (SQLiteException ignored) {
            // ideas_fts unavailable when device SQLite does not include FTS5.
        }
    }
}
