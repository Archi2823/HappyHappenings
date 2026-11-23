
package com.archi.happyhappenings;

import android.app.Activity;
import android.content.Intent;
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

public class UserDashboardActivity extends Activity {

    private LinearLayout categoryContainer;

    private final String[] categories = {
            "Catering",
            "Dresses",
            "Photography",
            "Decoration",
            "Music & DJ",
            "Venue",
            "Makeup",
            "Invitations"
    };

    private final String[] categoryIcons = {
            "🍽️",
            "👗",
            "📸",
            "🎊",
            "🎵",
            "🏛️",
            "💄",
            "💌"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main ScrollView
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.rgb(248, 246, 255));

        // Main Layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(24, 24, 24, 24);

        // App Title
        TextView title = new TextView(this);
        title.setText("Happy Happenings");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(108, 66, 193));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 18);

        // Welcome Text
        TextView welcome = new TextView(this);
        welcome.setText("Welcome!");
        welcome.setTextSize(23);
        welcome.setTextColor(Color.rgb(37, 33, 58));
        welcome.setTypeface(null, Typeface.BOLD);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, 0, 0, 8);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Explore services for your special events");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 24);

        // Categories Heading
        TextView categoryHeading = new TextView(this);
        categoryHeading.setText("Explore Categories");
        categoryHeading.setTextSize(21);
        categoryHeading.setTextColor(Color.rgb(65, 45, 100));
        categoryHeading.setTypeface(null, Typeface.BOLD);
        categoryHeading.setPadding(0, 0, 0, 16);

        // Category Container
        categoryContainer = new LinearLayout(this);
        categoryContainer.setOrientation(LinearLayout.VERTICAL);

        // Add Categories
        for (int i = 0; i < categories.length; i++) {
            addCategory(categories[i], categoryIcons[i]);
        }

        // Logout
        TextView logout = new TextView(this);
        logout.setText("LOGOUT");
        logout.setTextSize(16);
        logout.setTextColor(Color.WHITE);
        logout.setTypeface(null, Typeface.BOLD);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(0, 16, 0, 16);
        logout.setBackgroundColor(Color.rgb(108, 66, 193));

        LinearLayout.LayoutParams logoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        logoutParams.setMargins(0, 24, 0, 12);
        logout.setLayoutParams(logoutParams);

        logout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(
                    UserDashboardActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });

        // Add Views
        mainLayout.addView(title);
        mainLayout.addView(welcome);
        mainLayout.addView(subtitle);
        mainLayout.addView(categoryHeading);
        mainLayout.addView(categoryContainer);
        mainLayout.addView(logout);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    // Create Category Card
    private void addCategory(String category, String icon) {

        TextView categoryCard = new TextView(this);

        categoryCard.setText(icon + "     " + category);
        categoryCard.setTextSize(17);
        categoryCard.setTextColor(Color.rgb(75, 50, 120));
        categoryCard.setTypeface(null, Typeface.BOLD);
        categoryCard.setGravity(Gravity.CENTER_VERTICAL);
        categoryCard.setPadding(20, 0, 20, 0);

        // White card background with rounded corners
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(18);

        categoryCard.setBackground(background);
        categoryCard.setElevation(4);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        65
                );

        params.setMargins(0, 0, 0, 14);
        categoryCard.setLayoutParams(params);

        // Category click
        categoryCard.setOnClickListener(v ->
                Toast.makeText(
                        UserDashboardActivity.this,
                        category + " selected",
                        Toast.LENGTH_SHORT
                ).show()
        );

        categoryContainer.addView(categoryCard);
    }
}
