package com.android.getme.Activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class RideCompleteActivity extends AppCompatActivity {

    private TextView rideCmpDriverNameTxt;
    private TextView rideCmpDriverRatingTxt;
    private TextView rideCmpDriverMakeTxt;
    private TextView rideCmpDriverLicenseTxt;
    private TextView rideCmpBaseFareTxt;
    private TextView rideCmpDistanceTxt;
    private TextView rideCmpTotalCostTxt;
    private TextView rideCmpPaymentTxt;
    private ImageView rideCmpPaymentImg;
    private RatingBar rideCmpRatingBar;
    private Button rideCmpSubmitRatingBtn;
    private Button bookAnotherRideBtn;
    private int rideId;

    final private String ratingUrl = "http://10.0.2.2:8000/rating";

    private NotificationManager manager;
    final private int notificationId = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_complete);

        initializeComponents();

        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            rideId = b.getInt("rideId");
            rideCmpDriverNameTxt.setText(b.getString("name"));
            rideCmpDriverLicenseTxt.setText(b.getString("license"));
            rideCmpDriverMakeTxt.setText(b.getString("make"));
            rideCmpBaseFareTxt.setText(b.getString("fare"));
            String distance = b.getDouble("distance") + " km";
            rideCmpDistanceTxt.setText(distance);
            rideCmpTotalCostTxt.setText(b.getString("total"));
            String payment = b.getString("payment");
            if (payment.equals("Cash")) {
                rideCmpPaymentImg.setImageDrawable(ActivityCompat.getDrawable(this, R.drawable.cash));
                rideCmpPaymentTxt.setText("Paid with Cash");
            } else {
                rideCmpPaymentImg.setImageDrawable(ActivityCompat.getDrawable(this, R.drawable.credit_card));
                rideCmpPaymentTxt.setText("Paid with Card");
            }
        }

        rideCmpSubmitRatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(RideCompleteActivity.this);

                JSONObject body = new JSONObject();
                try {
                    body.put("rideId", rideId);
                    body.put("value", rideCmpRatingBar.getRating());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ratingUrl, body,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                Toast.makeText(RideCompleteActivity.this, "Rating Submitted", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                volleyError.printStackTrace();
                            }
                        });

                queue.add(request);
            }
        });

        bookAnotherRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotiChannel();

        sendNotification();
    }

    private void createNotiChannel() {
        String channelId = getPackageName();
        String name = ActivityCompat.getString(this, R.string.channel_name);
        String desc = ActivityCompat.getString(this, R.string.channel_desc);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(desc);
        channel.enableVibration(true);

        manager.createNotificationChannel(channel);
    }

    private void sendNotification() {
        String channelId = getPackageName();
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("Ride Completed Successfully")
                .setContentText("You have arrived at your destination. We hope to see you again")
                .setChannelId(channelId)
                .setSmallIcon(R.drawable.ic_getme_logo)
                .build();

        manager.notify(notificationId, notification);
    }

    private void initializeComponents() {
        rideCmpDriverNameTxt = findViewById(R.id.rideCompleteDriverName);
        rideCmpDriverRatingTxt = findViewById(R.id.rideCompleteDriverRating);
        rideCmpDriverMakeTxt = findViewById(R.id.rideCompleteDriverMake);
        rideCmpDriverLicenseTxt = findViewById(R.id.rideCompleteDriverLicense);
        rideCmpBaseFareTxt = findViewById(R.id.rideCompleteBaseFareTextView);
        rideCmpDistanceTxt = findViewById(R.id.rideCompleteDistanceTextView);
        rideCmpTotalCostTxt = findViewById(R.id.rideCompleteTotalCostTextView);
        rideCmpPaymentTxt = findViewById(R.id.rideCompletePaymentTextView);
        rideCmpPaymentImg = findViewById(R.id.rideCompletePaymentImgView);
        rideCmpRatingBar = findViewById(R.id.rideCompleteRatingBar);
        rideCmpSubmitRatingBtn = findViewById(R.id.submitRatingBtn);
        bookAnotherRideBtn = findViewById(R.id.bookAnotherRideBtn);
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        finish();
        super.onDestroy();
    }
}