package com.example.recipesplus.ui.recipes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;

public class RecipeDetailsFragment extends Fragment {

    public RecipeDetailsFragment() {
        super(R.layout.fragment_recipe_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvIngredients = view.findViewById(R.id.tv_ingredients);
        TextView tvInstructions = view.findViewById(R.id.tv_instructions);

        Button btnFavorite = view.findViewById(R.id.btn_favorite);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        String recipeId = getArguments() != null ? getArguments().getString("recipeId") : null;
        if (recipeId == null) {
            tvTitle.setText("Recipe not found");
            btnFavorite.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            return;
        }

        final Recipe recipe = RecipeRepository.getInstance().getById(recipeId);
        if (recipe == null) {
            tvTitle.setText("Recipe not found");
            btnFavorite.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            return;
        }

        tvTitle.setText(recipe.getTitle());
        tvIngredients.setText("Ingredients:\n" + nullToEmpty(recipe.getIngredients()));
        tvInstructions.setText("Instructions:\n" + nullToEmpty(recipe.getInstructions()));

        // Hide favorite button for manual recipes
        if ("manual".equals(recipe.getSource())) {
            btnFavorite.setVisibility(View.GONE);
        } else {
            btnFavorite.setVisibility(View.VISIBLE);
            updateFavoriteButton(btnFavorite, recipe.isFavorite());
            btnFavorite.setOnClickListener(v -> {
                boolean isCurrentlyFavorite = recipe.isFavorite();
                recipe.setFavorite(!isCurrentlyFavorite);
                RecipeRepository.getInstance().update(recipe);
                updateFavoriteButton(btnFavorite, recipe.isFavorite());
                Toast.makeText(requireContext(), recipe.isFavorite() ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
            });
        }

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
