
package com.archi.happyhappenings;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyBookingsActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout bookingsContainer;
    private TextView tvStatus;

    private String vendorUid;

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

        vendorUid = auth.getCurrentUser().getUid();

        createScreen();
        loadBookings();
    }

    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
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
        TextView tvAppTitle = new TextView(this);
        tvAppTitle.setText("Happy Happenings");
        tvAppTitle.setTextSize(25);
        tvAppTitle.setTextColor(Color.rgb(108, 66, 193));
        tvAppTitle.setTypeface(null, Typeface.BOLD);
        tvAppTitle.setGravity(Gravity.CENTER);
        tvAppTitle.setPadding(0, 0, 0, 20);

        root.addView(tvAppTitle);

        // Heading
        TextView tvHeading = new TextView(this);
        tvHeading.setText("My Bookings");
        tvHeading.setTextSize(24);
        tvHeading.setTextColor(Color.rgb(45, 35, 65));
        tvHeading.setTypeface(null, Typeface.BOLD);

        root.addView(tvHeading);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Manage service booking requests from customers");
        tvSubtitle.setTextSize(14);
        tvSubtitle.setTextColor(Color.GRAY);
        tvSubtitle.setPadding(0, 8, 0, 20);

        root.addView(tvSubtitle);

        // Status
        tvStatus = new TextView(this);
        tvStatus.setText("Loading bookings...");
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(Color.GRAY);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(10, 30, 10, 30);

        root.addView(tvStatus);

        // Booking cards container
        bookingsContainer = new LinearLayout(this);
        bookingsContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(bookingsContainer);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    // Load bookings belonging to the currently logged-in vendor
    private void loadBookings() {

        db.collection("bookings")
                .whereEqualTo("vendorUid", vendorUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    bookingsContainer.removeAllViews();

                    int count = querySnapshot.size();

                    if (count == 0) {
                        tvStatus.setText("You don't have any bookings yet.");
                        tvStatus.setVisibility(TextView.VISIBLE);
                        return;
                    }

                    tvStatus.setVisibility(TextView.GONE);

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        addBookingCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    tvStatus.setText("Unable to load bookings.");
                    tvStatus.setVisibility(TextView.VISIBLE);

                    Toast.makeText(
                            MyBookingsActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Build booking card
    private void addBookingCard(DocumentSnapshot document) {

        String customerEmail = document.getString("customerEmail");
        String serviceName = document.getString("serviceName");
        String category = document.getString("category");
        String eventDate = document.getString("eventDate");
        String note = document.getString("note");
        String status = document.getString("status");

        Object priceObject = document.get("price");
        double price = 0;

        if (priceObject instanceof Number) {
            price = ((Number) priceObject).doubleValue();
        }

        if (customerEmail == null) customerEmail = "Customer";
        if (serviceName == null) serviceName = "Service";
        if (category == null) category = "Services";
        if (eventDate == null) eventDate = "Not specified";
        if (note == null || note.trim().isEmpty()) note = "No note added";
        if (status == null) status = "Pending";

        // Card
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
        tvService.setText(serviceName);
        tvService.setTextSize(20);
        tvService.setTextColor(Color.rgb(65, 45, 100));
        tvService.setTypeface(null, Typeface.BOLD);

        card.addView(tvService);

        addInfo(card, "Customer", customerEmail);
        addInfo(card, "Category", category);
        addInfo(card, "Event Date", eventDate);
        addInfo(card, "Price", "₹ " + price);
        addInfo(card, "Customer Note", note);

        // Status
        TextView tvBookingStatus = new TextView(this);
        tvBookingStatus.setText("Status: " + status);
        tvBookingStatus.setTextSize(15);
        tvBookingStatus.setTypeface(null, Typeface.BOLD);
        tvBookingStatus.setPadding(0, 12, 0, 12);

        if ("Accepted".equalsIgnoreCase(status)) {
            tvBookingStatus.setTextColor(Color.rgb(46, 125, 50));
        } else if ("Rejected".equalsIgnoreCase(status)) {
            tvBookingStatus.setTextColor(Color.rgb(198, 40, 40));
        } else {
            tvBookingStatus.setTextColor(Color.rgb(230, 145, 20));
        }

        card.addView(tvBookingStatus);

        // Actions only for pending bookings
        if ("Pending".equalsIgnoreCase(status)) {

            LinearLayout buttonRow = new LinearLayout(this);
            buttonRow.setOrientation(LinearLayout.HORIZONTAL);
            buttonRow.setPadding(0, 8, 0, 0);

            Button btnAccept = new Button(this);
            btnAccept.setText("Accept");
            btnAccept.setAllCaps(false);
            btnAccept.setTextColor(Color.WHITE);

            GradientDrawable acceptBg = new GradientDrawable();
            acceptBg.setColor(Color.rgb(46, 125, 50));
            acceptBg.setCornerRadius(14);
            btnAccept.setBackground(acceptBg);

            LinearLayout.LayoutParams acceptParams =
                    new LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            acceptParams.setMargins(0, 0, 8, 0);
            btnAccept.setLayoutParams(acceptParams);

            Button btnReject = new Button(this);
            btnReject.setText("Reject");
            btnReject.setAllCaps(false);
            btnReject.setTextColor(Color.WHITE);

            GradientDrawable rejectBg = new GradientDrawable();
            rejectBg.setColor(Color.rgb(198, 40, 40));
            rejectBg.setCornerRadius(14);
            btnReject.setBackground(rejectBg);

            LinearLayout.LayoutParams rejectParams =
                    new LinearLayout.LayoutParams(0,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1);

            rejectParams.setMargins(8, 0, 0, 0);
            btnReject.setLayoutParams(rejectParams);

            String bookingId = document.getId();

            btnAccept.setOnClickListener(v ->
                    showConfirmation(bookingId, "Accepted"));

            btnReject.setOnClickListener(v ->
                    showConfirmation(bookingId, "Rejected"));

            buttonRow.addView(btnAccept);
            buttonRow.addView(btnReject);

            card.addView(buttonRow);
        }

        bookingsContainer.addView(card);
    }

    private void addInfo(
            LinearLayout card,
            String label,
            String value) {

        TextView tv = new TextView(this);
        tv.setText(label + ": " + value);
        tv.setTextSize(15);
        tv.setTextColor(Color.rgb(95, 85, 105));
        tv.setPadding(0, 10, 0, 0);

        card.addView(tv);
    }

    // Confirmation dialog before changing status
    private void showConfirmation(
            String bookingId,
            String newStatus) {

        new AlertDialog.Builder(this)
                .setTitle(newStatus + " Booking?")
                .setMessage("Are you sure you want to " +
                        newStatus.toLowerCase() + " this booking?")
                .setPositiveButton("Yes", (dialog, which) ->
                        updateBookingStatus(bookingId, newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Update status in Firestore
    private void updateBookingStatus(
            String bookingId,
            String newStatus) {

        db.collection("bookings")
                .document(bookingId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            MyBookingsActivity.this,
                            "Booking " + newStatus,
                            Toast.LENGTH_SHORT
                    ).show();

                    loadBookings();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MyBookingsActivity.this,
                            "Failed to update: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
