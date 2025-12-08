package com.android.getme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterDriverActivity extends AppCompatActivity {

    private EditText edtDriverFullName, edtDriverEmail, edtDriverPhone, edtDriverPassword, edtDriverConfirmPassword;
    private EditText edtVehicleModel, edtLicensePlate, edtVehicleColor;
    private CheckBox cbTerms;
    private MaterialButton btnDriverCreateRegister;
    private ImageView ivCar, ivBike;
    private TextView tvCar, tvBike, tvDriverSignin;

    private LinearLayout linearlayoutBike, linearlayoutCar;
    private String selectedVehicleType = "Car";

    private static final String BASE_URL = "http://10.0.2.2:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        initViews();
        setupVehicleSelection();
        selectVehicle(selectedVehicleType);

        btnDriverCreateRegister.setOnClickListener(v -> attemptRegister());

        tvDriverSignin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterDriverActivity.this, MainActivity.class);
            intent.putExtra("OPEN_AS_DRIVER", true);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        edtDriverFullName = findViewById(R.id.edtDriverFullName);
        edtDriverEmail = findViewById(R.id.edtDriverEmail);
        edtDriverPhone = findViewById(R.id.edtDriverPhone);
        edtDriverPassword = findViewById(R.id.edtDriverPassword);
        edtDriverConfirmPassword = findViewById(R.id.edtDriverConfirmPassword);

        edtVehicleModel = findViewById(R.id.edtVehicleModel);
        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        edtVehicleColor = findViewById(R.id.edtVehicleColor);

        ivBike = findViewById(R.id.ivBike);
        ivCar  = findViewById(R.id.ivCar);
        tvBike = findViewById(R.id.tvBike);
        tvCar  = findViewById(R.id.tvCar);
        tvDriverSignin = findViewById(R.id.tvDriverSignin);

        cbTerms = findViewById(R.id.cbTerms);
        btnDriverCreateRegister = findViewById(R.id.btnDriverCreateRegister);

        linearlayoutBike = findViewById(R.id.linearlayoutBike);
        linearlayoutCar = findViewById(R.id.linearlayoutCar);
    }

    private void setupVehicleSelection() {
        linearlayoutBike.setOnClickListener(v -> selectVehicle("Bike"));
        linearlayoutCar.setOnClickListener(v -> selectVehicle("Car"));
    }

    private void selectVehicle(String type) {
        selectedVehicleType = type;

        int selectedColor   = ContextCompat.getColor(this, R.color.primary_blue);
        int unselectedColor = 0xFF6B7280;

        if ("Bike".equals(type)) {
            findViewById(R.id.linearlayoutBike).setBackgroundResource(R.drawable.vehicle_type_selected);
            findViewById(R.id.linearlayoutCar).setBackgroundResource(R.drawable.vehicle_type_normal);

            ivBike.setColorFilter(selectedColor);
            tvBike.setTextColor(selectedColor);

            ivCar.setColorFilter(unselectedColor);
            tvCar.setTextColor(unselectedColor);

        } else {
            findViewById(R.id.linearlayoutCar).setBackgroundResource(R.drawable.vehicle_type_selected);
            findViewById(R.id.linearlayoutBike).setBackgroundResource(R.drawable.vehicle_type_normal);

            ivCar.setColorFilter(selectedColor);
            tvCar.setTextColor(selectedColor);

            ivBike.setColorFilter(unselectedColor);
            tvBike.setTextColor(unselectedColor);
        }
    }

    private void attemptRegister() {
        resetErrors();

        String fullname = edtDriverFullName.getText().toString().trim();
        String email = edtDriverEmail.getText().toString().trim();
        String phone = edtDriverPhone.getText().toString().trim();
        String password = edtDriverPassword.getText().toString();
        String confirm = edtDriverConfirmPassword.getText().toString();
        String model = edtVehicleModel.getText().toString().trim();
        String license = edtLicensePlate.getText().toString().trim().toUpperCase();
        String color = edtVehicleColor.getText().toString().trim();

        boolean cancel = false;

        if (TextUtils.isEmpty(fullname)) { edtDriverFullName.setError("Required"); cancel = true; }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtDriverEmail.setError("Valid email required"); cancel = true;
        }
        if (TextUtils.isEmpty(phone)) { edtDriverPhone.setError("Required"); cancel = true; }
        if (password.length() < 6) { edtDriverPassword.setError("Min 6 characters"); cancel = true; }
        if (!password.equals(confirm)) { edtDriverConfirmPassword.setError("Passwords don't match"); cancel = true; }
        if (TextUtils.isEmpty(model)) { edtVehicleModel.setError("Required"); cancel = true; }
        if (TextUtils.isEmpty(license)) { edtLicensePlate.setError("Required"); cancel = true; }
        if (TextUtils.isEmpty(color)) { edtVehicleColor.setError("Required"); cancel = true; }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Accept terms", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) return;

        registerDriver(fullname, email, phone, password, model, license, color);
    }

    private void registerDriver(String fullname, String email, String phone, String password,
                                String make, String license, String color) {

        String url = BASE_URL + "/register/driver";

        JSONObject json = new JSONObject();
        try {
            json.put("fullname", fullname);
            json.put("email", email);
            json.put("phone", phone);
            json.put("password", password);
            json.put("password_confirm", password);
            json.put("vehicleType", selectedVehicleType);
            json.put("make", make);
            json.put("license", license);
            json.put("color", color);
            json.put("earning", 0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                response -> {
                    Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();

                    getSharedPreferences("DriverPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("fullName", fullname)
                            .putString("email", email)
                            .putString("phone", phone)
                            .putString("vehicleModel", make)
                            .putString("licensePlate", license)
                            .putString("vehicleColor", color)
                            .putString("vehicleType", selectedVehicleType)
                            .apply();

                    Intent intent = new Intent(RegisterDriverActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String msg = "Registration failed. Try again.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, "utf-8");
                            JSONObject obj = new JSONObject(body);
                            msg = obj.optString("detail", "Email or license already exists");
                        } catch (Exception ignored) {}
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void resetErrors() {
        edtDriverFullName.setError(null);
        edtDriverEmail.setError(null);
        edtDriverPhone.setError(null);
        edtDriverPassword.setError(null);
        edtDriverConfirmPassword.setError(null);
        edtVehicleModel.setError(null);
        edtLicensePlate.setError(null);
        edtVehicleColor.setError(null);
    }
}