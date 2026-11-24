
package com.archi.happyhappenings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private EditText etName, etEmail, etPassword;
    private RadioGroup rgRole;
    private Button btnRegister;
    private TextView tvLogin;
    private Spinner spinnerVendorCategory;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String vendorCategory = null;

    private final String[] categories = {
            "Select Service Category",
            "Catering",
            "Dresses",
            "Photography",
            "Decoration",
            "Music & DJ",
            "Venue",
            "Makeup",
            "Invitations"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        spinnerVendorCategory = findViewById(R.id.spinnerVendorCategory);

        // Setup category dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerVendorCategory.setAdapter(adapter);
        spinnerVendorCategory.setVisibility(View.GONE);

        // Category selection
        spinnerVendorCategory.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position == 0) {
                            vendorCategory = null;
                        } else {
                            vendorCategory = categories[position];
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                        vendorCategory = null;
                    }
                }
        );

        // Role selection
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.rbVendor) {
                spinnerVendorCategory.setVisibility(View.VISIBLE);
            } else {
                spinnerVendorCategory.setVisibility(View.GONE);
                spinnerVendorCategory.setSelection(0);
                vendorCategory = null;
            }
        });

        // Register button
        btnRegister.setOnClickListener(v -> registerUser());

        // Login navigation
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );
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
            Toast.makeText(
                    this,
                    "Please select User or Vendor",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRoleId);

        String role = selectedRadioButton.getText()
                .toString()
                .trim()
                .toLowerCase();

        // Validate vendor category
        if (role.equals("vendor") && vendorCategory == null) {

            Toast.makeText(
                    this,
                    "Please select a service category",
                    Toast.LENGTH_SHORT
            ).show();

            spinnerVendorCategory.performClick();
            return;
        }

        // Disable button
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating Account...");

        // Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser() == null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText("Create Account");
                            Toast.makeText(
                                    MainActivity.this,
                                    "User account not found",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        String uid = mAuth.getCurrentUser().getUid();

                        // Private user profile
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", uid);
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("role", role);
                        userData.put("createdAt",
                                FieldValue.serverTimestamp());

                        // Public vendor profile
                        Map<String, Object> vendorData = new HashMap<>();

                        if (role.equals("vendor")) {

                            userData.put("vendorCategory", vendorCategory);

                            vendorData.put("uid", uid);

                            // IMPORTANT: Required by Firestore rules
                            vendorData.put("ownerUid", uid);

                            vendorData.put("name", name);
                            vendorData.put("email", email);
                            vendorData.put("category", vendorCategory);
                            vendorData.put("createdAt",
                                    FieldValue.serverTimestamp());
                        }

                        // Firestore batch
                        WriteBatch batch = db.batch();

                        // Save user profile
                        batch.set(
                                db.collection("users").document(uid),
                                userData
                        );

                        // Save public vendor profile only for vendors
                        if (role.equals("vendor")) {
                            batch.set(
                                    db.collection("vendors").document(uid),
                                    vendorData
                            );
                        }

                        batch.commit()
                                .addOnSuccessListener(unused -> {

                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Create Account");

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Registration successful!",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    // Clear form
                                    etName.setText("");
                                    etEmail.setText("");
                                    etPassword.setText("");

                                    rgRole.check(R.id.rbUser);

                                    spinnerVendorCategory.setSelection(0);
                                    spinnerVendorCategory.setVisibility(View.GONE);

                                    vendorCategory = null;
                                })
                                .addOnFailureListener(e -> {

                                    btnRegister.setEnabled(true);
                                    btnRegister.setText("Create Account");

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Profile save failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        btnRegister.setEnabled(true);
                        btnRegister.setText("Create Account");

                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";

                        Toast.makeText(
                                MainActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
