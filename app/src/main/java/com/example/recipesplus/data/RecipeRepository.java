package com.example.recipesplus.data;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.recipesplus.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRepository {

    public interface RecipeCallback {
        void onSuccess();
        void onError(Exception e);
    }

    private static RecipeRepository instance;

    private final List<Recipe> recipes = new ArrayList<>();

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private static final String TAG = "RecipeRepository";
    private static final String COLLECTION_RECIPES = "recipes";
    private boolean isLoaded = false;

    private RecipeRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized RecipeRepository getInstance() {
        if (instance == null) instance = new RecipeRepository();
        return instance;
    }

    public void loadRecipes(Runnable onComplete) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection(COLLECTION_RECIPES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recipes.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Recipe recipe = document.toObject(Recipe.class);
                                recipe.setId(document.getId());
                                recipes.add(recipe);
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document", e);
                            }
                        }
                        isLoaded = true;
                    } else {
                        Log.e(TAG, "Error loading recipes", task.getException());
                    }

                    if (onComplete != null) onComplete.run();
                });
    }

    public List<Recipe> getAll() {
        return new ArrayList<>(recipes);
    }

    public void add(Recipe recipe, @Nullable RecipeCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (callback != null) callback.onError(new Exception("User not logged in"));
            return;
        }

        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("title", recipe.getTitle());
        recipeData.put("ingredients", recipe.getIngredients());
        recipeData.put("instructions", recipe.getInstructions());
        recipeData.put("favorite", recipe.isFavorite());
        recipeData.put("userId", userId);
        recipeData.put("source", recipe.getSource());

        // merge of feature/categories + master
        recipeData.put("categories", recipe.getCategories());
        recipeData.put("imageUrl", recipe.getImageUrl());

        db.collection(COLLECTION_RECIPES)
                .add(recipeData)
                .addOnSuccessListener(documentReference -> {
                    String newId = documentReference.getId();
                    recipe.setId(newId);
                    recipes.add(recipe); // keep local cache consistent ONLY on success
                    Log.d(TAG, "Recipe saved to Firestore with ID: " + newId);

                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving recipe to Firestore", e);
                    if (callback != null) callback.onError(e);
                });
    }

    // Overloaded add method for calls that don't need a callback.
    public void add(Recipe recipe) {
        add(recipe, null);
    }

    public void update(Recipe recipe) {
        if (recipe == null || recipe.getId() == null) return;

        String userId = getCurrentUserId();
        if (userId == null) return;

        // Update local cache
        for (int i = 0; i < recipes.size(); i++) {
            Recipe existing = recipes.get(i);
            if (existing != null && recipe.getId().equals(existing.getId())) {
                recipes.set(i, recipe);
                break;
            }
        }

        // Update in Firestore (merge so we don't accidentally wipe other fields)
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", recipe.getTitle());
        updates.put("ingredients", recipe.getIngredients());
        updates.put("instructions", recipe.getInstructions());
        updates.put("favorite", recipe.isFavorite());
        updates.put("source", recipe.getSource());
        updates.put("categories", recipe.getCategories());
        updates.put("imageUrl", recipe.getImageUrl());
        // keep ownership consistent
        updates.put("userId", userId);

        db.collection(COLLECTION_RECIPES)
                .document(recipe.getId())
                .set(updates, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Error updating recipe", e));
    }

    public void delete(String recipeId) {
        if (recipeId == null) return;
        recipes.removeIf(r -> r != null && recipeId.equals(r.getId()));
        db.collection(COLLECTION_RECIPES).document(recipeId).delete();
    }

    public Recipe getById(String id) {
        if (id == null) return null;
        for (Recipe r : recipes) {
            if (r != null && id.equals(r.getId())) {
                return r;
            }
        }
        return null;
    }

    public Recipe getByTitle(String title) {
        if (title == null) return null;
        for (Recipe r : recipes) {
            if (r != null && r.getTitle() != null && title.equalsIgnoreCase(r.getTitle())) {
                return r;
            }
        }
        return null;
    }

    public void clear() {
        recipes.clear();
        isLoaded = false;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    private String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }
}
