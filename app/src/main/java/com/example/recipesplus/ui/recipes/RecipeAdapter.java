package com.example.recipesplus.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    // This method will safely update the list of recipes and refresh the display.
    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        this.recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.title.setText(recipe.getTitle());

        String preview;
        if (recipe.getIngredients() != null && !recipe.getIngredients().trim().isEmpty()) {
            preview = recipe.getIngredients().trim();
        } else if (recipe.getInstructions() != null) {
            preview = recipe.getInstructions().trim();
        } else {
            preview = "";
        }

        if (preview.length() > 80) {
            preview = preview.substring(0, 80) + "…";
        }
        holder.preview.setText(preview);

        if ("online".equals(recipe.getSource())) {
            holder.favoriteIcon.setVisibility(View.VISIBLE);
            holder.favoriteIcon.setImageResource(
                    recipe.isFavorite()
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off
            );
        } else {
            holder.favoriteIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView preview;
        ImageView favoriteIcon;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_recipe_title);
            preview = itemView.findViewById(R.id.tv_recipe_preview);
            favoriteIcon = itemView.findViewById(R.id.iv_favorite);
        }
    }
}