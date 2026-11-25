
package com.archi.happyhappenings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class VendorDashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.rgb(248, 246, 255));

        // App Title
        TextView title = new TextView(this);
        title.setText("Happy Happenings");
        title.setTextSize(28);
        title.setTextColor(Color.rgb(108, 66, 193));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 25);

        // Welcome Message
        TextView welcome = new TextView(this);
        welcome.setText("Welcome to Vendor Dashboard!");
        welcome.setTextSize(22);
        welcome.setTextColor(Color.DKGRAY);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, 0, 0, 15);

        // Description
        TextView description = new TextView(this);
        description.setText("Manage your event services and bookings here.");
        description.setTextSize(16);
        description.setTextColor(Color.GRAY);
        description.setGravity(Gravity.CENTER);
        description.setPadding(0, 0, 0, 30);

        // Manage Services Button
        Button manageServices = new Button(this);
        manageServices.setText("Manage Services");
        manageServices.setTextColor(Color.WHITE);
        manageServices.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(108, 66, 193)
                )
        );

        // My Bookings Button
        Button myBookings = new Button(this);
        myBookings.setText("My Bookings");
        myBookings.setTextColor(Color.WHITE);
        myBookings.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        Color.rgb(46, 125, 50)
                )
        );

        // Logout Button
        Button logout = new Button(this);
        logout.setText("Logout");

        // Add views
        layout.addView(title);
        layout.addView(welcome);
        layout.addView(description);
        layout.addView(manageServices);
        layout.addView(myBookings);
        layout.addView(logout);

        setContentView(layout);

        // Manage Services
        manageServices.setOnClickListener(v -> {
            Intent intent = new Intent(
                    VendorDashboardActivity.this,
                    ManageServicesActivity.class
            );
            startActivity(intent);
        });

        // My Bookings
        myBookings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    VendorDashboardActivity.this,
                    MyBookingsActivity.class
            );
            startActivity(intent);
        });

        // Logout
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(
                    VendorDashboardActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }
}
