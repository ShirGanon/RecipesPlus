package com.example.recipesplus.ui.recipes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

import java.util.UUID;

public class AddRecipeFragment extends Fragment {

    private ImageView ivRecipeImage;
    private Uri imageUri;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ivRecipeImage.setImageURI(imageUri);
                }
            }
    );

    public AddRecipeFragment() {
        super(R.layout.fragment_add_recipe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Input fields
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etIngredients = view.findViewById(R.id.et_ingredients);
        EditText etInstructions = view.findViewById(R.id.et_instructions);
        ivRecipeImage = view.findViewById(R.id.iv_recipe_image);
        Button btnSelectImage = view.findViewById(R.id.btn_select_image);

        // Save button
        Button btnSave = view.findViewById(R.id.btn_save_recipe);

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

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

            btnSave.setEnabled(false);

            if (imageUri != null) {
                uploadImageAndSaveRecipe(title, ingredients, instructions, v);
            } else {
                saveRecipe(title, ingredients, instructions, null, v);
            }
        });
    }

    private void uploadImageAndSaveRecipe(String title, String ingredients, String instructions, View v) {
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("recipe_images/" + fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveRecipe(title, ingredients, instructions, uri.toString(), v);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    saveRecipe(title, ingredients, instructions, null, v);
                });
    }

    private void saveRecipe(String title, String ingredients, String instructions, String imageUrl, View v) {
        // Create Recipe using the existing constructor
        Recipe recipe = new Recipe(title, ingredients, instructions);
        recipe.setFavorite(false);
        recipe.setImageUrl(imageUrl);

        // Save recipe to repository
        RecipeRepository.getInstance().add(recipe);

        Toast.makeText(requireContext(), "Recipe saved", Toast.LENGTH_SHORT).show();

        // Navigate back to My Recipes
        Navigation.findNavController(v)
                .navigate(R.id.action_addRecipeFragment_to_myRecipesFragment);
    }
}
