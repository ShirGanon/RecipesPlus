package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyRecipesFragment extends Fragment {

    private RecyclerView rv;
    private TextView empty;
    private RecipeAdapter adapter;
    private List<Recipe> allManualRecipes = new ArrayList<>();
    private ChipGroup chipGroup;

    public MyRecipesFragment() {
        super(R.layout.fragment_my_recipes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv = view.findViewById(R.id.rv_recipes);
        empty = view.findViewById(R.id.tv_empty_recipes);
        chipGroup = view.findViewById(R.id.chip_group_categories);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapter with an empty list.
        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Bundle args = new Bundle();
            args.putString("recipeId", recipe.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_myRecipesFragment_to_recipeDetailsFragment, args);
        });
        rv.setAdapter(adapter);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // When a chip is clicked, filter the list.
            filterAndDisplayRecipes();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load fresh data every time the screen is shown.
        loadRecipes();
    }

    private void loadRecipes() {
        RecipeRepository.getInstance().loadRecipes(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Get all "manual" recipes from the repository.
                    allManualRecipes = RecipeRepository.getInstance().getAll().stream()
                            .filter(r -> "manual".equals(r.getSource()))
                            .collect(Collectors.toList());
                    // Filter and display the recipes.
                    filterAndDisplayRecipes();
                });
            }
        });
    }

    private void filterAndDisplayRecipes() {
        List<Recipe> filteredList = new ArrayList<>();
        int checkedChipId = chipGroup.getCheckedChipId();

        if (checkedChipId == View.NO_ID || checkedChipId == R.id.chip_all) {
            filteredList.addAll(allManualRecipes);
        } else {
            Chip selectedChip = chipGroup.findViewById(checkedChipId);
            if (selectedChip != null) {
                String category = selectedChip.getText().toString();
                for (Recipe recipe : allManualRecipes) {
                    // Safe check for categories.
                    if (recipe.getCategories() != null && recipe.getCategories().contains(category)) {
                        filteredList.add(recipe);
                    }
                }
            } else {
                // If somehow the chip is not found, default to showing all.
                filteredList.addAll(allManualRecipes);
            }
        }

        // Update the adapter with the final list.
        adapter.updateRecipes(filteredList);

        // Show or hide the "No recipes yet" text.
        empty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
    }
}