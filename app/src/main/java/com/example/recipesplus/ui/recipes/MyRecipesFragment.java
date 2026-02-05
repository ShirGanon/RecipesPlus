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

public class MyRecipesFragment extends Fragment {

    private RecyclerView rv;
    private TextView empty;
    private RecipeAdapter adapter;
    private List<Recipe> allRecipes = new ArrayList<>();
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

        // Main list supports view, edit, and favorite toggling.
        adapter = new RecipeAdapter(new ArrayList<>(), new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Bundle args = new Bundle();
                args.putString("recipeId", recipe.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_myRecipesFragment_to_recipeDetailsFragment, args);
            }

            @Override
            public void onEditClick(Recipe recipe) {
                Bundle args = new Bundle();
                args.putSerializable("recipe", recipe);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_myRecipesFragment_to_addRecipeFragment, args);
            }

            @Override
            public void onFavoriteClick(Recipe recipe) {
                recipe.setFavorite(!recipe.isFavorite());
                RecipeRepository.getInstance().update(recipe);
                filterAndDisplay();
            }
        });

        rv.setAdapter(adapter);

        chipGroup.setOnCheckedChangeListener((group, id) -> filterAndDisplay());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload from Firestore to keep list fresh.
        RecipeRepository.getInstance().loadRecipes(() -> {
            allRecipes = RecipeRepository.getInstance().getAll();
            filterAndDisplay();
        });
    }

    private void filterAndDisplay() {
        // Filter by category chip selection.
        List<Recipe> filtered = new ArrayList<>();
        int id = chipGroup.getCheckedChipId();

        if (id == View.NO_ID || id == R.id.chip_all) {
            filtered.addAll(allRecipes);
        } else {
            Chip chip = chipGroup.findViewById(id);
            String cat = chip.getText().toString();
            for (Recipe r : allRecipes) {
                if (r.getCategories() != null && r.getCategories().contains(cat))
                    filtered.add(r);
            }
        }

        adapter.updateRecipes(filtered);

        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
