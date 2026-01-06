package com.example.recipesplus.data;

import com.example.recipesplus.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple in-memory repository (college-style).
 * Later you can replace it with Room without changing most UI code.
 */
public class RecipeRepository {

    private static RecipeRepository instance;

    private final List<Recipe> recipes = new ArrayList<>();

    private RecipeRepository() {}

    public static synchronized RecipeRepository getInstance() {
        if (instance == null) instance = new RecipeRepository();
        return instance;
    }

    /** Returns a copy to avoid external modification */
    public List<Recipe> getAll() {
        return new ArrayList<>(recipes);
    }

    public void add(Recipe recipe) {
        if (recipe == null) return;
        recipes.add(recipe);
    }

    /** === NEW: duplication protection by title (case-insensitive) === */
    public boolean existsByTitle(String title) {
        if (title == null) return false;
        String t = title.trim().toLowerCase();

        for (Recipe r : recipes) {
            if (r.getTitle() != null && r.getTitle().trim().toLowerCase().equals(t)) {
                return true;
            }
        }
        return false;
    }

    /** === NEW: use this from SearchOnline + AddRecipe to avoid duplicates === */
    public boolean addIfNotExists(Recipe recipe) {
        if (recipe == null) return false;
        if (existsByTitle(recipe.getTitle())) return false;

        add(recipe);
        return true;
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

}
