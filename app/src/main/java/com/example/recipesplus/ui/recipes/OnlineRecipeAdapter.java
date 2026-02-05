package com.example.recipesplus.ui.recipes;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.OnlineRecipe;
import com.example.recipesplus.model.Recipe;

import java.util.List;

public class OnlineRecipeAdapter extends RecyclerView.Adapter<OnlineRecipeAdapter.VH> {

    // Adapter for Spoonacular results (save/favorite actions).
    public interface OnSaveListener {
        void onSave(OnlineRecipe recipe, boolean asFavorite);
    }
    public interface OnItemClickListener {
        void onItemClick(OnlineRecipe recipe);
    }

    private final List<OnlineRecipe> items;
    private final OnSaveListener onSaveListener;
    // Used to determine saved/favorite state of online results.
    private final RecipeRepository recipeRepository;
    private final boolean showFavoritePrompt;
    private final OnItemClickListener onItemClickListener;

    public OnlineRecipeAdapter(List<OnlineRecipe> items, OnSaveListener onSaveListener) {
        this(items, false, onSaveListener);
    }

    public OnlineRecipeAdapter(List<OnlineRecipe> items, boolean showFavoritePrompt, OnSaveListener onSaveListener) {
        this(items, showFavoritePrompt, onSaveListener, null);
    }

    public OnlineRecipeAdapter(List<OnlineRecipe> items, boolean showFavoritePrompt, OnSaveListener onSaveListener, OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onSaveListener = onSaveListener;
        this.recipeRepository = RecipeRepository.getInstance();
        this.showFavoritePrompt = showFavoritePrompt;
        this.onItemClickListener = onItemClickListener;
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

        // Prefer instructions for preview; fall back to summary.
        String preview = !r.getInstructions().isEmpty() ? r.getInstructions() : r.getSummary();
        h.preview.setText(preview.isEmpty() ? "No description" : preview);

        // Determine whether the recipe is already saved locally.
        Recipe existing = recipeRepository.getByTitle(r.getTitle());
        boolean isSaved = existing != null;
        boolean isFavorite = isSaved && existing.isFavorite();

        int saveColor = ContextCompat.getColor(h.itemView.getContext(), R.color.recipe_save);
        int unsafeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.recipe_unsafe);

        // Save button toggles local persistence for this recipe.
        if (isSaved) {
            h.btnSave.setEnabled(true);
            h.btnSave.setText("Unsave");
            h.btnSave.setBackgroundTintList(ColorStateList.valueOf(unsafeColor));
            h.btnSave.setOnClickListener(v -> {
                if (onSaveListener != null) {
                    onSaveListener.onSave(r, false);
                }
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
                }
            });
        } else {
            h.btnSave.setEnabled(true);
            h.btnSave.setText("Save");
            h.btnSave.setBackgroundTintList(ColorStateList.valueOf(saveColor));
            h.btnSave.setOnClickListener(v -> {
                if (onSaveListener == null) return;
                onSaveListener.onSave(r, false);
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
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
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos);
                }
            });
        } else {
            h.ivFavorite.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(r);
            }
        });
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
