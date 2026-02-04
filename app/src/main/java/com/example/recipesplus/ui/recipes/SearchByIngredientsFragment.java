package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText etIngredientSearch;

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
        etIngredientSearch = view.findViewById(R.id.et_ingredient_search);

        rvIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));

        ingredientsAdapter = new IngredientsAdapter(new ArrayList<>());
        rvIngredients.setAdapter(ingredientsAdapter);

        // init empty adapter (keeps constructor signature consistent)
        recipeAdapter = new OnlineRecipeAdapter(new ArrayList<>(), new HashSet<>(), (online, asFavorite) -> {
            // Logic handled when results are actually present
        });
        rvRecipes.setAdapter(recipeAdapter);

        rvRecipes.setVisibility(View.GONE);

        loadIngredients();

        etIngredientSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ingredientsAdapter != null) {
                    ingredientsAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnSearch.setOnClickListener(v -> {
            if (rvRecipes.getVisibility() == View.VISIBLE) {
                // New Search
                rvRecipes.setVisibility(View.GONE);
                rvIngredients.setVisibility(View.VISIBLE);
                etIngredientSearch.setVisibility(View.VISIBLE);
                btnSearch.setText("Find Recipes");
                ingredientsAdapter.clearSelections();
            } else {
                List<String> selectedIngredients = ingredientsAdapter.getSelectedIngredients();
                if (selectedIngredients.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select at least one ingredient", Toast.LENGTH_SHORT).show();
                    return;
                }
                searchRecipes(selectedIngredients);
            }
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
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error loading ingredients: " + message, Toast.LENGTH_LONG).show()
                    );
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
                        // Switch UI to results
                        rvIngredients.setVisibility(View.GONE);
                        etIngredientSearch.setVisibility(View.GONE);
                        rvRecipes.setVisibility(View.VISIBLE);
                        btnSearch.setText("Find a New Recipe");

                        recipeAdapter = new OnlineRecipeAdapter(recipes, new HashSet<>(), (onlineRecipe, asFavorite) -> {
                            RecipeRepository repo = RecipeRepository.getInstance();
                            Recipe existing = repo.getByTitle(onlineRecipe.getTitle());

                            // If already exists -> update favorite or remove
                            if (existing != null) {
                                if (asFavorite) {
                                    if (!existing.isFavorite()) {
                                        existing.setFavorite(true);
                                        repo.update(existing);
                                        Toast.makeText(requireContext(), "Added to favorites.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(), "Already in favorites.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    repo.delete(existing.getId());
                                    Toast.makeText(requireContext(), "Recipe removed.", Toast.LENGTH_SHORT).show();
                                }
                                recipeAdapter.notifyDataSetChanged();
                                return;
                            }

                            // Doesn't exist -> save it (favorite or not)
                            String instructions = (onlineRecipe.getInstructions() != null && !onlineRecipe.getInstructions().isEmpty())
                                    ? onlineRecipe.getInstructions()
                                    : onlineRecipe.getSummary();

                            Recipe local = new Recipe(
                                    onlineRecipe.getTitle(),
                                    onlineRecipe.getIngredients(),
                                    instructions,
                                    "online"
                            );
                            local.setFavorite(asFavorite);

                            repo.add(local, new RecipeRepository.RecipeCallback() {
                                @Override
                                public void onSuccess() {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() -> {
                                        String message = asFavorite ? "Added to Favorites" : "Saved to My Recipes";
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                        recipeAdapter.notifyDataSetChanged();
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(), "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                    );
                                }
                            });
                        });

                        rvRecipes.setAdapter(recipeAdapter);
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error searching recipes: " + message, Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }
}
