package com.example.recipesplus.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // My Recipes
        view.findViewById(R.id.btn_my_recipes).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_myRecipesFragment)
        );

        // Add Recipe
        view.findViewById(R.id.btn_add_recipe).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_addRecipeFragment)
        );

        // Favorites
        view.findViewById(R.id.btn_favorites).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_favoritesFragment)
        );

        // Search Online
        view.findViewById(R.id.btn_search_online).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_searchOnlineFragment)
        );

        // Search by Ingredients
        view.findViewById(R.id.btn_search_by_ingredients).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_searchByIngredientsFragment)
        );

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            RecipeRepository.getInstance().clear();

            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build();

            Navigation.findNavController(v)
                    .navigate(R.id.loginFragment, null, navOptions);
        });

        // About
        view.findViewById(R.id.btn_about).setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_homeFragment_to_aboutFragment)
        );
    }
}
