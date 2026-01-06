package com.example.recipesplus.data;

import com.example.recipesplus.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeRepository {
    private static RecipeRepository instance;
    private final List<Recipe> recipes = new ArrayList<>();

    private RecipeRepository() {}


    public static synchronized RecipeRepository getInstance() {
        if (instance == null) instance = new RecipeRepository();
        return instance;
    }

    public List<Recipe> getAll() {
        return Collections.unmodifiableList(recipes);
    }

    public void add(Recipe recipe) {
        recipes.add(0, recipe);
    }

    public Recipe getById(String id) {
        for (Recipe r : recipes) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }
}
