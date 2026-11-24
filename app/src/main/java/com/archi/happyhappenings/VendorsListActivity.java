
package com.archi.happyhappenings;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class VendorsListActivity extends Activity {

    private LinearLayout vendorContainer;
    private TextView tvStatus;
    private String selectedCategory;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedCategory = getIntent().getStringExtra("category");
        db = FirebaseFirestore.getInstance();

        if (selectedCategory == null || selectedCategory.trim().isEmpty()) {
            Toast.makeText(this, "Category not found",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        createScreen();
        loadVendors();
    }

    private void createScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF4F7FB);
        root.setPadding(24, 24, 24, 24);

        // Back Button
        TextView tvBack = new TextView(this);
        tvBack.setText("←  Back");
        tvBack.setTextSize(16);
        tvBack.setTextColor(0xFF555555);
        tvBack.setPadding(0, 8, 0, 20);
        tvBack.setOnClickListener(v -> finish());

        root.addView(tvBack);

        // Heading
        TextView tvHeading = new TextView(this);
        tvHeading.setText(selectedCategory + " Vendors");
        tvHeading.setTextSize(25);
        tvHeading.setTextColor(0xFF222222);
        tvHeading.setTypeface(null, android.graphics.Typeface.BOLD);

        root.addView(tvHeading);

        // Subtitle
        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Find the right vendor for your event");
        tvSubtitle.setTextSize(14);
        tvSubtitle.setTextColor(0xFF777777);
        tvSubtitle.setPadding(0, 8, 0, 20);

        root.addView(tvSubtitle);

        // Status
        tvStatus = new TextView(this);
        tvStatus.setText("Loading vendors...");
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(0xFF777777);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(10, 30, 10, 30);

        root.addView(tvStatus);

        // Vendor Container
        vendorContainer = new LinearLayout(this);
        vendorContainer.setOrientation(LinearLayout.VERTICAL);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(vendorContainer);

        root.addView(scrollView);

        setContentView(root);
    }

    private void loadVendors() {

        db.collection("vendors")
                .whereEqualTo("category", selectedCategory)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    vendorContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvStatus.setText(
                                "No vendors available in "
                                        + selectedCategory + " yet."
                        );
                        tvStatus.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvStatus.setVisibility(View.GONE);

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String vendorName = document.getString("name");
                        String description = document.getString("description");
                        String location = document.getString("location");
                        String email = document.getString("email");
                        String phone = document.getString("phone");

                        if (vendorName == null ||
                                vendorName.trim().isEmpty()) {
                            vendorName = "Vendor";
                        }

                        addVendorCard(
                                vendorName,
                                selectedCategory,
                                description,
                                location,
                                email,
                                phone
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    tvStatus.setText("Unable to load vendors.");
                    tvStatus.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addVendorCard(
            String vendorName,
            String category,
            String description,
            String location,
            String email,
            String phone) {

        // Vendor Card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        card.setPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setElevation(3);

        // Vendor Name
        TextView tvVendorName = new TextView(this);
        tvVendorName.setText(vendorName);
        tvVendorName.setTextSize(20);
        tvVendorName.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        tvVendorName.setTextColor(0xFF222222);

        card.addView(tvVendorName);

        // Category
        TextView tvCategory = new TextView(this);
        tvCategory.setText(category);
        tvCategory.setTextSize(14);
        tvCategory.setTextColor(0xFF7B61FF);
        tvCategory.setPadding(0, 8, 0, 8);

        card.addView(tvCategory);

        // Brief Description
        TextView tvDescription = new TextView(this);

        if (description != null && !description.trim().isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText(
                    "Explore services offered by this vendor."
            );
        }

        tvDescription.setTextSize(14);
        tvDescription.setTextColor(0xFF666666);
        tvDescription.setPadding(0, 4, 0, 8);

        card.addView(tvDescription);

        // Location (if available)
        if (location != null && !location.trim().isEmpty()) {

            TextView tvLocation = new TextView(this);
            tvLocation.setText("📍 " + location);
            tvLocation.setTextSize(14);
            tvLocation.setTextColor(0xFF777777);
            tvLocation.setPadding(0, 4, 0, 8);

            card.addView(tvLocation);
        }

        // View Profile Button
        Button profileButton = new Button(this);
        profileButton.setText("View Profile");

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(0, 12, 0, 0);
        profileButton.setLayoutParams(buttonParams);

        profileButton.setOnClickListener(v ->
                showVendorProfile(
                        vendorName,
                        category,
                        description,
                        location,
                        email,
                        phone
                )
        );

        card.addView(profileButton);

        vendorContainer.addView(card);
    }

    private void showVendorProfile(
            String name,
            String category,
            String description,
            String location,
            String email,
            String phone) {

        StringBuilder details = new StringBuilder();

        details.append("Category: ").append(category);

        if (description != null && !description.trim().isEmpty()) {
            details.append("\n\nAbout: ").append(description);
        }

        if (location != null && !location.trim().isEmpty()) {
            details.append("\n\nLocation: ").append(location);
        }

        if (email != null && !email.trim().isEmpty()) {
            details.append("\n\nEmail: ").append(email);
        }

        if (phone != null && !phone.trim().isEmpty()) {
            details.append("\n\nPhone: ").append(phone);
        }

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
