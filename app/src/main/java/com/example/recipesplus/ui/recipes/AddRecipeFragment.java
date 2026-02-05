package com.example.recipesplus.ui.recipes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;
import com.example.recipesplus.services.SpoonacularService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AddRecipeFragment extends Fragment {

    // Create or edit a recipe, including image and ingredient picker.
    // Screen supports both add and edit; existingRecipe is set when editing.
    private Recipe existingRecipe = null;

    private ImageView ivRecipeImage;
    private Uri imageUri;

    private Button btnSave;

    private CheckBox cbMain, cbDesserts, cbVegan, cbBreakfast;
    private EditText etIngredients;
    private EditText etIngredientSearch;
    private RecyclerView rvIngredients;
    private ChipGroup cgSelectedIngredients;
    private HorizontalScrollView hsvSelectedIngredients;

    private IngredientsAdapter ingredientsAdapter;
    // Tracks ingredients inserted by the picker (so manual edits aren't removed).
    private final Set<String> autoAddedIngredients = new HashSet<>();
    // Guard to avoid recursive text-change events when we update the field programmatically.
    private boolean isUpdatingIngredientsText = false;
    private List<String> existingIngredientLines = new ArrayList<>();

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // Image chooser result handler.
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    if (imageUri != null && ivRecipeImage != null) {
                        ivRecipeImage.setVisibility(View.VISIBLE);
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
        etIngredients = view.findViewById(R.id.et_ingredients);
        EditText etInstructions = view.findViewById(R.id.et_instructions);
        etIngredientSearch = view.findViewById(R.id.et_ingredient_search_add);
        rvIngredients = view.findViewById(R.id.rv_ingredients_add);
        cgSelectedIngredients = view.findViewById(R.id.cg_selected_ingredients_add);
        hsvSelectedIngredients = view.findViewById(R.id.hsv_selected_ingredients_add);

        ivRecipeImage = view.findViewById(R.id.iv_recipe_image);
        Button btnSelectImage = view.findViewById(R.id.btn_select_image);

        cbMain = view.findViewById(R.id.cb_main_courses);
        cbDesserts = view.findViewById(R.id.cb_desserts);
        cbVegan = view.findViewById(R.id.cb_vegan_vegetarian);
        cbBreakfast = view.findViewById(R.id.cb_breakfast);

        btnSave = view.findViewById(R.id.btn_save_recipe);

        // Ingredient picker (search + list + chips) syncs with the manual field.
        setupIngredientPicker();

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Edit mode: pre-fill with existing recipe data.
        if (getArguments() != null && getArguments().containsKey("recipe")) {
            existingRecipe = (Recipe) getArguments().getSerializable("recipe");
            if (existingRecipe != null) {
                etTitle.setText(nullToEmpty(existingRecipe.getTitle()));
                etIngredients.setText(nullToEmpty(existingRecipe.getIngredients()));
                etInstructions.setText(nullToEmpty(existingRecipe.getInstructions()));
                existingIngredientLines = parseIngredients(nullToEmpty(existingRecipe.getIngredients()));

                applyCategoriesToCheckboxes(existingRecipe.getCategories());

                if (existingRecipe.getImageUrl() != null && !existingRecipe.getImageUrl().isEmpty()) {
                    ivRecipeImage.setVisibility(View.VISIBLE);
                    Glide.with(requireContext())
                            .load(existingRecipe.getImageUrl())
                            .centerCrop()
                            .into(ivRecipeImage);
                } else {
                    ivRecipeImage.setVisibility(View.GONE);
                }
            }
        } else {
            ivRecipeImage.setVisibility(View.GONE);
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

            List<String> categories = collectCategoriesFromCheckboxes();

            btnSave.setEnabled(false);

            // If user chose a new image -> upload it first.
            if (imageUri != null) {
                uploadImageAndThenSaveOrUpdate(title, ingredients, instructions, categories, v);
            } else {
                // No new image picked: keep old (edit) or null (add)
                String imageUrlToUse = (existingRecipe != null) ? existingRecipe.getImageUrl() : null;
                saveOrUpdate(title, ingredients, instructions, categories, imageUrlToUse, v);
            }
        });
    }

    private void setupIngredientPicker() {
        if (rvIngredients == null || etIngredientSearch == null) {
            return;
        }

        // Ingredient list with checkbox selections.
        rvIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>());
        ingredientsAdapter.setOnIngredientChangedListener((ingredient, isSelected) -> {
            if (isSelected) {
                addChipToGroup(ingredient);
                addIngredientToText(ingredient);
            } else {
                removeChipFromGroup(ingredient);
                removeIngredientFromTextIfAutoAdded(ingredient);
            }
        });
        rvIngredients.setAdapter(ingredientsAdapter);

        etIngredientSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ingredientsAdapter != null) {
                    ingredientsAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        if (etIngredients != null) {
            // Keep autoAddedIngredients in sync when user edits the text manually.
            etIngredients.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    if (isUpdatingIngredientsText) return;
                    Set<String> current = new LinkedHashSet<>(parseIngredients(s.toString()));
                    autoAddedIngredients.retainAll(current);
                }
            });
        }

        loadIngredients();
    }

    private void loadIngredients() {
        new SpoonacularService().getPopularIngredients(requireContext(), new SpoonacularService.IngredientsCallback() {
            @Override
            public void onSuccess(List<String> ingredients) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (ingredientsAdapter != null) {
                            ingredientsAdapter.updateData(ingredients);
                            applyExistingIngredientSelections();
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading ingredients: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void applyExistingIngredientSelections() {
        if (ingredientsAdapter == null || existingIngredientLines == null || existingIngredientLines.isEmpty()) {
            return;
        }

        // Only select ingredients that exist in the fetched list.
        List<String> knownSelections = new ArrayList<>();
        for (String ingredient : existingIngredientLines) {
            String canonical = ingredientsAdapter.findIngredient(ingredient);
            if (canonical != null) {
                knownSelections.add(canonical);
                addChipToGroup(canonical);
            }
        }
        ingredientsAdapter.setSelectedIngredients(knownSelections);
    }

    private void addChipToGroup(String text) {
        if (cgSelectedIngredients == null || text == null || text.trim().isEmpty()) return;
        for (int i = 0; i < cgSelectedIngredients.getChildCount(); i++) {
            Chip existing = (Chip) cgSelectedIngredients.getChildAt(i);
            if (existing.getText().toString().equalsIgnoreCase(text)) {
                return;
            }
        }
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            cgSelectedIngredients.removeView(chip);
            if (ingredientsAdapter != null) {
                removeIgnoreCaseFromList(ingredientsAdapter.getSelectedIngredients(), text);
                ingredientsAdapter.notifyDataSetChanged();
            }
            removeIngredientFromTextIfAutoAdded(text);
        });
        cgSelectedIngredients.addView(chip);
    }

    private void removeChipFromGroup(String text) {
        if (cgSelectedIngredients == null || text == null) return;
        for (int i = 0; i < cgSelectedIngredients.getChildCount(); i++) {
            Chip chip = (Chip) cgSelectedIngredients.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(text)) {
                cgSelectedIngredients.removeView(chip);
                break;
            }
        }
    }

    private void addIngredientToText(String ingredient) {
        if (etIngredients == null || ingredient == null) return;
        Set<String> current = new LinkedHashSet<>(parseIngredients(etIngredients.getText().toString()));
        if (containsIgnoreCase(current, ingredient)) return;
        current.add(ingredient);
        autoAddedIngredients.add(ingredient);
        updateIngredientsText(current);
    }

    private void removeIngredientFromTextIfAutoAdded(String ingredient) {
        if (etIngredients == null || ingredient == null) return;
        if (!containsIgnoreCase(autoAddedIngredients, ingredient)) return;
        Set<String> current = new LinkedHashSet<>(parseIngredients(etIngredients.getText().toString()));
        removeIgnoreCase(current, ingredient);
        removeIgnoreCase(autoAddedIngredients, ingredient);
        updateIngredientsText(current);
    }

    private void updateIngredientsText(Set<String> ingredients) {
        isUpdatingIngredientsText = true;
        // Preserve line-per-ingredient format.
        String text = TextUtils.join("\n", ingredients);
        etIngredients.setText(text);
        etIngredients.setSelection(text.length());
        isUpdatingIngredientsText = false;
    }

    private List<String> parseIngredients(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null) return result;
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private boolean containsIgnoreCase(Set<String> set, String value) {
        for (String item : set) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private void removeIgnoreCase(Set<String> set, String value) {
        String toRemove = null;
        for (String item : set) {
            if (item.equalsIgnoreCase(value)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            set.remove(toRemove);
        }
    }

    private void removeIgnoreCaseFromList(List<String> list, String value) {
        String toRemove = null;
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            list.remove(toRemove);
        }
    }

    private void uploadImageAndThenSaveOrUpdate(
            String title,
            String ingredients,
            String instructions,
            List<String> categories,
            View v
    ) {
        // Upload image to Firebase Storage, then save recipe with the download URL.
        String fileName = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("recipe_images/" + fileName);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                saveOrUpdate(title, ingredients, instructions, categories, uri.toString(), v)
                        )
                )
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();

                    // Fallback: keep old imageUrl in edit, or null in add
                    String imageUrlToUse = (existingRecipe != null) ? existingRecipe.getImageUrl() : null;
                    saveOrUpdate(title, ingredients, instructions, categories, imageUrlToUse, v);
                });
    }

    private void saveOrUpdate(
            String title,
            String ingredients,
            String instructions,
            List<String> categories,
            @Nullable String imageUrl,
            View v
    ) {
        if (existingRecipe != null) {
            // UPDATE existing
            existingRecipe.setTitle(title);
            existingRecipe.setIngredients(ingredients);
            existingRecipe.setInstructions(instructions);
            existingRecipe.setCategories(categories);
            existingRecipe.setImageUrl(imageUrl);

            RecipeRepository.getInstance().update(existingRecipe);

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Recipe updated!", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
            });

        } else {
            // ADD new
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

    private List<String> collectCategoriesFromCheckboxes() {
        List<String> categories = new ArrayList<>();
        if (cbMain != null && cbMain.isChecked()) categories.add("Main Courses");
        if (cbDesserts != null && cbDesserts.isChecked()) categories.add("Desserts");
        if (cbVegan != null && cbVegan.isChecked()) categories.add("Vegan / Vegetarian");
        if (cbBreakfast != null && cbBreakfast.isChecked()) categories.add("Breakfast");
        return categories;
    }

    private void applyCategoriesToCheckboxes(@Nullable List<String> categories) {
        if (categories == null) return;
        cbMain.setChecked(categories.contains("Main Courses"));
        cbDesserts.setChecked(categories.contains("Desserts"));
        cbVegan.setChecked(categories.contains("Vegan / Vegetarian"));
        cbBreakfast.setChecked(categories.contains("Breakfast"));
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
