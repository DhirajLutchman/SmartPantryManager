package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.RecipeAdapter;
import com.example.smartpantry.model.Recipe;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Suggested Recipes screen. Runs the strict-matching logic
 * (DatabaseHelper.getStrictlySuggestedRecipes) against the current pantry and shows
 * only recipes the user can make right now with zero missing ingredients.
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private View emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerSuggestions);
        emptyText = findViewById(R.id.textNoMatches);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_suggestions);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                finish(); // go back to the Pantry screen underneath
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(SuggestedRecipesActivity.this, SettingsActivity.class));
                finish(); // replace this screen so the back stack stays short
            }
            return true;
        });
        loadSuggestions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<Recipe> suggestions = dbHelper.getStrictlySuggestedRecipes();
        RecipeAdapter adapter = new RecipeAdapter(suggestions, this);
        recyclerView.setAdapter(adapter);

        boolean empty = suggestions.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe_id", recipe.getId());
        startActivity(intent);
    }
}