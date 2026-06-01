package pro.michaelcollard.id34;
import android.app.AlertDialog;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import pro.michaelcollard.id34.auth.DriveBackupHelper;
import pro.michaelcollard.id34.auth.GoogleAuthHelper;
import pro.michaelcollard.id34.data.HeatWord;
import pro.michaelcollard.id34.data.Idea;
import pro.michaelcollard.id34.data.IdeasDatabaseHelper;
import pro.michaelcollard.id34.data.IdeasRepository;
import pro.michaelcollard.id34.ui.BackupDialogFragment;
import pro.michaelcollard.id34.ui.FlowLayout;
import pro.michaelcollard.id34.ui.IdeaAdapter;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "Id34";
    private static final int DEFAULT_BACKUP_RETENTION = 20;
    private static final int MAX_SEARCH_RESULTS_WITHOUT_SCROLL = 6;
    private static final int SEARCH_RESULTS_SCROLL_HEIGHT_DP = 320;
    private IdeasRepository repository;
    private IdeaAdapter searchAdapter;
    private FlowLayout heatmapContainer;
    private RecyclerView searchResults;
    private EditText searchInput;
    private String currentFilter = "";
    private GoogleAuthHelper googleAuthHelper;
    private DriveBackupHelper driveBackupHelper;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private Runnable searchRunnable;
    private volatile long mutationSerial = 0L;
    private volatile long lastSuccessfulBackupMutationSerial = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new IdeasRepository(this);
        googleAuthHelper = new GoogleAuthHelper(this);
        driveBackupHelper = new DriveBackupHelper(repository);

        GoogleSignInAccount account = googleAuthHelper.getLastSignedIn(this);
        if (account == null) {
            googleAuthHelper.beginSignIn(this);
        } else {
            maybeAutoRestoreLatestBackup();
        }

        View rootContainer = findViewById(R.id.root_container);
        View mainContent = findViewById(R.id.main_content);
        View bottomInputRow = findViewById(R.id.bottom_input_row);
        heatmapContainer = findViewById(R.id.heatmap_container);
        searchResults = findViewById(R.id.search_results);
        searchInput = findViewById(R.id.search_input);
        int mainBasePaddingTop = mainContent.getPaddingTop();
        int bottomInputBasePaddingBottom = bottomInputRow.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(rootContainer, (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(systemBarsInsets.bottom, imeInsets.bottom);
            mainContent.setPadding(
                    mainContent.getPaddingLeft(),
                    mainBasePaddingTop + systemBarsInsets.top,
                    mainContent.getPaddingRight(),
                    mainContent.getPaddingBottom()
            );
            bottomInputRow.setPadding(
                    bottomInputRow.getPaddingLeft(),
                    bottomInputRow.getPaddingTop(),
                    bottomInputRow.getPaddingRight(),
                    bottomInputBasePaddingBottom + bottomInset
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(rootContainer);

        searchResults.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new IdeaAdapter(this, repository, this::onIdeasChanged);
        searchResults.setAdapter(searchAdapter);

        EditText ideaInput = findViewById(R.id.idea_input);
        TextView ideaCounter = findViewById(R.id.idea_counter);
        findViewById(R.id.add_idea_button).setOnClickListener(v -> {
            String content = ideaInput.getText().toString().trim();
            if (content.isEmpty()) {
                return;
            }
            repository.addIdea(content);
            ideaInput.setText("");
            onIdeasChanged();
        });
        ideaInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ideaCounter.setText(s.length() + "/512");
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                debounceSearch(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        findViewById(R.id.open_settings_button).setOnClickListener(v -> {
            BackupDialogFragment dialog = new BackupDialogFragment(repository, this::requestBackup, this::requestPurge);
            dialog.show(getSupportFragmentManager(), "backups");
        });

        refreshAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GoogleAuthHelper.RC_AUTH_RECOVER) {
            maybeAutoRestoreLatestBackup();
            return;
        }
        if (requestCode != GoogleAuthHelper.RC_SIGN_IN) {
            return;
        }
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            task.getResult(ApiException.class);
            toast("Signed in to Google.");
            maybeAutoRestoreLatestBackup();
        } catch (ApiException e) {
            toast("Google sign-in failed: " + e.getStatusCode());
        }
    }

    private void refreshAll() {
        refreshHeatmap();
        if (currentFilter == null || currentFilter.isEmpty()) {
            searchResults.setVisibility(View.GONE);
            return;
        }
        runSearch(currentFilter);
    }

    private void maybeAutoRestoreLatestBackup() {
        ioExecutor.execute(() -> {
            try {
                int localIdeas = repository.getActiveIdeaCount();
                if (localIdeas > 0) {
                    Log.i(LOG_TAG, "Skipping auto-restore; local ideas count is " + localIdeas);
                    return;
                }
                String token = googleAuthHelper.getAccessToken(this);
                String folderId = driveBackupHelper.findOrCreateId34Folder(token);
                if (folderId == null) {
                    Log.w(LOG_TAG, "Drive folder Id34 could not be found or created.");
                    runOnUiThread(() -> toast("Drive folder Id34 unavailable."));
                    return;
                }
                List<DriveBackupHelper.DriveBackupFile> files = driveBackupHelper.listBackupFiles(token, folderId);
                Log.i(LOG_TAG, "Drive backup files discovered: " + files.size());
                if (files.isEmpty()) {
                    runOnUiThread(() -> toast("No cloud backups found in Id34."));
                    return;
                }
                File dbFile = getDatabasePath(IdeasDatabaseHelper.DB_NAME);
                driveBackupHelper.downloadBackupFile(token, files.get(0).id, dbFile);
                runOnUiThread(() -> {
                    toast("Latest cloud backup restored.");
                    refreshAll();
                });
            } catch (UserRecoverableAuthException e) {
                Log.w(LOG_TAG, "User action required to grant Drive auth scope.", e);
                runOnUiThread(() -> startActivityForResult(e.getIntent(), GoogleAuthHelper.RC_AUTH_RECOVER));
            } catch (Exception e) {
                Log.e(LOG_TAG, "Auto-restore failed.", e);
                runOnUiThread(() -> toast("Restore failed: " + e.getMessage()));
            }
        });
    }

    private void debounceSearch(String q) {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        searchRunnable = () -> runSearch(q);
        searchHandler.postDelayed(searchRunnable, 180);
    }

    private void runSearch(String query) {
        currentFilter = query;
        if (query.isEmpty()) {
            searchAdapter.setIdeas(Collections.emptyList());
            searchResults.setVisibility(View.GONE);
            return;
        }
        List<Idea> results = repository.searchIdeas(query);
        searchAdapter.setIdeas(results);
        if (results.isEmpty()) {
            searchResults.setVisibility(View.GONE);
            return;
        }
        updateSearchResultsHeight(results.size());
        searchResults.setVisibility(View.VISIBLE);
    }

    private void requestBackup(int retention) {
        requestBackup(retention, true, "manual", false);
    }

    private void requestLifecycleBackupIfDirty(String trigger) {
        requestBackup(DEFAULT_BACKUP_RETENTION, false, trigger, true);
    }

    private void requestBackup(int retention, boolean showToasts, String trigger, boolean onlyIfDirty) {
        ioExecutor.execute(() -> {
            long requestedMutationSerial = mutationSerial;
            if (onlyIfDirty && requestedMutationSerial <= lastSuccessfulBackupMutationSerial) {
                Log.i(LOG_TAG, "Skipping auto-backup on " + trigger + "; no idea changes since last successful backup.");
                return;
            }
            try {
                String token = googleAuthHelper.getAccessToken(this);
                String folderId = driveBackupHelper.findOrCreateId34Folder(token);
                if (folderId == null) {
                    throw new IllegalStateException("Unable to find or create Id34 folder.");
                }
                repository.listActiveIdeas();
                File dbFile = getDatabasePath(IdeasDatabaseHelper.DB_NAME);
                if (!dbFile.exists()) {
                    throw new IllegalStateException("Local database file not found.");
                }
                int ideaCount = repository.getActiveIdeaCount();
                driveBackupHelper.uploadBackup(token, folderId, dbFile, ideaCount, retention);
                lastSuccessfulBackupMutationSerial = mutationSerial;
                if (showToasts) {
                    runOnUiThread(() -> toast("Backup complete."));
                } else {
                    Log.i(LOG_TAG, "Auto-backup complete on " + trigger + ".");
                }
            } catch (Exception e) {
                if (showToasts) {
                    runOnUiThread(() -> toast("Backup failed: " + e.getMessage()));
                } else {
                    Log.w(LOG_TAG, "Auto-backup failed on " + trigger + ".", e);
                }
            }
        });
    }

    private void requestPurge(int retention) {
        ioExecutor.execute(() -> {
            try {
                String token = googleAuthHelper.getAccessToken(this);
                String folderId = driveBackupHelper.findOrCreateId34Folder(token);
                if (folderId == null) {
                    throw new IllegalStateException("Unable to find or create Id34 folder.");
                }
                driveBackupHelper.purgeOldBackups(token, folderId, retention);
                runOnUiThread(() -> toast("Purge complete."));
            } catch (Exception e) {
                runOnUiThread(() -> toast("Purge failed: " + e.getMessage()));
            }
        });
    }

    private void refreshHeatmap() {
        heatmapContainer.removeAllViews();
        List<HeatWord> words = repository.computeHeatmap();
        for (HeatWord word : words) {
            TextView chip = new TextView(this);
            chip.setText(word.word);
            chip.setPadding(dp(12), dp(8), dp(12), dp(8));
            chip.setTextSize(12 + word.heat * 1.6f);
            chip.setTextColor(Color.WHITE);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(18));
            bg.setColor(heatColor(word.heat));
            chip.setBackground(bg);
            chip.setOnClickListener(v -> {
                if (word.word.equals(currentFilter) && searchResults.getVisibility() == View.VISIBLE) {
                    clearFilter();
                    return;
                }
                currentFilter = word.word;
                searchInput.setText(word.word);
                searchInput.setSelection(word.word.length());
                runSearch(word.word);
            });
            heatmapContainer.addView(chip);
        }
    }
    private void clearFilter() {
        currentFilter = "";
        searchInput.setText("");
        searchAdapter.setIdeas(Collections.emptyList());
        searchResults.setVisibility(View.GONE);
    }

    private void updateSearchResultsHeight(int resultCount) {
        ViewGroup.LayoutParams params = searchResults.getLayoutParams();
        int targetHeight = resultCount <= MAX_SEARCH_RESULTS_WITHOUT_SCROLL ? ViewGroup.LayoutParams.WRAP_CONTENT : dp(SEARCH_RESULTS_SCROLL_HEIGHT_DP);
        if (params.height == targetHeight) {
            return;
        }
        params.height = targetHeight;
        searchResults.setLayoutParams(params);
    }


    private void onIdeasChanged() {
        mutationSerial++;
        refreshAll();
    }


    @ColorInt
    private int heatColor(int heat) {
        if (heat <= 1) return getColorCompat(R.color.heat_1);
        if (heat == 2) return getColorCompat(R.color.heat_2);
        if (heat == 3) return getColorCompat(R.color.heat_3);
        if (heat == 4) return getColorCompat(R.color.heat_4);
        if (heat == 5) return getColorCompat(R.color.heat_5);
        return getColorCompat(R.color.heat_6);
    }

    private int getColorCompat(int id) {
        return androidx.core.content.ContextCompat.getColor(this, id);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        requestLifecycleBackupIfDirty("pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestLifecycleBackupIfDirty("stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }
}