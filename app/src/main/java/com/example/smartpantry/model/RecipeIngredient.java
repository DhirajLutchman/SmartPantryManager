package com.example.smartpantry.model;

public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String ingredientName;
    private double quantity;
    private String unit;

    public RecipeIngredient(long id, long recipeId, String ingredientName, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public long getRecipeId() { return recipeId; }
    public String getIngredientName() { return ingredientName; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
}
