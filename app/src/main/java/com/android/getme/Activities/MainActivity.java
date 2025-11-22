package com.android.getme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    LinearLayout linearLayoutLoginUser, linearLayoutLoginDriver;
    TextView tvLoginUser, tvLoginDriver;
    ImageView ivLoginUser, ivLoginDriver;
    EditText edtLoginEmail, edtLoginPassword;
    MaterialButton btnLogin;

    boolean isUser = true;
    String BASE_URL = "http://your.address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        linearLayoutLoginUser = findViewById(R.id.linearLayoutLoginUser);
        linearLayoutLoginDriver = findViewById(R.id.linearLayoutLoginDriver);
        tvLoginUser = findViewById(R.id.tvLoginUser);
        tvLoginDriver = findViewById(R.id.tvLoginDriver);
        ivLoginUser = findViewById(R.id.ivLoginUser);
        ivLoginDriver = findViewById(R.id.ivLoginDriver);
        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);

        setupToggle();
        btnLogin.setOnClickListener(v -> validateLogin());

    }

    private void validateLogin() {

        String email = edtLoginEmail.getText().toString().trim();
        String password = edtLoginPassword.getText().toString().trim();

        if (email.isEmpty()){
            edtLoginEmail.setError("Email cannot be blank");
            return;
        }

        if (password.isEmpty()) {
            edtLoginPassword.setError(("Password is required"));
            return;
        }

        loginToServer(email, password);

    }

    private void loginToServer(String email, String password) {
        String url = isUser
                ? BASE_URL + "/login/cust"
                : BASE_URL + "/login/driver";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {

                    try {
                        int userId = Integer.parseInt(response);
                        saveSession(userId);

                        Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();

                        if (isUser) {
                            startActivity(new Intent(this, HomeScreenActivity.class).putExtra("id", 1));
                        } else {
                            // startActivity(new Intent(this, DriverHomeScreenActivty.class));
                        }

                        finish();

                    } catch (Exception e) {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                },

                error -> Toast.makeText(this, "Invalid email or password", Toast.LENGTH_LONG).show()
        ) {
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", email);
                map.put("password", password);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    private void saveSession(int userId) {
        SharedPreferences sp = getSharedPreferences("SESSION", MODE_PRIVATE);
        sp.edit()
                .putBoolean("loggedIn", true)
                .putBoolean("isUser", isUser)
                .putInt("userId", userId)
                .apply();

    }

    private void setupToggle() {
        linearLayoutLoginUser.setOnClickListener(v -> {
            isUser = true;
            setToggleUI();
        });

        linearLayoutLoginDriver.setOnClickListener(v ->{
            isUser = false;
            setToggleUI();
        });

        setToggleUI();
    }

    private void setToggleUI() {
        if (isUser) {
            //when user selects
            linearLayoutLoginUser.setBackgroundResource(R.drawable.bg_toggle_selected);
            tvLoginUser.setTextColor(Color.WHITE);
            ivLoginUser.setColorFilter(Color.WHITE);

            linearLayoutLoginDriver.setBackgroundColor(Color.TRANSPARENT);
            tvLoginDriver.setTextColor(Color.parseColor("#4B5563"));
            ivLoginDriver.setColorFilter((Color.parseColor("#4B5563")));
        } else {
            //when driver selects
            linearLayoutLoginDriver.setBackgroundResource(R.drawable.bg_toggle_selected);
            tvLoginDriver.setTextColor(Color.WHITE);
            ivLoginDriver.setColorFilter(Color.WHITE);

            linearLayoutLoginUser.setBackgroundColor(Color.TRANSPARENT);
            tvLoginUser.setTextColor(Color.parseColor("#4B5563"));
            ivLoginUser.setColorFilter((Color.parseColor("#4B5563")));
        }
    }
}