package com.example.recipesplus.model;

public class OnlineRecipe {
    private final String title;
    private final String summary;
    private final String instructions;

    public OnlineRecipe(String title, String summary, String instructions) {
        this.title = title;
        this.summary = summary;
        this.instructions = instructions;
    }

    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getInstructions() { return instructions; }
}
