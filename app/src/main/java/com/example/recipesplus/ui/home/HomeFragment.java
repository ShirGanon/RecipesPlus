package com.example.recipesplus.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
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

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            // Debug toast (remove later if you want)
            Toast.makeText(requireContext(), "Logout clicked", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }
    }
}
