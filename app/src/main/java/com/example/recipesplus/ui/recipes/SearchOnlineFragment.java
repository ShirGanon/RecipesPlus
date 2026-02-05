package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.example.recipesplus.model.OnlineRecipe;
import com.example.recipesplus.model.Recipe;
import com.example.recipesplus.services.SpoonacularService;

import java.util.List;

public class SearchOnlineFragment extends Fragment {

    // Searches Spoonacular and lets users save results locally.
    private Button btnSearch;
    private EditText etQuery;
    private RecyclerView rv;
    private OnlineRecipeAdapter adapter;
    private boolean isRepositoryLoaded = false;

    public SearchOnlineFragment() {
        super(R.layout.fragment_search_online);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etQuery = view.findViewById(R.id.et_query);
        rv = view.findViewById(R.id.rv_online);
        btnSearch = view.findViewById(R.id.btn_search);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Disable search button until local recipes are loaded (used to show saved state).
        btnSearch.setEnabled(false);
        btnSearch.setText("Loading...");

        RecipeRepository.getInstance().loadRecipes(() -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    isRepositoryLoaded = true;
                    btnSearch.setEnabled(true);
                    btnSearch.setText("Search");
                });
            }
        });

        view.findViewById(R.id.btn_go_my_recipes).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_searchOnlineFragment_to_myRecipesFragment)
        );

        btnSearch.setOnClickListener(v -> {
            if (!isRepositoryLoaded) {
                Toast.makeText(requireContext(), "Still loading local data...", Toast.LENGTH_SHORT).show();
                return;
            }

            String q = etQuery.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(requireContext(), "Enter search text", Toast.LENGTH_SHORT).show();
                return;
            }

            new SpoonacularService().search(requireContext(), q, new SpoonacularService.RecipeCallback() {
                @Override
                public void onSuccess(List<OnlineRecipe> recipes) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        // Tap opens details; save button stores locally or toggles favorite.
                        OnlineRecipeAdapter.OnItemClickListener clickListener = onlineRecipe -> {
                            Bundle args = new Bundle();
                            args.putString("title", onlineRecipe.getTitle());
                            args.putString("ingredients", onlineRecipe.getIngredients());
                            String instructions = !onlineRecipe.getInstructions().isEmpty()
                                    ? onlineRecipe.getInstructions()
                                    : onlineRecipe.getSummary();
                            args.putString("instructions", instructions);
                            Navigation.findNavController(requireView())
                                    .navigate(R.id.action_searchOnlineFragment_to_recipeDetailsFragment, args);
                        };

                        adapter = new OnlineRecipeAdapter(recipes, false, (online, asFavorite) -> {
                            RecipeRepository repo = RecipeRepository.getInstance();
                            Recipe existing = repo.getByTitle(online.getTitle());

                            if (existing != null) {
                                if (asFavorite && !existing.isFavorite()) {
                                    // Add to favorites
                                    existing.setFavorite(true);
                                    repo.update(existing);
                                    Toast.makeText(requireContext(), "Added to favorites.", Toast.LENGTH_SHORT).show();
                                    adapter.notifyDataSetChanged();
                                } else if (!asFavorite) {
                                    // Unsave the recipe (removes it whether favorited or not)
                                    repo.delete(existing.getId());
                                    Toast.makeText(requireContext(), "Recipe removed.", Toast.LENGTH_SHORT).show();
                                    adapter.notifyDataSetChanged();
                                }
                                return;
                            }

                            // Recipe doesn't exist, so save it
                            String instructions = !online.getInstructions().isEmpty()
                                    ? online.getInstructions()
                                    : online.getSummary();

                            Recipe local = new Recipe(
                                    online.getTitle(),
                                    online.getIngredients(),
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
                                        adapter.notifyDataSetChanged();
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
                        }, clickListener);
                        rv.setAdapter(adapter);
                    });
                }

                @Override
                public void onError(String message) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_LONG).show()
                    );
                }
            });
        });
    }
}
