
package com.archi.happyhappenings;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ManageServicesActivity extends Activity {

    private LinearLayout servicesContainer;
    private Button addButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String vendorUid;

    private final int purple = Color.rgb(103, 58, 183);
    private final int dark = Color.rgb(45, 35, 65);
    private final int lightBg = Color.rgb(247, 244, 252);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        vendorUid = auth.getCurrentUser().getUid();

        // Load XML layout
        buildUI();

        // Load services from Firestore
        loadServices();
    }

    // Rounded background helper
    private GradientDrawable roundedBg(int color, int radius) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(radius);
        return bg;
    }

    // TextView helper
    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);

        if (bold) {
            tv.setTypeface(null, Typeface.BOLD);
        }

        return tv;
    }

    // Button helper
    private Button button(String label) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(roundedBg(purple, 24));
        return btn;
    }

    // Connect Java with XML layout
    private void buildUI() {

        setContentView(R.layout.activity_manage_services);

        servicesContainer = findViewById(R.id.servicesContainer);
        addButton = findViewById(R.id.btnAddService);

        // Add New Service click
        addButton.setOnClickListener(v -> showServiceDialog(null));
    }

    // Load vendor's own services from Firestore
    private void loadServices() {

        servicesContainer.removeAllViews();

        db.collection("vendorServices")
                .whereEqualTo("vendorUid", vendorUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        TextView empty = text(
                                "No services added yet.\nTap + Add New Service to get started.",
                                16,
                                Color.GRAY,
                                false
                        );

                        empty.setGravity(Gravity.CENTER);
                        empty.setPadding(20, 40, 20, 40);

                        servicesContainer.addView(empty);
                        return;
                    }

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        addServiceCard(doc);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load services: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // Create service card
    private void addServiceCard(DocumentSnapshot doc) {

        String title = doc.getString("title");
        String description = doc.getString("description");
        String category = doc.getString("category");

        Object priceValue = doc.get("price");

        double price = 0;

        if (priceValue instanceof Number) {
            price = ((Number) priceValue).doubleValue();
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);
        card.setBackground(roundedBg(Color.WHITE, 22));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);

        cardParams.setMargins(0, 0, 0, 16);

        // Service title
        TextView name = text(
                title == null ? "Untitled Service" : title,
                20,
                dark,
                true
        );

        // Category
        TextView categoryText = text(
                "Category: " + (category == null ? "" : category),
                14,
                purple,
                true
        );

        // Price
        TextView priceText = text(
                "Price: ₹" + String.format("%.2f", price),
                17,
                Color.rgb(30, 130, 80),
                true
        );

        // Description
        TextView desc = text(
                description == null ? "" : description,
                14,
                Color.DKGRAY,
                false
        );

        card.addView(name);

        LinearLayout.LayoutParams catParams =
                new LinearLayout.LayoutParams(-1, -2);

        catParams.setMargins(0, 8, 0, 0);
        card.addView(categoryText, catParams);

        LinearLayout.LayoutParams priceParams =
                new LinearLayout.LayoutParams(-1, -2);

        priceParams.setMargins(0, 8, 0, 0);
        card.addView(priceText, priceParams);

        LinearLayout.LayoutParams descParams =
                new LinearLayout.LayoutParams(-1, -2);

        descParams.setMargins(0, 10, 0, 0);
        card.addView(desc, descParams);

        // Edit and Delete buttons
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button editButton = button("Edit");
        Button deleteButton = button("Delete");

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(0, -2, 1);

        actionParams.setMargins(0, 16, 8, 0);
        actions.addView(editButton, actionParams);

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(0, -2, 1);

        deleteParams.setMargins(8, 16, 0, 0);
        actions.addView(deleteButton, deleteParams);

        card.addView(actions);
        servicesContainer.addView(card, cardParams);

        // Edit service
        editButton.setOnClickListener(v -> showServiceDialog(doc));

        // Delete service
        deleteButton.setOnClickListener(v ->
                confirmDelete(
                        doc.getId(),
                        title == null ? "this service" : title
                )
        );
    }

    // Add/Edit dialog
    private void showServiceDialog(DocumentSnapshot existing) {

        boolean isEdit = existing != null;

        EditText titleInput = new EditText(this);
        titleInput.setHint("Service Title");

        EditText descriptionInput = new EditText(this);
        descriptionInput.setHint("Service Description");
        descriptionInput.setMinLines(3);

        EditText priceInput = new EditText(this);
        priceInput.setHint("Price (₹)");
        priceInput.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        EditText categoryInput = new EditText(this);
        categoryInput.setHint("Category (e.g. Catering, Photography)");

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(30, 10, 30, 0);

        form.addView(titleInput);
        form.addView(descriptionInput);
        form.addView(priceInput);
        form.addView(categoryInput);

        // Fill existing values while editing
        if (isEdit) {

            titleInput.setText(existing.getString("title"));

            descriptionInput.setText(
                    existing.getString("description")
            );

            Object oldPrice = existing.get("price");

            if (oldPrice != null) {
                priceInput.setText(String.valueOf(oldPrice));
            }

            categoryInput.setText(
                    existing.getString("category")
            );
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Service" : "Add New Service")
                .setView(form)
                .setPositiveButton(isEdit ? "Update" : "Save", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String title = titleInput.getText()
                                .toString().trim();

                        String description = descriptionInput.getText()
                                .toString().trim();

                        String priceString = priceInput.getText()
                                .toString().trim();

                        String category = categoryInput.getText()
                                .toString().trim();

                        // Validation
                        if (title.isEmpty()
                                || description.isEmpty()
                                || priceString.isEmpty()
                                || category.isEmpty()) {

                            Toast.makeText(
                                    this,
                                    "Please fill all fields",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        double price;

                        try {
                            price = Double.parseDouble(priceString);

                            if (price < 0) {

                                Toast.makeText(
                                        this,
                                        "Price cannot be negative",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                        } catch (NumberFormatException e) {

                            Toast.makeText(
                                    this,
                                    "Enter a valid price",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        saveService(
                                existing,
                                title,
                                description,
                                price,
                                category,
                                dialog
                        );
                    });
        });

        dialog.show();
    }

    // Save or Update service
    private void saveService(
            DocumentSnapshot existing,
            String title,
            String description,
            double price,
            String category,
            AlertDialog dialog
    ) {

        Map<String, Object> data = new HashMap<>();

        data.put("vendorUid", vendorUid);
        data.put("title", title);
        data.put("description", description);
        data.put("price", price);
        data.put("category", category);
        data.put("updatedAt", FieldValue.serverTimestamp());

        // Add new service
        if (existing == null) {

            data.put("createdAt", FieldValue.serverTimestamp());

            db.collection("vendorServices")
                    .add(data)
                    .addOnSuccessListener(ref -> {

                        dialog.dismiss();

                        Toast.makeText(
                                this,
                                "Service added successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadServices();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Save failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );

        } else {

            // Update existing service
            db.collection("vendorServices")
                    .document(existing.getId())
                    .update(data)
                    .addOnSuccessListener(unused -> {

                        dialog.dismiss();

                        Toast.makeText(
                                this,
                                "Service updated successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadServices();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Update failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        }
    }

    // Delete confirmation
    private void confirmDelete(String serviceId, String serviceTitle) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Service")
                .setMessage(
                        "Are you sure you want to delete "
                                + serviceTitle + "?"
                )
                .setPositiveButton("Delete", (dialog, which) -> {

                    db.collection("vendorServices")
                            .document(serviceId)
                            .delete()
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        this,
                                        "Service deleted",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadServices();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Delete failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
