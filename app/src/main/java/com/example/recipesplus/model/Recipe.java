package com.example.recipesplus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipe {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private boolean favorite;
    private String source; // "manual" or "online"
    private List<String> categories;

    // Default constructor for Firestore
    public Recipe() {
        this.id = UUID.randomUUID().toString();
        this.favorite = false;
        this.source = "manual"; // Default source
        this.categories = new ArrayList<>();
    }

    // Main constructor for manually added recipes
    public Recipe(String title, String ingredients, String instructions) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = false;
        this.source = "manual"; // Ensure source is "manual"
        this.categories = new ArrayList<>();
    }

    // Constructor for online recipes
    public Recipe(String title, String ingredients, String instructions, String source) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = true;
        this.source = source; // Set source to "online"
        this.categories = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }
    public boolean isFavorite() { return favorite; }
    public String getSource() { return source; }
    public List<String> getCategories() { return categories; }

    public void setTitle(String title) { this.title = title; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setSource(String source) { this.source = source; }
    public void setCategories(List<String> categories) { this.categories = categories; }
}