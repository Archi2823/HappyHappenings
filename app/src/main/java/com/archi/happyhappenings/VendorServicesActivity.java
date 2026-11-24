
package com.archi.happyhappenings;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class VendorServicesActivity extends Activity {

    private LinearLayout servicesContainer;
    private TextView tvStatus;

    private FirebaseFirestore db;

    private String vendorUid;
    private String vendorName;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vendorUid = getIntent().getStringExtra("vendorUid");
        vendorName = getIntent().getStringExtra("vendorName");
        category = getIntent().getStringExtra("category");

        db = FirebaseFirestore.getInstance();

        if (vendorUid == null || vendorUid.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Vendor not found",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        if (vendorName == null || vendorName.trim().isEmpty()) {
            vendorName = "Vendor";
        }

        if (category == null || category.trim().isEmpty()) {
            category = "Services";
        }

        createScreen();
        loadServices();
    }

    // Create screen
    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(248, 246, 255));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        // Back Button
        TextView tvBack = new TextView(this);
        tvBack.setText("←  Back");
        tvBack.setTextSize(16);
        tvBack.setTextColor(Color.rgb(90, 80, 100));
        tvBack.setPadding(0, 8, 0, 20);

        tvBack.setOnClickListener(v -> finish());

        root.addView(tvBack);

        // App Title
        TextView tvAppTitle = new TextView(this);
        tvAppTitle.setText("Happy Happenings");
        tvAppTitle.setTextSize(25);
        tvAppTitle.setTextColor(Color.rgb(108, 66, 193));
        tvAppTitle.setTypeface(null, Typeface.BOLD);
        tvAppTitle.setGravity(Gravity.CENTER);
        tvAppTitle.setPadding(0, 0, 0, 20);

        root.addView(tvAppTitle);

        // Vendor Name
        TextView tvVendorName = new TextView(this);
        tvVendorName.setText(vendorName);
        tvVendorName.setTextSize(23);
        tvVendorName.setTextColor(Color.rgb(45, 35, 65));
        tvVendorName.setTypeface(null, Typeface.BOLD);

        root.addView(tvVendorName);

        // Selected Category
        TextView tvCategory = new TextView(this);
        tvCategory.setText("Category: " + category);
        tvCategory.setTextSize(15);
        tvCategory.setTextColor(Color.rgb(108, 66, 193));
        tvCategory.setPadding(0, 8, 0, 8);

        root.addView(tvCategory);

        // Subtitle
        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText(
                "Explore " + category + " services offered by this vendor"
        );
        tvSubtitle.setTextSize(14);
        tvSubtitle.setTextColor(Color.GRAY);
        tvSubtitle.setPadding(0, 0, 0, 20);

        root.addView(tvSubtitle);

        // Services Heading
        TextView tvHeading = new TextView(this);
        tvHeading.setText("Available Services");
        tvHeading.setTextSize(21);
        tvHeading.setTextColor(Color.rgb(65, 45, 100));
        tvHeading.setTypeface(null, Typeface.BOLD);
        tvHeading.setPadding(0, 0, 0, 12);

        root.addView(tvHeading);

        // Status
        tvStatus = new TextView(this);
        tvStatus.setText("Loading services...");
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(Color.GRAY);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(10, 30, 10, 30);

        root.addView(tvStatus);

        // Services Container
        servicesContainer = new LinearLayout(this);
        servicesContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(servicesContainer);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    // Load only services belonging to this vendor and category
    private void loadServices() {

        db.collection("vendorServices")
                .whereEqualTo("vendorUid", vendorUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    servicesContainer.removeAllViews();

                    int serviceCount = 0;

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String serviceCategory =
                                document.getString("category");

                        // If service category is missing,
                        // use vendor registration category
                        if (serviceCategory == null ||
                                serviceCategory.trim().isEmpty()) {
                            serviceCategory = category;
                        }

                        // Only show services in the selected category
                        if (!category.equalsIgnoreCase(
                                serviceCategory.trim())) {
                            continue;
                        }

                        String title = document.getString("title");
                        String description =
                                document.getString("description");

                        Object priceObject = document.get("price");

                        double price = 0;

                        if (priceObject instanceof Number) {
                            price = ((Number) priceObject).doubleValue();
                        }

                        if (title == null ||
                                title.trim().isEmpty()) {
                            title = "Service";
                        }

                        if (description == null) {
                            description = "";
                        }

                        addServiceCard(
                                title,
                                description,
                                price,
                                serviceCategory
                        );

                        serviceCount++;
                    }

                    if (serviceCount == 0) {
                        tvStatus.setText(
                                "No " + category +
                                        " services added by this vendor yet."
                        );
                        tvStatus.setVisibility(TextView.VISIBLE);
                    } else {
                        tvStatus.setVisibility(TextView.GONE);
                    }
                })
                .addOnFailureListener(e -> {

                    tvStatus.setText("Unable to load services.");
                    tvStatus.setVisibility(TextView.VISIBLE);

                    Toast.makeText(
                            VendorServicesActivity.this,
                            "Error loading services: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Create service card
    private void addServiceCard(
            String title,
            String description,
            double price,
            String serviceCategory) {

        // Card Layout
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(22);

        card.setBackground(cardBackground);
        card.setElevation(5);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        // Service Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(20);
        tvTitle.setTextColor(Color.rgb(65, 45, 100));
        tvTitle.setTypeface(null, Typeface.BOLD);

        card.addView(tvTitle);

        // Service Category
        TextView tvServiceCategory = new TextView(this);
        tvServiceCategory.setText("📌 " + serviceCategory);
        tvServiceCategory.setTextSize(14);
        tvServiceCategory.setTextColor(Color.rgb(108, 66, 193));
        tvServiceCategory.setPadding(0, 8, 0, 8);

        card.addView(tvServiceCategory);

        // Service Description
        TextView tvDescription = new TextView(this);

        if (description.trim().isEmpty()) {
            tvDescription.setText("No description available.");
        } else {
            tvDescription.setText(description);
        }

        tvDescription.setTextSize(15);
        tvDescription.setTextColor(Color.rgb(100, 100, 100));
        tvDescription.setPadding(0, 4, 0, 12);

        card.addView(tvDescription);

        // Price
        NumberFormat numberFormat =
                NumberFormat.getNumberInstance(new Locale("en", "IN"));

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        TextView tvPrice = new TextView(this);
        tvPrice.setText("₹ " + numberFormat.format(price));
        tvPrice.setTextSize(21);
        tvPrice.setTextColor(Color.rgb(46, 125, 50));
        tvPrice.setTypeface(null, Typeface.BOLD);

        card.addView(tvPrice);

        servicesContainer.addView(card);
    }
}
