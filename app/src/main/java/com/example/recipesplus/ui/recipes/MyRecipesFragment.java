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

import java.util.List;
import java.util.stream.Collectors;

public class MyRecipesFragment extends Fragment {

    private static final String TAG = "MyRecipesFragment";
    private RecyclerView rv;
    private TextView empty;
    private boolean isFirstLoad = true;

    public MyRecipesFragment() {
        super(R.layout.fragment_my_recipes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv = view.findViewById(R.id.rv_recipes);
        empty = view.findViewById(R.id.tv_empty_recipes);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadAndDisplayRecipes();
        isFirstLoad = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only refresh if this is not the first load (to avoid double loading)
        // This ensures we refresh when coming back from AddRecipeFragment
        if (!isFirstLoad && rv != null && empty != null) {
            loadAndDisplayRecipes();
        }
    }

    private void loadAndDisplayRecipes() {
        RecipeRepository repo = RecipeRepository.getInstance();

        // Always reload recipes when fragment is shown to ensure we have the latest data
        repo.loadRecipes(() -> {
            // Ensure we're on the main thread (Firestore callbacks should already be on main thread)
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    List<Recipe> recipes = repo.getAll();
                    Log.d(TAG, "Updating UI with " + recipes.size() + " recipes");
                    updateUI(rv, empty, recipes);
                });
            }
        });
    }

    private void updateUI(RecyclerView rv, TextView empty, List<Recipe> recipes) {
        List<Recipe> manualRecipes = recipes.stream()
                .filter(r -> "manual".equals(r.getSource()))
                .collect(Collectors.toList());

        empty.setVisibility(manualRecipes.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(manualRecipes.isEmpty() ? View.GONE : View.VISIBLE);

        rv.setAdapter(new RecipeAdapter(
                manualRecipes,
                new RecipeAdapter.OnRecipeClickListener() {
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
                }
        ));
    }

}
