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

    private Recipe existingRecipe = null;

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

        // Check if we are editing an existing recipe
        if (getArguments() != null && getArguments().containsKey("recipe")) {
            existingRecipe = (Recipe) getArguments().getSerializable("recipe");
            etTitle.setText(existingRecipe.getTitle());
            etIngredients.setText(existingRecipe.getIngredients());
            etInstructions.setText(existingRecipe.getInstructions());
        }

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

            if (existingRecipe != null) {
                // Update existing recipe
                existingRecipe.setTitle(title);
                existingRecipe.setIngredients(ingredients);
                existingRecipe.setInstructions(instructions);
                RecipeRepository.getInstance().update(existingRecipe);
                Toast.makeText(requireContext(), "Recipe updated", Toast.LENGTH_SHORT).show();
            } else {
                // Create new recipe
                Recipe recipe = new Recipe(title, ingredients, instructions);
                RecipeRepository.getInstance().add(recipe);
                Toast.makeText(requireContext(), "Recipe saved", Toast.LENGTH_SHORT).show();
            }

            // Navigate back to My Recipes
            Navigation.findNavController(v).popBackStack();
        });
    }
}
