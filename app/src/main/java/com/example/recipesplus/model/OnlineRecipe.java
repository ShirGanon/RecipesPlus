package com.example.recipesplus.model;

public class OnlineRecipe {
    private final String title;
    private final String summary;
    private final String instructions;
    private final String ingredients;

    public OnlineRecipe(String title, String summary, String instructions, String ingredients) {
        this.title = title;
        this.summary = summary;
        this.instructions = instructions;
        this.ingredients = ingredients;
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getInstructions() { return instructions; }
    public String getIngredients() { return ingredients; }
}
