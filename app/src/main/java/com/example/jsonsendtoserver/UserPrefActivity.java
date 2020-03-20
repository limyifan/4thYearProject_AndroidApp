package com.example.jsonsendtoserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.jsonsendtoserver.MapsResult.MapsResultActivity;
import com.example.jsonsendtoserver.Services.HttpHandler;
import com.example.jsonsendtoserver.Services.NetworkCall;

import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class UserPrefActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private String TAG = UserPrefActivity.class.getSimpleName();
    NetworkCall networkCall;
    private static String url = "https://201.team/api/v2/GetRoute.php/";
    private ProgressDialog pDialog;
    public Location location;
    private GoogleApiClient googleApiClient;
    String place_id;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    private String latitude, longitude, time;
    private ArrayList<String> pref = new ArrayList<>();
    String[] listItems;
    boolean[] checkedItems;
    ArrayList<Integer> mUserItems = new ArrayList<>();

    int count = 0;
    private ArrayList<HashMap<String, String>> resultNew = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latlng);

        networkCall = new NetworkCall();

        final ChipGroup prefSelectList = findViewById(R.id.tvItemSelected);
        SeekBar seekBar = findViewById(R.id.timeSeekbar);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        final TextView textView = findViewById(R.id.timeDisplay);

        setSupportActionBar(myToolbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                textView.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(String.valueOf(progress));
                time = String.valueOf(progress * 60);
            }
        });

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

        Button goButton = (Button) findViewById(R.id.goButton);
        Button prefSelectButton = (Button) findViewById(R.id.prefSelect);

        listItems = getResources().getStringArray(R.array.peferences_choice);
        checkedItems = new boolean[listItems.length];

        prefSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserPrefActivity.this);
                mBuilder.setTitle(R.string.pref_selection);
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {

                        count += isChecked ? 1 : -1;
                        checkedItems[position] = isChecked;

                        if (isChecked) {
                            if (count > 3) {
                                Toast.makeText(getApplicationContext(), "Not more than 3 preferences", Toast.LENGTH_LONG).show();
                                checkedItems[position] = false;
                                count--;
                                ((AlertDialog) dialogInterface).getListView().setItemChecked(position, false);
                            } else {
                                mUserItems.add(position);
                            }
                        } else {
                            mUserItems.remove((Integer.valueOf(position)));
                            if (count > 3) {
                                Toast.makeText(getApplicationContext(), "Not more than 3 preferences", Toast.LENGTH_LONG).show();
                                checkedItems[position] = false;
                                count--;
                                ((AlertDialog) dialogInterface).getListView().setItemChecked(position, false);
                            }
                        }
                    }
                });
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        pref.clear();
                        prefSelectList.removeAllViews();

                        for (int i = 0; i < mUserItems.size(); i++) {
                            String item = listItems[mUserItems.get(i)];
                            final Chip entryChip = getChip(prefSelectList, item);
                            prefSelectList.addView(entryChip);
                            pref.add(item);
                        }
                    }
                });

                mBuilder.setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setNeutralButton(R.string.clear_all_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                            mUserItems.clear();
                            prefSelectList.removeAllViews();
                            pref.clear();
                            count = 0;
                        }
                    }
                });

                final AlertDialog mDialog = mBuilder.create();

                mDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        mDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        mDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                });

                mDialog.show();

            }
        });

        goButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                resultNew.clear();
                new ParseJSON(UserPrefActivity.this).execute();
                Log.d("TAG", "after execution" + resultNew.toString());

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
            Log.d(TAG, "You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
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
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {

            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Log.d(TAG, "latitude" + latitude + "");
            Log.d(TAG, "longitude" + longitude + "");
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
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Log.d(TAG, "latitude" + latitude + "");
            Log.d(TAG, "latitude" + longitude + "");
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
                            new AlertDialog.Builder(UserPrefActivity.this).
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

    protected class ParseJSON extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {

        private Context mContext;

        ParseJSON(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute(); //before background task is run
            // Showing progress dialog
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();
            String jsonString = null, prefString = "";
            ArrayList<HashMap<String, String>> result = new ArrayList<>();

            if (pref.size() != 0) {
                for (int i = 0; i < pref.size(); i++) {
                    int j = 1 + i;
                    prefString += "&pref" + j + "=" + pref.get(i);

                }
                latitude = "54.009";
                longitude = "-6.4049";
                jsonString = handler.makeServiceCall(url + "?lat=" + latitude + "&lng=" + longitude+ prefString +"&time="+time);
                Log.d(TAG, "Response from url: " + url + "?lat=" + latitude + "&lng=" + longitude  + prefString + "&time="+time);
            }


            if (jsonString != null) {
                try {
                    int count = 0;
                    JSONObject jsonObj = new JSONObject(jsonString);
                    JSONArray candidates = jsonObj.getJSONArray("PlaceObject");

                    HashMap<String, String> current = new HashMap<>();

                   // current.put("place_id", place_id);
                    current.put("name", "Current Location");
                    current.put("lat", latitude);
                    current.put("lng", longitude);
                    current.put("count", "0");
                    current.put("rating", "No Rating");
                    current.put("place_type","");
                    current.put("img", "No Photos Provided");
                    current.put("average_time", "N/A");

                    resultNew.add(current);

                    for (int i = 0; i < candidates.length(); i++) {
                        if(i%2 == 0) {
                            HashMap<String, String> candidate = new HashMap<>();

                            JSONObject c = candidates.getJSONObject(i);
                            String place_id = c.getString("place_id");
                            String lat = c.getString("latitude");
                            String lng = c.getString("longitude");
                            String name = c.getString("place_name");
                            String img = c.getString("cover_image");
                            String rating = c.getString("rating");
                            String placeType = c.getString("place_type");
                            String average_time = c.getString("average_time");
                            String countToString = Integer.toString(count);

                            candidate.put("place_id", place_id);
                            candidate.put("name", name);
                            candidate.put("lat", lat);
                            candidate.put("lng", lng);
                            candidate.put("img", img);
                            candidate.put("rating", rating);
                            candidate.put("place_type", placeType);
                            candidate.put("count", countToString);
                            candidate.put("average_time", average_time);
                            count++;
                            Log.d(TAG, "Place id is: " + place_id);
                            resultNew.add(candidate);
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                            Log.d(TAG, "TEST: " + e.getMessage());
                        }
                    });

                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) { //running in uithread, only runinbackground runs in background
            super.onPostExecute(result);
            Log.d("TAG", "onPost" + resultNew);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (resultNew.size()<=1) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(UserPrefActivity.this);
                builder.setMessage(R.string.dialog_no_suggestion)
                        .setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                final AlertDialog dialog = builder.create();

                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    }
                });

                dialog.show();
            }else {

                Toast.makeText(UserPrefActivity.this, "Data Passed", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(mContext, MapsResultActivity.class);
                intent.putExtra("result", (Serializable) resultNew);
                Log.d("TAG", "RESULT IS" + resultNew.toString());
                startActivity(intent);
            }
        }
    }

    private Chip getChip(final ChipGroup entryChipGroup, final String text) {
        final Chip chip = new Chip(this);
        chip.setChipDrawable(ChipDrawable.createFromResource(this, R.xml.chip));
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        chip.setText(text);
        chip.setCheckedIconVisible(false);
        chip.setCheckable(false);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entryChipGroup.removeView(chip);
                pref.remove(text);

            }
        });
        return chip;

    }
}
