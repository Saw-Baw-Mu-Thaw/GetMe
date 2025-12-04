package com.android.getme.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Fragments.NotificationFragment;
import com.android.getme.Models.NotiResult;
import com.android.getme.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private NotiResult[] notifications;
    private Context context;

    public NotificationAdapter(Context context, NotiResult[] notifications) {
        this.notifications = notifications;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.noti_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotiResult noti = notifications[position];

        holder.notiTitle.setText(noti.title);
        holder.notiContent.setText(noti.content);

        switch (noti.title) {
            case("Ride Completed"):
                holder.notiLinlay.setBackground(ActivityCompat.getDrawable(context, R.drawable.bg_lightblue_rect));
                holder.notiImg.setImageDrawable(ActivityCompat.getDrawable(context, R.drawable.standard_car));
                holder.notiImg.setBackground(ActivityCompat.getDrawable(context, R.drawable.blue_circle_bg));
                break;
            case("Ride Accepted"):
                holder.notiLinlay.setBackground(ActivityCompat.getDrawable(context, R.drawable.bg_lightgreen_rect));
                holder.notiImg.setImageDrawable(ActivityCompat.getDrawable(context, R.drawable.check_circle_24px));
                holder.notiImg.setBackground(ActivityCompat.getDrawable(context, R.drawable.green_circle_bg));
                break;
            case("Driver Arrived"):
                holder.notiLinlay.setBackground(ActivityCompat.getDrawable(context, R.drawable.bg_purple_rect));
                holder.notiImg.setImageDrawable(ActivityCompat.getDrawable(context, R.drawable.location));
                holder.notiImg.setBackground(ActivityCompat.getDrawable(context, R.drawable.purple_circle_bg));
                break;
            case("Ride Cancelled"):
                holder.notiLinlay.setBackground(ActivityCompat.getDrawable(context, R.drawable.bg_red_rectangle));
                holder.notiImg.setImageDrawable(ActivityCompat.getDrawable(context, R.drawable.cancel_24px));
                holder.notiImg.setBackground(ActivityCompat.getDrawable(context, R.drawable.red_circle_bg));
                break;
        }

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(noti.time, formatter1);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("LLL d, hh:mm a");
        String date = localDateTime.format(formatter2);

        holder.notiTime.setText(date);
    }

    @Override
    public int getItemCount() {
        return notifications != null ? notifications.length : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView notiTitle;
        TextView notiContent;
        LinearLayout notiLinlay;
        TextView notiTime;
        ImageView notiImg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            notiLinlay = itemView.findViewById(R.id.notiLinlay);
            notiImg = itemView.findViewById(R.id.notiImgView);
            notiTitle = itemView.findViewById(R.id.notiTitle);
            notiTime = itemView.findViewById(R.id.notiTime);
            notiContent = itemView.findViewById(R.id.notiContent);
        }
    }
}
