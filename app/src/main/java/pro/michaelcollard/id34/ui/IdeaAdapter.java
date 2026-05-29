package pro.michaelcollard.id34.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pro.michaelcollard.id34.R;
import pro.michaelcollard.id34.data.Idea;
import pro.michaelcollard.id34.data.IdeasRepository;

import java.util.ArrayList;
import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.Holder> {
    private final Context context;
    private final IdeasRepository repository;
    private final Runnable onChanged;
    private final List<Idea> ideas = new ArrayList<>();

    public IdeaAdapter(Context context, IdeasRepository repository, Runnable onChanged) {
        this.context = context;
        this.repository = repository;
        this.onChanged = onChanged;
    }

    public void setIdeas(List<Idea> rows) {
        ideas.clear();
        ideas.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_idea, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Idea idea = ideas.get(position);
        holder.ideaText.setText(idea.content);
        holder.editButton.setOnClickListener(v -> showEditDialog(idea));
        holder.deleteButton.setOnClickListener(v -> {
            repository.softDeleteIdea(idea.id);
            onChanged.run();
        });
    }

    private void showEditDialog(Idea idea) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(idea.content);
        new AlertDialog.Builder(context)
                .setTitle("Edit idea")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    repository.updateIdea(idea.id, input.getText().toString().trim());
                    onChanged.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return ideas.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView ideaText;
        final ImageButton editButton;
        final ImageButton deleteButton;

        Holder(@NonNull View itemView) {
            super(itemView);
            ideaText = itemView.findViewById(R.id.idea_text);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
