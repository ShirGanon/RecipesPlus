package com.example.recipesplus.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.OnlineRecipe;
import com.example.recipesplus.model.Recipe;

import java.util.List;

public class OnlineRecipeAdapter extends RecyclerView.Adapter<OnlineRecipeAdapter.VH> {

    public interface OnSaveListener {
        void onSave(OnlineRecipe recipe, boolean asFavorite);
    }

    private final List<OnlineRecipe> items;
    private final OnSaveListener onSaveListener;
    private final RecipeRepository recipeRepository;

    public OnlineRecipeAdapter(List<OnlineRecipe> items, OnSaveListener onSaveListener) {
        this.items = items;
        this.onSaveListener = onSaveListener;
        this.recipeRepository = RecipeRepository.getInstance();
    }

    // Overloaded constructor for backward compatibility (ignored)
    public OnlineRecipeAdapter(List<OnlineRecipe> items, java.util.Set<String> unused, OnSaveListener onSaveListener) {
        this(items, onSaveListener);
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

        Recipe existing = recipeRepository.getByTitle(r.getTitle());
        boolean isSaved = existing != null;
        boolean isFavorite = isSaved && existing.isFavorite();

        // Configure Save Button
        if (isSaved) {
            h.btnSave.setEnabled(true);
            h.btnSave.setText("Unsave");
            h.btnSave.setOnClickListener(v -> {
                if (onSaveListener != null) {
                    onSaveListener.onSave(r, false);
                }
            });
        } else {
            h.btnSave.setEnabled(true);
            h.btnSave.setText("Save");
            h.btnSave.setOnClickListener(v -> {
                if (onSaveListener != null) {
                    onSaveListener.onSave(r, false);
                }
            });
        }

        // Configure Favorite Icon - only available for saved recipes
        if (isSaved) {
            h.ivFavorite.setVisibility(View.VISIBLE);
            h.ivFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
            h.ivFavorite.setAlpha(isFavorite ? 1.0f : 0.6f);
            // Toggle favorite on click
            h.ivFavorite.setOnClickListener(v -> {
                if (onSaveListener != null) {
                    onSaveListener.onSave(r, !isFavorite);
                }
            });
        } else {
            h.ivFavorite.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, preview;
        ImageView ivFavorite;
        Button btnSave;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            preview = itemView.findViewById(R.id.tv_preview);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            btnSave = itemView.findViewById(R.id.btn_save);
        }
    }
}
