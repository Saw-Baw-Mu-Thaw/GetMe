package com.android.getme.Adapters;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Listeners.PickupSearchListener;
import com.android.getme.Models.GHGeocodeResult;
import com.android.getme.R;

import java.util.ArrayList;
import java.util.List;

public class PickupSearchAdapter extends RecyclerView.Adapter<PickupSearchAdapter.ViewHolder> {

    List<GHGeocodeResult.Hit> searchResults;
    PickupSearchListener listener;

    public PickupSearchAdapter(Context context) {
        super();
        searchResults = new ArrayList<>();
        if (context instanceof PickupSearchListener) {
            listener = (PickupSearchListener) context;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.pickup_search_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GHGeocodeResult.Hit address = searchResults.get(position);

        if(address != null) {
            String name = address.name;
            String addressString = address.housenumber + ", " + address.street + ", " + address.city;

            holder.searchLocationNameTextView.setText(name);
            holder.searchLocationAddressTextView.setText(addressString);
            holder.pickupSearchItemLinlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onPickupSearchItemClicked(name, addressString, address.point.lat, address.point.lng);
                }
            });
        }



    }

    @Override
    public int getItemCount() {
        return searchResults == null ? 0 : searchResults.size();
    }

    public void setSearchResults(List<GHGeocodeResult.Hit> addresses) {
        searchResults = addresses;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout pickupSearchItemLinlay;
        TextView searchLocationNameTextView;
        TextView searchLocationAddressTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pickupSearchItemLinlay = itemView.findViewById(R.id.pickupSearchItemLinlay);
            searchLocationNameTextView = itemView.findViewById(R.id.searchLocationNameTextView);
            searchLocationAddressTextView = itemView.findViewById(R.id.searchLocationAddressTextView);
        }
    }
}
