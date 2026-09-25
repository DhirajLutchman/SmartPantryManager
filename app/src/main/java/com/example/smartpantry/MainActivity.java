package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.PantryAdapter;
import com.example.smartpantry.model.PantryItem;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView emptyText;
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
            // Temporary: adds a quick test item so we can confirm the database + list work.
            // This gets replaced with a real Add/Edit screen in the next stage.
            dbHelper.addPantryItem("Test Item", 1, "pcs", "");
            Toast.makeText(this, "Test item added", Toast.LENGTH_SHORT).show();
            loadPantryItems();
        });

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
        // Wired up properly once the Add/Edit screen exists in the next stage.
        Toast.makeText(this, "Edit screen coming in the next stage", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(PantryItem item) {
        dbHelper.deletePantryItem(item.getId());
        loadPantryItems();
    }
}