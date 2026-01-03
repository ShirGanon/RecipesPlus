package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

        String recipeId = getArguments() != null ? getArguments().getString("recipeId") : null;
        if (recipeId == null) {
            tvTitle.setText("Recipe not found");
            return;
        }

        Recipe recipe = RecipeRepository.getInstance().getById(recipeId);
        if (recipe == null) {
            tvTitle.setText("Recipe not found");
            return;
        }

        tvTitle.setText(recipe.getTitle());
        tvIngredients.setText(recipe.getIngredients());
        tvInstructions.setText(recipe.getInstructions());
    }
}
