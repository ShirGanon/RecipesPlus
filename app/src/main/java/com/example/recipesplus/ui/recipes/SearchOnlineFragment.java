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

                            Recipe local = new Recipe(
                                    online.getTitle(),
                                    "", // ingredients not available in this simplified flow
                                    instructions
                            );

                            boolean saved = RecipeRepository.getInstance().addIfNotExists(local);

                            if (saved) {
                                Toast.makeText(requireContext(), "Saved to My Recipes", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Already saved", Toast.LENGTH_SHORT).show();
                            }

                            return saved;
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
}
