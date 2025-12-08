package com.android.getme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Adapters.PickupSearchAdapter;
import com.android.getme.Fragments.WarningDialogFragment;
import com.android.getme.Listeners.PickupSearchListener;
import com.android.getme.Models.GHGeocodeResult;
import com.android.getme.R;
import com.android.getme.ViewModels.DropoffViewModel;
import com.android.getme.ViewModels.PickupViewModel;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class ChooseDropoffActivity extends AppCompatActivity implements
        PickupSearchListener {
    MapView map;
    IMapController mapController;
    Marker dropoffMarker;
    Marker pickupMarker;
    RecyclerView dropoffResultsRecyclerView;
    DropoffViewModel viewModel;
    String userAgent;

    LinearLayout dropoffBackLinlay;
    TextView pickupNameTextView;
    TextView pickupAddressTextView;
    EditText dropoffNameTextView;
    LinearLayout setDestinationLinlay;
    RecyclerView recyclerView;
    PickupSearchAdapter adapter;

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
                            String payment = b.getString("payment");
                            intent.putExtra("payment", payment);
                            int amount = b.getInt("amount");
                            intent.putExtra("amount", amount);
                            int duration = b.getInt("duration");
                            double distance = b.getDouble("distance");
                            intent.putExtra("distance", distance);
                            intent.putExtra("duration", duration);


                            intent.putExtra("dropoffLat", viewModel.dropoffLocation.getLatitude());
                            intent.putExtra("dropoffLng", viewModel.dropoffLocation.getLongitude());
                            intent.putExtra("dropoffName", viewModel.dropoffName);
                            intent.putExtra("dropoffAddress", viewModel.dropoffAddress);
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
        setContentView(R.layout.activity_choose_dropoff);


        userAgent = ActivityCompat.getString(this, R.string.GetMe_OSM_User_Agent);
        Configuration.getInstance().setUserAgentValue(userAgent);



        viewModel = new ViewModelProvider(this).get(DropoffViewModel.class);
        if(getIntent().getExtras() != null) {
            viewModel.vehicleType = getIntent().getExtras().getString("vehicleType");
            double pickupLat = getIntent().getExtras().getDouble("pickupLat");
            double pickupLng = getIntent().getExtras().getDouble("pickupLng");
            viewModel.pickupName = getIntent().getExtras().getString("pickupName");
            viewModel.pickupAddress = getIntent().getExtras().getString("pickupAddress");
            viewModel.pickupLocation = new GeoPoint(pickupLat, pickupLng);
            viewModel.custId = getIntent().getExtras().getInt("custId");
        }

        initializeViewComponents();

        dropoffBackLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        pickupNameTextView.setText(viewModel.pickupName);
        pickupAddressTextView.setText(viewModel.pickupAddress);

        dropoffNameTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_SEARCH) {
                    RequestQueue queue = Volley.newRequestQueue(ChooseDropoffActivity.this);
                    String url = "https://graphhopper.com/api/1/geocode?";
                    String q = "q=" + dropoffNameTextView.getText();
                    String key = "key=" + ActivityCompat.getString(ChooseDropoffActivity.this, R.string.GH_key);
                    String limit = "limit=" + 3;
                    String point = "&point=10.8231,106.6297";
                    url = url + q + "&" + key + "&" + limit + point;

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Gson gson = new Gson();
                            GHGeocodeResult result = gson.fromJson(s, GHGeocodeResult.class);
                            adapter.setSearchResults(result.hits);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                            Log.e("Volley Error", volleyError.toString());
                        }
                    });

                    queue.add(stringRequest);
                }
                return true;
            }
        });

        setDestinationLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewModel.dropoffLocation == null) {

                    WarningDialogFragment.newInstance("Destination Error", "Destionation not selected")
                            .show(getSupportFragmentManager(), "Destination Warning Dialog");
                    return;
                }
                Intent intent = new Intent(ChooseDropoffActivity.this, ChooseVehicleActivity.class);
                intent.putExtra("vehicleType", viewModel.vehicleType);
                intent.putExtra("pickupLat", viewModel.pickupLocation.getLatitude());
                intent.putExtra("pickupLng", viewModel.pickupLocation.getLongitude());
                intent.putExtra("pickupName", viewModel.pickupName);
                intent.putExtra("pickupAddress", viewModel.pickupAddress);
                intent.putExtra("dropoffLat", viewModel.dropoffLocation.getLatitude());
                intent.putExtra("dropoffLng", viewModel.dropoffLocation.getLongitude());
                intent.putExtra("dropoffName", viewModel.dropoffName);
                intent.putExtra("dropoffAddress", viewModel.dropoffAddress);
                intent.putExtra("custId", viewModel.custId);
                startForResult.launch(intent);
            }
        });
    }

    private void initializeViewComponents() {
        map = findViewById(R.id.dropoffMap);
        mapController = map.getController();
        mapController.setZoom(15.0);
        mapController.setCenter(viewModel.pickupLocation);

        pickupMarker = new Marker(map);
        pickupMarker.setPosition(viewModel.pickupLocation);
        pickupMarker.setTitle(viewModel.pickupName);
        pickupMarker.setSubDescription(viewModel.pickupAddress);
        pickupMarker.setIcon(ActivityCompat.getDrawable(this, R.drawable.pickup_icon));
        map.getOverlays().add(pickupMarker);
        map.invalidate();

        dropoffBackLinlay = findViewById(R.id.dropoffBackLinlay);
        pickupNameTextView = findViewById(R.id.pickupNameTextView);
        pickupAddressTextView = findViewById(R.id.pickupAddressTextView);
        dropoffNameTextView = findViewById(R.id.dropoffNameTextView);
        setDestinationLinlay = findViewById(R.id.setDestinationLinlay);
        recyclerView = findViewById(R.id.dropoffResultsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PickupSearchAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void finish() {

        if(viewModel.dropoffLocation == null) {
            setResult(RESULT_OK);
        }
        super.finish();
    }

    @Override
    public void onPickupSearchItemClicked(String name, String address, double lat, double lng) {
        viewModel.dropoffName = name;
        viewModel.dropoffAddress = address;
        viewModel.dropoffLocation = new GeoPoint(lat,lng);
        dropoffNameTextView.setText(name);

        if(dropoffMarker!=null) {
            map.getOverlays().remove(dropoffMarker);
        }
        dropoffMarker = new Marker(map);
        dropoffMarker.setPosition(viewModel.dropoffLocation);
        dropoffMarker.setTitle(name);
        dropoffMarker.setSubDescription(address);
        dropoffMarker.setIcon(ActivityCompat.getDrawable(this, R.drawable.dropoff_icon));
        mapController.setCenter(new GeoPoint(lat,lng));
        mapController.setZoom(15.0);
        map.getOverlays().add(dropoffMarker);
        map.invalidate();
        adapter.setSearchResults(new ArrayList<>());
    }
}