package com.android.getme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayoutLoginUser, linearLayoutLoginDriver;
    private TextView tvLoginUser, tvLoginDriver, tvRegister;
    private ImageView ivLoginUser, ivLoginDriver;
    private EditText edtLoginEmail, edtLoginPassword;
    private MaterialButton btnLogin;

    private boolean isUser = true;
    private static final String BASE_URL = "http://10.0.2.2:8000";
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("SESSION", MODE_PRIVATE);


        if (sp.getBoolean("loggedIn", false)) {
            launchCorrectHomeScreen();
            return;
        }

        setContentView(R.layout.activity_login);
        initViews();
        setupToggle();

        btnLogin.setOnClickListener(v -> validateAndLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent = isUser
                    ? new Intent(this, RegisterUserActivity.class)
                    : new Intent(this, RegisterDriverActivity.class);
            startActivity(intent);
        });

        if (getIntent().getBooleanExtra("OPEN_AS_DRIVER", false)) {
            isUser = false;
            updateToggleUI();
        }
    }

    private void initViews() {
        linearLayoutLoginUser = findViewById(R.id.linearLayoutLoginUser);
        linearLayoutLoginDriver = findViewById(R.id.linearLayoutLoginDriver);
        tvLoginUser = findViewById(R.id.tvLoginUser);
        tvLoginDriver = findViewById(R.id.tvLoginDriver);
        ivLoginUser = findViewById(R.id.ivLoginUser);
        ivLoginDriver = findViewById(R.id.ivLoginDriver);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void validateAndLogin() {
        String email = edtLoginEmail.getText().toString().trim();
        String password = edtLoginPassword.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtLoginEmail.setError("Valid email required");
            edtLoginEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edtLoginPassword.setError("Password required");
            edtLoginPassword.requestFocus();
            return;
        }

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        String url = isUser ? BASE_URL + "/login/cust" : BASE_URL + "/login/driver";

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    try {
                        int userId = isUser ? response.getInt("custId") : response.getInt("driverId");


                        saveSession(userId, isUser);


                        fetchUserProfile(userId, isUser);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                    }
                },
                error -> {
                    String msg = "Invalid email or password";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String res = new String(error.networkResponse.data, "utf-8");
                            JSONObject obj = new JSONObject(res);
                            msg = obj.optString("detail", msg);
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                });

        Volley.newRequestQueue(this).add(request);
    }


    private void fetchUserProfile(int userId, boolean isCustomer) {
        String url = isCustomer
                ? BASE_URL + "/profile/customer?custId=" + userId
                : BASE_URL + "/profile/driver?driverId=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (isCustomer) {
                            saveCustomerProfile(response);
                        } else {
                            saveDriverProfile(response);
                        }

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        launchCorrectHomeScreen();

                    } catch (JSONException e) {
                        Log.e("PROFILE", "Error parsing profile", e);

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Login");
                        launchCorrectHomeScreen();
                    }
                },
                error -> {
                    Log.e("PROFILE", "Failed to load profile", error);

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    launchCorrectHomeScreen();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }


    private void saveCustomerProfile(JSONObject response) throws JSONException {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String existingGender = prefs.getString("gender", "male");

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("fullName", response.getString("fullname"));
        editor.putString("email", response.getString("email"));
        editor.putString("phone", response.getString("phone"));


        String genderFromBackend = response.optString("gender", "");
        if (genderFromBackend.isEmpty() || genderFromBackend.trim().isEmpty()) {
            editor.putString("gender", existingGender);
        } else {
            editor.putString("gender", genderFromBackend);
        }

        editor.putBoolean("isPremium", response.optBoolean("isPremium", false));

        editor.apply();
        Log.d("LOGIN_PROFILE", "Customer profile saved to UserPrefs");
    }

    private void saveDriverProfile(JSONObject response) throws JSONException {
        SharedPreferences prefs = getSharedPreferences("DriverPrefs", MODE_PRIVATE);

        String existingGender = prefs.getString("gender", "male");

        SharedPreferences.Editor editor = prefs.edit();


        editor.putString("fullName", response.getString("fullname"));
        editor.putString("email", response.getString("email"));
        editor.putString("phone", response.getString("phone"));

        String genderFromBackend = response.optString("gender", "");
        if (genderFromBackend.isEmpty() || genderFromBackend.trim().isEmpty()) {
            editor.putString("gender", existingGender);
        } else {
            editor.putString("gender", genderFromBackend);
        }


        editor.putString("vehicleModel", response.optString("make", "Not set"));
        editor.putString("licensePlate", response.optString("license", "Not set"));
        editor.putString("vehicleColor", response.optString("color", "Not set"));
        editor.putString("vehicleType", response.optString("vehicleType", "Car"));


        editor.putInt("totalRides", response.optInt("totalRides", 0));
        editor.putInt("acceptanceRate", response.optInt("acceptanceRate", 100));
        editor.putBoolean("isPremium", response.optBoolean("isPremium", false));
        editor.putBoolean("isLoggedIn", true);

        editor.apply();
        Log.d("LOGIN_PROFILE", "Driver profile saved to DriverPrefs");
    }

    private void saveSession(int userId, boolean isCustomer) {
        sp.edit()
                .putBoolean("loggedIn", true)
                .putBoolean("isUser", isCustomer)
                .putInt("userId", userId)
                .apply();
    }

    private void launchCorrectHomeScreen() {
        boolean isCustomer = sp.getBoolean("isUser", true);
        int userId = sp.getInt("userId", -1);

        Intent intent = isCustomer
                ? new Intent(this, HomeScreenActivity.class)
                : new Intent(this, DriverDashboard.class);

        intent.putExtra("id", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupToggle() {
        linearLayoutLoginUser.setOnClickListener(v -> { isUser = true; updateToggleUI(); });
        linearLayoutLoginDriver.setOnClickListener(v -> { isUser = false; updateToggleUI(); });
        updateToggleUI();
    }

    private void updateToggleUI() {
        int selectedBg = R.drawable.bg_toggle_selected;
        int unselectedBg = Color.TRANSPARENT;
        int white = Color.WHITE;
        int gray = Color.parseColor("#4B5563");

        if (isUser) {
            linearLayoutLoginUser.setBackgroundResource(selectedBg);
            tvLoginUser.setTextColor(white);
            ivLoginUser.setColorFilter(white);
            linearLayoutLoginDriver.setBackgroundColor(unselectedBg);
            tvLoginDriver.setTextColor(gray);
            ivLoginDriver.setColorFilter(gray);
        } else {
            linearLayoutLoginDriver.setBackgroundResource(selectedBg);
            tvLoginDriver.setTextColor(white);
            ivLoginDriver.setColorFilter(white);
            linearLayoutLoginUser.setBackgroundColor(unselectedBg);
            tvLoginUser.setTextColor(gray);
            ivLoginUser.setColorFilter(gray);
        }
    }
}