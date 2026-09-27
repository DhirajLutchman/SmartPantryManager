package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.model.PantryItem;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"pcs", "g", "kg", "ml", "l", "cup", "tbsp", "tsp"};

    private DatabaseHelper dbHelper;
    private EditText editName, editQuantity, editExpiry;
    private Spinner spinnerUnit;
    private long editingItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        dbHelper = new DatabaseHelper(this);

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        TextView titleView = findViewById(R.id.textFormTitle);
        Button saveButton = findViewById(R.id.btnSaveItem);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        editExpiry.setOnClickListener(v -> showDatePicker());

        if (getIntent().hasExtra("item_id")) {
            editingItemId = getIntent().getLongExtra("item_id", -1);
            titleView.setText("Edit Ingredient");
            populateFieldsForEdit();
        }

        saveButton.setOnClickListener(v -> saveItem());
    }

    private void populateFieldsForEdit() {
        PantryItem item = dbHelper.getPantryItem(editingItemId);
        if (item == null) return;
        editName.setText(item.getName());
        editQuantity.setText(trimNumber(item.getQuantity()));
        editExpiry.setText(item.getExpiryDate());
        int unitIndex = Arrays.asList(UNITS).indexOf(item.getUnit());
        if (unitIndex >= 0) spinnerUnit.setSelection(unitIndex);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            editExpiry.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveItem() {
        String name = editName.getText().toString().trim();
        String qtyStr = editQuantity.getText().toString().trim();
        String unit = (String) spinnerUnit.getSelectedItem();
        String expiry = editExpiry.getText().toString().trim();

        // ---- Input validation ----
        if (name.isEmpty()) {
            editName.setError("Ingredient name is required");
            editName.requestFocus();
            return;
        }
        if (qtyStr.isEmpty()) {
            editQuantity.setError("Quantity is required");
            editQuantity.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
        } catch (NumberFormatException e) {
            editQuantity.setError("Quantity must be a number");
            editQuantity.requestFocus();
            return;
        }
        if (quantity <= 0) {
            editQuantity.setError("Quantity must be greater than 0");
            editQuantity.requestFocus();
            return;
        }

        if (editingItemId == -1) {
            dbHelper.addPantryItem(name, quantity, unit, expiry);
            Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updatePantryItem(editingItemId, name, quantity, unit, expiry);
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String trimNumber(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}