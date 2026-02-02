package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesFragment";
    private RecyclerView recyclerView;
    private TextView emptyText;

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.rv_favorites);
        emptyText = view.findViewById(R.id.tv_empty_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loadAndDisplayFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAndDisplayFavorites();
    }

    private void loadAndDisplayFavorites() {
        RecipeRepository repo = RecipeRepository.getInstance();
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

        Log.d(TAG, "Displaying " + favorites.size() + " favorite recipes");

        if (favorites.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(
                    new RecipeAdapter(
                            favorites,
                            new RecipeAdapter.OnRecipeClickListener() {
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
                            }
                    )
            );
        }
    }
}
