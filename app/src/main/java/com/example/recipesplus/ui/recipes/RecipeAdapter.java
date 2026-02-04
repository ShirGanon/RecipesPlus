package com.example.recipesplus.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipesplus.R;
import com.example.recipesplus.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
        void onEditClick(Recipe recipe);
        void onFavoriteClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = (recipes != null) ? new ArrayList<>(recipes) : new ArrayList<>();
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        if (newRecipes != null) recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.title.setText(recipe.getTitle() == null ? "" : recipe.getTitle());

        String preview = "";
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty())
            preview = recipe.getIngredients();
        else if (recipe.getInstructions() != null)
            preview = recipe.getInstructions();

        if (preview.length() > 80)
            preview = preview.substring(0, 80) + "…";

        holder.preview.setText(preview);

        holder.favoriteIcon.setImageResource(
                recipe.isFavorite()
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(holder.recipeImage.getContext())
                    .load(recipe.getImageUrl())
                    .centerCrop()
                    .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.ic_chef_placeholder);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));

        holder.editIcon.setOnClickListener(v -> listener.onEditClick(recipe));

        holder.favoriteIcon.setOnClickListener(v -> listener.onFavoriteClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView title, preview;
        ImageView favoriteIcon, recipeImage, editIcon;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_recipe_title);
            preview = itemView.findViewById(R.id.tv_recipe_preview);
            favoriteIcon = itemView.findViewById(R.id.iv_favorite);
            recipeImage = itemView.findViewById(R.id.iv_recipe_image);
            editIcon = itemView.findViewById(R.id.iv_edit);
        }
    }
}
