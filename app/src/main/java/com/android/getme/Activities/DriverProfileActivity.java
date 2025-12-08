package com.android.getme.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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


    private TextView tvUserName;
    private TextView tvFullName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvProfileVehicleModel;
    private TextView tvProfileLicensePlate;
    private TextView tvProfileVehicleColor;
    private TextView tvProfileVehicleType;
    private TextView tvTotalRides;
    private TextView tvAcceptanceRate;
    private Spinner spinnerGender;
    private MaterialButton btnLogout;

    private boolean isLoadingProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        setupToolbar();
        initViews();


        setupGenderSpinner();
        setupLogoutButton();
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
        tvProfileVehicleModel = findViewById(R.id.tvProfileVehicleModel);
        tvProfileLicensePlate = findViewById(R.id.tvProfileLicensePlate);
        tvProfileVehicleColor = findViewById(R.id.tvProfileVehicleColor);
        tvProfileVehicleType = findViewById(R.id.tvProfileVehicleType);
        tvTotalRides = findViewById(R.id.tvTotalRides);
        tvAcceptanceRate = findViewById(R.id.tvAcceptanceRate);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
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

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isLoadingProfile) {
                    String selectedGender = parent.getItemAtPosition(position).toString().toLowerCase();
                    saveGender(selectedGender);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadDriverProfile() {
        isLoadingProfile = true;

        String fullName = sharedPreferences.getString(KEY_FULL_NAME, "");
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        String phone = sharedPreferences.getString(KEY_PHONE, "");
        String gender = sharedPreferences.getString(KEY_GENDER, "male");
        String vehicleModel = sharedPreferences.getString(KEY_VEHICLE_MODEL, "Not set");
        String licensePlate = sharedPreferences.getString(KEY_LICENSE_PLATE, "Not set");
        String vehicleColor = sharedPreferences.getString(KEY_VEHICLE_COLOR, "Not set");
        String vehicleType = sharedPreferences.getString(KEY_VEHICLE_TYPE, "Not set");
        int totalRides = sharedPreferences.getInt(KEY_TOTAL_RIDES, 0);
        int acceptanceRate = sharedPreferences.getInt(KEY_ACCEPTANCE_RATE, 100);
        boolean isPremium = sharedPreferences.getBoolean(KEY_IS_PREMIUM, false);

        Log.d("DRIVER_PROFILE", "Loading gender: '" + gender + "'");

        if (fullName.isEmpty()) {
            fullName = "Driver Name";
        }
        if (email.isEmpty()) {
            email = "No email";
        }
        if (phone.isEmpty()) {
            phone = "No phone";
        }


        tvUserName.setText(fullName);
        tvFullName.setText(fullName);
        tvEmail.setText(email);
        tvPhone.setText(phone);


        tvProfileVehicleModel.setText(vehicleModel);
        tvProfileLicensePlate.setText(licensePlate);
        tvProfileVehicleColor.setText(vehicleColor);
        tvProfileVehicleType.setText(vehicleType);

        tvTotalRides.setText(String.valueOf(20));
        tvAcceptanceRate.setText(acceptanceRate + "%");

        int genderPosition = 0;
        switch (gender.trim().toLowerCase()) {
            case "female":
                genderPosition = 1;
                break;
            case "other":
                genderPosition = 2;
                break;
            case "prefer not to say":
                genderPosition = 3;
                break;
            case "male":
            default:
                genderPosition = 0;
                break;
        }
        spinnerGender.setSelection(genderPosition);

        isLoadingProfile = false;
    }

    private void saveGender(String gender) {
        sharedPreferences.edit()
                .putString(KEY_GENDER, gender)
                .apply();
        Log.d("DRIVER_PROFILE", "Gender saved: " + gender);
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
        SharedPreferences sessionPrefs = getSharedPreferences("SESSION", Context.MODE_PRIVATE);
        sessionPrefs.edit().clear().apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DriverProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}