package com.android.getme.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup; // Import this
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.android.getme.Fragments.NavigationFragment;
import com.android.getme.Fragments.OfflineFragment;
import com.android.getme.Fragments.OnlineFragment;
import com.android.getme.Listeners.TrackRideListener;
import com.android.getme.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class DriverDashboard extends AppCompatActivity implements TrackRideListener {

    private SwitchMaterial switchStatus;
    private TextView tvStatusLabel, tvDriver;
    private ImageView ivDriverProfile;

    // 1. New variables for the containers
    private ViewGroup layoutHeader;
    private ViewGroup layoutStatus;
    private View btnEarnings; // Changed to View to match CardView or ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        // 2. Initialize Views
        layoutHeader = findViewById(R.id.layoutHeader);
        layoutStatus = findViewById(R.id.layoutStatus);

        switchStatus = findViewById(R.id.switchStatus);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);
        ivDriverProfile = findViewById(R.id.ivDriverProfile);
        tvDriver = findViewById(R.id.tvDriver);

        String realName = getSharedPreferences("DriverPrefs", MODE_PRIVATE)
                .getString("fullName", "Driver");

        tvDriver.setText(realName);

        ivDriverProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DriverDashboard.this, DriverProfileActivity.class);
            startActivity(intent);
        });



        // 3. Setup Listener to Hide/Show Containers
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof NavigationFragment) {
                // Hide the ENTIRE header and status bar
                setDashboardVisible(false);
            } else {
                // Show them
                setDashboardVisible(true);
            }
        });

        // Button Click Logic
        if (btnEarnings != null) {
            btnEarnings.setOnClickListener(v -> {
                Intent intent = new Intent(DriverDashboard.this, DriverEarningActivity.class);
                startActivity(intent);
            });
        }

        // Switch Logic
        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvStatusLabel.setText("You're ONLINE");
                tvStatusLabel.setTextColor(Color.parseColor("#10B981"));
                loadFragment(new OnlineFragment());
            } else {
                tvStatusLabel.setText("You're OFFLINE");
                tvStatusLabel.setTextColor(Color.parseColor("#94A3B8"));
                loadFragment(new OfflineFragment());
            }
        });

        loadFragment(new OfflineFragment());
    }

    // 4. Helper Method to toggle visibility
    private void setDashboardVisible(boolean visible) {
        int state = visible ? View.VISIBLE : View.GONE;

        if (layoutHeader != null) layoutHeader.setVisibility(state);
        if (layoutStatus != null) layoutStatus.setVisibility(state);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void setDuration(int duration) {
        int minutes = duration / 60;
        Log.d("DriverDashboard", "Trip Duration: " + minutes + " mins");
    }

    // Inside DriverDashboard.java

    @Override
    public void OnArrivalAnimationCompleted() {
        Log.d("DriverDashboard", "Animation Complete");

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof NavigationFragment) {
            NavigationFragment navFragment = (NavigationFragment) currentFragment;

            // 1. Update Status Text
            navFragment.updateStatusText("Arrived to the pick up point", "#10B981");

            // 2. ENABLE THE BUTTON
            navFragment.enableArrivalButton();
        }
    }
}
