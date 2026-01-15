package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;
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

    public SearchOnlineFragment() {
        super(R.layout.fragment_search_online);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        EditText etQuery = view.findViewById(R.id.et_query);
        RecyclerView rv = view.findViewById(R.id.rv_online);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Optional shortcut - user stays here unless they click it
        view.findViewById(R.id.btn_go_my_recipes).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_searchOnlineFragment_to_myRecipesFragment)
        );

        view.findViewById(R.id.btn_search).setOnClickListener(v -> {
            String q = etQuery.getText().toString().trim();
            if (q.isEmpty()) {
                Toast.makeText(requireContext(), "Enter search text", Toast.LENGTH_SHORT).show();
                return;
            }

            new SpoonacularService().search(requireContext(), q, new SpoonacularService.Callback() {
                @Override
                public void onSuccess(List<OnlineRecipe> recipes) {
                    requireActivity().runOnUiThread(() -> {
                        rv.setAdapter(new OnlineRecipeAdapter(recipes, savedKeys, online -> {

                            String instructions = !online.getInstructions().isEmpty()
                                    ? online.getInstructions()
                                    : online.getSummary();

                            RecipeRepository repo = RecipeRepository.getInstance();
                            Recipe existing = repo.getByTitle(online.getTitle());

                            if (existing != null) {
                                boolean updated = false;

                                if (isBlank(existing.getIngredients()) && !isBlank(online.getIngredients())) {
                                    existing.setIngredients(online.getIngredients());
                                    updated = true;
                                }

                                if (isBlank(existing.getInstructions()) && !isBlank(instructions)) {
                                    existing.setInstructions(instructions);
                                    updated = true;
                                }

                                if (updated) {
                                    repo.update(existing);
                                    Toast.makeText(requireContext(), "Updated recipe details", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Already saved", Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            }

                            Recipe local = new Recipe(
                                    online.getTitle(),
                                    online.getIngredients(),
                                    instructions
                            );

                            repo.add(local);
                            Toast.makeText(requireContext(), "Saved to My Recipes", Toast.LENGTH_SHORT).show();
                            return true;
                        }));
                    });
                }

                @Override
                public void onError(String message) {
                    requireActivity().runOnUiThread(() ->
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
