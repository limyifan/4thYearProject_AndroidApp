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
    private TextView nextPlaceName, timeEstFinish, placeDistance;
    private static final String TAG = NavigateActivity.class.getSimpleName();

    Boolean skipButtonClicked = false, nextButtonClicked = false;
    int skipButtonClickedCount = 0,  nextButtonClickedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        latLngPlot = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("result2");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

                if (skipButtonClickedCount == latLngPlot.size()) {
                    skipButtonClickedCount = 0;
                }

                Log.d("BUTTON CLICKED", skipButtonClickedCount + "times");
                HashMap<String, String> resultHashMap = latLngPlot.get(skipButtonClickedCount);

                String name = resultHashMap.get("name");
                String lng = resultHashMap.get("lng");
                String lat = resultHashMap.get("lat");
                String countToString = resultHashMap.get("count");


                Double latDouble = Double.parseDouble(lat);
                Double lngDouble = Double.parseDouble(lng);
                int count = Integer.parseInt(countToString);
                LatLng location = new LatLng(latDouble, lngDouble);

                mMap.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();

                skipButtonClickedCount++;
                skipButtonClicked = true;

                mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
                Log.d(TAG, "Skip Button is onclick: "+name);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
