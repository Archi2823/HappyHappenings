
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout bookingsContainer;
    private LinearLayout filterContainer;
    private TextView tvStatus;

    private String vendorUid;
    private String selectedStatus = "All";

    private List<DocumentSnapshot> allBookings = new ArrayList<>();

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

        TextView tvBack = new TextView(this);
        tvBack.setText("←  Back");
        tvBack.setTextSize(16);
        tvBack.setTextColor(Color.rgb(90, 80, 100));
        tvBack.setPadding(0, 8, 0, 20);
        tvBack.setOnClickListener(v -> finish());
        root.addView(tvBack);

        TextView tvAppTitle = new TextView(this);
        tvAppTitle.setText("Happy Happenings");
        tvAppTitle.setTextSize(25);
        tvAppTitle.setTextColor(Color.rgb(108, 66, 193));
        tvAppTitle.setTypeface(null, Typeface.BOLD);
        tvAppTitle.setGravity(Gravity.CENTER);
        tvAppTitle.setPadding(0, 0, 0, 20);
        root.addView(tvAppTitle);

        TextView tvHeading = new TextView(this);
        tvHeading.setText("My Bookings");
        tvHeading.setTextSize(24);
        tvHeading.setTextColor(Color.rgb(45, 35, 65));
        tvHeading.setTypeface(null, Typeface.BOLD);
        root.addView(tvHeading);

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("View and manage booking requests from customers");
        tvSubtitle.setTextSize(14);
        tvSubtitle.setTextColor(Color.GRAY);
        tvSubtitle.setPadding(0, 8, 0, 20);
        root.addView(tvSubtitle);

        filterContainer = new LinearLayout(this);
        filterContainer.setOrientation(LinearLayout.HORIZONTAL);
        filterContainer.setPadding(0, 0, 0, 12);

        String[] filters = {"All", "Pending", "Accepted", "Rejected"};

        for (String filter : filters) {
            Button filterButton = createFilterButton(filter);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );

            params.setMargins(3, 3, 3, 3);
            filterButton.setLayoutParams(params);
            filterContainer.addView(filterButton);
        }

        root.addView(filterContainer);

        tvStatus = new TextView(this);
        tvStatus.setText("Loading bookings...");
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(Color.GRAY);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(10, 30, 10, 30);
        root.addView(tvStatus);

        bookingsContainer = new LinearLayout(this);
        bookingsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(bookingsContainer);

        scrollView.addView(root);
        setContentView(scrollView);

        updateFilterButtons();
    }

    private Button createFilterButton(String filter) {

        Button button = new Button(this);
        button.setText(filter);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setPadding(2, 8, 2, 8);
        button.setOnClickListener(v -> {
            selectedStatus = filter;
            updateFilterButtons();
            renderBookings();
        });

        return button;
    }

    private void updateFilterButtons() {

        for (int i = 0; i < filterContainer.getChildCount(); i++) {

            Button button = (Button) filterContainer.getChildAt(i);
            String filter = button.getText().toString();

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(18);

            if (filter.equals(selectedStatus)) {
                bg.setColor(Color.rgb(108, 66, 193));
                button.setTextColor(Color.WHITE);
            } else {
                bg.setColor(Color.WHITE);
                bg.setStroke(2, Color.rgb(220, 210, 240));
                button.setTextColor(Color.rgb(90, 70, 110));
            }

            button.setBackground(bg);
        }
    }

    private void loadBookings() {

        tvStatus.setText("Loading bookings...");
        tvStatus.setVisibility(TextView.VISIBLE);
        bookingsContainer.removeAllViews();

        db.collection("bookings")
                .whereEqualTo("vendorUid", vendorUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    allBookings.clear();
                    allBookings.addAll(querySnapshot.getDocuments());

                    renderBookings();
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

    private void renderBookings() {

        bookingsContainer.removeAllViews();

        if (allBookings.isEmpty()) {
            tvStatus.setText("You don't have any bookings yet.");
            tvStatus.setVisibility(TextView.VISIBLE);
            return;
        }

        int filteredCount = 0;

        for (DocumentSnapshot document : allBookings) {

            String status = document.getString("status");

            if (status == null) {
                status = "Pending";
            }

            if ("All".equals(selectedStatus)
                    || selectedStatus.equalsIgnoreCase(status)) {

                addBookingCard(document);
                filteredCount++;
            }
        }

        if (filteredCount == 0) {
            tvStatus.setText("No " + selectedStatus.toLowerCase()
                    + " bookings found.");
            tvStatus.setVisibility(TextView.VISIBLE);
        } else {
            tvStatus.setVisibility(TextView.GONE);
        }
    }

    private void addBookingCard(DocumentSnapshot document) {

        String customerEmail = document.getString("customerEmail");
        String eventName = document.getString("eventName");
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

        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            customerEmail = "Customer";
        }

        if (eventName == null || eventName.trim().isEmpty()) {
            eventName = "Not specified";
        }

        if (serviceName == null || serviceName.trim().isEmpty()) {
            serviceName = "Service";
        }

        if (category == null || category.trim().isEmpty()) {
            category = "Services";
        }

        if (eventDate == null || eventDate.trim().isEmpty()) {
            eventDate = "Not specified";
        }

        if (note == null || note.trim().isEmpty()) {
            note = "No message added";
        }

        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(22);

        card.setBackground(cardBg);
        card.setElevation(4);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        cardParams.setMargins(0, 0, 0, 18);
        card.setLayoutParams(cardParams);

        TextView tvService = new TextView(this);
        tvService.setText(serviceName);
        tvService.setTextSize(20);
        tvService.setTextColor(Color.rgb(65, 45, 100));
        tvService.setTypeface(null, Typeface.BOLD);
        card.addView(tvService);

        addInfo(card, "Customer", customerEmail);
        addInfo(card, "Event Name", eventName);
        addInfo(card, "Category", category);
        addInfo(card, "Event Date", eventDate);

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);

        addInfo(card, "Price", "₹ " + nf.format(price));

        TextView tvMessageHeading = new TextView(this);
        tvMessageHeading.setText("Customer Message");
        tvMessageHeading.setTextSize(15);
        tvMessageHeading.setTextColor(Color.rgb(65, 45, 100));
        tvMessageHeading.setTypeface(null, Typeface.BOLD);
        tvMessageHeading.setPadding(0, 18, 0, 8);
        card.addView(tvMessageHeading);

        TextView tvMessage = new TextView(this);
        tvMessage.setText(note);
        tvMessage.setTextSize(15);
        tvMessage.setTextColor(Color.rgb(80, 70, 90));
        tvMessage.setPadding(14, 14, 14, 14);

        GradientDrawable messageBg = new GradientDrawable();
        messageBg.setColor(Color.rgb(246, 243, 252));
        messageBg.setCornerRadius(14);

        tvMessage.setBackground(messageBg);
        card.addView(tvMessage);

        TextView tvBookingStatus = new TextView(this);
        tvBookingStatus.setText("Status: " + status);
        tvBookingStatus.setTextSize(15);
        tvBookingStatus.setTypeface(null, Typeface.BOLD);
        tvBookingStatus.setPadding(0, 16, 0, 12);

        if ("Accepted".equalsIgnoreCase(status)) {
            tvBookingStatus.setTextColor(Color.rgb(46, 125, 50));
        } else if ("Rejected".equalsIgnoreCase(status)) {
            tvBookingStatus.setTextColor(Color.rgb(198, 40, 40));
        } else {
            tvBookingStatus.setTextColor(Color.rgb(230, 145, 20));
        }

        card.addView(tvBookingStatus);

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

            LinearLayout.LayoutParams acceptParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );

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

            LinearLayout.LayoutParams rejectParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );

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

    private void addInfo(LinearLayout card, String label, String value) {

        TextView tv = new TextView(this);
        tv.setText(label + ": " + value);
        tv.setTextSize(15);
        tv.setTextColor(Color.rgb(95, 85, 105));
        tv.setPadding(0, 10, 0, 0);

        card.addView(tv);
    }

    private void showConfirmation(String bookingId, String newStatus) {

        new AlertDialog.Builder(this)
                .setTitle(newStatus + " Booking?")
                .setMessage("Are you sure you want to "
                        + newStatus.toLowerCase() + " this booking?")
                .setPositiveButton("Yes", (dialog, which) ->
                        updateBookingStatus(bookingId, newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBookingStatus(String bookingId, String newStatus) {

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
