package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;

public class AddRecipeFragment extends Fragment {

    public AddRecipeFragment() {
        super(R.layout.fragment_add_recipe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Input fields
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etIngredients = view.findViewById(R.id.et_ingredients);
        EditText etInstructions = view.findViewById(R.id.et_instructions);

        // Save button
        Button btnSave = view.findViewById(R.id.btn_save_recipe);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String ingredients = etIngredients.getText().toString().trim();
            String instructions = etInstructions.getText().toString().trim();

            // Basic validation
            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required");
                etTitle.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(ingredients)) {
                etIngredients.setError("Ingredients are required");
                etIngredients.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(instructions)) {
                etInstructions.setError("Instructions are required");
                etInstructions.requestFocus();
                return;
            }

            // Create Recipe using the existing constructor
            Recipe recipe = new Recipe(title, ingredients, instructions);
            recipe.setFavorite(false);

            // Save recipe to repository
            RecipeRepository.getInstance().add(recipe);

            Toast.makeText(requireContext(), "Recipe saved", Toast.LENGTH_SHORT).show();

            // Navigate back to My Recipes
            Navigation.findNavController(v)
                    .navigate(R.id.action_addRecipeFragment_to_myRecipesFragment);
        });
    }
}
