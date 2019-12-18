package com.example.jsonsendtoserver;

import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;

public class MapsDisplay extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsDisplay.class.getSimpleName();
    private GoogleMap mMap;
    Boolean setMarkerBegin = false;
    int totalTime = 0;
    int averageTime = 0;
    int markerClickedCount = 0;
    boolean doubleBackToExitPressedOnce = false;
    static final int GET_TIME_REQUEST = 1;
    String result = "";
    int timeTaken = 0;


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
        Log.d("TAG", "LATLNGPLOT IS" + log);
        Boolean nextClicked = false;


        for (int i = 0; i < latLngPlot.size(); i++) {

            HashMap<String, String> resultHashMap = latLngPlot.get(i);

            String name = resultHashMap.get("name");
            String lng = resultHashMap.get("lng");
            String lat = resultHashMap.get("lat");
            String countToString = resultHashMap.get("count");


            Double latDouble = Double.parseDouble(lat);
            Double lngDouble = Double.parseDouble(lng);
            int count = Integer.parseInt(countToString);

            LatLng location = new LatLng(latDouble, lngDouble);

            Log.e(TAG, name);
            mMap.addMarker(new MarkerOptions().position(location).title("Marker in " + name));
            if (count == 0) {
                mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
                mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                mMap.setBuildingsEnabled(true);
                mMap.setIndoorEnabled(true);
            }
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                return true;
            }
        });
//        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                if (markerClickedCount == 0) {
                    setMarkerBegin = true;

                }
                if (doubleBackToExitPressedOnce) {

                    onPause();
                    Intent intent = new Intent(MapsDisplay.this, GetTime.class);

                    startActivityForResult(intent, GET_TIME_REQUEST);
                    timeTaken = Integer.parseInt(result);
                    Log.d("TAG", "Time Taken is"+timeTaken);
                } else {
                    doubleBackToExitPressedOnce = true;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }, 2000);
                }
                markerClickedCount++;

                return true;

            }
        });
    }

    // mMap.setMyLocationEnabled(true);


    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                result = data.getStringExtra("result");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("TAG", "NO RESULT");
            }
        }
    }
}

