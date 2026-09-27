package com.example.smartpantry.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the "strict matching" logic described in the assignment brief (Section 2.3).
 *
 * A recipe is only "suggested" if every one of its required ingredients is present
 * in the pantry in at least the required quantity. To avoid a naive string match that
 * breaks on trivial real-world differences (e.g. "tomato" vs "tomatoes"), names are
 * normalised before comparison, and quantities are converted to a common base unit
 * (grams for weight, millilitres for volume) where possible so different-but-compatible
 * units (e.g. "1 kg" vs "500 g") are handled correctly.
 */
public class IngredientMatcher {

    private static final Map<String, Double> WEIGHT_TO_GRAMS = new HashMap<>();
    private static final Map<String, Double> VOLUME_TO_ML = new HashMap<>();

    static {
        WEIGHT_TO_GRAMS.put("g", 1.0);
        WEIGHT_TO_GRAMS.put("kg", 1000.0);

        VOLUME_TO_ML.put("ml", 1.0);
        VOLUME_TO_ML.put("l", 1000.0);
        VOLUME_TO_ML.put("cup", 240.0);
        VOLUME_TO_ML.put("tbsp", 15.0);
        VOLUME_TO_ML.put("tsp", 5.0);
    }

    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String name = rawName.trim().toLowerCase();
        if (name.endsWith("es") && name.length() > 3) {
            name = name.substring(0, name.length() - 2);
        } else if (name.endsWith("s") && name.length() > 2) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private static String normalizeUnit(String rawUnit) {
        if (rawUnit == null) return "";
        return rawUnit.trim().toLowerCase();
    }

    public static boolean pantryHasEnough(double pantryQty, String pantryUnit,
                                          double requiredQty, String requiredUnit) {
        String pUnit = normalizeUnit(pantryUnit);
        String rUnit = normalizeUnit(requiredUnit);

        if (pUnit.equals(rUnit)) {
            return pantryQty >= requiredQty;
        }

        if (WEIGHT_TO_GRAMS.containsKey(pUnit) && WEIGHT_TO_GRAMS.containsKey(rUnit)) {
            double pantryGrams = pantryQty * WEIGHT_TO_GRAMS.get(pUnit);
            double requiredGrams = requiredQty * WEIGHT_TO_GRAMS.get(rUnit);
            return pantryGrams >= requiredGrams;
        }

        if (VOLUME_TO_ML.containsKey(pUnit) && VOLUME_TO_ML.containsKey(rUnit)) {
            double pantryMl = pantryQty * VOLUME_TO_ML.get(pUnit);
            double requiredMl = requiredQty * VOLUME_TO_ML.get(rUnit);
            return pantryMl >= requiredMl;
        }

        return pantryQty >= requiredQty;
    }
}