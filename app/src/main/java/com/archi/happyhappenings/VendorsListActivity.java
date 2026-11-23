
package com.archi.happyhappenings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

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
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
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

        // Back button
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

        // Status text
        tvStatus = new TextView(this);
        tvStatus.setText("Loading vendors...");
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(0xFF777777);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(10, 30, 10, 30);

        root.addView(tvStatus);

        // Vendors container
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
                                        + selectedCategory
                                        + " yet."
                        );
                        tvStatus.setVisibility(android.view.View.VISIBLE);
                        return;
                    }

                    tvStatus.setVisibility(android.view.View.GONE);

                    for (com.google.firebase.firestore.DocumentSnapshot document
                            : querySnapshot.getDocuments()) {

                        String vendorName = document.getString("name");

                        if (vendorName == null || vendorName.trim().isEmpty()) {
                            vendorName = "Vendor";
                        }

                        addVendorCard(vendorName);
                    }
                })
                .addOnFailureListener(e -> {

                    tvStatus.setText("Unable to load vendors.");
                    tvStatus.setVisibility(android.view.View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addVendorCard(String vendorName) {

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

        TextView tvVendorName = new TextView(this);
        tvVendorName.setText(vendorName);
        tvVendorName.setTextSize(18);
        tvVendorName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvVendorName.setTextColor(0xFF222222);

        card.addView(tvVendorName);

        TextView tvCategory = new TextView(this);
        tvCategory.setText(selectedCategory);
        tvCategory.setTextSize(14);
        tvCategory.setTextColor(0xFF777777);
        tvCategory.setPadding(0, 8, 0, 0);

        card.addView(tvCategory);

        vendorContainer.addView(card);
    }
}
