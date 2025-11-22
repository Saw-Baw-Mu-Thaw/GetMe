package com.android.getme.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.android.getme.R;
import com.android.getme.ViewModels.FindDriverViewModel;

import org.osmdroid.util.GeoPoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindDriverActivity extends AppCompatActivity {

    private String requestTAG = "RideRequestTAG";
    private FindDriverViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);

        populateViewModel();

        TextView findDriverPickupTextView = findViewById(R.id.findDriverPickupTextView);
        TextView findDriverDropoffTextView = findViewById(R.id.findDriverDropoffTextView);
        Button findDriverCancelBtn = findViewById(R.id.findDriverCancelBtn);

        findDriverPickupTextView.setText(viewModel.pickupName);
        findDriverDropoffTextView.setText(viewModel.dropoffName);
        findDriverCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: cancel volley request
                finish();
            }
        });

        fakeRequest();

    }

    private void returnData() {
        Intent intent = new Intent();
        intent.putExtra("rideId", viewModel.rideId);
        intent.putExtra("driverId", viewModel.driverId);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void fakeRequest() {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle b = msg.getData();
                if(b != null) {
                    viewModel.driverId = b.getInt("driverId");
                    viewModel.rideId = b.getInt("rideId");
                    returnData();
                }
                super.handleMessage(msg);
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                // simulate sending the request for a ride
                try{
                    Thread.sleep(3000);
                }catch(Exception e) {
                    e.printStackTrace();
                }


                // simuate driver accepting the request
                try{
                    Thread.sleep(3000);
                }catch(Exception e) {
                    e.printStackTrace();
                }
               Message msg = handler.obtainMessage();
                Bundle b = new Bundle();
                b.putInt("rideId", 1);
                b.putInt("driverId", 1);
                msg.setData(b);
                handler.sendMessage(msg);
            }
        });
        executorService.shutdown();
    }

    private void populateViewModel() {
        viewModel = new ViewModelProvider(this).get(FindDriverViewModel.class);

        if(getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();

            String type = b.getString("vehicleType");
            if(type.equals("Economy") || type.equals("Standard")) {
                viewModel.vehicleType = "Car";
            }else {
                viewModel.vehicleType = "Bike";
            }
            viewModel.amount = b.getInt("amount");
            viewModel.payment = b.getString("payment");
            viewModel.distance = b.getDouble("distance");
            viewModel.duration = b.getInt("duration");

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
    }
}