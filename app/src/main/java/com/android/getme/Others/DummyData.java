package com.android.getme.Others;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DummyData {

    public static List<RideHistory> getDummyHistory() {
        List<RideHistory> history = new ArrayList<>();
        history.add(new RideHistory("Completed", LocalDateTime.of(2025, 11, 19, 14, 45), "Downtown Plaza Mall", "Central Business District", 8.5, 12.50));
        history.add(new RideHistory("Cancelled", LocalDateTime.of(2024, 12, 15, 11, 30), "University Campus", "City Center Mall", 12.3, 0.00));
        return history;

    }

    public static class RideHistory {
        public String status;
        public LocalDateTime date;
        public String LocationFrom;
        public String LocationTo;
        public double distance;
        public double amount;

        public RideHistory(String status, LocalDateTime date, String locationFrom, String locationTo, double distance, double amount) {
            this.status = status;
            this.date = date;
            LocationFrom = locationFrom;
            LocationTo = locationTo;
            this.distance = distance;
            this.amount = amount;
        }
    }
}
