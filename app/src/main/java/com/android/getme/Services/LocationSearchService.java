package com.android.getme.Services;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationSearchService extends Service {

    private final IBinder myBinder = new LocationServiceBinder();
    private String theAddress;

    public LocationSearchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public String getCurrentLocationAddress(String userAgent, double lat, double lng) {

        GeocoderNominatim geocoderNominatim = new GeocoderNominatim(userAgent);
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Bundle bundle = msg.getData();
                theAddress = bundle.getString("theAddress");
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String theAddress = null;
                try {
                    List<Address> addresses = geocoderNominatim.getFromLocation(lat, lng, 1);
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
        return theAddress;
    }

    public class LocationServiceBinder extends Binder {
        public LocationSearchService getService() {
            return LocationSearchService.this;
        }
    }
}