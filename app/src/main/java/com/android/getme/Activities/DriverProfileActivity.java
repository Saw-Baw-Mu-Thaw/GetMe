package com.android.getme.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.getme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

public class DriverProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "DriverPrefs";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_VEHICLE_MODEL = "vehicleModel";
    private static final String KEY_LICENSE_PLATE = "licensePlate";
    private static final String KEY_VEHICLE_COLOR = "vehicleColor";
    private static final String KEY_VEHICLE_TYPE = "vehicleType";
    private static final String KEY_TOTAL_RIDES = "totalRides";
    private static final String KEY_ACCEPTANCE_RATE = "acceptanceRate";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_PREMIUM = "isPremium";

    // Views
    private TextView tvUserName;
    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvMembershipStatus;
    private TextView tvProfileVehicleModel;
    private TextView tvProfileLicensePlate;
    private TextView tvProfileVehicleColor;
    private TextView tvProfileVehicleType;
    private TextView tvTotalRides;
    private TextView tvAcceptanceRate;
    private Spinner spinnerGender;
    private ImageView ivSettings;
    private ShapeableImageView ivPicture;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Setup toolbar with back button
        setupToolbar();

        // Initialize views
        initViews();

        // Setup components
        setupGenderSpinner();
        setupClickListeners();
        loadDriverProfile();
    }

    private void setupToolbar() {
        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvMembershipStatus = findViewById(R.id.tvMembershipStatus);
        tvProfileVehicleModel = findViewById(R.id.tvProfileVehicleModel);
        tvProfileLicensePlate = findViewById(R.id.tvProfileLicensePlate);
        tvProfileVehicleColor = findViewById(R.id.tvProfileVehicleColor);
        tvProfileVehicleType = findViewById(R.id.tvProfileVehicleType);
        tvTotalRides = findViewById(R.id.tvTotalRides);
        tvAcceptanceRate = findViewById(R.id.tvAcceptanceRate);
        spinnerGender = findViewById(R.id.spinnerGender);
        ivSettings = findViewById(R.id.ivSettings);
        ivPicture = findViewById(R.id.ivPicture);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Settings button click
        ivSettings.setOnClickListener(v -> {
            Toast.makeText(DriverProfileActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
            // Navigate to settings activity
            // Intent intent = new Intent(DriverProfileActivity.this, SettingsActivity.class);
            // startActivity(intent);
        });

        // Logout button click
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Profile picture click (for changing photo)
        ivPicture.setOnClickListener(v -> {
            Toast.makeText(DriverProfileActivity.this, "Change profile picture", Toast.LENGTH_SHORT).show();
            // Implement image picker
            // openImagePicker();
        });
    }

    private void loadDriverProfile() {
        // Load real user data from SharedPreferences (saved during login/registration)
        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String phone = sharedPreferences.getString(KEY_PHONE, "");
        String gender = sharedPreferences.getString(KEY_GENDER, "Male");
        String vehicleModel = sharedPreferences.getString(KEY_VEHICLE_MODEL, "Not set");
        String licensePlate = sharedPreferences.getString(KEY_LICENSE_PLATE, "Not set");
        String vehicleColor = sharedPreferences.getString(KEY_VEHICLE_COLOR, "Not set");
        String vehicleType = sharedPreferences.getString(KEY_VEHICLE_TYPE, "Not set");
        int totalRides = sharedPreferences.getInt(KEY_TOTAL_RIDES, 0);
        int acceptanceRate = sharedPreferences.getInt(KEY_ACCEPTANCE_RATE, 100);
        boolean isPremium = sharedPreferences.getBoolean(KEY_IS_PREMIUM, false);

        // Check if user data exists
        if (fullName.isEmpty()) {
            fullName = "Driver Name";
        }
        if (email.isEmpty()) {
            email = "No email";
        }
        if (phone.isEmpty()) {
            phone = "No phone";
        }

        // Set name in header
        tvUserName.setText(fullName);

        // Set name in personal info
        tvFullName.setText(fullName);

        // Set email
        tvEmail.setText(email);

        // Set phone
        tvPhone.setText(phone);

        // Set membership status
        tvMembershipStatus.setText(isPremium ? "Premium Member" : "Standard Member");

        // Set vehicle information
        tvProfileVehicleModel.setText(vehicleModel);
        tvProfileLicensePlate.setText(licensePlate);
        tvProfileVehicleColor.setText(vehicleColor);
        tvProfileVehicleType.setText(vehicleType);

        // Set statistics
        tvTotalRides.setText(String.valueOf(totalRides));
        tvAcceptanceRate.setText(acceptanceRate + "%");

        // Set gender spinner selection
        int genderPosition;
        switch (gender.toLowerCase()) {
            case "male":
                genderPosition = 0;
                break;
            case "female":
                genderPosition = 1;
                break;
            case "other":
                genderPosition = 2;
                break;
            default:
                genderPosition = 3;
                break;
        }
        spinnerGender.setSelection(genderPosition);
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        // Clear driver data from DriverPrefs
        SharedPreferences.Editor driverEditor = sharedPreferences.edit();
        driverEditor.clear();
        driverEditor.apply();

        // Clear session from SESSION SharedPreferences (used by MainActivity)
        SharedPreferences sessionPrefs = getSharedPreferences("SESSION", Context.MODE_PRIVATE);
        SharedPreferences.Editor sessionEditor = sessionPrefs.edit();
        sessionEditor.clear();
        sessionEditor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity (login screen)
        Intent intent = new Intent(DriverProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish all activities
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}