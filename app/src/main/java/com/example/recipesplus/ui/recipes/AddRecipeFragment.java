package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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
        EditText title = view.findViewById(R.id.et_title);
        EditText ingredients = view.findViewById(R.id.et_ingredients);
        EditText instructions = view.findViewById(R.id.et_instructions);

        view.findViewById(R.id.btn_save).setOnClickListener(v -> {
            RecipeRepository.getInstance().add(
                    new Recipe(
                            title.getText().toString(),
                            ingredients.getText().toString(),
                            instructions.getText().toString()
                    )
            );

            Navigation.findNavController(v)
                    .navigate(R.id.action_addRecipeFragment_to_myRecipesFragment);
        });
    }
}
