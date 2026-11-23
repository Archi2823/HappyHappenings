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

public class UserDashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(30, 30, 30, 30);
        layout.setBackgroundColor(Color.rgb(248, 246, 255));

        TextView title = new TextView(this);
        title.setText("Happy Happenings");
        title.setTextSize(28);
        title.setTextColor(Color.rgb(108, 66, 193));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 25);

        TextView welcome = new TextView(this);
        welcome.setText("Welcome to User Dashboard!");
        welcome.setTextSize(22);
        welcome.setTextColor(Color.DKGRAY);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, 0, 0, 15);

        TextView description = new TextView(this);
        description.setText("Explore events and discover amazing happenings.");
        description.setTextSize(16);
        description.setTextColor(Color.GRAY);
        description.setGravity(Gravity.CENTER);
        description.setPadding(0, 0, 0, 30);

        Button logout = new Button(this);
        logout.setText("Logout");

        layout.addView(title);
        layout.addView(welcome);
        layout.addView(description);
        layout.addView(logout);

        setContentView(layout);

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}