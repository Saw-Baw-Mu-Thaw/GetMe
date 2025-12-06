package com.android.getme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.getme.R;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private LinearLayout linearLayoutLoginUser, linearLayoutLoginDriver;
    private TextView tvLoginUser, tvLoginDriver, tvRegister;
    private ImageView ivLoginUser, ivLoginDriver;
    private EditText edtLoginEmail, edtLoginPassword;
    private MaterialButton btnLogin;

    private boolean isUser = true;
    private static final String BASE_URL = "http://10.0.2.2:8000"; // Your IP

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("SESSION", MODE_PRIVATE);

        // Auto login if saved
        if (sp.getBoolean("loggedIn", false)) {
            launchCorrectHomeScreen();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupToggle();

        btnLogin.setOnClickListener(v -> validateAndLogin());

        tvRegister.setOnClickListener(v -> {
            Intent intent;
            if (isUser) {
                intent = new Intent(MainActivity.this, RegisterUserActivity.class);
            } else {
                intent = new Intent(MainActivity.this, RegisterDriverActivity.class);
            }
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

        if (email.isEmpty()) {
            edtLoginEmail.setError("Enter email");
            edtLoginEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtLoginEmail.setError("Invalid email");
            edtLoginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtLoginPassword.setError("Enter password");
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
        } catch (JSONException e) {}

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        int id = isUser ? response.getInt("custId") : response.getInt("driverId");

                        saveSession(id, isUser);
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        launchCorrectHomeScreen();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String msg = "Invalid email or password";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String res = new String(error.networkResponse.data);
                            JSONObject obj = new JSONObject(res);
                            msg = obj.optString("detail", msg);
                        } catch (Exception ignored) {}
                    }

                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void saveSession(int userId, boolean isCustomer) {
        SharedPreferences.Editor editor = sp.edit();

        editor.putBoolean("loggedIn", true);
        editor.putBoolean("isUser", isCustomer);
        editor.putInt("userId", userId);

        editor.apply();
    }

    private void launchCorrectHomeScreen() {
        boolean isCustomer = sp.getBoolean("isUser", true);

        Intent intent = isCustomer
                ? new Intent(this, HomeScreenActivity.class)
                : new Intent(this, DriverDashboard.class);

        int id = sp.getInt("userId", -1);
        intent.putExtra("id", id);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupToggle() {
        linearLayoutLoginUser.setOnClickListener(v -> {
            isUser = true;
            updateToggleUI();
        });

        linearLayoutLoginDriver.setOnClickListener(v -> {
            isUser = false;
            updateToggleUI();
        });

        updateToggleUI();
    }

    private void updateToggleUI() {
        if (isUser) {
            linearLayoutLoginUser.setBackgroundResource(R.drawable.bg_toggle_selected);
            tvLoginUser.setTextColor(Color.WHITE);
            ivLoginUser.setColorFilter(Color.WHITE);

            linearLayoutLoginDriver.setBackgroundColor(Color.TRANSPARENT);
            tvLoginDriver.setTextColor(Color.parseColor("#4B5563"));
            ivLoginDriver.setColorFilter(Color.parseColor("#4B5563"));
        } else {
            linearLayoutLoginDriver.setBackgroundResource(R.drawable.bg_toggle_selected);
            tvLoginDriver.setTextColor(Color.WHITE);
            ivLoginDriver.setColorFilter(Color.WHITE);

            linearLayoutLoginUser.setBackgroundColor(Color.TRANSPARENT);
            tvLoginUser.setTextColor(Color.parseColor("#4B5563"));
            ivLoginUser.setColorFilter(Color.parseColor("#4B5563"));
        }
    }
}