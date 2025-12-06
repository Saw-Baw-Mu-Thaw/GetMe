package com.android.getme.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.getme.Activities.MainActivity;
import com.android.getme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

public class UserProfileFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_IS_PREMIUM = "isPremium";

    // Views
    private TextView tvProfileUserName, tvProfileFullName, tvProfileEmail, tvProfilePhone;
    private Spinner spinnerGender;
    private ShapeableImageView ivProfilePicture;
    private MaterialButton btnProfileLogout;

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupGenderSpinner();
        setupClickListeners();
        loadUserProfile(); // First load
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void initViews(View view) {
        tvProfileUserName = view.findViewById(R.id.tvProfileUserName);
        tvProfileFullName = view.findViewById(R.id.tvProfileFullName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfilePhone = view.findViewById(R.id.tvProfilePhone);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnProfileLogout = view.findViewById(R.id.btnProfileLogout);
    }

    private void setupGenderSpinner() {
        String[] genderOptions = {"Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, genderOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupClickListeners() {
        ivProfilePicture.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Change profile picture", Toast.LENGTH_SHORT).show());

        btnProfileLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }


    private void loadUserProfile() {
        String fullName = sharedPreferences.getString("fullName", "User");
        String email = sharedPreferences.getString("email", "No email");
        String phone = sharedPreferences.getString("phone", "Not set");

        Log.d("PROFILE_DEBUG", "Name: '" + fullName + "' | Email: '" + email + "' | Phone: '" + phone + "'");

        tvProfileUserName.setText(fullName);
        tvProfileFullName.setText(fullName);
        tvProfileEmail.setText(email);
        tvProfilePhone.setText(phone);

        // Gender
        String gender = sharedPreferences.getString("gender", " ");
        int position = 0;
        switch (gender.toLowerCase()) {
            case "female": position = 1; break;
            case "other": position = 2; break;
            case "prefer not to say": position = 3; break;
        }
        spinnerGender.setSelection(position);
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear both SESSION and UserPrefs
        requireContext().getSharedPreferences("SESSION", Context.MODE_PRIVATE)
                .edit().clear().apply();

        requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .edit().clear().apply();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}