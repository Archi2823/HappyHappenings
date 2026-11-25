
package com.archi.happyhappenings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends Activity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView tvSelectedDate;
    private EditText etNote;
    private Button btnConfirm;

    private String vendorUid;
    private String vendorName;
    private String serviceId;
    private String serviceName;
    private String serviceDescription;
    private String category;

    private double servicePrice;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get data from VendorServicesActivity
        vendorUid = getIntent().getStringExtra("vendorUid");
        vendorName = getIntent().getStringExtra("vendorName");
        serviceId = getIntent().getStringExtra("serviceId");
        serviceName = getIntent().getStringExtra("serviceName");
        serviceDescription = getIntent().getStringExtra("serviceDescription");
        category = getIntent().getStringExtra("category");
        servicePrice = getIntent().getDoubleExtra("servicePrice", 0);

        if (vendorUid == null || serviceId == null) {
            Toast.makeText(this, "Service information missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (vendorName == null) vendorName = "Vendor";
        if (serviceName == null) serviceName = "Service";
        if (serviceDescription == null) serviceDescription = "";
        if (category == null) category = "Services";

        createScreen();
    }

    private void createScreen() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(248, 246, 255));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);

        // Back
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
        tvAppTitle.setTypeface(null, Typeface.BOLD);
        tvAppTitle.setTextColor(Color.rgb(108, 66, 193));
        tvAppTitle.setGravity(Gravity.CENTER);
        tvAppTitle.setPadding(0, 0, 0, 20);

        root.addView(tvAppTitle);

        // Heading
        TextView tvHeading = new TextView(this);
        tvHeading.setText("Book Your Service");
        tvHeading.setTextSize(24);
        tvHeading.setTypeface(null, Typeface.BOLD);
        tvHeading.setTextColor(Color.rgb(45, 35, 65));
        tvHeading.setPadding(0, 0, 0, 20);

        root.addView(tvHeading);

        // Booking details card
        LinearLayout detailsCard = new LinearLayout(this);
        detailsCard.setOrientation(LinearLayout.VERTICAL);
        detailsCard.setPadding(20, 20, 20, 20);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(22);

        detailsCard.setBackground(cardBg);
        detailsCard.setElevation(4);

        // Vendor
        addDetail(detailsCard, "Vendor", vendorName);

        // Service
        addDetail(detailsCard, "Service", serviceName);

        // Category
        addDetail(detailsCard, "Category", category);

        // Price
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);

        addDetail(detailsCard, "Price", "₹ " + nf.format(servicePrice));

        root.addView(detailsCard);

        // Event date heading
        TextView tvDateHeading = new TextView(this);
        tvDateHeading.setText("Event Date");
        tvDateHeading.setTextSize(17);
        tvDateHeading.setTypeface(null, Typeface.BOLD);
        tvDateHeading.setTextColor(Color.rgb(65, 45, 100));
        tvDateHeading.setPadding(0, 24, 0, 10);

        root.addView(tvDateHeading);

        // Date picker button
        tvSelectedDate = new TextView(this);
        tvSelectedDate.setText("📅  Select Event Date");
        tvSelectedDate.setTextSize(16);
        tvSelectedDate.setTextColor(Color.rgb(90, 80, 100));
        tvSelectedDate.setPadding(18, 18, 18, 18);
        tvSelectedDate.setGravity(Gravity.CENTER_VERTICAL);

        GradientDrawable dateBg = new GradientDrawable();
        dateBg.setColor(Color.WHITE);
        dateBg.setCornerRadius(16);
        dateBg.setStroke(2, Color.rgb(220, 210, 240));

        tvSelectedDate.setBackground(dateBg);

        tvSelectedDate.setOnClickListener(v -> openDatePicker());

        root.addView(tvSelectedDate);

        // Note heading
        TextView tvNoteHeading = new TextView(this);
        tvNoteHeading.setText("Special Note (Optional)");
        tvNoteHeading.setTextSize(17);
        tvNoteHeading.setTypeface(null, Typeface.BOLD);
        tvNoteHeading.setTextColor(Color.rgb(65, 45, 100));
        tvNoteHeading.setPadding(0, 24, 0, 10);

        root.addView(tvNoteHeading);

        // Note input
        etNote = new EditText(this);
        etNote.setHint("Write event details or special requirements...");
        etNote.setTextSize(15);
        etNote.setTextColor(Color.rgb(60, 50, 70));
        etNote.setHintTextColor(Color.GRAY);
        etNote.setGravity(Gravity.TOP | Gravity.START);
        etNote.setPadding(18, 18, 18, 18);
        etNote.setMinHeight(130);

        GradientDrawable noteBg = new GradientDrawable();
        noteBg.setColor(Color.WHITE);
        noteBg.setCornerRadius(16);
        noteBg.setStroke(2, Color.rgb(220, 210, 240));

        etNote.setBackground(noteBg);

        root.addView(etNote);

        // Confirm booking button
        btnConfirm = new Button(this);
        btnConfirm.setText("Confirm Booking");
        btnConfirm.setTextSize(16);
        btnConfirm.setAllCaps(false);
        btnConfirm.setTypeface(null, Typeface.BOLD);
        btnConfirm.setTextColor(Color.WHITE);

        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setColor(Color.rgb(108, 66, 193));
        buttonBg.setCornerRadius(18);

        btnConfirm.setBackground(buttonBg);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(0, 24, 0, 16);
        btnConfirm.setLayoutParams(buttonParams);
        btnConfirm.setPadding(12, 16, 12, 16);

        btnConfirm.setOnClickListener(v -> confirmBooking());

        root.addView(btnConfirm);

        scrollView.addView(root);
        setContentView(scrollView);
    }

    // Add label/value rows
    private void addDetail(
            LinearLayout container,
            String label,
            String value) {

        TextView tv = new TextView(this);
        tv.setText(label + "\n" + value);
        tv.setTextSize(16);
        tv.setTextColor(Color.rgb(65, 45, 100));
        tv.setPadding(0, 0, 0, 16);

        container.addView(tv);
    }

    // Date picker
    private void openDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);

                    SimpleDateFormat sdf =
                            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                    selectedDate = new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.US
                    ).format(selected.getTime());

                    tvSelectedDate.setText(
                            "📅  " + sdf.format(selected.getTime())
                    );

                    tvSelectedDate.setTextColor(Color.rgb(65, 45, 100));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Disable past dates
        datePickerDialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        datePickerDialog.show();
    }

    // Save booking to Firestore
    private void confirmBooking() {

        if (selectedDate.isEmpty()) {
            Toast.makeText(
                    this,
                    "Please select event date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Booking...");

        String customerUid = auth.getCurrentUser().getUid();
        String customerEmail = auth.getCurrentUser().getEmail();

        String note = etNote.getText().toString().trim();

        Map<String, Object> booking = new HashMap<>();

        booking.put("customerUid", customerUid);
        booking.put("customerEmail", customerEmail);

        booking.put("vendorUid", vendorUid);
        booking.put("vendorName", vendorName);

        booking.put("serviceId", serviceId);
        booking.put("serviceName", serviceName);
        booking.put("serviceDescription", serviceDescription);

        booking.put("category", category);
        booking.put("price", servicePrice);

        booking.put("eventDate", selectedDate);
        booking.put("note", note);

        booking.put("status", "Pending");

        booking.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            BookingActivity.this,
                            "Booking submitted successfully!",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirm Booking");

                    Toast.makeText(
                            BookingActivity.this,
                            "Booking failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
