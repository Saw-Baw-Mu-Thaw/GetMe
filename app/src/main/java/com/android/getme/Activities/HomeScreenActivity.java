package com.android.getme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.getme.Fragments.ActivityFragment;
import com.android.getme.Fragments.HomeScreenFragment;
import com.android.getme.Listeners.CustHomeFragListener;
import com.android.getme.R;
import com.android.getme.ViewModels.CustRideViewModel;

import org.osmdroid.util.GeoPoint;

public class HomeScreenActivity extends AppCompatActivity implements
        CustHomeFragListener {

    LinearLayout homeScreenLinLay;
    LinearLayout activityScreenLinLay;
    LinearLayout notificationScreenLinLay;
    LinearLayout profileScreenLinlay;
    CustRideViewModel custRideViewModel;

    ActivityResultLauncher<Intent> launchTrackRideForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if(o.getResultCode() == RESULT_OK && o.getData() != null) {

                        String status = o.getData().getStringExtra("status");
                        if(status.equals("Cancelled") || status.equals("Completed")) {
                            unpopulateViewModel();
                        }

                    }
                }
            }
    );

    private void unpopulateViewModel() {
        custRideViewModel.status = null;
        custRideViewModel.rideId = 0;
        custRideViewModel.driverId = 0;
        custRideViewModel.vehicleType = null;
        custRideViewModel.amount = 0;
        custRideViewModel.payment = null;
        custRideViewModel.pickup = null;
        custRideViewModel.locationFrom = null;
        custRideViewModel.locationTo = null;
        custRideViewModel.dropoff = null;
        custRideViewModel.distance = 0.0;
        custRideViewModel.duration = 0;
    }

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK) {
                        Bundle b = result.getData().getExtras();
                        if(b != null) {

                            String status = b.getString("status");
                            if(status.equals("Cancelled")) {
                                unpopulateViewModel();
                            }else{
                                populateViewModelWithResult(b);
                                startTrackRide();
                            }
                        }
                    }
                }
            }
    );

    private void populateViewModelWithResult(Bundle b) {
        int rideId = b.getInt("rideId");
        int driverId = b.getInt("driverId");
        String payment = b.getString("payment");
        int amount = b.getInt("amount");
        int duration = b.getInt("duration");
        double distance = b.getDouble("distance");
        custRideViewModel.amount = amount;
        custRideViewModel.duration = duration;
        custRideViewModel.distance = distance;
        custRideViewModel.payment = payment;
        custRideViewModel.rideId = rideId;
        custRideViewModel.driverId = driverId;


        double dropoffLat = b.getDouble("dropoffLat");
        double dropoffLng = b.getDouble("dropoffLng");
        String dropoffName = b.getString("dropoffName");
        custRideViewModel.dropoff = new GeoPoint(dropoffLat, dropoffLng);
        custRideViewModel.locationTo = dropoffName;

        double pickupLat = b.getDouble("pickupLat");
        double pickupLng = b.getDouble("pickupLng");
        String pickupName = b.getString("pickupName");
        custRideViewModel.pickup = new GeoPoint(pickupLat, pickupLng);
        custRideViewModel.locationFrom = pickupName;

        custRideViewModel.status = "Waiting";
    }

    private void startTrackRide() {
        Intent intent = new Intent(HomeScreenActivity.this, TrackRideActivity.class);
        intent.putExtra("pickupLat", custRideViewModel.pickup.getLatitude());
        intent.putExtra("pickupLng", custRideViewModel.pickup.getLongitude());
        intent.putExtra("locationFrom", custRideViewModel.locationFrom);
        intent.putExtra("dropoffLat", custRideViewModel.dropoff.getLatitude());
        intent.putExtra("dropoffLng", custRideViewModel.dropoff.getLongitude());
        intent.putExtra("locationTo", custRideViewModel.locationTo);
        intent.putExtra("status", custRideViewModel.status);
        intent.putExtra("vehicleType", custRideViewModel.vehicleType);
        intent.putExtra("distance", custRideViewModel.distance);
        intent.putExtra("driverId", custRideViewModel.driverId);
        intent.putExtra("payment", custRideViewModel.payment);
        intent.putExtra("amount", custRideViewModel.amount);
        intent.putExtra("rideId", custRideViewModel.rideId);
        launchTrackRideForResult.launch(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(getIntent().getExtras() == null) {
            return;
        }
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        custRideViewModel = viewModelProvider.get(CustRideViewModel.class);

        custRideViewModel.custId = getIntent().getExtras().getInt("id");


        homeScreenLinLay = findViewById(R.id.homeScreenLinlay);
        activityScreenLinLay = findViewById(R.id.activityScreenLinlay);
        notificationScreenLinLay = findViewById(R.id.notificationScreenLinlay);
        profileScreenLinlay = findViewById(R.id.profileScreenLinlay);

        initializeListeners();

        // Sets Home screen as default screen
        getSupportFragmentManager().beginTransaction().replace(R.id.homeScreenFragContainer, new HomeScreenFragment()).commit();
    }

    private void initializeListeners() {
        homeScreenLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.homeScreenFragContainer, new HomeScreenFragment()).commit();
            }
        });

        activityScreenLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.homeScreenFragContainer, new ActivityFragment()).commit();
            }
        });

        notificationScreenLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Notification screen will be shown here");
            }
        });

        profileScreenLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Profile screen will be shown here");
            }
        });
    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookRideClicked(String vehicleType) {
        custRideViewModel.vehicleType = vehicleType;
        Intent intent = new Intent(this, ChoosePickupActivity.class);
        intent.putExtra("vehicleType", vehicleType);
        intent.putExtra("custId", custRideViewModel.custId);
        startForResult.launch(intent);
    }
}