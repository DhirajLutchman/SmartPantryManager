package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.PantryAdapter;
import com.example.smartpantry.model.PantryItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private BottomNavigationView bottomNav;
    private List<PantryItem> pantryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerPantry);
        emptyText = findViewById(R.id.textEmptyPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addButton = findViewById(R.id.btnAddItem);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suggestions) {
                startActivity(new Intent(MainActivity.this, SuggestedRecipesActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
            // nav_pantry: already on this screen, nothing to do
            return true;
        });

        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list, and reset the highlighted tab to Pantry when we come back
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        loadPantryItems();
    }

    private void loadPantryItems() {
        pantryItems = dbHelper.getAllPantryItems();
        PantryAdapter adapter = new PantryAdapter(pantryItems, this);
        recyclerView.setAdapter(adapter);

        boolean empty = pantryItems.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra("item_id", item.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(PantryItem item) {
        dbHelper.deletePantryItem(item.getId());
        loadPantryItems();
    }
}