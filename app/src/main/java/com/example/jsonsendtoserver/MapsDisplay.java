package com.example.jsonsendtoserver;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsDisplay extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsDisplay.class.getSimpleName();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Intent intent = getIntent();
        Log.d("TAG", "NEW INTENT");
        ArrayList<HashMap<String, String>> latLngPlot = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("result");
        String log = latLngPlot.toString();
        Log.d("TAG", "LATLNGPLOT IS" +log);
        HashMap<String, String> resultHashMap = latLngPlot.get(latLngPlot.size()-1);

        String lng = resultHashMap.get("lng");
        String lat = resultHashMap.get("lat");

        Double latDouble = Double.parseDouble(lat);
        Double lngDouble = Double.parseDouble(lng);

        LatLng location = new LatLng(latDouble, lngDouble);

        mMap.addMarker(new MarkerOptions().position(location).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }
}
