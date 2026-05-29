package pro.michaelcollard.id34.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pro.michaelcollard.id34.R;
import pro.michaelcollard.id34.data.IdeasRepository;

import java.util.ArrayList;
import java.util.List;

public class BackupDialogFragment extends DialogFragment {
    public interface RetentionAction {
        void run(int retention);
    }
    private final IdeasRepository repository;
    private final RetentionAction onBackupRequested;
    private final RetentionAction onPurgeRequested;

    public BackupDialogFragment(IdeasRepository repository, RetentionAction onBackupRequested, RetentionAction onPurgeRequested) {
        this.repository = repository;
        this.onBackupRequested = onBackupRequested;
        this.onPurgeRequested = onPurgeRequested;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_backup, null);
        Spinner retentionSpinner = root.findViewById(R.id.retention_spinner);
        List<String> values = new ArrayList<>();
        for (int i = 1; i <= 99; i++) {
            values.add(String.valueOf(i));
        }
        retentionSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, values));
        retentionSpinner.setSelection(19);

        RecyclerView list = root.findViewById(R.id.backups_list);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        SimpleStringAdapter adapter = new SimpleStringAdapter();
        list.setAdapter(adapter);
        adapter.setRows(repository.listBackupRegistryFilenames());

        root.findViewById(R.id.backup_now_button).setOnClickListener(v -> {
            int retention = Integer.parseInt(retentionSpinner.getSelectedItem().toString());
            onBackupRequested.run(retention);
            Toast.makeText(requireContext(), "Backup requested.", Toast.LENGTH_SHORT).show();
        });
        root.findViewById(R.id.purge_button).setOnClickListener(v -> {
            int retention = Integer.parseInt(retentionSpinner.getSelectedItem().toString());
            onPurgeRequested.run(retention);
            Toast.makeText(requireContext(), "Purge requested.", Toast.LENGTH_SHORT).show();
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle("Backups")
                .setView(root)
                .setNegativeButton("Close", null)
                .create();
    }
}
