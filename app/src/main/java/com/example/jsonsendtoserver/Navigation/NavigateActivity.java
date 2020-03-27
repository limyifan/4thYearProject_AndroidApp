package com.example.jsonsendtoserver.Navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsonsendtoserver.MapsResult.ui.map.MapsFragment;
import com.example.jsonsendtoserver.R;
import com.example.jsonsendtoserver.UserPrefActivity;
import com.example.jsonsendtoserver.Services.DataParser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class NavigateActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private ArrayList<HashMap<String, String>> latLngPlot;
    private GoogleMap mMap;
    private Marker mMarker;
    private TextView originPlaceName, destinationPlaceName, nextPlaceName, timeEstFinish, placeDistance;
    Button backButton;
    private static final String TAG = NavigateActivity.class.getSimpleName();


    Boolean skipButtonClicked = false, nextButtonClicked = false;
    int skipButtonClickedCount = 0, nextButtonClickedCount = 0, PERMISSION_ID = 44;
    LatLng currentLocation;
    FusedLocationProviderClient mFusedLocationClient;
    private HashMap<String, String> originHashMap;


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
        backButton = findViewById(R.id.backButton);

        nextPlaceName = findViewById(R.id.nextPlaceName);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

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
                String origin, name, lng, lat, orgLng, orgLat;
                Double latDouble, lngDouble, orgLatDouble, orgLngDouble;
                int count;
                String imgString;
                LatLng location, originLocation = null;
                HashMap<String, String> originHashMap, resultHashMap;
                int latLngPlotSize = latLngPlot.size();
                if (skipButtonClickedCount == latLngPlotSize - 1) {
                    skipButtonClickedCount = 1;
                }

                Log.d("BUTTON CLICKED", skipButtonClickedCount + "times");
                //resultHashMap = latLngPlot.get(skipButtonClickedCount);

                originHashMap = latLngPlot.get(0);

                origin = originHashMap.get("name");
                orgLng = originHashMap.get("lng");
                orgLat = originHashMap.get("lat");
                orgLatDouble = Double.parseDouble(orgLat);
                orgLngDouble = Double.parseDouble(orgLng);
                orginLocation = new LatLng(orgLatDouble, orgLngDouble);

                if (skipButtonClickedCount == 0) {
                    resultHashMap = latLngPlot.get(skipButtonClickedCount + 1);

                    name = resultHashMap.get("name");

<<<<<<< Updated upstream
                    orgLng = originHashMap.get("lng");
                    orgLat = originHashMap.get("lat");
                    imgString = resultHashMap.get("img");
=======
>>>>>>> Stashed changes
                    lng = resultHashMap.get("lng");
                    lat = resultHashMap.get("lat");

                    latDouble = Double.parseDouble(lat);
                    lngDouble = Double.parseDouble(lng);

                    location = new LatLng(latDouble, lngDouble);
                    originLocation = new LatLng(orgLatDouble, orgLngDouble);

                    mMap.addMarker(new MarkerOptions().position(originLocation).title(origin)).showInfoWindow();
                    mMap.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();
                    setRouteInfo(name, origin);

                    skipButtonClickedCount++;
                    skipButtonClicked = true;

                    new PolylineDraw().execute(originLocation, location);
                    mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                    Log.d(TAG, "Skip Button is onclick: " + name);
                }
                else{
                    skipButtonClickedCount++;
                    resultHashMap = latLngPlot.get(skipButtonClickedCount);
                    //originHashMap = latLngPlot.get(skipButtonClickedCount - 1);
                    //origin = originHashMap.get("name");
                    name = resultHashMap.get("name");
                    orgLng = originHashMap.get("lng");
                    orgLat = originHashMap.get("lat");
                    imgString = resultHashMap.get("img");
                    lng = resultHashMap.get("lng");
                    lat = resultHashMap.get("lat");

                    latDouble = Double.parseDouble(lat);
                    lngDouble = Double.parseDouble(lng);
                    orgLatDouble = Double.parseDouble(orgLat);
                    orgLngDouble = Double.parseDouble(orgLng);

                    location = new LatLng(latDouble, lngDouble);
                    originLocation = new LatLng(orgLatDouble, orgLngDouble);

                    if(originLocation!= null) {
                        mMap.clear();
                        MarkerOptions options = new MarkerOptions()
                                .position(location)
                                .title(name);
                        mMap.addMarker(new MarkerOptions().position(originLocation).title(origin)).showInfoWindow();
                        mMarker = mMap.addMarker(options);
                        mMarker.showInfoWindow();

                        setRouteInfo(name, origin);

                        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                        Log.d(TAG, "Skip Button is onclick: " + name);

                        new PolylineDraw().execute(originLocation, location);
                    }

                }


              //  mMap.setOnMarkerClickListener(new View.OnClickListener())
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // finish();
                        Intent intent = new Intent(NavigateActivity.this, UserPrefActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });

            }
        });

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });*/
        /*mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View placeInfo = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                TextView title = (TextView) placeInfo.findViewById(R.id.title);
                //  title.setText(marker.getTitle());
                title.setText("test");
                TextView distance = (TextView) placeInfo.findViewById(R.id.distance);
                //distance.setText(marker.getSnippet());

                ImageView markerImage = (ImageView) placeInfo.findViewById(R.id.markerImage);

                return placeInfo;
            }
        });*/


    }


    public void setRouteInfo(String name, String origin) {
        originPlaceName.setText(origin);
        destinationPlaceName.setText(name);
    }

    ;

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            currentLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude", "status");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
      //  mMap.setOnInfoWindowClickListener();
        mMap.setMyLocationEnabled(true);

    }

    private class PolylineDraw extends AsyncTask<LatLng, Void, ArrayList<PolylineOptions>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<PolylineOptions> doInBackground(LatLng... stgr) {
            ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();
            DataParser parser = new DataParser();

            String origin = stgr[0].latitude + "," + stgr[0].longitude;
            String destination = stgr[1].latitude + "," + stgr[1].longitude;

            Log.d(TAG, "polyline: " + parser.addPolyline("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&avoid=highways&mode=walking&key=AIzaSyCCgD7_3jYnOb7sfejC0h79cUlzvVbWzy0"));
            polylineOptions.add(parser.addPolyline("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&avoid=highways&mode=walking&key=AIzaSyCCgD7_3jYnOb7sfejC0h79cUlzvVbWzy0"));

            return polylineOptions;
        }

        @Override
        protected void onPostExecute(ArrayList<PolylineOptions> result) {
            super.onPostExecute(result);

            for (int i = 0; i < result.size(); i++) {
                mMap.addPolyline(result.get(i));
            }
        }
    }

}


