package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    // Shows only recipes marked as favorites, with a search filter.
    private static final String TAG = "FavoritesFragment";
    private RecyclerView recyclerView;
    private TextView emptyText;
    private EditText etSearch;
    private RecipeAdapter adapter;
    private List<Recipe> allFavorites = new ArrayList<>();

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.rv_favorites);
        emptyText = view.findViewById(R.id.tv_empty_favorites);
        etSearch = view.findViewById(R.id.et_favorites_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Favorites list supports search and unfavorite actions.
        adapter = new RecipeAdapter(new ArrayList<>(), new RecipeAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Bundle args = new Bundle();
                args.putString("recipeId", recipe.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_favoritesFragment_to_recipeDetailsFragment, args);
            }

            @Override
            public void onEditClick(Recipe recipe) {
                // Not applicable for favorites, do nothing.
            }

            @Override
            public void onFavoriteClick(Recipe recipe) {
                recipe.setFavorite(false);
                RecipeRepository.getInstance().update(recipe);
                loadAndDisplayFavorites();
            }
        });
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadAndDisplayFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAndDisplayFavorites();
    }

    private void loadAndDisplayFavorites() {
        RecipeRepository repo = RecipeRepository.getInstance();
        // Refresh from Firestore to keep favorites accurate.
        repo.loadRecipes(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    List<Recipe> allRecipes = repo.getAll();
                    updateUI(recyclerView, emptyText, allRecipes);
                });
            }
        });
    }

    private void updateUI(RecyclerView recyclerView, TextView emptyText, List<Recipe> allRecipes) {
        List<Recipe> favorites = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.isFavorite()) {
                favorites.add(recipe);
            }
        }

        allFavorites = favorites;
        Log.d(TAG, "Displaying " + favorites.size() + " favorite recipes");
        applyFilter(etSearch != null ? etSearch.getText().toString() : "");
    }

    private void applyFilter(String query) {
        // Simple client-side filter across title, ingredients, and instructions.
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Recipe> filtered = new ArrayList<>();

        if (q.isEmpty()) {
            filtered.addAll(allFavorites);
        } else {
            for (Recipe r : allFavorites) {
                String title = r.getTitle() == null ? "" : r.getTitle();
                String ingredients = r.getIngredients() == null ? "" : r.getIngredients();
                String instructions = r.getInstructions() == null ? "" : r.getInstructions();
                if (title.toLowerCase().contains(q)
                        || ingredients.toLowerCase().contains(q)
                        || instructions.toLowerCase().contains(q)) {
                    filtered.add(r);
                }
            }
        }

        if (filtered.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (q.isEmpty()) {
                emptyText.setText(R.string.no_favorites_yet);
            } else {
                emptyText.setText("No favorites match your search");
            }
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter != null) {
            adapter.updateRecipes(filtered);
        }
    }
}
