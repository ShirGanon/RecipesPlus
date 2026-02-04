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

    private List<String> ingredients;
    private final List<String> ingredientsFull;
    private final List<String> selectedIngredients = new ArrayList<>();

    public IngredientsAdapter(List<String> ingredients) {
        this.ingredients = ingredients;
        this.ingredientsFull = new ArrayList<>(ingredients);
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        String ingredient = ingredients.get(position);
        holder.checkBox.setText(ingredient);
        holder.checkBox.setChecked(selectedIngredients.contains(ingredient));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedIngredients.contains(ingredient)) {
                    selectedIngredients.add(ingredient);
                }
            } else {
                selectedIngredients.remove(ingredient);
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

    public void clearSelections() {
        selectedIngredients.clear();
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return ingredientFilter;
    }

    private final Filter ingredientFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(ingredientsFull);
            } else {
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
