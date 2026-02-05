package com.example.recipesplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.recipesplus.ui.profile.EditProfileActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Hosts the Navigation component and a custom toolbar.
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Use a custom centered TextView for titles instead of the default ActionBar title.
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbarTitle = findViewById(R.id.toolbar_title);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Top-level destinations hide the back arrow.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.loginFragment, R.id.homeFragment)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_bold);
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Sync toolbar title and back button with the current screen.
            toolbarTitle.setText(destination.getLabel());
            boolean isTopLevel = destination.getId() == R.id.homeFragment
                    || destination.getId() == R.id.loginFragment;
            if (isTopLevel) {
                toolbar.setNavigationIcon(null);
            } else {
                toolbar.setNavigationIcon(R.drawable.ic_back_bold);
            }
            boolean isExcluded = destination.getId() == R.id.homeFragment
                    || destination.getId() == R.id.loginFragment;
            toolbarTitle.setTranslationY(isExcluded ? 0f : dpToPx(12));
            invalidateOptionsMenu();
        });
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Show profile + logout only on the home screen.
        boolean isHome = navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == R.id.homeFragment;

        MenuItem editProfile = menu.findItem(R.id.action_edit_profile);
        if (editProfile != null) {
            editProfile.setVisible(isHome);
        }

        MenuItem logout = menu.findItem(R.id.action_logout);
        if (logout != null) {
            logout.setVisible(isHome);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (NavigationUI.onNavDestinationSelected(item, navController)) {
            return true;
        }

        if (item.getItemId() == R.id.action_edit_profile) {
            // Launch profile edit as a separate activity.
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.action_logout) {
            // Clear auth state and return to login.
            FirebaseAuth.getInstance().signOut();
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build();
            navController.navigate(R.id.loginFragment, null, navOptions);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
