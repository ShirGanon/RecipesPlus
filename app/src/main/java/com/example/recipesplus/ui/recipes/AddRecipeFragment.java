package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
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
        super.onViewCreated(view, savedInstanceState);

        EditText title = view.findViewById(R.id.et_title);
        EditText ingredients = view.findViewById(R.id.et_ingredients);
        EditText instructions = view.findViewById(R.id.et_instructions);

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String titleText = safeText(title);
            String ingredientsText = safeText(ingredients);
            String instructionsText = safeText(instructions);

            boolean isValid = true;

            if (titleText.isEmpty()) {
                title.setError("Title is required");
                isValid = false;
            } else {
                title.setError(null);
            }

            if (ingredientsText.isEmpty()) {
                ingredients.setError("Ingredients are required");
                isValid = false;
            } else {
                ingredients.setError(null);
            }

            if (!isValid) {
                Toast.makeText(requireContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            RecipeRepository.getInstance().add(
                    new Recipe(
                            titleText,
                            ingredientsText,
                            instructionsText
                    )
            );

            Navigation.findNavController(v)
                    .navigate(R.id.action_addRecipeFragment_to_myRecipesFragment);
        });
    }

    private String safeText(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }
}
