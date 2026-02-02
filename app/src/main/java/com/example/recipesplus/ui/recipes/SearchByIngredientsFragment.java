package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.OnlineRecipe;
import com.example.recipesplus.model.Recipe;
import com.example.recipesplus.services.SpoonacularService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchByIngredientsFragment extends Fragment {

    private IngredientsAdapter ingredientsAdapter;
    private OnlineRecipeAdapter recipeAdapter;
    private RecyclerView rvIngredients;
    private RecyclerView rvRecipes;
    private Button btnSearch;

    public SearchByIngredientsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_by_ingredients, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvIngredients = view.findViewById(R.id.rv_ingredients);
        rvRecipes = view.findViewById(R.id.rv_recipes);
        btnSearch = view.findViewById(R.id.btn_search_by_ingredients);

        rvIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapters with empty lists
        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>());
        rvIngredients.setAdapter(ingredientsAdapter);

        recipeAdapter = new OnlineRecipeAdapter(new ArrayList<>(), new HashSet<>(), onlineRecipe -> true);
        rvRecipes.setAdapter(recipeAdapter);

        // Initially hide the recipe results view
        rvRecipes.setVisibility(View.GONE);

        loadIngredients();

        btnSearch.setOnClickListener(v -> {
            List<String> selectedIngredients = ingredientsAdapter.getSelectedIngredients();
            if (selectedIngredients.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one ingredient", Toast.LENGTH_SHORT).show();
                return;
            }
            searchRecipes(selectedIngredients);
        });
    }

    private void loadIngredients() {
        new SpoonacularService().getPopularIngredients(requireContext(), new SpoonacularService.IngredientsCallback() {
            @Override
            public void onSuccess(List<String> ingredients) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ingredientsAdapter = new IngredientsAdapter(ingredients);
                        rvIngredients.setAdapter(ingredientsAdapter);
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading ingredients: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void searchRecipes(List<String> ingredients) {
        new SpoonacularService().searchByIngredients(requireContext(), ingredients, new SpoonacularService.RecipeCallback() {
            @Override
            public void onSuccess(List<OnlineRecipe> recipes) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        rvIngredients.setVisibility(View.GONE);
                        rvRecipes.setVisibility(View.VISIBLE);

                        recipeAdapter = new OnlineRecipeAdapter(recipes, new HashSet<>(), onlineRecipe -> {
                            RecipeRepository repo = RecipeRepository.getInstance();
                            Recipe existing = repo.getByTitle(onlineRecipe.getTitle());

                            if (existing != null) {
                                Toast.makeText(requireContext(), "Recipe already saved.", Toast.LENGTH_SHORT).show();
                            } else {
                                Recipe local = new Recipe(
                                        onlineRecipe.getTitle(),
                                        onlineRecipe.getIngredients(),
                                        onlineRecipe.getInstructions(),
                                        "online"
                                );
                                repo.add(local);
                                Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT).show();
                            }
                            return true;
                        });
                        rvRecipes.setAdapter(recipeAdapter);
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error searching recipes: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
