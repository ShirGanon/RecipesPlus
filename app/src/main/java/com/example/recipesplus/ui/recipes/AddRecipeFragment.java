package com.example.recipesplus.ui.recipes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddRecipeFragment extends Fragment {

    private ImageView ivRecipeImage;
    private Uri imageUri;

    private Button btnSave;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null && ivRecipeImage != null) {
                        ivRecipeImage.setImageURI(imageUri);
                    }
                }
            }
    );

    public AddRecipeFragment() {
        super(R.layout.fragment_add_recipe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etIngredients = view.findViewById(R.id.et_ingredients);
        EditText etInstructions = view.findViewById(R.id.et_instructions);

        ivRecipeImage = view.findViewById(R.id.iv_recipe_image);
        Button btnSelectImage = view.findViewById(R.id.btn_select_image);

        CheckBox cbMain = view.findViewById(R.id.cb_main_courses);
        CheckBox cbDesserts = view.findViewById(R.id.cb_desserts);
        CheckBox cbVegan = view.findViewById(R.id.cb_vegan_vegetarian);
        CheckBox cbBreakfast = view.findViewById(R.id.cb_breakfast);

        btnSave = view.findViewById(R.id.btn_save_recipe);

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String ingredients = etIngredients.getText().toString().trim();
            String instructions = etInstructions.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Title is required");
                etTitle.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(instructions)) {
                etInstructions.setError("Instructions are required");
                etInstructions.requestFocus();
                return;
            }

            // Collect categories
            List<String> categories = new ArrayList<>();
            if (cbMain.isChecked()) categories.add("Main Courses");
            if (cbDesserts.isChecked()) categories.add("Desserts");
            if (cbVegan.isChecked()) categories.add("Vegan / Vegetarian");
            if (cbBreakfast.isChecked()) categories.add("Breakfast");

            btnSave.setEnabled(false);

            if (imageUri != null) {
                uploadImageAndSaveRecipe(title, ingredients, instructions, categories, v);
            } else {
                saveRecipe(title, ingredients, instructions, categories, null, v);
            }
        });
    }

    private void uploadImageAndSaveRecipe(
            String title,
            String ingredients,
            String instructions,
            List<String> categories,
            View v
    ) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("recipe_images/" + fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                saveRecipe(title, ingredients, instructions, categories, uri.toString(), v)
                        )
                )
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    // Continue saving without image
                    saveRecipe(title, ingredients, instructions, categories, null, v);
                });
    }

    private void saveRecipe(
            String title,
            String ingredients,
            String instructions,
            List<String> categories,
            @Nullable String imageUrl,
            View v
    ) {
        Recipe recipe = new Recipe(title, ingredients, instructions);
        recipe.setFavorite(false);
        recipe.setCategories(categories);
        recipe.setImageUrl(imageUrl);

        RecipeRepository.getInstance().add(recipe, new RecipeRepository.RecipeCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v)
                            .navigate(R.id.action_addRecipeFragment_to_myRecipesFragment);
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
