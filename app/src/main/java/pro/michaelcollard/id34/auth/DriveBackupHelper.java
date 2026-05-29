package pro.michaelcollard.id34.auth;

import androidx.annotation.Nullable;
import pro.michaelcollard.id34.data.IdeasDatabaseHelper;
import pro.michaelcollard.id34.data.IdeasRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DriveBackupHelper {
    private static final String DRIVE_BASE = "https://www.googleapis.com/drive/v3";
    private final OkHttpClient client = new OkHttpClient();
    private final IdeasRepository repository;

    public DriveBackupHelper(IdeasRepository repository) {
        this.repository = repository;
    }

    @Nullable
    public String findOrCreateId34Folder(String accessToken) throws Exception {
        String query = "name='Id34' and mimeType='application/vnd.google-apps.folder' and trashed=false";
        HttpUrl findUrl = HttpUrl.parse(DRIVE_BASE + "/files").newBuilder()
                .addQueryParameter("q", query)
                .addQueryParameter("fields", "files(id,name)")
                .build();
        Request find = new Request.Builder()
                .url(findUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        try (Response response = client.newCall(find).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            JSONObject body = new JSONObject(response.body().string());
            JSONArray files = body.optJSONArray("files");
            if (files != null && files.length() > 0) {
                return files.getJSONObject(0).optString("id");
            }
        }

        JSONObject folderMeta = new JSONObject();
        folderMeta.put("name", "Id34");
        folderMeta.put("mimeType", "application/vnd.google-apps.folder");
        Request create = new Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(RequestBody.create(folderMeta.toString(), MediaType.parse("application/json")))
                .build();
        try (Response response = client.newCall(create).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            return new JSONObject(response.body().string()).optString("id");
        }
    }

    private String buildBackupFilename() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss-SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return "backup_cloud_" + format.format(new Date()) + ".sqlite";
    }

    public void uploadBackup(String accessToken, String folderId, File dbFile, int ideaCount, int keepCount) throws Exception {
        String filename = buildBackupFilename();
        JSONObject metadata = new JSONObject();
        metadata.put("name", filename);
        metadata.put("parents", new JSONArray().put(folderId));

        RequestBody multipart = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("metadata", null, RequestBody.create(metadata.toString(), MediaType.parse("application/json; charset=UTF-8")))
                .addFormDataPart("file", filename, RequestBody.create(dbFile, MediaType.parse("application/x-sqlite3")))
                .build();
        Request request = new Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .addHeader("Authorization", "Bearer " + accessToken)
                .post(multipart)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Drive upload failed: " + response.code());
            }
        }
        repository.saveBackupRegistryEntry(filename, IdeasDatabaseHelper.isoNow(), dbFile.length() + " bytes", ideaCount);
        purgeOldBackups(accessToken, folderId, keepCount);
    }

    public List<String> listBackups(String accessToken, String folderId) throws Exception {
        List<DriveBackupFile> files = listBackupFiles(accessToken, folderId);
        List<String> names = new ArrayList<>();
        for (DriveBackupFile file : files) {
            names.add(file.name);
        }
        return names;
    }

    public List<DriveBackupFile> listBackupFiles(String accessToken, String folderId) throws Exception {
        String q = "'" + folderId + "' in parents and name contains 'backup_cloud_' and name contains '.sqlite' and trashed=false";
        HttpUrl listUrl = HttpUrl.parse(DRIVE_BASE + "/files").newBuilder()
                .addQueryParameter("q", q)
                .addQueryParameter("orderBy", "createdTime desc")
                .addQueryParameter("fields", "files(id,name)")
                .build();
        Request request = new Request.Builder()
                .url(listUrl)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        List<DriveBackupFile> files = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return files;
            }
            JSONArray rows = new JSONObject(response.body().string()).optJSONArray("files");
            if (rows == null) {
                return files;
            }
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                files.add(new DriveBackupFile(row.optString("id"), row.optString("name")));
            }
        }
        return files;
    }

    public void purgeOldBackups(String accessToken, String folderId, int keepCount) throws Exception {
        if (keepCount < 1) {
            keepCount = 1;
        }
        List<DriveBackupFile> files = listBackupFiles(accessToken, folderId);
        for (int i = keepCount; i < files.size(); i++) {
            Request delete = new Request.Builder()
                    .url(DRIVE_BASE + "/files/" + files.get(i).id)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .delete()
                    .build();
            try (Response ignored = client.newCall(delete).execute()) {
                // best-effort cleanup
            }
        }
    }

    public boolean restoreLatestBackupIfLocalEmpty(String accessToken, String folderId, File localDbFile) throws Exception {
        if (repository.getActiveIdeaCount() > 0) {
            return false;
        }
        List<DriveBackupFile> files = listBackupFiles(accessToken, folderId);
        if (files.isEmpty()) {
            return false;
        }
        downloadBackupFile(accessToken, files.get(0).id, localDbFile);
        return true;
    }

    public void downloadBackupFile(String accessToken, String fileId, File target) throws Exception {
        Request request = new Request.Builder()
                .url(DRIVE_BASE + "/files/" + fileId + "?alt=media")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Drive download failed: " + response.code());
            }
            if (target.getParentFile() != null && !target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            try (FileOutputStream output = new FileOutputStream(target, false)) {
                output.write(response.body().bytes());
                output.flush();
            }
        }
    }

    public static class DriveBackupFile {
        public final String id;
        public final String name;

        public DriveBackupFile(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}