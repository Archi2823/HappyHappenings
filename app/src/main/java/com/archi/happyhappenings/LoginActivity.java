package com.archi.happyhappenings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends Activity {

    private EditText etLoginEmail, etLoginPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });
    }

    private void loginUser() {

        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etLoginEmail.setError("Enter your email");
            etLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etLoginPassword.setError("Enter your password");
            etLoginPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(document -> {

                                    btnLogin.setEnabled(true);
                                    btnLogin.setText("Login");

                                    if (!document.exists()) {
                                        Toast.makeText(this,
                                                "User profile not found in database",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                        return;
                                    }

                                    String role = document.getString("role");

                                    if (role == null) {
                                        Toast.makeText(this,
                                                "User role not found",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                        return;
                                    }

                                    role = role.trim().toLowerCase();

                                    if (role.equals("user")) {
                                        Intent intent = new Intent(
                                                LoginActivity.this,
                                                UserDashboardActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else if (role.equals("vendor")) {
                                        Intent intent = new Intent(
                                                LoginActivity.this,
                                                VendorDashboardActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(this,
                                                "Invalid user role: " + role,
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    btnLogin.setEnabled(true);
                                    btnLogin.setText("Login");

                                    Toast.makeText(this,
                                            "Failed to load user profile: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");

                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed";

                        Toast.makeText(this,
                                error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}