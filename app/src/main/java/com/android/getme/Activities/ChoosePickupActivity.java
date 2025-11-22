package com.android.getme.Activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Adapters.PickupSearchAdapter;
import com.android.getme.Adapters.TabPageAdapter;
import com.android.getme.Listeners.PickupSearchListener;
import com.android.getme.Models.GHGeocodeResult;
import com.android.getme.R;
import com.android.getme.Services.LocationSearchService;
import com.android.getme.ViewModels.CustRideViewModel;
import com.android.getme.ViewModels.PickupViewModel;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChoosePickupActivity extends AppCompatActivity
implements PickupSearchListener {

    private boolean LocationPermissionGranted = false;
    private String userAgent;
    private MapView map;
    private IMapController mapController;
    private PickupViewModel viewModel;
    Marker currLocationMarker;
    Marker pickupMarker;

    EditText pickupSearchEditText;
    LinearLayout pickupBackLinlay;
    TextView pickupCurrLocAddress;
    LinearLayout pickupCurrLocLinlay;
    RecyclerView pickupSearchRecyclerView;
    PickupSearchAdapter adapter;

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK) {
                        // TODO: set data for dropoff, driverId, vehicleType, payment and rideId
                        if(result.getData() != null) {
                            Bundle b = result.getData().getExtras();
                            if(b != null) {
                                Intent intent = new Intent();
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

                                double dropoffLat = b.getDouble("dropoffLat");
                                double dropoffLng = b.getDouble("dropoffLng");
                                String dropoffName = b.getString("dropoffName");
                                String dropoffAddress = b.getString("dropoffAddress");
                                intent.putExtra("dropoffLat", dropoffLat);
                                intent.putExtra("dropoffLng", dropoffLng);
                                intent.putExtra("dropoffName", dropoffName);
                                intent.putExtra("dropoffAddress", dropoffAddress);

                                intent.putExtra("pickupLat", viewModel.pickupLocation.getLatitude());
                                intent.putExtra("pickupLng", viewModel.pickupLocation.getLongitude());
                                intent.putExtra("pickupName", viewModel.pickupName);
                                intent.putExtra("pickupAddress", viewModel.pickupAddress);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }

                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pickup);

        // Needed for OSM to work
        userAgent = ActivityCompat.getString(this, R.string.GetMe_OSM_User_Agent);
        Configuration.getInstance().setUserAgentValue(userAgent);


        viewModel = new ViewModelProvider(this).get(PickupViewModel.class);
        viewModel.vehicleType = getIntent().getExtras().getString("vehicleType");
        viewModel.custId = getIntent().getExtras().getInt("custId");


        // checking and requesting permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            LocationPermissionGranted = true;
        }

        if (LocationPermissionGranted) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            // if you see an error ignore it, the locationPermissionGranted already checks for location permission
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        viewModel.currLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                        currLocationMarker = new Marker(map);
                        currLocationMarker.setPosition(viewModel.currLocation);
                        currLocationMarker.setInfoWindow(null);
                        currLocationMarker.setIcon(ActivityCompat.getDrawable(ChoosePickupActivity.this, R.drawable.my_location_24px));
                        mapController.setCenter(viewModel.currLocation);
                        map.getOverlays().add(currLocationMarker);
                        map.invalidate();
                        setCurrentLocationAddress();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
        initializeViewComponents();

        initializeListeners();
    }

    private void initializeListeners() {
        // setting listeners
        pickupBackLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add result data in here if any
                finish();
            }
        });

        pickupCurrLocLinlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickupMarker = new Marker(map);
                pickupMarker.setPosition(viewModel.currLocation);
                pickupMarker.setInfoWindow(null);
                map.getOverlays().add(pickupMarker);
                map.invalidate();
                viewModel.pickupLocation = viewModel.currLocation;
                viewModel.pickupName = pickupCurrLocAddress.getText().toString();
                viewModel.pickupAddress = pickupCurrLocAddress.getText().toString();
                // start pick dropoff activity
                launchDropoffActivity();
            }
        });

        pickupSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_SEARCH) {
//                    Toast.makeText(ChoosePickupActivity.this, pickupSearchEditText.getText(), Toast.LENGTH_SHORT).show();

                    RequestQueue queue = Volley.newRequestQueue(ChoosePickupActivity.this);
                    String url = "https://graphhopper.com/api/1/geocode?";
                    String q = "q=" + pickupSearchEditText.getText();
                    String key = "key=" + ActivityCompat.getString(ChoosePickupActivity.this, R.string.GH_key);
                    String limit = "limit=" + 3;
                    String point = "&point=10.8231,106.6297"; // to bias ho chi minh city
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
                            // do nothing yet
                            Log.e("Volley Error", volleyError.toString());
                        }
                    });

                    queue.add(stringRequest);
                }
                return true;
            }
        });
    }

    private void setCurrentLocationAddress() {
        // gets the current address for current location
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                String theAddress = bundle.getString("theAddress");
                pickupCurrLocAddress.setText(theAddress);
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String theAddress = null;
                try {
                    GeocoderNominatim geocoderNominatim = new GeocoderNominatim(userAgent);
                    List<Address> addresses = geocoderNominatim.getFromLocation(viewModel.currLocation.getLatitude(), viewModel.currLocation.getLongitude(), 1);
                    StringBuilder sb = new StringBuilder();
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        int n = address.getMaxAddressLineIndex();
                        for (int i = 0; i < n; i++) {
                            if (i != 0) {
                                sb.append(", ");
                            }
                            sb.append(address.getAddressLine(i));
                        }
                        theAddress = sb.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("theAddress", theAddress);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
        executorService.shutdown();
    }

    private void initializeViewComponents() {
        map = findViewById(R.id.pickupMapView);
        map.setMultiTouchControls(false);
        mapController = map.getController();
        mapController.setZoom(15.0);
        if (viewModel.currLocation != null) {
            mapController.setCenter(viewModel.currLocation);
        }
        map.invalidate();

        pickupSearchEditText = findViewById(R.id.pickupSearchEditText);
        pickupBackLinlay = findViewById(R.id.pickupBackLinlay);
        pickupCurrLocAddress = findViewById(R.id.pickupCurrLocAddress);
        pickupCurrLocLinlay = findViewById(R.id.pickupCurrLocLinlay);
        pickupSearchRecyclerView = findViewById(R.id.pickupSearchRecyclerView);
        adapter = new PickupSearchAdapter(this);
        pickupSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pickupSearchRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 1) {
            LocationPermissionGranted = true;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    public void onPickupSearchItemClicked(String name, String address, double lat, double lng) {
        pickupSearchEditText.setText(name);
        if(pickupMarker != null) {
            map.getOverlays().remove(pickupMarker);
        }else {
            pickupMarker = new Marker(map);
        }
        pickupMarker.setPosition(new GeoPoint(lat, lng));
        map.getOverlays().add(pickupMarker);
        mapController.setCenter(new GeoPoint(lat, lng));
        map.invalidate();
        viewModel.pickupLocation = new GeoPoint(lat, lng);

        viewModel.pickupName = name;
        viewModel.pickupAddress = address;
        adapter.setSearchResults(new ArrayList<>());
        // start new activity
        launchDropoffActivity();
    }

    private void launchDropoffActivity() {


        Intent intent = new Intent(ChoosePickupActivity.this, ChooseDropoffActivity.class);
        intent.putExtra("vehicleType", viewModel.vehicleType);
        intent.putExtra("pickupLat", viewModel.pickupLocation.getLatitude());
        intent.putExtra("pickupLng", viewModel.pickupLocation.getLongitude());
        intent.putExtra("pickupName", viewModel.pickupName);
        intent.putExtra("pickupAddress", viewModel.pickupAddress);
        intent.putExtra("custId", viewModel.custId);
        startForResult.launch(intent);
    }
}