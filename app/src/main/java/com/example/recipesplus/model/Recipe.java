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
    private String imageUrl;

    // Default constructor for Firestore
    public Recipe() {
        // If you prefer Firestore doc id as the true id, you can set id=null here.
        this.id = UUID.randomUUID().toString();
        this.favorite = false;
        this.source = "manual";
        this.categories = new ArrayList<>();
        this.imageUrl = null;
    }

    // Main constructor for manually added recipes
    public Recipe(String title, String ingredients, String instructions) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.favorite = false;
        this.source = "manual";
        this.categories = new ArrayList<>();
        this.imageUrl = null;
    }

    // Constructor for online recipes
    public Recipe(String title, String ingredients, String instructions, String source) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;

        // Online recipes should NOT be favorites by default
        this.favorite = false;

        this.source = source; // e.g. "online"
        this.categories = new ArrayList<>();
        this.imageUrl = null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) {
        this.categories = (categories != null) ? categories : new ArrayList<>();
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
