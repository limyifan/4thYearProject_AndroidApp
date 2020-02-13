package com.example.jsonsendtoserver.MapsResult.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.graphics.Color;

import com.example.jsonsendtoserver.R;
import com.example.jsonsendtoserver.Services.DataParser;
import com.example.jsonsendtoserver.Services.NetworkCall;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

import android.widget.Toast;

import org.json.JSONObject;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Location mLastLocation;
    Marker mCurrLocationMarker;
    private static final String TAG = MapsFragment.class.getSimpleName();
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
    ArrayList<HashMap<String, String>> latLngPlot;

    Boolean nextButtonClicked = false;
    int nextButtonClickedCount = 0;
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private EditText numBox;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_maps, container, false);

         SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
         if (supportMapFragment == null) {
             FragmentManager fm = getFragmentManager();
             FragmentTransaction ft = fm.beginTransaction();
             supportMapFragment = SupportMapFragment.newInstance();
             ft.replace(R.id.map,supportMapFragment).commit();
         }

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }
        SupportMapFragment mapFrag = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(getActivity()).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        return root;
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
            return getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getActivity().getIntent();
        Log.d("TAG", "NEW INTENT");
        latLngPlot = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("result");
        String log = latLngPlot.toString();
        Log.d("TAG", "LATLNGPLOT IS" + log);

//        int length = latLngPlot.size() - 1 > 5 ? 5 : latLngPlot.size() - 1;
//        for (int i = 0; i < length; i++) {
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

            if(i==0) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(location);
                markerOptions.title(name);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mMap.addMarker(markerOptions);
            }
            else {
                mMap.addMarker(new MarkerOptions().position(location).title("Marker in " + name));
            }

            if (count == 0) {
                mMap.setBuildingsEnabled(true);
                mMap.setIndoorEnabled(true);
            }

            mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
            mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 13)));

            mMap.setBuildingsEnabled(true);
            mMap.setIndoorEnabled(true);

            latLngs.add(location);

        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                marker.showInfoWindow();
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                showAlertDialogButtonClicked();
            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);

        new LoopMarker().execute();
    }

    public void showAlertDialogButtonClicked() {
        // create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Time Suggestion");
        final View customLayout = getLayoutInflater().inflate(R.layout.alertdialog, null);
        builder.setView(customLayout);
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 numBox =  customLayout.findViewById(R.id.numBox);
                nextButtonClicked = true;

                if (nextButtonClickedCount == latLngPlot.size()) {
                    nextButtonClickedCount = 0;
                }

                Log.d("BUTTON CLICKED", nextButtonClickedCount + "times");
                HashMap<String, String> resultHashMap = latLngPlot.get(nextButtonClickedCount);

                String name = resultHashMap.get("name");
                String lng = resultHashMap.get("lng");
                String lat = resultHashMap.get("lat");
                String countToString = resultHashMap.get("count");

                Double latDouble = Double.parseDouble(lat);
                Double lngDouble = Double.parseDouble(lng);

                LatLng location = new LatLng(latDouble, lngDouble);

                numBox.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            int timeTaken = Integer.parseInt(numBox.getText().toString());
                            SendDeviceDetails("https://201.team/time.php/?timespent=" + timeTaken);

                            return true;
                        }
                        return false;
                    }
                });
                mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onStart() {
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

    public String SendDeviceDetails(String url) {
            NetworkCall networkCall = new NetworkCall();
            return networkCall.makeServiceCall(url);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.getContext(), "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
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
                            new AlertDialog.Builder(MapsFragment.this.getContext()).
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

    private class LoopMarker extends AsyncTask<Void, Void, ArrayList<PolylineOptions>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<PolylineOptions> doInBackground(Void... stgr) {
            ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();

            int length = latLngs.size() - 1 > 5 ? 5 : latLngs.size() - 1;
            for (int i = 0; i < length; i++) {
                String origin = latLngs.get(i).latitude + "," + latLngs.get(i).longitude;
                String destination = latLngs.get(i + 1).latitude + "," + latLngs.get(i + 1).longitude;
                if (i==0) {
                    polylineOptions.add(addMarker("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&avoid=highways&mode=bicycling&key=AIzaSyC-Qr_9Y10nFQMNzNtmOnuBf6QY3AuFCiw").color(Color.BLUE));
                }
                else {
                    polylineOptions.add(addMarker("https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&avoid=highways&mode=bicycling&key=AIzaSyC-Qr_9Y10nFQMNzNtmOnuBf6QY3AuFCiw").color(Color.GRAY));
                 }
            }
            return polylineOptions;
        }

        @Override
        protected void onPostExecute(ArrayList<PolylineOptions> result) {
            super.onPostExecute(result);

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
            Log.e("Background Task", e.toString());
        }
        return null;
    }

    private PolylineOptions parserTaskToPolyLine(String jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        PolylineOptions lineOptions = new PolylineOptions();

        Log.d(TAG,"jsonData " +jsonData);
        try {
            jObject = new JSONObject(jsonData);
            DataParser parser = new DataParser();

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
            }
        } catch (Exception e) {
            Log.e("parserTaskToPolyLine", e.toString());
            e.printStackTrace();
        }

        Log.d("onPostExecute", "lineOptions result zone: " + lineOptions.getPoints().toString());
        return lineOptions;

    }
}

