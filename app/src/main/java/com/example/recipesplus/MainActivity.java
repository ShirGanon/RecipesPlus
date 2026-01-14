package com.example.recipesplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.recipesplus.ui.profile.EditProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        navController = navHostFragment.getNavController();

        // Connect toolbar with navigation (title/back button)
        NavigationUI.setupWithNavController(toolbar, navController);

        // Inflate the menu ONCE and handle clicks directly on the toolbar
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_home);

        toolbar.setOnMenuItemClickListener(this::handleToolbarMenuClick);

        // Show menu items only on Home screen
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            boolean isHome = destination.getId() == R.id.homeFragment;
            for (int i = 0; i < toolbar.getMenu().size(); i++) {
                toolbar.getMenu().getItem(i).setVisible(isHome);
            }
        });
    }

    private boolean handleToolbarMenuClick(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_edit_profile) {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.action_logout) {
            Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();

            FirebaseAuth.getInstance().signOut();

            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build();

            navController.navigate(R.id.loginFragment, null, navOptions);
            return true;
        }

        return false;
    }
}
