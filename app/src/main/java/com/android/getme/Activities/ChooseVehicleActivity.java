package com.android.getme.Activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.getme.Fragments.WarningDialogFragment;
import com.android.getme.R;
import com.android.getme.ViewModels.ChooseVehicleViewModel;

import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChooseVehicleActivity extends AppCompatActivity {

    private String userAgent;
    ChooseVehicleViewModel viewModel;
    TextView chooseVehPickupNameTextView;
    TextView chooseVehPickupAddressTextView;
    TextView chooseVehDropoffNameTextView;
    TextView chooseVehDropoffAddressTextView;
    TextView economyPriceTextView;
    TextView standardPriceTextView;
    TextView bikePriceTextView;
    LinearLayout chooseVehBackLinlay;
    LinearLayout chooseVehRideEconomyLinlay;
    LinearLayout chooseVehRideStandardLinlay;
    LinearLayout chooseVehRideBikeLinlay;
    LinearLayout chooseVehCardLinlay;
    LinearLayout chooseVehCashLinlay;
    Button chooseVehBookBtn;
    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null) {

                        Bundle b = result.getData().getExtras();
                        if(b != null) {
                            Intent intent = new Intent();

                            String status = b.getString("status");
                            intent.putExtra("status", status);

                            if(status.equals("Cancelled")) {
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            int rideId = b.getInt("rideId");
                            int driverId = b.getInt("driverId");

                            intent.putExtra("rideId", rideId);
                            intent.putExtra("driverId", driverId);

                            intent.putExtra("payment", viewModel.payment);
                            intent.putExtra("amount", viewModel.amount);
                            intent.putExtra("distance", viewModel.distance);
                            intent.putExtra("duration", viewModel.duration);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_vehicle);

        userAgent = ActivityCompat.getString(this, R.string.GetMe_OSM_User_Agent);
        Configuration.getInstance().setUserAgentValue(userAgent);

        populateViewModel();

        initializeComponents();

        setPrice();

        initializeListeners();
    }

    private void initializeListeners() {
        chooseVehBackLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        chooseVehRideStandardLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeVehChoice();
                viewModel.chosenVehicleType = 2;
                viewModel.amount = Integer.parseInt(standardPriceTextView.getText().toString().replace(" VND", ""));
                setChoice();
            }
        });

        chooseVehRideEconomyLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeVehChoice();
                viewModel.chosenVehicleType = 1;
                viewModel.amount = Integer.parseInt(economyPriceTextView.getText().toString().replace(" VND", ""));
                setChoice();
            }
        });

        chooseVehRideBikeLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeVehChoice();
                viewModel.chosenVehicleType = 3;
                viewModel.amount = Integer.parseInt(bikePriceTextView.getText().toString().replace(" VND", ""));
                setChoice();
            }
        });

        chooseVehCardLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePaymentChoice();
                viewModel.chosenPaymentMethod = 1;
                viewModel.payment = "Card";
                setChoice();
            }
        });

        chooseVehCashLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePaymentChoice();
                viewModel.chosenPaymentMethod = 2;
                viewModel.payment = "Cash";
                setChoice();
            }
        });

        chooseVehBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewModel.chosenVehicleType != 0) {
                    // start FindDriver Activity
                    Intent intent = new Intent(ChooseVehicleActivity.this, FindDriverActivity.class);
                    intent.putExtra("vehicleType", viewModel.vehicleType);
                    intent.putExtra("pickupLat", viewModel.pickupLocation.getLatitude());
                    intent.putExtra("pickupLng", viewModel.pickupLocation.getLongitude());
                    intent.putExtra("pickupName", viewModel.pickupName);
                    intent.putExtra("pickupAddress", viewModel.pickupAddress);
                    intent.putExtra("dropoffLat", viewModel.dropoffLocation.getLatitude());
                    intent.putExtra("dropoffLng", viewModel.dropoffLocation.getLongitude());
                    intent.putExtra("dropoffName", viewModel.dropoffName);
                    intent.putExtra("dropoffAddress", viewModel.dropoffAddress);
                    intent.putExtra("duration", viewModel.duration);
                    intent.putExtra("amount", viewModel.amount);
                    intent.putExtra("distance", viewModel.distance);
                    intent.putExtra("payment", viewModel.payment);
                    intent.putExtra("custId", viewModel.custId);
                    startForResult.launch(intent);

                }else {
                    WarningDialogFragment.newInstance("Warning", "Please Choose a Vehicle Type")
                            .show(getSupportFragmentManager(), "Vehicle Warning Dialog");
                }
            }
        });
    }

    private void setPrice() {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                double distance = bundle.getDouble("distance");
                double duration = bundle.getDouble("duration");

                int economyRate = Integer.parseInt(ActivityCompat.getString(ChooseVehicleActivity.this, R.string.economy_rate));
                int standardRate = Integer.parseInt(ActivityCompat.getString(ChooseVehicleActivity.this, R.string.standard_rate));
                int bikeRate = Integer.parseInt(ActivityCompat.getString(ChooseVehicleActivity.this, R.string.bike_rate));

                int economyPrice = (((int) Math.round(distance * economyRate) / 1000) * 1000);
                int standardPrice = (((int) Math.round(distance * standardRate) / 1000) * 1000);
                int bikePrice = (((int)Math.round(distance * bikeRate) / 1000) * 1000);

                String economy = economyPrice+" VND";
                String standard = standardPrice+" VND";
                String bike = bikePrice + " VND";
                economyPriceTextView.setText(economy);
                standardPriceTextView.setText(standard);
                bikePriceTextView.setText(bike);

                switch (viewModel.chosenVehicleType) {
                    case 1:
                        viewModel.amount = economyPrice;
                        break;
                    case 2:
                        viewModel.amount = standardPrice;
                        break;
                    case 3:
                        viewModel.amount = bikePrice;
                        break;
                }

                viewModel.duration = ((int)duration) / 60;
                viewModel.distance = distance;

                setChoice();
                super.handleMessage(msg);
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String provider = ActivityCompat.getString(ChooseVehicleActivity.this, R.string.provider);

                RoadManager roadManager;
                if(provider.equals("OSRM")) {
                    roadManager = new OSRMRoadManager(ChooseVehicleActivity.this, userAgent);
                    ((OSRMRoadManager) roadManager).setMean(OSRMRoadManager.MEAN_BY_CAR);
                } else {
                    String api_key = ActivityCompat.getString(ChooseVehicleActivity.this, R.string.GH_key);
                    roadManager = new GraphHopperRoadManager(api_key, false);
                    roadManager.addRequestOption("profile=car");
                    roadManager.addRequestOption("snap_prevention=motorway");
                    roadManager.addRequestOption("snap_prevention=ferry");
                    roadManager.addRequestOption("snap_prevention=tunnel");
                    roadManager.addRequestOption("locale=en");
                }

                ArrayList<GeoPoint> waypoints = new ArrayList<>();
                waypoints.add(viewModel.pickupLocation);
                waypoints.add(viewModel.dropoffLocation);

                Road road = roadManager.getRoad(waypoints);

                if(road.mStatus == Road.STATUS_INVALID) {
                    WarningDialogFragment.newInstance("Status Invalid", "Road does not exist to those location")
                            .show(getSupportFragmentManager(), "Road Warning Dialog");
                } else if(road.mStatus == Road.STATUS_TECHNICAL_ISSUE) {
                    WarningDialogFragment.newInstance("Technical Issues", "Location provider is having some issues.")
                            .show(getSupportFragmentManager(), "Road Warning Dialog");
                } else if (road.mStatus == Road.STATUS_OK) {
                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("distance", road.mLength);
                    bundle.putDouble("duration", road.mDuration);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        });
        executorService.shutdown();
    }

    private void initializeComponents() {
        chooseVehPickupNameTextView = findViewById(R.id.chooseVehPickupNameTextView);
        chooseVehPickupAddressTextView = findViewById(R.id.chooseVehPickupAddressTextView);
        chooseVehDropoffNameTextView = findViewById(R.id.chooseVehDropoffNameTextView);
        chooseVehDropoffAddressTextView = findViewById(R.id.chooseVehDropoffAddressTextView);
        chooseVehBackLinlay = findViewById(R.id.chooseVehBackLinlay);
        chooseVehRideEconomyLinlay = findViewById(R.id.chooseVehRideEconomyLinlay);
        chooseVehRideStandardLinlay = findViewById(R.id.chooseVehRideStandardLinlay);
        chooseVehRideBikeLinlay = findViewById(R.id.chooseVehRideBikeLinlay);
        chooseVehBookBtn = findViewById(R.id.chooseVehBookBtn);
        chooseVehCardLinlay = findViewById(R.id.chooseVehCardLinlay);
        chooseVehCashLinlay = findViewById(R.id.chooseVehCashLinlay);
        economyPriceTextView = findViewById(R.id.economyPriceTextView);
        standardPriceTextView = findViewById(R.id.standardPriceTextView);
        bikePriceTextView = findViewById(R.id.bikePriceTextView);

        chooseVehPickupNameTextView.setText(viewModel.pickupName);
        chooseVehPickupAddressTextView.setText(viewModel.pickupAddress);
        chooseVehDropoffNameTextView.setText(viewModel.dropoffName);
        chooseVehDropoffAddressTextView.setText(viewModel.dropoffAddress);
    }

    private void setChoice() {
        Drawable highlight = ActivityCompat.getDrawable(this, R.drawable.highlight_selection_bg);
        switch (viewModel.chosenVehicleType) {
            case 1:
                chooseVehRideEconomyLinlay.setBackground(highlight);
                break;
            case 2:
                chooseVehRideStandardLinlay.setBackground(highlight);
                break;
            case 3:
                chooseVehRideBikeLinlay.setBackground(highlight);
                break;
        }

        switch (viewModel.chosenPaymentMethod) {
            case 1:
                chooseVehCardLinlay.setBackground(highlight);
                break;
            case 2:
                chooseVehCashLinlay.setBackground(highlight);
                break;
        }
    }

    public void removeVehChoice() {
        switch (viewModel.chosenVehicleType) {
            case 1:
                chooseVehRideEconomyLinlay.setBackground(null);
                break;
            case 2:
                chooseVehRideStandardLinlay.setBackground(null);
                break;
            case 3:
                chooseVehRideBikeLinlay.setBackground(null);
                break;
        }
    }

    public void removePaymentChoice() {
        if (viewModel.chosenPaymentMethod == 1) {
            chooseVehCardLinlay.setBackground(null);
        } else {
            chooseVehCashLinlay.setBackground(null);
        }
    }

    private void populateViewModel() {
        viewModel = new ViewModelProvider(this).get(ChooseVehicleViewModel.class);
        if (getIntent().getExtras() != null) {
            viewModel.vehicleType = getIntent().getExtras().getString("vehicleType");
            double pickupLat = getIntent().getExtras().getDouble("pickupLat");
            double pickupLng = getIntent().getExtras().getDouble("pickupLng");
            viewModel.pickupName = getIntent().getExtras().getString("pickupName");
            viewModel.pickupAddress = getIntent().getExtras().getString("pickupAddress");
            viewModel.pickupLocation = new GeoPoint(pickupLat, pickupLng);
            double dropoffLat = getIntent().getExtras().getDouble("dropoffLat");
            double dropoffLng = getIntent().getDoubleExtra("dropoffLng", 0.0);
            viewModel.dropoffLocation = new GeoPoint(dropoffLat, dropoffLng);
            viewModel.dropoffName = getIntent().getExtras().getString("dropoffName");
            viewModel.dropoffAddress = getIntent().getExtras().getString("dropoffAddress");
            viewModel.custId = getIntent().getExtras().getInt("custId");
        }

        if (viewModel.vehicleType.equals("Standard")) {
            viewModel.chosenVehicleType = 2;
        } else if (viewModel.vehicleType.equals("Economy")) {
            viewModel.chosenVehicleType = 1;
        } else if (viewModel.vehicleType.equals("Bike")) {
            viewModel.chosenVehicleType = 3;
        }
    }
}