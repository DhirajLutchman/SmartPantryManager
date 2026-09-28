package com.example.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

/**
 * Recipe Detail screen. Receives a recipe_id via Intent extra and shows
 * the full ingredient list and preparation method for that recipe.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recipeId = getIntent().getLongExtra("recipe_id", -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe recipe = dbHelper.getRecipeById(recipeId);

        TextView nameView = findViewById(R.id.textRecipeDetailName);
        TextView ingredientsView = findViewById(R.id.textRecipeDetailIngredients);
        TextView stepsView = findViewById(R.id.textRecipeDetailSteps);

        if (recipe == null) {
            nameView.setText("Recipe not found");
            return;
        }

        nameView.setText(recipe.getName());

        StringBuilder ingredientsText = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredientsText.append("\u2022 ")
                    .append(trimNumber(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit() != null ? ingredient.getUnit() : "")
                    .append(" ")
                    .append(ingredient.getIngredientName())
                    .append("\n");
        }
        ingredientsView.setText(ingredientsText.toString().trim());
        stepsView.setText(recipe.getSteps());
    }

    private String trimNumber(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}