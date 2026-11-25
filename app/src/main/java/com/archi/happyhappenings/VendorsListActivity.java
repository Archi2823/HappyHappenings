
package com.archi.happyhappenings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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

import java.util.HashSet;
import java.util.Set;

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

        if (selectedCategory == null ||
                selectedCategory.trim().isEmpty()) {
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

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        root.addView(scrollView, scrollParams);

        setContentView(root);
    }

    // Load vendors from registration category OR added service category
    private void loadVendors() {

        // First get vendors who have services in this category
        db.collection("vendorServices")
                .whereEqualTo("category", selectedCategory)
                .get()
                .addOnSuccessListener(serviceSnapshot -> {

                    Set<String> serviceVendorUids = new HashSet<>();

                    for (DocumentSnapshot serviceDoc :
                            serviceSnapshot.getDocuments()) {

                        String uid = serviceDoc.getString("vendorUid");

                        if (uid != null && !uid.trim().isEmpty()) {
                            serviceVendorUids.add(uid);
                        }
                    }

                    // Load vendor profiles
                    db.collection("vendors")
                            .get()
                            .addOnSuccessListener(vendorSnapshot -> {

                                vendorContainer.removeAllViews();

                                int vendorCount = 0;

                                for (DocumentSnapshot document :
                                        vendorSnapshot.getDocuments()) {

                                    String vendorUid =
                                            document.getString("uid");

                                    if (vendorUid == null ||
                                            vendorUid.trim().isEmpty()) {
                                        vendorUid = document.getId();
                                    }

                                    String registeredCategory =
                                            document.getString("category");

                                    // Check registration category
                                    boolean registeredInCategory =
                                            selectedCategory.equals(
                                                    registeredCategory
                                            );

                                    // Check added services category
                                    boolean hasServiceInCategory =
                                            serviceVendorUids.contains(
                                                    vendorUid
                                            );

                                    // Vendor can appear through either category
                                    if (registeredInCategory ||
                                            hasServiceInCategory) {

                                        String vendorName =
                                                document.getString("name");

                                        String description =
                                                document.getString("description");

                                        String location =
                                                document.getString("location");

                                        String email =
                                                document.getString("email");

                                        String phone =
                                                document.getString("phone");

                                        if (vendorName == null ||
                                                vendorName.trim().isEmpty()) {
                                            vendorName = "Vendor";
                                        }

                                        addVendorCard(
                                                vendorUid,
                                                vendorName,
                                                selectedCategory,
                                                description,
                                                location,
                                                email,
                                                phone
                                        );

                                        vendorCount++;
                                    }
                                }

                                if (vendorCount == 0) {
                                    tvStatus.setText(
                                            "No vendors available in "
                                                    + selectedCategory
                                                    + " yet."
                                    );

                                    tvStatus.setVisibility(View.VISIBLE);
                                } else {
                                    tvStatus.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> {

                                tvStatus.setText(
                                        "Unable to load vendors."
                                );

                                tvStatus.setVisibility(View.VISIBLE);

                                Toast.makeText(
                                        this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    tvStatus.setText(
                            "Unable to load category services."
                    );

                    tvStatus.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Create Vendor Card
    private void addVendorCard(
            String vendorUid,
            String vendorName,
            String category,
            String description,
            String location,
            String email,
            String phone) {

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

        // Selected Category
        TextView tvCategory = new TextView(this);
        tvCategory.setText("Category: " + category);
        tvCategory.setTextSize(14);
        tvCategory.setTextColor(0xFF7B61FF);
        tvCategory.setPadding(0, 8, 0, 8);

        card.addView(tvCategory);

        // Brief Description
        TextView tvDescription = new TextView(this);

        if (description != null &&
                !description.trim().isEmpty()) {
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

        // Location
        if (location != null &&
                !location.trim().isEmpty()) {

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

        // View Services Button
        Button servicesButton = new Button(this);
        servicesButton.setText("View Services");

        LinearLayout.LayoutParams servicesButtonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        servicesButtonParams.setMargins(0, 8, 0, 0);
        servicesButton.setLayoutParams(servicesButtonParams);
        servicesButton.setTextColor(0xFFFFFFFF);
        servicesButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        0xFF6C42C1
                )
        );
        servicesButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    VendorsListActivity.this,
                    VendorServicesActivity.class
            );

            intent.putExtra("vendorUid", vendorUid);
            intent.putExtra("vendorName", vendorName);
            intent.putExtra("category", category);

            startActivity(intent);
        });

        card.addView(servicesButton);

        vendorContainer.addView(card);
    }

    // Show Vendor Profile
    private void showVendorProfile(
            String name,
            String category,
            String description,
            String location,
            String email,
            String phone) {

        StringBuilder details = new StringBuilder();

        details.append("Category: ").append(category);

        if (description != null &&
                !description.trim().isEmpty()) {
            details.append("\n\nAbout: ").append(description);
        }

        if (location != null &&
                !location.trim().isEmpty()) {
            details.append("\n\nLocation: ").append(location);
        }

        if (email != null &&
                !email.trim().isEmpty()) {
            details.append("\n\nEmail: ").append(email);
        }

        if (phone != null &&
                !phone.trim().isEmpty()) {
            details.append("\n\nPhone: ").append(phone);
        }

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage(details.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
