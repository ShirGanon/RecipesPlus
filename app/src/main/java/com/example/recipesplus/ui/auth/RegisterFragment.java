package com.example.recipesplus.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.recipesplus.R;
import com.example.recipesplus.data.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterFragment extends Fragment {

    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        EditText etFullName = view.findViewById(R.id.et_full_name);
        EditText etPhone = view.findViewById(R.id.et_phone);
        EditText etCountry = view.findViewById(R.id.et_country);
        EditText etEmail = view.findViewById(R.id.et_email);
        EditText etPassword = view.findViewById(R.id.et_password);
        EditText etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        Button btnRegister = view.findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            String country = etCountry.getText() != null ? etCountry.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(country)
                    || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnRegister.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build();
                                user.updateProfile(profileUpdates);
                            }
                            Toast.makeText(requireContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                            
                            // Load recipes from Firestore (will be empty for new user)
                            RecipeRepository.getInstance().loadRecipes(() -> {
                                Navigation.findNavController(view)
                                        .navigate(R.id.action_registerFragment_to_homeFragment);
                            });
                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Registration failed";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
