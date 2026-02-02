package com.example.recipesplus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Recipe implements Serializable {

    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private boolean favorite;
    private String source; // "manual" or "online"

    private List<String> categories;
    private String imageUrl;

    // Required empty constructor for Firestore
    public Recipe() {
        this.id = UUID.randomUUID().toString();
        this.favorite = false;
        this.source = "manual";
        this.categories = new ArrayList<>();
        this.imageUrl = null;
    }

    // Manual recipe constructor
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

    // Online recipe constructor
    public Recipe(String title, String ingredients, String instructions, String source) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;

        // NOT favorite by default
        this.favorite = false;

        this.source = source;
        this.categories = new ArrayList<>();
        this.imageUrl = null;
    }

    // Getters / Setters

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
