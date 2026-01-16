package com.example.recipesplus.model;

import java.io.Serializable;
import java.util.UUID;

public class Recipe implements Serializable {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private boolean favorite;
    private String source; // "manual" or "online"

    // Default constructor for Firestore
    public Recipe() {
        this.id = UUID.randomUUID().toString();
        this.favorite = false;
        this.source = "manual";
    }

    // Constructor with auto-generated ID
    public Recipe(String title, String ingredients, String instructions) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = false;
        this.source = "manual";
    }

    // Constructor for online recipes
    public Recipe(String title, String ingredients, String instructions, String source) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = true; // Online recipes are favorites by default
        this.source = source;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public String getIngredients() { return ingredients; }
    public String getInstructions() { return instructions; }
    public boolean isFavorite() { return favorite; }
    public String getSource() { return source; }

    public void setTitle(String title) { this.title = title; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setSource(String source) { this.source = source; }
}
