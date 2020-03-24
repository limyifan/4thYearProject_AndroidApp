package com.example.jsonsendtoserver.Navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jsonsendtoserver.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class NavigateActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ArrayList<HashMap<String, String>> latLngPlot;
    private GoogleMap mMap;
    private TextView originPlaceName, destinationPlaceName, nextPlaceName, timeEstFinish, placeDistance;
    private static final String TAG = NavigateActivity.class.getSimpleName();

    Boolean skipButtonClicked = false, nextButtonClicked = false;
    int skipButtonClickedCount = 0, nextButtonClickedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        latLngPlot = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("result2");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        originPlaceName = findViewById(R.id.textTitlePlace);
        destinationPlaceName = findViewById(R.id.textDestination);
        nextPlaceName = findViewById(R.id.nextPlaceName);
        timeEstFinish = findViewById(R.id.timeEstFinish);
        placeDistance = findViewById(R.id.placeDistance);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Button skipButton = findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipButtonClicked = true;
                String origin, name, lng, lat, orgLng, orgLat, countToString;
                Double latDouble, lngDouble, orgLatDouble, orgLngDouble;
                int count;
                LatLng location, orginLocation;
                HashMap<String, String> originHashMap, resultHashMap;
                int latLngPlotSize = latLngPlot.size();
                if (skipButtonClickedCount == latLngPlotSize - 1) {
                    skipButtonClickedCount = 0;
                }

                Log.d("BUTTON CLICKED", skipButtonClickedCount + "times");
               // resultHashMap = latLngPlot.get(skipButtonClickedCount);
                if(skipButtonClickedCount == 0) {
                    originHashMap = latLngPlot.get(0);
                    resultHashMap = latLngPlot.get(skipButtonClickedCount + 1);

                    origin = originHashMap.get("name");
                    name = resultHashMap.get("name");

                    orgLng = originHashMap.get("lng");
                    orgLat = originHashMap.get("lat");

                    lng = resultHashMap.get("lng");
                    lat = resultHashMap.get("lat");
                    countToString = resultHashMap.get("count");

                    latDouble = Double.parseDouble(lat);
                    lngDouble = Double.parseDouble(lng);
                    orgLatDouble = Double.parseDouble(orgLat);
                    orgLngDouble = Double.parseDouble(orgLng);

                    count = Integer.parseInt(countToString);

                    location = new LatLng(latDouble, lngDouble);
                    orginLocation = new LatLng(orgLatDouble, orgLngDouble);

                    mMap.addMarker(new MarkerOptions().position(orginLocation).title(origin)).showInfoWindow();
                    mMap.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();
                    setRouteInfo(name, origin);
                    skipButtonClickedCount++;
                    skipButtonClicked = true;

                    mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                    Log.d(TAG, "Skip Button is onclick: " + name);
                }
                else{
                    skipButtonClickedCount++;
                    resultHashMap = latLngPlot.get(skipButtonClickedCount);
                    originHashMap = latLngPlot.get(skipButtonClickedCount - 1);
                   origin = originHashMap.get("name");
                    name = resultHashMap.get("name");
                    lng = resultHashMap.get("lng");
                    lat = resultHashMap.get("lat");
                    countToString = resultHashMap.get("count");
                    latDouble = Double.parseDouble(lat);
                    lngDouble = Double.parseDouble(lng);
                    count = Integer.parseInt(countToString);

                    location = new LatLng(latDouble, lngDouble);

                    mMap.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();
                    setRouteInfo(name, origin);
                   // skipButtonClickedCount++;
                    skipButtonClicked = true;

                    mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                    Log.d(TAG, "Skip Button is onclick: " + name);
                }

            }
        });

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public void setRouteInfo(String name, String origin)
    {
        originPlaceName.setText(origin);
        destinationPlaceName.setText(name);
    };
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }
}

