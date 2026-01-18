package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeFragment extends Fragment {

    public AddRecipeFragment() {
        super(R.layout.fragment_add_recipe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etCategory = view.findViewById(R.id.et_category);
        EditText etIngredients = view.findViewById(R.id.et_ingredients);
        EditText etInstructions = view.findViewById(R.id.et_instructions);

        CheckBox cbMain = view.findViewById(R.id.cb_main_courses);
        CheckBox cbDesserts = view.findViewById(R.id.cb_desserts);
        CheckBox cbVegan = view.findViewById(R.id.cb_vegan_vegetarian);
        CheckBox cbBreakfast = view.findViewById(R.id.cb_breakfast);

        Button btnSave = view.findViewById(R.id.btn_save_recipe);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String ingredients = etIngredients.getText().toString().trim();
            String instructions = etInstructions.getText().toString().trim();
            String customCategory = etCategory.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required");
                return;
            }

            // This is the crucial part that gathers the categories
            List<String> categories = new ArrayList<>();
            if (!customCategory.isEmpty()) {
                categories.add(customCategory);
            }
            if (cbMain.isChecked()) categories.add("Main Courses");
            if (cbDesserts.isChecked()) categories.add("Desserts");
            if (cbVegan.isChecked()) categories.add("Vegan / Vegetarian");
            if (cbBreakfast.isChecked()) categories.add("Breakfast");

            Recipe recipe = new Recipe(title, ingredients, instructions);
            // This line ensures the categories are set on the recipe object before saving
            recipe.setCategories(categories);

            RecipeRepository.getInstance().add(recipe, new RecipeRepository.RecipeCallback() {
                @Override
                public void onSuccess() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show();
                            // Navigate back only after the recipe is successfully saved
                            Navigation.findNavController(v).navigate(R.id.action_addRecipeFragment_to_myRecipesFragment);
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        });
    }
}