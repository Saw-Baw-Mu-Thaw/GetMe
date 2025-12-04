package com.android.getme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.getme.Adapters.NotificationAdapter;
import com.android.getme.Models.NotiResult;
import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

public class NotificationFragment extends Fragment {

    private int custId;
    final private String BASEURL = "http://10.0.2.2:8000";

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SESSION", MODE_PRIVATE);

        custId = sharedPreferences.getInt("userId",0);

        RecyclerView recyclerView = view.findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchNotifications(recyclerView);
    }

    private void fetchNotifications(RecyclerView recyclerView) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = BASEURL + "/notification/" + custId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Gson gson = new Gson();
                        NotiResult[] results = gson.fromJson(s, NotiResult[].class);

                        NotificationAdapter adapter = new NotificationAdapter(getContext(), results);
                        recyclerView.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                WarningDialogFragment.newInstance("Network Error", "Could not fetch notifications")
                        .show(getChildFragmentManager(), "Network Warning Dialog");
            }
        });

        queue.add(request);
    }
}