
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomerMyBookingsActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout bookingsContainer;
    private TextView tvLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        createScreen();
        loadBookings();
    }

    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.rgb(248, 246, 255));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        // Back button
        TextView tvBack = new TextView(this);
        tvBack.setText("←  Back");
        tvBack.setTextSize(16);
        tvBack.setTextColor(Color.rgb(90, 80, 100));
        tvBack.setPadding(0, 8, 0, 20);
        tvBack.setOnClickListener(v -> finish());

        root.addView(tvBack);

        // App title
        TextView title = new TextView(this);
        title.setText("Happy Happenings");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(108, 66, 193));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        root.addView(title);

        // Heading
        TextView heading = new TextView(this);
        heading.setText("My Bookings");
        heading.setTextSize(24);
        heading.setTextColor(Color.rgb(45, 35, 65));
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 0, 0, 8);

        root.addView(heading);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Track your event service bookings");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setPadding(0, 0, 0, 22);

        root.addView(subtitle);

        // Loading text
        tvLoading = new TextView(this);
        tvLoading.setText("Loading bookings...");
        tvLoading.setTextSize(16);
        tvLoading.setTextColor(Color.GRAY);
        tvLoading.setGravity(Gravity.CENTER);
        tvLoading.setPadding(0, 30, 0, 30);

        root.addView(tvLoading);

        // Bookings container
        bookingsContainer = new LinearLayout(this);
        bookingsContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(bookingsContainer);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    // Load current customer's bookings
    private void loadBookings() {

        String customerUid = auth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("customerUid", customerUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    tvLoading.setVisibility(TextView.GONE);
                    bookingsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        showEmptyBookings();
                        return;
                    }

                    for (com.google.firebase.firestore.DocumentSnapshot document
                            : querySnapshot.getDocuments()) {

                        addBookingCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    tvLoading.setVisibility(TextView.VISIBLE);
                    tvLoading.setText("Unable to load bookings.");
                    Toast.makeText(
                            CustomerMyBookingsActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Empty booking message
    private void showEmptyBookings() {

        TextView empty = new TextView(this);
        empty.setText("📋\n\nNo bookings yet!\nBook a service to see it here.");
        empty.setTextSize(17);
        empty.setTextColor(Color.rgb(100, 85, 120));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(20, 50, 20, 50);

        bookingsContainer.addView(empty);
    }

    // Create booking card
    private void addBookingCard(
            com.google.firebase.firestore.DocumentSnapshot document) {

        String vendorName = document.getString("vendorName");
        String serviceName = document.getString("serviceName");
        String category = document.getString("category");
        String eventDate = document.getString("eventDate");
        String note = document.getString("note");
        String status = document.getString("status");

        Double price = document.getDouble("price");

        if (vendorName == null) vendorName = "Vendor";
        if (serviceName == null) serviceName = "Service";
        if (category == null) category = "-";
        if (eventDate == null) eventDate = "-";
        if (note == null || note.trim().isEmpty()) note = "No special note";
        if (status == null) status = "Pending";
        if (price == null) price = 0.0;

        // Main card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(22);

        card.setBackground(cardBg);
        card.setElevation(4);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 18);
        card.setLayoutParams(cardParams);

        // Service name
        TextView tvService = new TextView(this);
        tvService.setText("🎉  " + serviceName);
        tvService.setTextSize(20);
        tvService.setTextColor(Color.rgb(75, 50, 120));
        tvService.setTypeface(null, Typeface.BOLD);
        tvService.setPadding(0, 0, 0, 12);

        card.addView(tvService);

        // Status
        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status: " + status);
        tvStatus.setTextSize(14);
        tvStatus.setTypeface(null, Typeface.BOLD);
        tvStatus.setPadding(14, 10, 14, 10);
        tvStatus.setGravity(Gravity.CENTER);

        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setCornerRadius(30);

        if (status.equalsIgnoreCase("Accepted")) {
            statusBg.setColor(Color.rgb(220, 245, 225));
            tvStatus.setTextColor(Color.rgb(30, 130, 65));
        } else if (status.equalsIgnoreCase("Rejected")) {
            statusBg.setColor(Color.rgb(255, 225, 225));
            tvStatus.setTextColor(Color.rgb(190, 45, 45));
        } else {
            statusBg.setColor(Color.rgb(255, 240, 210));
            tvStatus.setTextColor(Color.rgb(180, 115, 20));
        }

        tvStatus.setBackground(statusBg);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        statusParams.setMargins(0, 0, 0, 16);
        tvStatus.setLayoutParams(statusParams);

        card.addView(tvStatus);

        // Vendor
        addInfo(card, "Vendor", vendorName);

        // Category
        addInfo(card, "Category", category);

        // Event date
        addInfo(card, "Event Date", eventDate);

        // Price
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);

        addInfo(card, "Price", "₹ " + nf.format(price));

        // Note
        addInfo(card, "Special Note", note);

        bookingsContainer.addView(card);
    }

    // Add information row
    private void addInfo(
            LinearLayout container,
            String label,
            String value) {

        TextView textView = new TextView(this);

        textView.setText(label + "\n" + value);
        textView.setTextSize(15);
        textView.setTextColor(Color.rgb(75, 65, 95));
        textView.setPadding(0, 0, 0, 14);

        container.addView(textView);
    }
}
