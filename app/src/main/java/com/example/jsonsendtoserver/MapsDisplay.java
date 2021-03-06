package com.example.jsonsendtoserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.app.ProgressDialog;
import android.graphics.Color;

import com.example.jsonsendtoserver.Services.DataParser;
import com.example.jsonsendtoserver.Services.NetworkCall;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class MapsDisplay extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Location mLastLocation;
    Marker mCurrLocationMarker;
    private static final String TAG = MapsDisplay.class.getSimpleName();
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;
    Boolean setMarkerBegin = false;
    int markerClickedCount = 0;
    boolean doubleBackToExitPressedOnce = false;
    ArrayList<HashMap<String, String>> latLngPlot;
    LatLng location;
    int timeTaken = 0;
    Boolean skipButtonClicked = false;
    int skipButtonClickedCount = 0;
    Button skipButton;
    EditText numBox;
    SupportMapFragment mapFrag;
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_maps_result);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        /*TextView origin = findViewById(R.id.origin);
        TextView destination = findViewById(R.id.destination);
        TextView distance = findViewById(R.id.distance);
        TextView distanceVal = findViewById(R.id.distanceVal);*/
        Button skipButton = findViewById(R.id.floatingActionButton);

        permissionsToRequest = permissionsToRequest(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        new LoopMarker().execute();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        Log.d("TAG", "NEW INTENT");

        latLngPlot = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("result");
        String log = latLngPlot.toString();
        Log.d("TAG", "LATLNGPLOT IS" + log);

        for (int i = 0; i < latLngPlot.size(); i++) {

            HashMap<String, String> resultHashMap = latLngPlot.get(i);

            String place_id = resultHashMap.get("place_id");
            String name = resultHashMap.get("name");
            String lng = resultHashMap.get("lng");
            String lat = resultHashMap.get("lat");
            String countToString = resultHashMap.get("count");
            Log.d("TAG", "LAT IS" + lat);
            Double latDouble = Double.parseDouble(lat);
            Double lngDouble = Double.parseDouble(lng);
            int count = Integer.parseInt(countToString);

            LatLng location = new LatLng(latDouble, lngDouble);

            Log.e(TAG, name);


            mMap.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();
            mMap.addMarker(new MarkerOptions().position(location).title("Marker in " + name));

            mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));

            mMap.setBuildingsEnabled(true);
            mMap.setIndoorEnabled(true);

            latLngs.add(location);
        }

        for(int i = 0; i < latLngPlot.size(); i++) {
            HashMap<String, String> hashMapToDisplay = latLngPlot.get(i);

        }

        // fits all markers on the screen


       // nextButton = (Button) findViewById(R.id.nextButton);
        numBox = (EditText) findViewById(R.id.numBox);
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


                    Log.e(TAG, name);
                mMap.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();

                    skipButtonClickedCount++;
                    skipButtonClicked = true;

                LatLng location = new LatLng(latDouble, lngDouble);

                skipButtonClickedCount++;
                skipButtonClicked = true;


                skipButton.setVisibility(View.INVISIBLE);

                swapButtonAndInput(skipButton, numBox);

                numBox.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {

                            swapButtonAndInput(skipButton, numBox);
                            skipButton.setVisibility(View.VISIBLE);
                            return true;
                        }
                        return false;
                    }
                });
                mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                marker.showInfoWindow();
                if (markerClickedCount == 0) {
                    setMarkerBegin = true;
                }
                if (doubleBackToExitPressedOnce) {
                    Log.d("DOUBLECLICK", "DOUBLE CLICK PRESSED");
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
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }


    public void swapButtonAndInput(Button b, EditText e) {
        float buttonPosX = b.getX();
        float buttonPosY = b.getY();
        float numPosX = e.getX();
        float numPosY = e.getY();

        e.setX(buttonPosX);
        e.setY(buttonPosY);
        b.setX(numPosX);
        b.setY(numPosY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public class SendDeviceDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) { //what you want done in the thread

                NetworkCall networkCall = new NetworkCall();
                return networkCall.makeServiceCall(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // receiving response code from server

        }
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

//        if (location != null) {
//
//            latitude= String.valueOf(location.getLatitude());
//            longitude= String.valueOf(location.getLongitude());
//
//            Log.d(TAG,"latitude"+latitude+"");
//            Log.d(TAG,"latitude"+longitude+"");
//            LatLng mylocation = new LatLng( Double.parseDouble(latitude), Double.parseDouble(longitude));
//            Log.d("TAG", "ur location" + latitude);
//            mMap.addMarker(new MarkerOptions().position(mylocation).title("You are Here"));
//        }

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

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        Log.d(TAG, "location changed" + latLng + "");

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

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
                            new AlertDialog.Builder(MapsDisplay.this).
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                timeTaken = data.getIntExtra("result", 0);

                Log.d("DOUBLECLICK", "LINE BEFORE TIMETAKEN");
                //timeTaken = Integer.parseInt(result);
                Log.d("TAG", "Time Taken is" + timeTaken);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("TAG", "NO RESULT");
            }
        }

    }


    private class LoopMarker extends AsyncTask<Void, Void, ArrayList<PolylineOptions>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapsDisplay.this);
            pDialog.setIndeterminate(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage("Please wait...");
            pDialog.show();
            pDialog.dismiss();
        }

        @Override
        protected ArrayList<PolylineOptions> doInBackground(Void... stgr) {
            ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();

            for (int i = 0; i < latLngs.size() - 1; i++) {

                String origin = latLngs.get(i).latitude + "," + latLngs.get(i).longitude;
                String destination = latLngs.get(i + 1).latitude + "," + latLngs.get(i + 1).longitude;
              //  String name = latLngPlot.get(i).
                polylineOptions.add(addMarker("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&avoid=highways&mode=bicycling&key=AIzaSyC-Qr_9Y10nFQMNzNtmOnuBf6QY3AuFCiw"));

            }
            return polylineOptions;
        }

        @Override
        protected void onPostExecute(ArrayList<PolylineOptions> result) {
            super.onPostExecute(result);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    mMap.addPolyline(result.get(i));
                }
            }
        }
    }

    public PolylineOptions addMarker(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            String data = client.newCall(request).execute().body().string();
            return parserTaskToPolyLine(data);

        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return null;
    }

    private PolylineOptions parserTaskToPolyLine(String jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        PolylineOptions lineOptions = new PolylineOptions();
        try {
            jObject = new JSONObject(jsonData);
            DataParser parser = new DataParser();
            Log.d("parserTaskToPolyLine", parser.toString());

            routes = parser.parse(jObject);

            ArrayList<LatLng> points;
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                List<HashMap<String, String>> path = routes.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }
        } catch (Exception e) {
            Log.e("parserTaskToPolyLine", e.toString());
            e.printStackTrace();
        }

        Log.d("onPostExecute", "lineOptions result zone: " + lineOptions.getPoints().toString());
        return lineOptions;

    }
}

