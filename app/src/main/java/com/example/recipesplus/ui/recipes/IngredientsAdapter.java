package com.example.recipesplus.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;

import java.util.ArrayList;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> implements Filterable {

    // Listener to notify the fragment when a selection changes, so it can update the chips at the top.
    public interface OnIngredientChangedListener {
        void onIngredientChanged(String ingredient, boolean isSelected);
    }

    private List<String> ingredients; // The list of ingredients currently displayed (can be filtered)
    private final List<String> ingredientsFull; // The original, complete list of all ingredients
    private final List<String> selectedIngredients; // The list of ingredients the user has checked
    private OnIngredientChangedListener listener;

    public IngredientsAdapter(List<String> ingredients) {
        this.ingredients = new ArrayList<>(ingredients);
        this.ingredientsFull = new ArrayList<>(ingredients);
        this.selectedIngredients = new ArrayList<>();
    }

    public void setOnIngredientChangedListener(OnIngredientChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        final String ingredient = ingredients.get(position);
        holder.checkBox.setText(ingredient);

        // --- ROBUST FIX for CheckBox State ---
        // This is the standard way to handle checkboxes in a RecyclerView to prevent state loss.
        // 1. Temporarily remove the listener to prevent it from firing when we programmatically set the check state.
        holder.checkBox.setOnCheckedChangeListener(null);

        // 2. Set the checked state based on whether this ingredient is in our master list of selections.
        holder.checkBox.setChecked(selectedIngredients.contains(ingredient));

        // 3. Re-attach the listener to handle user interactions.
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If the user checks the box, add the ingredient to our selection list if it isn't already there.
                if (!selectedIngredients.contains(ingredient)) {
                    selectedIngredients.add(ingredient);
                }
            } else {
                // If the user unchecks the box, remove the ingredient from our selection list.
                selectedIngredients.remove(ingredient);
            }
            // Notify the fragment that a change occurred so it can update the chips at the top.
            if (listener != null) {
                listener.onIngredientChanged(ingredient, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public List<String> getSelectedIngredients() {
        return selectedIngredients;
    }

    // Clears all user selections. Called when starting a new search.
    public void clearSelections() {
        selectedIngredients.clear();
        notifyDataSetChanged();
    }

    // Updates the adapter's data source when new ingredients are fetched from the API.
    public void updateData(List<String> newIngredients) {
        this.ingredientsFull.clear();
        this.ingredientsFull.addAll(newIngredients);
        // Also update the currently displayed list.
        this.ingredients.clear();
        this.ingredients.addAll(newIngredients);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return ingredientFilter;
    }

    // This is the filtering logic that runs when the user types in the search bar.
    private final Filter ingredientFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                // If the search bar is empty, show the full list of ingredients.
                filteredList.addAll(ingredientsFull);
            } else {
                // Otherwise, filter the full list based on the search text.
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (String item : ingredientsFull) {
                    if (item.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Update the currently displayed list with the filtered results and refresh the view.
            ingredients.clear();
            ingredients.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_ingredient);
        }
    }
}