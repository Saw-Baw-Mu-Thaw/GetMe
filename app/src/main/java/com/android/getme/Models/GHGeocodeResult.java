package com.android.getme.Models;

import java.util.ArrayList;

public class GHGeocodeResult {
    public ArrayList<Hit> hits;
    public int took;

    public class Hit{
        public long osm_id;
        public String osm_type;
        public String country;
        public String osm_key;
        public String city;
        public String osm_value;
        public String postcode;
        public String name;
        public Point point;
        public ArrayList<Double> extent;
        public String street;
        public String housenumber;
    }

    public class Point{
        public double lng;
        public double lat;
    }

}
