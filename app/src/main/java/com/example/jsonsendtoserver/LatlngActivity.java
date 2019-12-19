package com.example.jsonsendtoserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
public class LatlngActivity extends AppCompatActivity {
    private FusedLocationProviderClient client;
    private String TAG = LatlngActivity.class.getSimpleName();
     NetworkCall networkCall;
    private static String url = "https://www.201.team/api/placebasic.php/";
    private ProgressDialog pDialog;
    int time;
    String perf;

    int count = 0;

    private String latitude;
    private String longitude;


    ArrayList<HashMap<String, String>> result = new ArrayList<>();
    ArrayList<HashMap<String, String>> resultNew = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latlng);

        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                           latitude= String.valueOf(location.getLatitude());
                            longitude= String.valueOf(location.getLongitude());
                            Log.d(TAG,"latitude"+latitude+""); Log.d(TAG,"latitude"+longitude+"");

                        }
                    }
                });
        networkCall = new NetworkCall();
        final TextView lat = findViewById(R.id.lat);
        final TextView lng =  findViewById(R.id.lng);

        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);

        Button food = findViewById(R.id.food);
        Button museum = findViewById(R.id.museum);

        Button goButton = (Button) findViewById(R.id.goButton);

        food.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                perf = "food";
            }
        });

        museum.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                perf = "museum";
            }
        });

        goButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                lat.setText(latitude);
                lng.setText(longitude);

                Log.d("TAG", "before execution"+resultNew.toString());
                try {
                    resultNew = new ParseJSON().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "after execution"+resultNew.toString());

                Intent intent = new Intent(LatlngActivity.this, MapsDisplay.class);
                intent.putExtra("result",(Serializable) resultNew);
                Log.d("TAG", "RESULT IS"+resultNew.toString());
                startActivity(intent);
            }

        });
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


            //String jsonString = handler.makeServiceCall(url +"?lat="+ location + "&time=" + time);
            String jsonString = handler.makeServiceCall(url+"?lat="+latitude+"&lng="+longitude+"&pref1="+perf);
            Log.d(TAG, "Response from url: " + jsonString);
            if (jsonString != null){
                try {
                    JSONObject jsonObj = new JSONObject(jsonString);
                    JSONArray candidates = jsonObj.getJSONArray("PlaceObject");
                    for (int i = 0; i < candidates.length(); i++)
                    {
                        HashMap<String, String> candidate = new HashMap<>();

                        JSONObject c = candidates.getJSONObject(i);
                        String lat = c.getString("latitude");
                        String lng = c.getString("longitude");
                        String name = c.getString("name");
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
