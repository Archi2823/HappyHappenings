
package com.archi.happyhappenings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Calendar;

public class CustomerMyBookingsActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout bookingsContainer;
    private TextView tvLoading;

    private Spinner spinnerCategory;
    private Spinner spinnerVendor;
    private Button btnDateFilter;
    private Button btnClearFilters;
    private TextView tvFilterInfo;

    private List<DocumentSnapshot> allBookings = new ArrayList<>();

    private String selectedCategory = "All Categories";
    private String selectedVendor = "All Vendors";
    private String selectedDate = "";

    private boolean suppressFilterCallbacks = false;

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

        TextView tvBack = new TextView(this);
        tvBack.setText("←  Back");
        tvBack.setTextSize(16);
        tvBack.setTextColor(Color.rgb(90, 80, 100));
        tvBack.setPadding(0, 8, 0, 20);
        tvBack.setOnClickListener(v -> finish());

        root.addView(tvBack);

        TextView title = new TextView(this);
        title.setText("Happy Happenings");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(108, 66, 193));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        root.addView(title);

        TextView heading = new TextView(this);
        heading.setText("My Bookings");
        heading.setTextSize(24);
        heading.setTextColor(Color.rgb(45, 35, 65));
        heading.setTypeface(null, Typeface.BOLD);
        heading.setPadding(0, 0, 0, 8);

        root.addView(heading);

        TextView subtitle = new TextView(this);
        subtitle.setText("Track and filter your event service bookings");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setPadding(0, 0, 0, 22);

        root.addView(subtitle);

        createFilterSection(root);

        tvLoading = new TextView(this);
        tvLoading.setText("Loading bookings...");
        tvLoading.setTextSize(16);
        tvLoading.setTextColor(Color.GRAY);
        tvLoading.setGravity(Gravity.CENTER);
        tvLoading.setPadding(0, 30, 0, 30);

        root.addView(tvLoading);

        bookingsContainer = new LinearLayout(this);
        bookingsContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(bookingsContainer);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    private void createFilterSection(LinearLayout root) {

        LinearLayout filterBox = new LinearLayout(this);
        filterBox.setOrientation(LinearLayout.VERTICAL);
        filterBox.setPadding(16, 16, 16, 16);

        GradientDrawable filterBg = new GradientDrawable();
        filterBg.setColor(Color.WHITE);
        filterBg.setCornerRadius(20);

        filterBox.setBackground(filterBg);
        filterBox.setElevation(3);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        boxParams.setMargins(0, 0, 0, 20);
        filterBox.setLayoutParams(boxParams);

        TextView filterHeading = new TextView(this);
        filterHeading.setText("🔎  Filter Bookings");
        filterHeading.setTextSize(18);
        filterHeading.setTypeface(null, Typeface.BOLD);
        filterHeading.setTextColor(Color.rgb(75, 50, 120));
        filterHeading.setPadding(0, 0, 0, 14);

        filterBox.addView(filterHeading);

        TextView categoryLabel = createFilterLabel("Category");
        filterBox.addView(categoryLabel);

        spinnerCategory = new Spinner(this);
        filterBox.addView(spinnerCategory);

        TextView vendorLabel = createFilterLabel("Vendor");
        vendorLabel.setPadding(0, 14, 0, 6);
        filterBox.addView(vendorLabel);

        spinnerVendor = new Spinner(this);
        filterBox.addView(spinnerVendor);

        TextView dateLabel = createFilterLabel("Event Date");
        dateLabel.setPadding(0, 14, 0, 6);
        filterBox.addView(dateLabel);

        btnDateFilter = new Button(this);
        btnDateFilter.setText("📅  Select Event Date");
        btnDateFilter.setTextColor(Color.rgb(90, 60, 140));
        btnDateFilter.setAllCaps(false);
        btnDateFilter.setOnClickListener(v -> openDatePicker());

        filterBox.addView(btnDateFilter);

        btnClearFilters = new Button(this);
        btnClearFilters.setText("✖  Clear All Filters");
        btnClearFilters.setTextColor(Color.rgb(110, 75, 140));
        btnClearFilters.setAllCaps(false);
        btnClearFilters.setOnClickListener(v -> clearFilters());

        LinearLayout.LayoutParams clearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        clearParams.setMargins(0, 8, 0, 0);
        btnClearFilters.setLayoutParams(clearParams);

        filterBox.addView(btnClearFilters);

        tvFilterInfo = new TextView(this);
        tvFilterInfo.setText("Showing all bookings");
        tvFilterInfo.setTextSize(13);
        tvFilterInfo.setTextColor(Color.GRAY);
        tvFilterInfo.setPadding(0, 12, 0, 0);

        filterBox.addView(tvFilterInfo);

        root.addView(filterBox);
    }

    private TextView createFilterLabel(String text) {

        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(Color.rgb(75, 65, 95));
        label.setPadding(0, 0, 0, 6);

        return label;
    }

    private void loadBookings() {

        String customerUid = auth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("customerUid", customerUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    tvLoading.setVisibility(TextView.GONE);
                    allBookings.clear();
                    allBookings.addAll(querySnapshot.getDocuments());

                    if (allBookings.isEmpty()) {
                        showEmptyBookings();
                        return;
                    }

                    setupCategoryFilter();
                    setupVendorFilter();
                    applyFilters();
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

    private void setupCategoryFilter() {

        Set<String> categorySet = new HashSet<>();
        categorySet.add("All Categories");

        for (DocumentSnapshot document : allBookings) {

            String category = document.getString("category");

            if (category != null && !category.trim().isEmpty()) {
                categorySet.add(category);
            }
        }

        List<String> categories = new ArrayList<>(categorySet);
        Collections.sort(categories);
        categories.remove("All Categories");
        categories.add(0, "All Categories");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        selectedCategory = categories.get(position);

                        if (!suppressFilterCallbacks) {
                            applyFilters();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    private void setupVendorFilter() {

        Set<String> vendorSet = new HashSet<>();
        vendorSet.add("All Vendors");

        for (DocumentSnapshot document : allBookings) {

            String vendorName = document.getString("vendorName");

            if (vendorName != null && !vendorName.trim().isEmpty()) {
                vendorSet.add(vendorName);
            }
        }

        List<String> vendors = new ArrayList<>(vendorSet);
        Collections.sort(vendors);
        vendors.remove("All Vendors");
        vendors.add(0, "All Vendors");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                vendors
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerVendor.setAdapter(adapter);

        spinnerVendor.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        selectedVendor = vendors.get(position);

                        if (!suppressFilterCallbacks) {
                            applyFilters();
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );
    }

    private void openDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    selectedDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    btnDateFilter.setText("📅  " + selectedDate);
                    applyFilters();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void applyFilters() {

        if (bookingsContainer == null || allBookings == null) {
            return;
        }

        bookingsContainer.removeAllViews();

        int count = 0;

        for (DocumentSnapshot document : allBookings) {

            String category = document.getString("category");
            String vendorName = document.getString("vendorName");
            String eventDate = document.getString("eventDate");

            if (category == null) category = "";
            if (vendorName == null) vendorName = "";
            if (eventDate == null) eventDate = "";

            boolean categoryMatch =
                    selectedCategory.equals("All Categories")
                            || selectedCategory.equalsIgnoreCase(category);

            boolean vendorMatch =
                    selectedVendor.equals("All Vendors")
                            || selectedVendor.equalsIgnoreCase(vendorName);

            boolean dateMatch =
                    selectedDate.isEmpty()
                            || selectedDate.equals(eventDate);

            if (categoryMatch && vendorMatch && dateMatch) {
                addBookingCard(document);
                count++;
            }
        }

        updateFilterInfo(count);

        if (count == 0) {
            showNoFilterResults();
        }
    }

    private void updateFilterInfo(int count) {

        StringBuilder info = new StringBuilder();

        info.append("Showing ")
                .append(count)
                .append(count == 1 ? " booking" : " bookings");

        if (!selectedCategory.equals("All Categories")) {
            info.append(" • ").append(selectedCategory);
        }

        if (!selectedVendor.equals("All Vendors")) {
            info.append(" • ").append(selectedVendor);
        }

        if (!selectedDate.isEmpty()) {
            info.append(" • ").append(selectedDate);
        }

        tvFilterInfo.setText(info.toString());
    }

    private void clearFilters() {

        suppressFilterCallbacks = true;

        selectedCategory = "All Categories";
        selectedVendor = "All Vendors";
        selectedDate = "";

        if (spinnerCategory != null && spinnerCategory.getAdapter() != null) {
            spinnerCategory.setSelection(0);
        }

        if (spinnerVendor != null && spinnerVendor.getAdapter() != null) {
            spinnerVendor.setSelection(0);
        }

        btnDateFilter.setText("📅  Select Event Date");

        suppressFilterCallbacks = false;

        applyFilters();
    }

    private void showEmptyBookings() {

        bookingsContainer.removeAllViews();

        TextView empty = new TextView(this);
        empty.setText("📋\n\nNo bookings yet!\nBook a service to see it here.");
        empty.setTextSize(17);
        empty.setTextColor(Color.rgb(100, 85, 120));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(20, 50, 20, 50);

        bookingsContainer.addView(empty);
    }

    private void showNoFilterResults() {

        TextView empty = new TextView(this);
        empty.setText("🔍\n\nNo bookings found for selected filters.\nTry changing or clearing the filters.");
        empty.setTextSize(16);
        empty.setTextColor(Color.rgb(100, 85, 120));
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(20, 35, 20, 35);

        bookingsContainer.addView(empty);
    }

    private void addBookingCard(DocumentSnapshot document) {

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

        TextView tvService = new TextView(this);
        tvService.setText("🎉  " + serviceName);
        tvService.setTextSize(20);
        tvService.setTextColor(Color.rgb(75, 50, 120));
        tvService.setTypeface(null, Typeface.BOLD);
        tvService.setPadding(0, 0, 0, 12);

        card.addView(tvService);

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

        addInfo(card, "Vendor", vendorName);
        addInfo(card, "Category", category);
        addInfo(card, "Event Date", eventDate);

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);

        addInfo(card, "Price", "₹ " + nf.format(price));
        addInfo(card, "Special Note", note);

        bookingsContainer.addView(card);
    }

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
