
package com.archi.happyhappenings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private EditText etName, etEmail, etPassword;
    private RadioGroup rgRole;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Register button
        btnRegister.setOnClickListener(v -> registerUser());

        // Open Login screen
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate name
        if (name.isEmpty()) {
            etName.setError("Please enter your full name");
            etName.requestFocus();
            return;
        }

        // Validate email
        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (password.isEmpty()) {
            etPassword.setError("Please enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Validate role
        int selectedRoleId = rgRole.getCheckedRadioButtonId();

        if (selectedRoleId == -1) {
            Toast.makeText(this,
                    "Please select User or Vendor",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRoleId);
        String role = selectedRadioButton.getText().toString().trim().toLowerCase();

        // Disable button to prevent duplicate clicks
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // Create Firebase Authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        // Create user profile data
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", uid);
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("role", role);
                        userData.put("createdAt", FieldValue.serverTimestamp());

                        // Save profile in Firestore
                        db.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(unused -> {

                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Create Account");

                                    Toast.makeText(this,
                                            "Registration successful! Role: " + role,
                                            Toast.LENGTH_LONG).show();

                                    // Clear registration form
                                    etName.setText("");
                                    etEmail.setText("");
                                    etPassword.setText("");
                                    rgRole.check(R.id.rbUser);

                                })
                                .addOnFailureListener(e -> {

                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Create Account");

                                    Toast.makeText(this,
                                            "Account created, but profile save failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    } else {

                        btnRegister.setEnabled(true);
                        btnRegister.setText("Create Account");

                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";

                        Toast.makeText(this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
