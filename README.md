# Smart Pantry Manager

Mobile App Development 700 – Practical Assignment

An Android app (Java) that helps cut down food waste by suggesting recipes you can 
actually make right now, using only the ingredients you already have. No recipe gets 
suggested unless every single ingredient it needs is sitting in your pantry in 
enough quantity — that's the "strict matching" rule the whole project is built around.

## The idea

I built this because I kept buying groceries, using half of them, and then not 
knowing what to do with the rest before they went off. This app tracks what you 
actually have at home and only shows you recipes you can cook right now, so there's 
no back-and-forth checking whether you're missing one ingredient.

## Features

- Add, edit and delete pantry ingredients (name, quantity, unit, optional expiry date)
- Pantry List screen backed by a RecyclerView and a custom Adapter
- 18 recipes seeded into the database automatically on first run
- Suggested Recipes screen — only shows a recipe if 100% of its ingredients are in 
  your pantry, in sufficient quantity
- Recipe Detail screen with the full ingredient list and method
- Settings screen (expiry alert toggle, preferred unit system)
- Input validation on the Add/Edit form
- Bottom navigation tying Pantry, Suggestions and Settings together

## Why SQLite

I went with SQLite (via 'SQLiteOpenHelper', no Room) instead of Firebase or 
PostgreSQL because this app doesn't need any cloud sync — it's meant to work fully 
offline on one device. It also matched what we covered in the persistent data 
section of the module, so I could actually explain the raw SQL rather than relying 
on an abstraction I didn't fully understand yet.

Three tables:
- 'pantry_items' — what the user currently has
- 'recipes' — recipe name + method
- 'recipe_ingredients' — each recipe's required ingredients, linked back via 'recipe_id'

## The strict-matching logic

This is the core of the app (see 'DatabaseHelper.getStrictlySuggestedRecipes()' and 
'util/IngredientMatcher.java'). A naive exact-string match would break on something 
as simple as "tomato" vs "tomatoes", so ingredient names get normalised (lower-cased, 
trimmed, simple plural stripped) before comparing. Quantities also get converted to 
a common unit where possible (grams for weight, millilitres for volume), so, for 
example, having 1kg of flour correctly satisfies a recipe that only needs 500g.

## Setup / running it

1. Clone the repo
2. Open the project folder in Android Studio
3. Let Gradle sync (accept creating the Gradle wrapper if prompted)
4. Run on an emulator or physical device — min SDK 24 (Android 7.0)
5. The 18 recipes seed themselves into SQLite the first time the app runs

## Project Structure

app/src/main/java/com/example/smartpantry/
DatabaseHelper.java - all SQLite access: CRUD + strict-matching logic
MainActivity.java - Pantry List screen (launcher)
AddEditIngredientActivity.java - add/edit form with validation
SuggestedRecipesActivity.java - strict-matching results
RecipeDetailActivity.java - full recipe view
SettingsActivity.java - preferences via SharedPreferences
model/ - PantryItem, Recipe, RecipeIngredient
adapter/ - PantryAdapter, RecipeAdapter
util/IngredientMatcher.java - name normalisation + unit-aware matching

## Author
Dhiraj Lutchman - 402307666
Mobile App Development 700
