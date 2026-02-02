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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchOnlineFragment extends Fragment {

    private final Set<String> savedKeys = new HashSet<>();
    private Button btnSearch;
    private EditText etQuery;
    private RecyclerView rv;
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

        // Disable search button until local recipes are loaded to prevent a crash
        btnSearch.setEnabled(false);
        btnSearch.setText("Loading...");

        // Pre-load the recipe repository to prevent race conditions
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
                Toast.makeText(requireContext(), "Still loading local data, please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            String q = etQuery.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(requireContext(), "Enter search text", Toast.LENGTH_SHORT).show();
                return;
            }

            new SpoonacularService().search(requireContext(), q, new SpoonacularService.Callback() {
                @Override
                public void onSuccess(List<OnlineRecipe> recipes) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        rv.setAdapter(new OnlineRecipeAdapter(recipes, savedKeys, online -> {
                            RecipeRepository repo = RecipeRepository.getInstance();

                            // --- ROBUST FIX START ---
                            // Manually and safely check for an existing recipe to avoid the crash
                            Recipe existing = null;
                            String onlineTitle = online.getTitle();
                            if (onlineTitle != null) {
                                List<Recipe> allLocalRecipes = repo.getAll();
                                for (Recipe localRecipe : allLocalRecipes) {
                                    if (localRecipe != null && localRecipe.getTitle() != null && onlineTitle.equalsIgnoreCase(localRecipe.getTitle())) {
                                        existing = localRecipe;
                                        break;
                                    }
                                }
                            }
                            // --- ROBUST FIX END ---

                            if (existing != null) {
                                if (!existing.isFavorite()) {
                                    existing.setFavorite(true);
                                    repo.update(existing);
                                    Toast.makeText(requireContext(), "Recipe moved to favorites.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Recipe already in favorites.", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }

                            String instructions = !online.getInstructions().isEmpty()
                                    ? online.getInstructions()
                                    : online.getSummary();

                            Recipe local = new Recipe(
                                    online.getTitle(),
                                    online.getIngredients(),
                                    instructions,
                                    "online"
                            );

                            repo.add(local);
                            Toast.makeText(requireContext(), "Added to Favorites", Toast.LENGTH_SHORT).show();
                            return true;
                        }));
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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}