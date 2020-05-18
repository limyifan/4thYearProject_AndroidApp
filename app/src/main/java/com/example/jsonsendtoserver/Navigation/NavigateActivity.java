package com.example.jsonsendtoserver.Navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
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

import com.example.jsonsendtoserver.R;
import com.example.jsonsendtoserver.Services.HttpHandler;
import com.example.jsonsendtoserver.UserPrefActivity;
import com.example.jsonsendtoserver.Services.DataParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class NavigateActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public ArrayList<HashMap<String, String>> latLngPlot;
    private GoogleMap mMap;
    private Marker mMarker, markerDestination;
    private TextView originPlaceName, destinationPlaceName, nextPlaceName, timeEstFinish, placeDistance, placeType;
    AppCompatButton backButton;
    private static final String TAG = NavigateActivity.class.getSimpleName();
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds



    Boolean skipButtonClicked = false, nextButtonClicked = false;
    int skipButtonClickedCount = 2, nextButtonClickedCount = 0, PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    private LatLng originLocation;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private ArrayList<Polyline> naviLine = new ArrayList<>();


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
        placeType = findViewById(R.id.placeType);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        timeEstFinish = findViewById(R.id.timeEstFinish);
        placeDistance = findViewById(R.id.placeDistance);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AppCompatButton skipButton = findViewById(R.id.skipButton);

        HashMap<String, String> originHashMap = latLngPlot.get(0);
        final String origin = originHashMap.get("name");

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipButtonClicked = true;
                String  name, lng, lat, orgLng, orgLat, stringPlaceType;
                Double latDouble, lngDouble;
                int count;
                String imgString, travelTime;
                LatLng location;
                HashMap<String, String> resultHashMap;
                int latLngPlotSize = latLngPlot.size();
                if (skipButtonClickedCount == latLngPlotSize - 1) {
                    skipButtonClickedCount = 0;
                }

                Log.d("BUTTON CLICKED", skipButtonClickedCount + "times");

                    skipButtonClickedCount++;
                    resultHashMap = latLngPlot.get(skipButtonClickedCount);
                    //originHashMap = latLngPlot.get(skipButtonClickedCount - 1);
                    //origin = originHashMap.get("name");
                    name = resultHashMap.get("name");
                    imgString = resultHashMap.get("img");
                    stringPlaceType = resultHashMap.get("place_type");
                    lng = resultHashMap.get("lng");
                    lat = resultHashMap.get("lat");
                    travelTime = resultHashMap.get("timeTravel");
                    Log.d("travelObject", "travel time is"+travelTime);

                    latDouble = Double.parseDouble(lat);
                    lngDouble = Double.parseDouble(lng);



                    location = new LatLng(latDouble, lngDouble);

                    if(originLocation!= null) {
                        mMap.clear();

                        mMarker = mMap.addMarker(new MarkerOptions()
                                .position(originLocation)
                                .title("Current Location"));

                        markerDestination = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latDouble,lngDouble))
                                .title(name));

                        mMarker.setPosition(originLocation);

                        markerDestination.setPosition(location);
                        markerDestination.setTitle(name);
                        setRouteInfo(name, origin, stringPlaceType);

                        mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                        Log.d(TAG, "Skip Button is onclick: " + name);

                        new PolylineDraw().execute(originLocation, location);
                    }

              //  mMap.setOnMarkerClickListener(new View.OnClickListener())
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        HashMap<String,String> originMap = latLngPlot.get(0);
        String name = originMap.get("name");

        Double latDouble = Double.parseDouble(originMap.get("lat"));
        Double lngDouble = Double.parseDouble(originMap.get("lng"));

        HashMap<String,String> resultHashMap = latLngPlot.get(1);
        String name1 = resultHashMap.get("name");

        Double latDouble1 = Double.parseDouble(resultHashMap.get("lat"));
        Double lngDouble1 = Double.parseDouble(resultHashMap.get("lng"));

        originLocation = new LatLng(latDouble,lngDouble);
        mMap.setMyLocationEnabled(true);

        mMarker = mMap.addMarker(new MarkerOptions()
                .position(originLocation)
                .title(name));

        markerDestination = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latDouble1,lngDouble1))
                .title(name1));

        mMarker.setPosition(originLocation);
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
         Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            originLocation = new LatLng(location.getLatitude(),location.getLongitude());

        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            originLocation = new LatLng(location.getLatitude(),location.getLongitude());
            //mMarker.setPosition(originLocation);

             if(mMarker != null) {
                 mMarker.setPosition(originLocation);
             }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(NavigateActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    public void getTravelTimes() {
        HttpHandler handler = new HttpHandler();
        String url = "https://201.team/api/v2/GetTime_Client.php/?";
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        int latLngPlotSize = latLngPlot.size();

        for (int i = 0; i < latLngPlot.size()-1; i++) {
            HashMap<String, String> resultHashMap = latLngPlot.get(i);
            String lng1 = resultHashMap.get("lng");
            String lat1 = resultHashMap.get("lat");

            HashMap<String, String> resultHashMap2 = latLngPlot.get(i+1);
            String lng2 = resultHashMap2.get("lng");
            String lat2 = resultHashMap2.get("lat");
            try {

                String url2 = url + "originLat=" + lat1 + "&originLng=" + lng1 + "&destinationLat="+lat2 + "&destinationLng="+lng2;
                String jsonString = handler.makeServiceCall(url2);

                JSONObject jsonObj = new JSONObject(jsonString);
                JSONObject candidates = jsonObj.getJSONObject("TravelObject");

                latLngPlot.get(i).put("timeTravel",candidates.getString("travelTime"));
                Log.d(TAG,"size : "+ latLngPlot.get(i).get("name")+ " "+ latLngPlot.get(i).get("timeTravel"));

                Log.d(TAG, "doInBackground: "+url2);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void setRouteInfo(String name, String origin, String stringPlaceType) {
        originPlaceName.setText(origin);
        destinationPlaceName.setText(name);
        placeType.setText(stringPlaceType);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkPlayServices()) {
            Log.d(TAG, "You need to install Google Play Services to use the App properly");
        }
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

            Log.d(TAG, "naviline size: "+naviLine.size());

            if(naviLine.size() != 0) {
                for (int i = 0; i < result.size(); i++) {
                    naviLine.get(i).remove();
                }
            }

            for (int i = 0; i < result.size(); i++) {
                naviLine.add(mMap.addPolyline(result.get(i)));
            }
        }
    }

}


