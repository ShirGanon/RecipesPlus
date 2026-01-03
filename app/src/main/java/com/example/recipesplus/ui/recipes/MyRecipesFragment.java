package com.example.recipesplus.ui.recipes;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;

public class MyRecipesFragment extends Fragment {

    public MyRecipesFragment() {
        super(R.layout.fragment_my_recipes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rv_recipes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        rv.setAdapter(new RecipeAdapter(
                RecipeRepository.getInstance().getAll(),
                recipe -> {
                    Bundle args = new Bundle();
                    args.putString("recipeId", recipe.getId());

                    Navigation.findNavController(view)
                            .navigate(R.id.action_myRecipesFragment_to_recipeDetailsFragment, args);
                }
        ));
    }
}
