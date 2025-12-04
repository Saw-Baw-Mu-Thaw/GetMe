package com.android.getme.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.getme.Models.CreateRideResult;
import com.android.getme.R;
import com.android.getme.ViewModels.FindDriverViewModel;
import com.android.volley.AuthFailureError;
import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class FindDriverActivity extends AppCompatActivity{

    private String requestTAG = "RideRequestTAG";
    private FindDriverViewModel viewModel;

    private RequestQueue queue;
    private OkHttpClient client;
    private WebSocket webSocket;
    private final String BASEURL = "http://10.0.2.2:8000";
    private final String WSURL = "ws://10.0.2.2:8000/ws";

    private NotificationManager manager;
    final private int notificationId = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();

        populateViewModel();

        TextView findDriverPickupTextView = findViewById(R.id.findDriverPickupTextView);
        TextView findDriverDropoffTextView = findViewById(R.id.findDriverDropoffTextView);
        Button findDriverCancelBtn = findViewById(R.id.findDriverCancelBtn);

        findDriverPickupTextView.setText(viewModel.pickupName);
        findDriverDropoffTextView.setText(viewModel.dropoffName);

        findDriverCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelRide();
            }
        });

        makeRequest();
    }

    private void createNotificationChannel() {
        String id = getPackageName();
        String name = ActivityCompat.getString(this, R.string.channel_name);
        String description = ActivityCompat.getString(this, R.string.channel_desc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel =
                new NotificationChannel(id, name, importance);

        channel.setDescription(description);
        channel.enableVibration(true);
        manager.createNotificationChannel(channel);
    }

    private void cancelRide() {

        sendCancelNotification();

        queue.cancelAll(requestTAG);

        String url = BASEURL + "/ride/cancel";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("rideId", viewModel.rideId);
        }catch(Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Intent intent = new Intent();
                        intent.putExtra("status", "Cancelled");
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Find Driver", "Cancel Ride Error." + volleyError.toString());
            }
        });

        queue.add(request);

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

    private void makeRequest() {
        queue = Volley.newRequestQueue(this);
        String url = BASEURL + "/ride/create";

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("custId", viewModel.custId);
            if(viewModel.vehicleType.equals("Economy") || viewModel.vehicleType.equals("Standard")) {
                jsonObject.put("vehicleType", "Car");
            }else{
                jsonObject.put("vehicleType", "Bike");
            }
            jsonObject.put("amount", viewModel.amount);
            jsonObject.put("payment", viewModel.payment);
            jsonObject.put("LocationFrom", viewModel.pickupName);
            jsonObject.put("pickupLat", String.format("%.3f", viewModel.pickupLocation.getLatitude()));
            jsonObject.put("pickupLong", String.format("%.3f", viewModel.pickupLocation.getLongitude()));
            jsonObject.put("LocationTo", viewModel.dropoffName);
            jsonObject.put("dropoffLat", String.format("%.3f",viewModel.dropoffLocation.getLatitude()));
            jsonObject.put("dropoffLong", String.format("%.3f", viewModel.dropoffLocation.getLongitude()));
            jsonObject.put("distance", String.format("%.1f", viewModel.distance));
            jsonObject.put("duration", viewModel.duration/60);
            jsonObject.put("status", "Finding Driver");
        }catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try{
                            viewModel.rideId = jsonObject.getInt("rideId");

                            openWebSocket();
                        }catch(Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Find Driver", "Ride request encountered an error.");
                volleyError.printStackTrace();
            }
        });

        request.setTag(requestTAG);

        queue.add(request);
    }

    private void openWebSocket() {
        client = new OkHttpClient.Builder().build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(WSURL)
                .build();

        webSocket = client.newWebSocket(request, new MyListener());
    }

    private void returnData() {
        sendNotification();
        Intent intent = new Intent();
        intent.putExtra("rideId", viewModel.rideId);
        intent.putExtra("driverId", viewModel.driverId);
        intent.putExtra("status", "Waiting");
        setResult(RESULT_OK, intent);
        finish();
    }

    private void sendNotification() {
        String channelId = getPackageName();

        Notification notification =
                new Notification.Builder(this, channelId)
                        .setContentTitle("Ride Accepted")
                        .setContentText("Your Driver is on the way")
                        .setSmallIcon(R.drawable.ic_getme_logo)
                        .setChannelId(channelId)
                        .build();

        manager.notify(notificationId, notification);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.close(1000, "Activity Destroyed");
        client.dispatcher().executorService().shutdown();
    }

    private class MyListener extends WebSocketListener {
        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            String[] messages = text.split(",");

            if(messages[0].equals("ACCEPTED")) {
                Log.e("Find Driver", "Ride Accepted. Driver ID : " + messages[0]);
                viewModel.rideId = Integer.parseInt(messages[1]);
                viewModel.driverId = Integer.parseInt(messages[2]);
                webSocket.close(1000, "Client initiated close");
                client.dispatcher().executorService().shutdown();
                returnData();
            }
        }
    }
}