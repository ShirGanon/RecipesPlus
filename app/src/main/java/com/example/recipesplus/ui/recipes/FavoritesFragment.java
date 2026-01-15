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
    private boolean isFirstLoad = true;

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.rv_favorites);
        emptyText = view.findViewById(R.id.tv_empty_favorites);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadAndDisplayFavorites();
        isFirstLoad = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only refresh if this is not the first load (to avoid double loading)
        // This ensures we refresh when coming back from other screens
        if (!isFirstLoad && recyclerView != null && emptyText != null) {
            loadAndDisplayFavorites();
        }
    }

    private void loadAndDisplayFavorites() {
        RecipeRepository repo = RecipeRepository.getInstance();
        
        // Always reload recipes when fragment is shown to ensure we have the latest data
        repo.loadRecipes(() -> {
            // Ensure we're on the main thread
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

        // Filter recipes by favorite status (synced to Firestore)
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
                            recipe -> {
                                Bundle args = new Bundle();
                                args.putString("recipeId", recipe.getId());

                                Navigation.findNavController(requireView())
                                        .navigate(
                                                R.id.action_favoritesFragment_to_recipeDetailsFragment,
                                                args
                                        );
                            }
                    )
            );
        }
    }
}
