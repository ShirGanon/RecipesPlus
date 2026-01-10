package com.example.recipesplus.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.model.OnlineRecipe;

import java.util.List;
import java.util.Set;

public class OnlineRecipeAdapter extends RecyclerView.Adapter<OnlineRecipeAdapter.VH> {

    public interface OnSaveClick {
        /**
         * @return true only if saved successfully (no duplicates)
         */
        boolean onSave(OnlineRecipe recipe);
    }

    private final List<OnlineRecipe> items;
    private final Set<String> savedKeys;
    private final OnSaveClick onSaveClick;

    public OnlineRecipeAdapter(List<OnlineRecipe> items, Set<String> savedKeys, OnSaveClick onSaveClick) {
        this.items = items;
        this.savedKeys = savedKeys;
        this.onSaveClick = onSaveClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_online_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        OnlineRecipe r = items.get(position);

        h.title.setText(r.getTitle());

        String preview = !r.getInstructions().isEmpty() ? r.getInstructions() : r.getSummary();
        h.preview.setText(preview.isEmpty() ? "No description" : preview);

        String key = buildKey(r);
        boolean alreadySaved = savedKeys.contains(key);

        if (alreadySaved) {
            h.btnSave.setEnabled(false);
            h.btnSave.setText("Saved");
            h.btnSave.setOnClickListener(null);
        } else {
            h.btnSave.setEnabled(true);
            h.btnSave.setText("Save");

            h.btnSave.setOnClickListener(v -> {
                boolean saved = false;
                if (onSaveClick != null) {
                    saved = onSaveClick.onSave(r);
                }
                if (saved) {
                    savedKeys.add(key);
                    notifyItemChanged(h.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String buildKey(OnlineRecipe r) {
        return (r.getTitle() == null ? "" : r.getTitle().trim().toLowerCase());
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, preview;
        Button btnSave;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            preview = itemView.findViewById(R.id.tv_preview);
            btnSave = itemView.findViewById(R.id.btn_save);
        }
    }
}
