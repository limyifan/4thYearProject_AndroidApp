package com.example.jsonsendtoserver;

import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String TAG = MapsActivity.class.getSimpleName();
    private static String url = "https://www.201.team/tripit-http.php/?location=";
    private ProgressDialog pDialog;
    private GoogleMap mMap;
    ArrayList<HashMap<String, String>> candidateList;
    private ListView listView;
    String location;
    int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.activity_maps);

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        editLocation = (EditText) findViewById(R.id.editTextLocation);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //   SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //    mapFragment.getMapAsync(this);
        Button goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText locationText = (EditText) findViewById(R.id.editText2);
                EditText timeNum = (EditText) findViewById(R.id.editText3);
                location = locationText.getText().toString();
                candidateList = new ArrayList<>();
                listView = (ListView) findViewById(R.id.list);

                time = Integer.parseInt(timeNum.getText().toString());
               // new SendDeviceDetails().execute("https://www.201.team/tripit-http.php/?location=" + location + "&time=" + time);
                new ParseJSON().execute();
            }

        });


    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private class SendDeviceDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) { //what you wantn done in the thread

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoOutput(true);

                Log.d("test3", Integer.toString(params.length));
                Log.d("testerror", params[0]);


                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes("request="); // + params[1]
                wr.flush();
                wr.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();

                }
            }

            return data;
        }



        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
            new ParseJSON().execute();
        }
    }
    protected class ParseJSON extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute(); //beore background task is run
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();
            ArrayList<HashMap<String, String>> result = new ArrayList<>();
            HashMap<String, String> candidate = new HashMap<>();

            String jsonString = handler.makeServiceCall(url + location + "&time=" + time);
            jsonString = jsonString.replace("You are in "+location+ ". You have "+time+" minutes."," ");
            Log.d(TAG, "Response from url: " + jsonString);
            if (jsonString != null){
                try {
                    JSONObject jsonObj = new JSONObject(jsonString);

                    JSONArray candidates = jsonObj.getJSONArray("candidates");

                    for (int i = 0; i < candidates.length(); i++)
                    {

                        JSONObject c = candidates.getJSONObject(i);
                            JSONObject geometry = c.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                    String lat = location.getString("lat");
                                    String lng = location.getString("lng");
                                JSONObject viewport = geometry.getJSONObject("viewport");
                                    JSONObject northeast = viewport.getJSONObject("northeast");
                                        double vNELat = northeast.getDouble("lat");
                                        double vNELng = northeast.getDouble("lng");
                                    JSONObject southwest = viewport.getJSONObject("southwest");
                                        double vSWLat = southwest.getDouble("lat");
                                        double vSWLng = southwest.getDouble("lng");
                            String name = c.getString("name");




                        candidate.put("name", name);
                        candidate.put("lat", lat);
                        candidate.put("lng", lng);

                        result.add(candidate);
                    }
                }
             catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                // We cannot write to the UI as we are in a Thread, so in order to display
                // something on the UI Thread, we must create a Runnable
                // and pass it as an argument to the runOnUiThread() method.
                //
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
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter( //join together UI with DATA, and manage for you - Adapter
                    MapsActivity.this, //reference to this activity
                    result,                              // list of Map objects| data
                    R.layout.list_item,                       // view layout (XML)
                    new String[]{"name", "lat", "lng"},  // key values used in the map //     match up key values from
                    new int[]{R.id.name, R.id.lat, R.id.lng});  // name of target TextViews   source data to name of fields //puts into view called "name, email"
            // in list
            listView.setAdapter(adapter);   // connect adapter to ListView                         from map nbeing supplied, there is something called "name"
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // onClickBtn(R.layout.activity_maps);
       /* JSONObject request = new JSONObject();
        try {
            request.put("location", location);
            request.put("time", time);
        } catch (JSONException e) {
            e.printStackTrace();
            request.toString()
        }*/


    }

}
