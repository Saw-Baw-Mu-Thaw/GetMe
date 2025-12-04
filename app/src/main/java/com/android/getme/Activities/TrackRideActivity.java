package com.android.getme.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.voice.VoiceInteractionSession;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCallerLauncher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.getme.Fragments.AnimatedMapFragment;
import com.android.getme.Listeners.TrackRideListener;
import com.android.getme.Models.DriverProfileResult;
import com.android.getme.R;
import com.android.getme.ViewModels.CustRideViewModel;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class TrackRideActivity extends AppCompatActivity implements TrackRideListener {
    private TextView trackRideRideStatusTextView;
    private TextView trackRideArrivalTextView;

    private TextView trackRideDriverNameTextView;
    private TextView trackDriverRatingTextView;
    private TextView trackRideMakeTextView;
    private TextView trackDriverColorTextView;
    private TextView trackRideLicenseTextView;
    private TextView trackRideSeatsTextView;
    private TextView trackRidePickupNameTextView;
    private TextView trackRideDropoffNameTextView;
    private TextView trackRideBaseFareTextView;
    private TextView trackRideDistanceTextView;
    private TextView trackRideTotalCostTextView;
    private ImageView trackRidePaymentImgView;
    private ImageView trackRideVehicleImgView;
    private TextView trackRidePaymentTextView;
    private Button trackRideCancelBtn;
    private CustRideViewModel mViewModel;

    private String driverName;
    private String driverLicense;
    private int driverRating;
    private String driverMake;
    private String baseFare;
    private double distance;
    private String total;

    private final double driverLat = 10.740897;
    private final double driverLng = 106.695322;

    private OkHttpClient client;
    private WebSocket webSocket;
    final private String BASEURL = "http://10.0.2.2:8000";
    final private String WSURL = "ws://10.0.2.2:8000/ws";
    private NotificationManager manager;
    final private int notificationId = 101;

    private Handler ArrivalHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            sendNotification();
            loadAnimatedMap();
        }
    };

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if(o.getResultCode() == RESULT_OK) {
                        Intent intent = new Intent();
                        intent.putExtra("status", "Completed");
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_ride);

        mViewModel = new ViewModelProvider(this).get(CustRideViewModel.class);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();

        populateViewModel();

        initializeViewComponents();

        fetchDriver();

        setPickupAndDropoff();

        setPrices();

        trackRideCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCancelNotification();
                Intent intent = new Intent();
                intent.putExtra("status", "Cancelled");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        loadAnimatedMap();

        openWebSocket();
    }

    private void createChannel() {
        String id = getPackageName();
        String name = ActivityCompat.getString(this, R.string.channel_name);
        String desc = ActivityCompat.getString(this, R.string.channel_desc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(desc);
        channel.enableVibration(true);
        manager.createNotificationChannel(channel);
    }

    private void sendNotification() {
        String channelId = getPackageName();
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("Driver has arrived")
                .setContentText("Driver has arrived and is waiting for you at pickup")
                .setSmallIcon(R.drawable.ic_getme_logo)
                .setChannelId(channelId)
                .build();

        manager.notify(notificationId, notification);
    }

    private void sendCancelNotification() {
        String channelId = getPackageName();

        Notification notification =
                new Notification.Builder(this, channelId)
                        .setContentTitle("Ride Cancelled")
                        .setContentText("Your Ride has been cancelled.")
                        .setSmallIcon(R.drawable.ic_getme_logo)
                        .setChannelId(channelId)
                        .build();

        manager.notify(notificationId, notification);
    }

    private void loadAnimatedMap() {
        if (mViewModel.status.equals("Waiting")) {
            trackRideRideStatusTextView.setText("Driver is on the way");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.trackRideFragContainer,
                            AnimatedMapFragment.newInstance(driverLat, driverLng, mViewModel.pickup.getLatitude(), mViewModel.pickup.getLongitude()))
                    .commit();
        } else if (mViewModel.status.equals("In Transit")) {
            trackRideCancelBtn.setVisibility(View.GONE);
            trackRideRideStatusTextView.setText("You are going to destination");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.trackRideFragContainer,
                            AnimatedMapFragment.newInstance(mViewModel.pickup.getLatitude(),
                                    mViewModel.pickup.getLongitude(), mViewModel.dropoff.getLatitude(),
                                    mViewModel.dropoff.getLongitude()))
                    .commit();
        }
    }

    private void setPrices() {
        switch (mViewModel.vehicleType) {
            case "Standard":
                trackRideBaseFareTextView.setText("100,000 VND");
                break;
            case "Economy":
                trackRideBaseFareTextView.setText("80,000 VND");
                break;
            case "Bike":
                trackRideBaseFareTextView.setText("50,000 VND");
                break;
        }

        String distance = String.format("%.2f km", mViewModel.distance);
        trackRideDistanceTextView.setText(distance);
        trackRideTotalCostTextView.setText(mViewModel.amount + " VND");

        if (mViewModel.payment.equals("Cash")) {
            trackRidePaymentImgView.setImageDrawable(ActivityCompat.getDrawable(this, R.drawable.cash));
            trackRidePaymentTextView.setText("Payment via Cash");
        } else {
            trackRidePaymentImgView.setImageDrawable(ActivityCompat.getDrawable(this, R.drawable.credit_card));
            trackRidePaymentTextView.setText("Payment via Card");
        }
    }

    private void setPickupAndDropoff() {
        trackRidePickupNameTextView.setText(mViewModel.locationFrom);
        trackRideDropoffNameTextView.setText(mViewModel.locationTo);
    }

    private void fetchDriver() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BASEURL + "/profile/driver?driverId=" + Integer.toString(mViewModel.driverId);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                DriverProfileResult result = gson.fromJson(s, DriverProfileResult.class);

                driverName = result.fullname;
                driverLicense = result.license;
                driverMake = result.make;

                trackRideDriverNameTextView.setText(result.fullname);
                trackDriverRatingTextView.setText("Rating: 4.0 (3 rides)");
                trackRideMakeTextView.setText(result.make);
                trackDriverColorTextView.setText(result.color);
                trackRideLicenseTextView.setText(result.license);
                if (mViewModel.vehicleType.equals("Standard")) {
                    trackRideSeatsTextView.setText("4");
                    trackRideVehicleImgView.setBackground(ActivityCompat.getDrawable(TrackRideActivity.this, R.drawable.home_ride_otp_std_img_bg));
                    trackRideVehicleImgView.setImageDrawable(ActivityCompat.getDrawable(TrackRideActivity.this, R.drawable.standard_car));
                } else if (mViewModel.vehicleType.equals("Economy")) {
                    trackRideSeatsTextView.setText("4");
                    trackRideVehicleImgView.setBackground(ActivityCompat.getDrawable(TrackRideActivity.this, R.drawable.home_ride_otp_eco_bg));
                    trackRideVehicleImgView.setImageDrawable(ActivityCompat.getDrawable(TrackRideActivity.this, R.drawable.economy_car));
                } else {
                    trackRideSeatsTextView.setText("2");
                    trackRideVehicleImgView.setBackground(ActivityCompat.getDrawable(TrackRideActivity.this, R.drawable.home_ride_otp_bike_bg));
                    trackRideVehicleImgView.setImageDrawable(ActivityCompat.getDrawable(TrackRideActivity.this, R.drawable.bike));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });

        queue.add(request);
    }

    private void initializeViewComponents() {
        trackRideRideStatusTextView = findViewById(R.id.trackRideRideStatusTextView);
        trackRideArrivalTextView = findViewById(R.id.trackRideArrivalTextView);
        trackRideDriverNameTextView = findViewById(R.id.trackRideDriverNameTextView);
        trackDriverRatingTextView = findViewById(R.id.trackDriverRatingTextView);
        trackRideMakeTextView = findViewById(R.id.trackRideMakeTextView);
        trackDriverColorTextView = findViewById(R.id.trackDriverColorTextView);
        trackRideLicenseTextView = findViewById(R.id.trackRideLicenseTextView);
        trackRideSeatsTextView = findViewById(R.id.trackRideSeatsTextView);
        trackRidePickupNameTextView = findViewById(R.id.trackRidePickupNameTextView);
        trackRideDropoffNameTextView = findViewById(R.id.trackRideDropoffNameTextView);
        trackRideBaseFareTextView = findViewById(R.id.trackRideBaseFareTextView);
        trackRideDistanceTextView = findViewById(R.id.trackRideDistanceTextView);
        trackRideTotalCostTextView = findViewById(R.id.trackRideTotalCostTextView);
        trackRidePaymentImgView = findViewById(R.id.trackRidePaymentImgView);
        trackRidePaymentTextView = findViewById(R.id.trackRidePaymentTextView);
        trackRideCancelBtn = findViewById(R.id.trackRideCancelBtn);
        trackRideVehicleImgView = findViewById(R.id.trackDriverVehicleImgView);
    }

    private void populateViewModel() {
        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            double pickupLat = b.getDouble("pickupLat");
            double pickupLng = b.getDouble("pickupLng");
            String locationFrom = b.getString("locationFrom");
            mViewModel.pickup = new GeoPoint(pickupLat, pickupLng);
            mViewModel.locationFrom = locationFrom;

            double dropoffLat = b.getDouble("dropoffLat");
            double dropoffLng = b.getDouble("dropoffLng");
            String locationTo = b.getString("locationTo");
            mViewModel.dropoff = new GeoPoint(dropoffLat, dropoffLng);
            mViewModel.locationTo = locationTo;

            mViewModel.status = b.getString("status");
            mViewModel.vehicleType = b.getString("vehicleType");
            mViewModel.distance = b.getDouble("distance");
            mViewModel.driverId = b.getInt("driverId");
            mViewModel.payment = b.getString("payment");
            mViewModel.amount = b.getInt("amount");
            mViewModel.rideId = b.getInt("rideId");

        }
    }

    @Override
    public void OnArrivalAnimationCompleted() {


    }

    public void openWebSocket() {
        client = new OkHttpClient.Builder().build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(WSURL)
                .build();

        webSocket = client.newWebSocket(request, new TrackRideWsListener());
    }

    @Override
    public void setDuration(int duration) {
        String durationString = "Wait Time : " + formatDuration(duration);
        trackRideArrivalTextView.setText(durationString);
    }

    public String formatDuration(int duration) {
        String timeString = "";

        if(duration >= 3600) {
            int hour = duration / 3600;
            duration = duration % 3600;
            timeString += hour + " hour";
        }
        if(duration >= 60) {
            int minutes = duration / 60;
            duration = duration % 60;
            timeString += minutes + " minutes ";
        }else{
            timeString += duration + " minutes ";
        }
        return timeString;

    }

    private class TrackRideWsListener extends WebSocketListener {
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            if(text.equals("ARRIVED")) {
                mViewModel.status = "In Transit";
                ArrivalHandler.sendEmptyMessage(0);
            }else if(text.equals("COMPLETE")) {

                webSocket.close(1000, "Client initiated close");
                client.dispatcher().executorService().shutdown();
                // show complete activity
                Intent intent = new Intent(TrackRideActivity.this, RideCompleteActivity.class);
                intent.putExtra("rideId", mViewModel.rideId);
                intent.putExtra("name", driverName);
                intent.putExtra("license", driverLicense);
                intent.putExtra("make", driverMake);
                intent.putExtra("fare", trackRideBaseFareTextView.getText().toString());
                intent.putExtra("distance", mViewModel.distance);
                intent.putExtra("total", trackRideTotalCostTextView.getText().toString());
                intent.putExtra("payment", mViewModel.payment);
                        // also include rating
                startForResult.launch(intent);
            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.close(1000, "Activity Destroyed");
        client.dispatcher().executorService().shutdown();
    }
}