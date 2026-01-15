package com.example.recipesplus.model;

import java.util.UUID;

public class Recipe {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private boolean favorite;

    // Default constructor for Firestore
    public Recipe() {
        this.id = UUID.randomUUID().toString();
        this.favorite = false;
    }

    // Constructor with auto-generated ID
    public Recipe(String title, String ingredients, String instructions) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = false;
    }

    // Constructor with existing ID (for Firestore)
    public Recipe(String id, String title, String ingredients, String instructions, boolean favorite) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = favorite;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }
    public boolean isFavorite() { return favorite; }

    public void setTitle(String title) { this.title = title; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}