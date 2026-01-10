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

import java.lang.reflect.Method;

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
            safeDisableButtons(btnFavorite, btnDelete);
            return;
        }

        Recipe recipe = RecipeRepository.getInstance().getById(recipeId);
        if (recipe == null) {
            tvTitle.setText("Recipe not found");
            safeDisableButtons(btnFavorite, btnDelete);
            return;
        }

        tvTitle.setText(recipe.getTitle());
        tvIngredients.setText("Ingredients:\n" + nullToEmpty(recipe.getIngredients()));
        tvInstructions.setText("Instructions:\n" + nullToEmpty(recipe.getInstructions()));

        boolean isFav = readFavoriteSafe(recipe);
        updateFavoriteButton(btnFavorite, isFav);

        btnFavorite.setOnClickListener(v -> {
            boolean current = readFavoriteSafe(recipe);
            boolean next = !current;

            boolean updated = writeFavoriteSafe(recipeId, recipe, next);
            if (!updated) {
                Toast.makeText(requireContext(), "Favorite update is not supported yet", Toast.LENGTH_SHORT).show();
                return;
            }

            updateFavoriteButton(btnFavorite, next);
            Toast.makeText(requireContext(), next ? "Added to favorites" : "Removed from favorites", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete recipe")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = deleteByIdSafe(recipeId);
                        if (!deleted) {
                            Toast.makeText(requireContext(), "Delete is not supported yet", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        goBackSafe();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void safeDisableButtons(Button btnFavorite, Button btnDelete) {
        if (btnFavorite != null) btnFavorite.setEnabled(false);
        if (btnDelete != null) btnDelete.setEnabled(false);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void updateFavoriteButton(Button btnFavorite, boolean isFav) {
        if (btnFavorite == null) return;
        btnFavorite.setText(isFav ? "♥ Favorite" : "♡ Favorite");
    }

    private boolean readFavoriteSafe(Recipe recipe) {
        if (recipe == null) return false;

        String[] candidates = new String[]{"isFavorite", "getFavorite", "isFav", "getFav"};
        for (String name : candidates) {
            try {
                Method m = recipe.getClass().getMethod(name);
                Object res = m.invoke(recipe);
                if (res instanceof Boolean) return (Boolean) res;
            } catch (Exception ignored) { }
        }

        return false;
    }

    private boolean writeFavoriteSafe(String recipeId, Recipe recipe, boolean value) {
        Object repo = RecipeRepository.getInstance();

        try {
            Method m = repo.getClass().getMethod("toggleFavorite", String.class);
            m.invoke(repo, recipeId);
            return true;
        } catch (Exception ignored) { }

        try {
            Method m = repo.getClass().getMethod("setFavorite", String.class, boolean.class);
            m.invoke(repo, recipeId, value);
            return true;
        } catch (Exception ignored) { }

        try {
            Method m = repo.getClass().getMethod("setFavorite", String.class, Boolean.class);
            m.invoke(repo, recipeId, value);
            return true;
        } catch (Exception ignored) { }

        if (recipe != null) {
            String[] modelSetters = new String[]{"setFavorite", "setIsFavorite", "setFav"};
            for (String name : modelSetters) {
                try {
                    Method m = recipe.getClass().getMethod(name, boolean.class);
                    m.invoke(recipe, value);
                    return true;
                } catch (Exception ignored) { }
            }
        }

        return false;
    }

    private boolean deleteByIdSafe(String recipeId) {
        Object repo = RecipeRepository.getInstance();

        String[] candidates = new String[]{"deleteById", "delete", "removeById", "remove"};
        for (String name : candidates) {
            try {
                Method m = repo.getClass().getMethod(name, String.class);
                m.invoke(repo, recipeId);
                return true;
            } catch (Exception ignored) { }
        }

        return false;
    }

    private void goBackSafe() {
        try {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        } catch (Exception ignored) { }
    }
}
