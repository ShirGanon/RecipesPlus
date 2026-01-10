package com.example.recipesplus.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        view.findViewById(R.id.btn_my_recipes).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_myRecipesFragment)
        );

        view.findViewById(R.id.btn_add_recipe).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_addRecipeFragment)
        );

        view.findViewById(R.id.btn_search_online).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_searchOnlineFragment)
        );

        // ✅ Favorites
        view.findViewById(R.id.btn_favorites).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_favoritesFragment)
        );
    }
}
