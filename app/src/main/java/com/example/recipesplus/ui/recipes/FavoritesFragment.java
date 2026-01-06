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

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        RecyclerView recyclerView = view.findViewById(R.id.rv_favorites);
        TextView emptyText = view.findViewById(R.id.tv_empty_favorites);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Recipe> allRecipes = RecipeRepository.getInstance().getAll();
        List<Recipe> favorites = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (recipe.isFavorite()) {
                favorites.add(recipe);
            }
        }

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

                                Navigation.findNavController(view)
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
