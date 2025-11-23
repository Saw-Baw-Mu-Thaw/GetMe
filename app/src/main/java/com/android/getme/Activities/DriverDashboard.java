package com.android.getme.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.getme.Fragments.OfflineFragment;
import com.android.getme.Fragments.OnlineFragment;
import com.android.getme.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class DriverDashboard extends AppCompatActivity {

    private SwitchMaterial switchStatus;
    private TextView tvStatusLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        switchStatus = findViewById(R.id.switchStatus);
        tvStatusLabel = findViewById(R.id.tvStatusLabel);

        // Load initial fragment (Offline)
        loadFragment(new OfflineFragment());

        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Switch to ONLINE
                tvStatusLabel.setText("You're ONLINE");
                tvStatusLabel.setTextColor(Color.parseColor("#10B981")); // Green
                loadFragment(new OnlineFragment());
            } else {
                // Switch to OFFLINE
                tvStatusLabel.setText("You're OFFLINE");
                tvStatusLabel.setTextColor(Color.parseColor("#94A3B8")); // Grey
                loadFragment(new OfflineFragment());


            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}