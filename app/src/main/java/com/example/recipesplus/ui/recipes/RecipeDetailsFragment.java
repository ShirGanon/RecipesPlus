package com.example.recipesplus.ui.recipes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;

public class RecipeDetailsFragment extends Fragment {

    // Displays recipe details from local storage or online preview args.
    public RecipeDetailsFragment() {
        super(R.layout.fragment_recipe_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivRecipeImage = view.findViewById(R.id.iv_recipe_image);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvIngredients = view.findViewById(R.id.tv_ingredients);
        TextView tvInstructions = view.findViewById(R.id.tv_instructions);

        Button btnFavorite = view.findViewById(R.id.btn_favorite);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        // This screen can show either a local recipe (by id) or an online preview (via args).
        Bundle args = getArguments();
        String recipeId = args != null ? args.getString("recipeId") : null;

        if (recipeId != null) {
            // Local recipe flow: full actions (favorite/delete).
            final Recipe recipe = RecipeRepository.getInstance().getById(recipeId);
            if (recipe == null) {
                tvTitle.setText("Recipe not found");
                btnFavorite.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                return;
            }

            tvTitle.setText(recipe.getTitle());
            tvIngredients.setText(nullToEmpty(recipe.getIngredients()));
            tvInstructions.setText(nullToEmpty(recipe.getInstructions()));

            // Show image if available
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                ivRecipeImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(recipe.getImageUrl())
                        .centerCrop()
                        .into(ivRecipeImage);
            } else {
                ivRecipeImage.setVisibility(View.GONE);
            }

            updateFavoriteButton(btnFavorite, recipe.isFavorite());
            btnFavorite.setOnClickListener(v -> {
                boolean isCurrentlyFavorite = recipe.isFavorite();
                recipe.setFavorite(!isCurrentlyFavorite);
                RecipeRepository.getInstance().update(recipe);
                updateFavoriteButton(btnFavorite, recipe.isFavorite());
                Toast.makeText(requireContext(), recipe.isFavorite() ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
            });

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete recipe")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            RecipeRepository.getInstance().delete(recipe.getId());
                            goBackSafe();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
            return;
        }

        // Online preview flow: read-only content.
        String title = args != null ? args.getString("title") : null;
        String ingredients = args != null ? args.getString("ingredients") : null;
        String instructions = args != null ? args.getString("instructions") : null;

        if (title == null && ingredients == null && instructions == null) {
            tvTitle.setText("Recipe not found");
            btnFavorite.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            return;
        }

        tvTitle.setText(nullToEmpty(title));
        tvIngredients.setText(nullToEmpty(ingredients));
        tvInstructions.setText(nullToEmpty(instructions));
        ivRecipeImage.setVisibility(View.GONE);
        btnFavorite.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void updateFavoriteButton(Button btnFavorite, boolean isFav) {
        if (btnFavorite == null) return;
        btnFavorite.setText(isFav ? "♥ Favorite" : "♡ Favorite");
    }

    private void goBackSafe() {
        try {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        } catch (Exception ignored) { }
    }
}
