package com.example.jsonsendtoserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.example.jsonsendtoserver.MapsResult.MapsResultActivity;
import com.example.jsonsendtoserver.Services.HttpHandler;
import com.example.jsonsendtoserver.Services.NetworkCall;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class LatlngActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private String TAG = LatlngActivity.class.getSimpleName();
     NetworkCall networkCall;
    private static String url = "https://201.team/api/randomroute/getroute.php/";
    private ProgressDialog pDialog;
    String perf;
    TextView lat;
    TextView lng ;
    public Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    private String latitude;
    private String longitude;

    ArrayList<HashMap<String, String>> result = new ArrayList<>();
    ArrayList<HashMap<String, String>> resultNew = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latlng);

        networkCall = new NetworkCall();
        lat = findViewById(R.id.lat);
         lng =  findViewById(R.id.lng);

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
        Button food = findViewById(R.id.food);
        Button museum = findViewById(R.id.museum);

        Button goButton = (Button) findViewById(R.id.goButton);

        food.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                perf = "Food";
            }
        });

        museum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                perf = "Museum";
            }
        });

        goButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("TAG", "before execution"+resultNew.toString());
                try {
                    resultNew = new ParseJSON().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "after execution"+resultNew.toString());

                Intent intent = new Intent(LatlngActivity.this, MapsResultActivity.class);
                intent.putExtra("result",(Serializable) resultNew);
                Log.d("TAG", "RESULT IS"+ resultNew.toString());
                startActivity(intent);
            }

        });
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

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Log.d(TAG,"You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
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
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {

            latitude= String.valueOf(location.getLatitude());
                           longitude= String.valueOf(location.getLongitude());
                            lat.setText(latitude);
                           lng.setText(longitude);
                      Log.d(TAG,"latitude"+latitude+"");
                           Log.d(TAG,"latitude"+longitude+"");
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
                &&  ActivityCompat.checkSelfPermission(this,
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
            latitude= String.valueOf(location.getLatitude());
            longitude= String.valueOf(location.getLongitude());
            lat.setText(latitude);
            lng.setText(longitude);
            Log.d(TAG,"latitude"+latitude+"");
            Log.d(TAG,"latitude"+longitude+"");}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(LatlngActivity.this).
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


    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    protected class ParseJSON extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute(); //before background task is run
            // Showing progress dialog
            pDialog = new ProgressDialog(LatlngActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            String jsonString = handler.makeServiceCall(url+"?lat="+latitude+"&lng="+longitude+"&pref1="+perf);
            Log.d(TAG, "Response from url: " + url+"?lat="+latitude+"&lng="+longitude+"&pref1="+perf);
            Log.d(TAG, "Response from url: " + jsonString);
            if (jsonString != null){
                try {
                    int count = 0;
                    JSONObject jsonObj = new JSONObject(jsonString);
                    JSONArray candidates = jsonObj.getJSONArray("PlaceObject");
                    for (int i = 0; i < candidates.length(); i++)
                    {
                        HashMap<String, String> candidate = new HashMap<>();

                        JSONObject c = candidates.getJSONObject(i);
                        String lat = c.getString("latitude");
                        String lng = c.getString("longitude");
                        String name = c.getString("place_name");
                        String countToString = Integer.toString(count);

                        candidate.put("name", name);
                        candidate.put("lat", lat);
                        candidate.put("lng", lng);
                        candidate.put("count", countToString);
                        count++;

                        result.add(candidate);
                    }
                }
                catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                            Log.d(TAG,"TEST: "+e.getMessage());
                        }
                    });

                }
            }
            return result;
        }
        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>>result) { //running in uithread, only runinbackground runs in background
            super.onPostExecute(result);
            Log.d("TAG","onPost"+result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Toast.makeText(LatlngActivity.this,"Data Passed",Toast.LENGTH_LONG).show();
        }
    }
}
