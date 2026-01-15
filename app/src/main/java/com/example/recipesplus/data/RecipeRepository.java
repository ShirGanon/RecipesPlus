package com.example.recipesplus.data;

import android.util.Log;

import com.example.recipesplus.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository that syncs with Firebase Firestore.
 * Recipes are stored per user and loaded when user logs in.
 */
public class RecipeRepository {

    private static RecipeRepository instance;

    // All recipes (manual/created and favorites from online)
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

    /**
     * Load recipes from Firestore for the current user
     * Call this when user logs in or app starts
     */
    public void loadRecipes(Runnable onComplete) {
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.w(TAG, "No user logged in, cannot load recipes");
            if (onComplete != null) onComplete.run();
            return;
        }

        Log.d(TAG, "Loading recipes for userId: " + userId);
        recipes.clear();
        db.collection(COLLECTION_RECIPES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Recipe recipe = document.toObject(Recipe.class);
                                if (recipe != null) {
                                    String documentId = document.getId();
                                    recipe.setId(documentId);

                                    // Check for duplicates by ID before adding
                                    boolean exists = false;
                                    for (Recipe r : recipes) {
                                        if (r.getId() != null && r.getId().equals(documentId)) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (!exists) {
                                        recipes.add(recipe);
                                        count++;
                                        Log.d(TAG, "Loaded recipe: " + recipe.getTitle() + " (id: " + documentId + ")");
                                    } else {
                                        Log.w(TAG, "Skipping duplicate recipe with id: " + documentId);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Recipe: " + document.getId(), e);
                            }
                        }
                        isLoaded = true;
                        Log.d(TAG, "Successfully loaded " + count + " recipes from Firestore (total: " + recipes.size() + ")");
                    } else {
                        Log.e(TAG, "Error loading recipes from Firestore", task.getException());
                        if (task.getException() != null) {
                            Log.e(TAG, "Exception details: " + task.getException().getMessage());
                        }
                    }
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    /** Returns a copy to avoid external modification */
    public List<Recipe> getAll() {
        return new ArrayList<>(recipes);
    }

    /**
     * Add a recipe to both local list and Firestore
     */
    public void add(Recipe recipe) {
        if (recipe == null) return;

        String userId = getCurrentUserId();
        if (userId == null) {
            Log.w(TAG, "No user logged in, recipe not saved to Firestore");
            recipes.add(recipe);
            return;
        }

        // Add to local list first for immediate UI update
        recipes.add(recipe);

        // Save to Firestore
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("title", recipe.getTitle());
        recipeData.put("ingredients", recipe.getIngredients());
        recipeData.put("instructions", recipe.getInstructions());
        recipeData.put("favorite", recipe.isFavorite());
        recipeData.put("userId", userId);
        recipeData.put("source", recipe.getSource());

        db.collection(COLLECTION_RECIPES)
                .add(recipeData) // Use add to generate a new document ID
                .addOnSuccessListener(documentReference -> {
                    String newId = documentReference.getId();
                    recipe.setId(newId);
                    Log.d(TAG, "Recipe saved to Firestore with ID: " + newId);
                    // No need to update the local object with ID if we reload, but good practice
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving recipe to Firestore", e);
                    // Optional: remove from local list on failure
                    recipes.remove(recipe);
                });
    }

    /**
     * Update recipe in Firestore
     */
    public void update(Recipe recipe) {
        if (recipe == null || recipe.getId() == null) return;

        String userId = getCurrentUserId();
        if (userId == null) {
            Log.w(TAG, "No user logged in, recipe not updated in Firestore");
            return;
        }

        // Update local list
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).getId().equals(recipe.getId())) {
                recipes.set(i, recipe);
                break;
            }
        }

        // Update in Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", recipe.getTitle());
        updates.put("ingredients", recipe.getIngredients());
        updates.put("instructions", recipe.getInstructions());
        updates.put("favorite", recipe.isFavorite());
        updates.put("source", recipe.getSource());

        db.collection(COLLECTION_RECIPES)
                .document(recipe.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe updated in Firestore: " + recipe.getTitle());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating recipe in Firestore", e);
                });
    }

    /**
     * Delete recipe from Firestore
     */
    public void delete(String recipeId) {
        if (recipeId == null) return;

        // Remove from local list
        recipes.removeIf(r -> r.getId().equals(recipeId));

        // Delete from Firestore
        db.collection(COLLECTION_RECIPES)
                .document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recipe deleted from Firestore");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting recipe from Firestore", e);
                });
    }


    /**
     * Toggle favorite status
     */
    public void toggleFavorite(String recipeId) {
        Recipe recipe = getById(recipeId);
        if (recipe != null) {
            recipe.setFavorite(!recipe.isFavorite());
            update(recipe);
        }
    }

    /**
     * Set favorite status
     */
    public void setFavorite(String recipeId, boolean favorite) {
        Recipe recipe = getById(recipeId);
        if (recipe != null) {
            recipe.setFavorite(favorite);
            update(recipe);
        }
    }

    public Recipe getByTitle(String title) {
        if (title == null) return null;

        String t = title.trim().toLowerCase();
        for (Recipe r : recipes) {
            if (r.getTitle() != null &&
                    r.getTitle().trim().toLowerCase().equals(t)) {
                return r;
            }
        }
        return null;
    }

    public Recipe getById(String id) {
        if (id == null) return null;

        for (Recipe r : recipes) {
            if (id.equals(r.getId())) {
                return r;
            }
        }
        return null;
    }

    /**
     * Clear local recipes (call on logout)
     */
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
