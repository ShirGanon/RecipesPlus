package com.example.recipesplus.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        NavController nav = Navigation.findNavController(view);

        // Auto-login, but do NOT auto-login when we arrived here from another screen (e.g., Logout)
        // When coming from Home -> Login, previousBackStackEntry exists.
        if (auth.getCurrentUser() != null && nav.getPreviousBackStackEntry() == null) {
            // Load recipes from Firestore before navigating
            RecipeRepository.getInstance().loadRecipes(() -> {
                nav.navigate(R.id.action_loginFragment_to_homeFragment);
            });
            return;
        }

        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        TextInputEditText etPassword = view.findViewById(R.id.et_password);

        Button btnLogin = view.findViewById(R.id.btn_login);
        Button btnGoRegister = view.findViewById(R.id.btn_go_register);

        btnGoRegister.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_loginFragment_to_registerFragment)
        );

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(), "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), "Logged in", Toast.LENGTH_SHORT).show();
                            
                            // Load recipes from Firestore
                            RecipeRepository.getInstance().loadRecipes(() -> {
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_loginFragment_to_homeFragment);
                            });
                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Login failed";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
