package com.android.getme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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

public class RegisterUserActivity extends AppCompatActivity {

    EditText edtUserFullName, edtUserEmail, edtUserPhoneNo, edtUserPassword, edtUserConfirmPassword;
    CheckBox cbUserTerms;
    TextView tvUserSignin;
    MaterialButton btnUserCreateAccount;

    private final String BASE_URL = "http://10.0.2.2:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        initViews();
        setupClickListeners();

        tvUserSignin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterUserActivity.this, MainActivity.class));
        });
    }

    private void initViews() {
        edtUserFullName = findViewById(R.id.edtUserFullName);
        edtUserEmail = findViewById(R.id.edtUserEmail);
        edtUserPhoneNo = findViewById(R.id.edtUserPhoneNo);
        edtUserPassword = findViewById(R.id.edtUserPassword);
        edtUserConfirmPassword = findViewById(R.id.edtUserConfirmPassword);
        cbUserTerms = findViewById(R.id.cbUserTerms);
        btnUserCreateAccount = findViewById(R.id.btnUserCreateAccount);
        tvUserSignin = findViewById(R.id.tvUserSignin);
    }

    private void setupClickListeners() {
        btnUserCreateAccount.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {

        edtUserFullName.setError(null);
        edtUserEmail.setError(null);
        edtUserPhoneNo.setError(null);
        edtUserPassword.setError(null);
        edtUserConfirmPassword.setError(null);

        String fullname = edtUserFullName.getText().toString().trim();
        String email = edtUserEmail.getText().toString().trim();
        String phone = edtUserPhoneNo.getText().toString().trim();
        String password = edtUserPassword.getText().toString();
        String confirmPassword = edtUserConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(fullname)) {
            edtUserFullName.setError("Full name is required");
            focusView = edtUserFullName;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            edtUserEmail.setError("Email is required");
            focusView = edtUserEmail;
            cancel = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtUserEmail.setError("Enter a valid email");
            focusView = edtUserEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone)) {
            edtUserPhoneNo.setError("Phone number is required");
            focusView = edtUserPhoneNo;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            edtUserPassword.setError("Password is required");
            focusView = edtUserPassword;
            cancel = true;
        } else if (password.length() < 6) {
            edtUserPassword.setError("Password must be at least 6 characters");
            focusView = edtUserPassword;
            cancel = true;
        }

        if (!password.equals(confirmPassword)) {
            edtUserConfirmPassword.setError("Passwords do not match");
            focusView = edtUserConfirmPassword;
            cancel = true;
        }

        if (!cbUserTerms.isChecked()) {
            Toast.makeText(this, "You must accept terms and conditions", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) focusView.requestFocus();
            return;
        }


        registerUser(fullname, email, phone, password);
    }

    private void registerUser(String fullname, String email, String phone, String password) {
        String url = BASE_URL + "/register/cust";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("fullname", fullname);
            jsonBody.put("email", email);
            jsonBody.put("phone", phone);
            jsonBody.put("password", password);
            jsonBody.put("password_confirm", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {

                    Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();


                    getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("fullName", fullname)
                            .putString("email", email)
                            .putString("phone", phone)
                            .putString("gender", "Male")
                            .putBoolean("isPremium", false)
                            .apply();


                    Intent intent = new Intent(RegisterUserActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                },
                error -> {
                    String errorMsg = "Registration failed. Try again.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, "utf-8");
                            JSONObject data = new JSONObject(body);
                            errorMsg = data.optString("detail", "Email already exists");
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void saveUserSessionAndProfile(int custId, String fullname, String email, String phone) {

        getSharedPreferences("SESSION", MODE_PRIVATE)
                .edit()
                .putBoolean("loggedIn", true)
                .putBoolean("isUser", true)
                .putInt("userId", custId)
                .putString("userName", fullname)
                .putString("userEmail", email)
                .putString("userPhone", phone)
                .apply();


        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("fullName", fullname)
                .putString("email", email)
                .putString("phone", phone)
                .putString("gender", "Male")
                .putBoolean("isPremium", false)
                .apply();
    }
}