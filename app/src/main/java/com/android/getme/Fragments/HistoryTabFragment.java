package com.android.getme.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.getme.Adapters.CustHistoryAdapter;
import com.android.getme.Models.CustRideHistoryResult;
import com.android.getme.Others.DummyData;
import com.android.getme.R;
import com.android.getme.ViewModels.CustHistoryViewModel;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class HistoryTabFragment extends Fragment {
    private int custId;
    final private String BASEURL = "http://10.0.2.2:8000";


    public HistoryTabFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SESSION", MODE_PRIVATE);
        custId = sharedPreferences.getInt("userId", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.history_recycler_view);

        fetchHistory(recyclerView);
    }

    private void fetchHistory(RecyclerView recyclerView) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = BASEURL + "/activity/history?custId=" + custId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        Gson gson = new Gson();
                        CustRideHistoryResult[] results = gson.fromJson(s, CustRideHistoryResult[].class);

                        CustHistoryAdapter adapter = new CustHistoryAdapter(getContext(), Arrays.asList(results));
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                WarningDialogFragment.newInstance("Network Error", "Could not fetch history")
                        .show(getChildFragmentManager(), "Network Warning Fragment");
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String jsonString = new String(
                        response.data,
                        StandardCharsets.UTF_8
                );
                return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(request);
    }
}