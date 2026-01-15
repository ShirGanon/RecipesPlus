package com.example.recipesplus.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recipesplus.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSave = findViewById(R.id.btn_save);

        // מילוי נתונים קיימים
        if (user.getDisplayName() != null) etFullName.setText(user.getDisplayName());
        if (user.getEmail() != null) etEmail.setText(user.getEmail());

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm = etConfirmPassword.getText().toString();

        boolean wantsPasswordChange = !TextUtils.isEmpty(password) || !TextUtils.isEmpty(confirm);

        // אימות בסיסי
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (wantsPasswordChange) {
            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }
            if (!password.equals(confirm)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }
        }

        // 1) עדכון שם (displayName)
        UserProfileChangeRequest profileUpdates =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build();

        user.updateProfile(profileUpdates)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update name: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );

        // 2) עדכון אימייל (רק אם השתנה)
        if (user.getEmail() == null || !email.equals(user.getEmail())) {
            user.updateEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to update email: " + e.getMessage() +
                                            "\n(If it says 'recent login required' — logout/login and try again.)",
                                    Toast.LENGTH_LONG).show()
                    );
        }

        // 3) עדכון סיסמה (אם המשתמש הזין)
        if (wantsPasswordChange) {
            user.updatePassword(password)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to update password: " + e.getMessage() +
                                            "\n(If it says 'recent login required' — logout/login and try again.)",
                                    Toast.LENGTH_LONG).show()
                    );
        }

        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
