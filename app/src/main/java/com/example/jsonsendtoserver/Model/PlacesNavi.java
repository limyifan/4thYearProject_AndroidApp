package com.example.jsonsendtoserver.Model;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

public class PlacesNavi {
    String distance;
    String time;
    List<List<HashMap<String,String>>> navilines;
    PolylineOptions polylineOptions;

    public PlacesNavi(String distance, String time, List<List<HashMap<String, String>>> navilines) {
        this.distance = distance;
        this.time = time;
        this.navilines = navilines;
    }

    public PlacesNavi(String distance, String time, PolylineOptions polylineOptions) {
        this.distance = distance;
        this.time = time;
        this.polylineOptions = polylineOptions;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<List<HashMap<String, String>>> getNavilines() {
        return navilines;
    }

    public void setNavilines(List<List<HashMap<String, String>>> navilines) {
        this.navilines = navilines;
    }

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public void setPolylineOptions(PolylineOptions polylineOptions) {
        this.polylineOptions = polylineOptions;
    }

    @Override
    public String toString() {
        return "PlacesNavi{" +
                "distance='" + distance + '\'' +
                ", time='" + time + '\'' +
                ", navilines=" + navilines +
                '}';
    }

}
