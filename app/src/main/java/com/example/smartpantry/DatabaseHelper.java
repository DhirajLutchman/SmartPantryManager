package com.example.smartpantry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import com.example.smartpantry.model.PantryItem;
import java.util.ArrayList;
import java.util.List;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;
import com.example.smartpantry.util.IngredientMatcher;
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_P_ID = "id";
    public static final String COL_P_NAME = "name";
    public static final String COL_P_QTY = "quantity";
    public static final String COL_P_UNIT = "unit";
    public static final String COL_P_EXPIRY = "expiry_date";

    // Recipes table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_R_ID = "id";
    public static final String COL_R_NAME = "name";
    public static final String COL_R_STEPS = "steps";

    // Recipe ingredients table
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_QTY + " REAL NOT NULL, " +
                COL_P_UNIT + " TEXT, " +
                COL_P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_NAME + " TEXT NOT NULL, " +
                COL_R_STEPS + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + "))");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }
    //  Pantry CRUD
    public long addPantryItem(String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, name);
        cv.put(COL_P_QTY, qty);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        return db.insert(TABLE_PANTRY, null, cv);
    }

    public void updatePantryItem(long id, String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, name);
        cv.put(COL_P_QTY, qty);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        db.update(TABLE_PANTRY, cv, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) {
            item = cursorToPantryItem(c);
        }
        c.close();
        return item;
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_P_NAME + " ASC");
        while (c.moveToNext()) {
            list.add(cursorToPantryItem(c));
        }
        c.close();
        return list;
    }

    private PantryItem cursorToPantryItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(COL_P_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(COL_P_QTY)),
                c.getString(c.getColumnIndexOrThrow(COL_P_UNIT)),
                c.getString(c.getColumnIndexOrThrow(COL_P_EXPIRY))
        );
    }
    // ---------- Recipe reads ----------

    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor rc = db.query(TABLE_RECIPES, null, null, null, null, null, COL_R_NAME + " ASC");
        while (rc.moveToNext()) {
            long id = rc.getLong(rc.getColumnIndexOrThrow(COL_R_ID));
            String name = rc.getString(rc.getColumnIndexOrThrow(COL_R_NAME));
            String steps = rc.getString(rc.getColumnIndexOrThrow(COL_R_STEPS));
            Recipe recipe = new Recipe(id, name, steps);
            recipe.setIngredients(getIngredientsForRecipe(db, id));
            recipes.add(recipe);
        }
        rc.close();
        return recipes;
    }

    public Recipe getRecipeById(long recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor rc = db.query(TABLE_RECIPES, null, COL_R_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        Recipe recipe = null;
        if (rc.moveToFirst()) {
            long id = rc.getLong(rc.getColumnIndexOrThrow(COL_R_ID));
            String name = rc.getString(rc.getColumnIndexOrThrow(COL_R_NAME));
            String steps = rc.getString(rc.getColumnIndexOrThrow(COL_R_STEPS));
            recipe = new Recipe(id, name, steps);
            recipe.setIngredients(getIngredientsForRecipe(db, id));
        }
        rc.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor ic = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, COL_RI_ID + " ASC");
        while (ic.moveToNext()) {
            ingredients.add(new RecipeIngredient(
                    ic.getLong(ic.getColumnIndexOrThrow(COL_RI_ID)),
                    ic.getLong(ic.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                    ic.getString(ic.getColumnIndexOrThrow(COL_RI_NAME)),
                    ic.getDouble(ic.getColumnIndexOrThrow(COL_RI_QTY)),
                    ic.getString(ic.getColumnIndexOrThrow(COL_RI_UNIT))
            ));
        }
        ic.close();
        return ingredients;
    }

    /**
     * Core business logic (Section 2.3): returns only the recipes for which every
     * required ingredient is present in the pantry in at least the required amount.
     */
    public List<Recipe> getStrictlySuggestedRecipes() {
        List<PantryItem> pantry = getAllPantryItems();
        List<Recipe> allRecipes = getAllRecipesWithIngredients();
        List<Recipe> suggested = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (countMissingIngredients(recipe, pantry) == 0) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    private int countMissingIngredients(Recipe recipe, List<PantryItem> pantry) {
        int missing = 0;
        for (RecipeIngredient req : recipe.getIngredients()) {
            String normalizedReqName = IngredientMatcher.normalizeName(req.getIngredientName());
            boolean satisfied = false;
            for (PantryItem p : pantry) {
                if (IngredientMatcher.normalizeName(p.getName()).equals(normalizedReqName)) {
                    if (IngredientMatcher.pantryHasEnough(p.getQuantity(), p.getUnit(),
                            req.getQuantity(), req.getUnit())) {
                        satisfied = true;
                        break;
                    }
                }
            }
            if (!satisfied) missing++;
        }
        return missing;
    }

    // ---------- Seed data (Section 2.2: 15-20 recipes pre-loaded on first run) ----------

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Whisk eggs with a splash of milk. 2. Melt butter in a pan over low heat. " +
                        "3. Pour in eggs, stir gently until softly set. 4. Toast the bread and serve eggs on top.",
                new Object[][]{
                        {"egg", 2.0, "pcs"}, {"bread", 2.0, "pcs"}, {"butter", 10.0, "g"}, {"milk", 30.0, "ml"}
                });

        addRecipe(db, "Tomato Pasta",
                "1. Boil pasta until al dente. 2. Saute garlic in olive oil. 3. Add chopped tomato and simmer 10 min. " +
                        "4. Toss pasta through the sauce, season and serve.",
                new Object[][]{
                        {"pasta", 200.0, "g"}, {"tomato", 3.0, "pcs"}, {"garlic", 2.0, "pcs"}, {"olive oil", 15.0, "ml"}
                });

        addRecipe(db, "Vegetable Stir Fry",
                "1. Heat oil in a wok. 2. Add chopped vegetables in order of cook time (carrot first, then broccoli, onion). " +
                        "3. Add soy sauce. 4. Stir fry 5-7 minutes until tender-crisp.",
                new Object[][]{
                        {"carrot", 1.0, "pcs"}, {"broccoli", 100.0, "g"}, {"onion", 1.0, "pcs"}, {"soy sauce", 20.0, "ml"}
                });

        addRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice. 2. Place cheese between unbuttered sides. " +
                        "3. Grill in a pan until golden on both sides and cheese is melted.",
                new Object[][]{
                        {"bread", 2.0, "pcs"}, {"cheese", 40.0, "g"}, {"butter", 10.0, "g"}
                });

        addRecipe(db, "Fried Rice",
                "1. Heat oil in a wok. 2. Scramble egg and set aside. 3. Fry rice, add carrot, onion and peas. " +
                        "4. Stir in soy sauce and the scrambled egg, mix well.",
                new Object[][]{
                        {"rice", 300.0, "g"}, {"egg", 2.0, "pcs"}, {"carrot", 1.0, "pcs"},
                        {"onion", 1.0, "pcs"}, {"soy sauce", 15.0, "ml"}
                });

        addRecipe(db, "Pancakes",
                "1. Mix flour, milk and egg into a smooth batter. 2. Melt a little butter in a pan. " +
                        "3. Pour batter and cook until bubbles form, flip and cook the other side.",
                new Object[][]{
                        {"flour", 200.0, "g"}, {"milk", 300.0, "ml"}, {"egg", 1.0, "pcs"}, {"butter", 15.0, "g"}
                });

        addRecipe(db, "Cheese Omelette",
                "1. Whisk eggs with salt and pepper. 2. Pour into a hot buttered pan. " +
                        "3. Sprinkle cheese on one half once mostly set. 4. Fold over and serve.",
                new Object[][]{
                        {"egg", 3.0, "pcs"}, {"cheese", 30.0, "g"}, {"butter", 10.0, "g"}
                });

        addRecipe(db, "Chicken Soup",
                "1. Saute onion and carrot in a pot. 2. Add chicken and stock, bring to a boil. " +
                        "3. Simmer 25 minutes until chicken is cooked through. 4. Season and serve.",
                new Object[][]{
                        {"chicken", 300.0, "g"}, {"carrot", 1.0, "pcs"}, {"onion", 1.0, "pcs"}, {"stock", 500.0, "ml"}
                });

        addRecipe(db, "Banana Smoothie",
                "1. Add banana, milk and yoghurt to a blender. 2. Blend until smooth. 3. Pour and serve chilled.",
                new Object[][]{
                        {"banana", 2.0, "pcs"}, {"milk", 200.0, "ml"}, {"yoghurt", 100.0, "g"}
                });

        addRecipe(db, "Garlic Bread",
                "1. Mix softened butter with crushed garlic. 2. Spread onto sliced bread. " +
                        "3. Bake at 180C for 8-10 minutes until golden.",
                new Object[][]{
                        {"bread", 4.0, "pcs"}, {"garlic", 3.0, "pcs"}, {"butter", 40.0, "g"}
                });

        addRecipe(db, "Caprese Salad",
                "1. Slice tomato and mozzarella. 2. Arrange alternating slices on a plate. " +
                        "3. Drizzle with olive oil and scatter basil leaves on top.",
                new Object[][]{
                        {"tomato", 2.0, "pcs"}, {"mozzarella", 150.0, "g"}, {"basil", 10.0, "g"}, {"olive oil", 10.0, "ml"}
                });

        addRecipe(db, "Mashed Potatoes",
                "1. Boil peeled potato chunks until soft. 2. Drain and mash with butter and milk. " +
                        "3. Season with salt and pepper.",
                new Object[][]{
                        {"potato", 500.0, "g"}, {"butter", 30.0, "g"}, {"milk", 50.0, "ml"}
                });

        addRecipe(db, "Tuna Salad",
                "1. Drain tuna and flake it into a bowl. 2. Mix with mayonnaise and chopped onion. " +
                        "3. Serve on its own or with bread.",
                new Object[][]{
                        {"tuna", 150.0, "g"}, {"mayonnaise", 30.0, "g"}, {"onion", 0.5, "pcs"}
                });

        addRecipe(db, "Vegetable Soup",
                "1. Saute onion and carrot. 2. Add stock and chopped potato. 3. Simmer 20 minutes until vegetables are soft. " +
                        "4. Blend if a smooth soup is preferred, or serve chunky.",
                new Object[][]{
                        {"onion", 1.0, "pcs"}, {"carrot", 2.0, "pcs"}, {"potato", 1.0, "pcs"}, {"stock", 600.0, "ml"}
                });

        addRecipe(db, "French Toast",
                "1. Whisk egg with milk and a little sugar. 2. Dip bread slices in the mixture. " +
                        "3. Fry in buttered pan until golden on both sides.",
                new Object[][]{
                        {"bread", 3.0, "pcs"}, {"egg", 2.0, "pcs"}, {"milk", 60.0, "ml"}, {"butter", 10.0, "g"}
                });

        addRecipe(db, "Cheese Quesadilla",
                "1. Sprinkle cheese over half a tortilla. 2. Fold over and cook in a dry pan until golden and cheese melts. " +
                        "3. Slice into wedges and serve.",
                new Object[][]{
                        {"tortilla", 2.0, "pcs"}, {"cheese", 60.0, "g"}
                });

        addRecipe(db, "Rice and Beans",
                "1. Cook rice according to package directions. 2. Warm beans with chopped onion and garlic. " +
                        "3. Serve beans over rice.",
                new Object[][]{
                        {"rice", 250.0, "g"}, {"bean", 200.0, "g"}, {"onion", 1.0, "pcs"}, {"garlic", 1.0, "pcs"}
                });

        addRecipe(db, "Fruit Salad",
                "1. Chop banana, apple and any other fruit on hand into bite-size pieces. " +
                        "2. Combine in a bowl. 3. Optionally drizzle with a little yoghurt.",
                new Object[][]{
                        {"banana", 1.0, "pcs"}, {"apple", 1.0, "pcs"}, {"yoghurt", 50.0, "g"}
                });
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues rcv = new ContentValues();
        rcv.put(COL_R_NAME, name);
        rcv.put(COL_R_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, rcv);

        for (Object[] ing : ingredients) {
            ContentValues icv = new ContentValues();
            icv.put(COL_RI_RECIPE_ID, recipeId);
            icv.put(COL_RI_NAME, (String) ing[0]);
            icv.put(COL_RI_QTY, (Double) ing[1]);
            icv.put(COL_RI_UNIT, (String) ing[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, icv);
        }
    }
}