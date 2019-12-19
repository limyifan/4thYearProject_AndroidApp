package com.example.jsonsendtoserver;

import androidx.fragment.app.FragmentActivity;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.example.jsonsendtoserver.Services.DataParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class MapsDisplay extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = MapsDisplay.class.getSimpleName();
    private GoogleMap mMap;

    Boolean setMarkerBegin = false;
    int markerClickedCount = 0;
    boolean doubleBackToExitPressedOnce = false;
    static final int GET_TIME_REQUEST = 1;
    ArrayList<HashMap<String, String>> latLngPlot;


    int timeTaken = 0;
    Boolean nextButtonClicked = false;
    int nextButtonClickedCount = 0;
    Button nextButton;
    EditText numBox;

    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
    //    getFragmentManager().findFragmentById(R.id.map).getView().setLayoutParams();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        Intent intent = getIntent();
        Log.d("TAG", "NEW INTENT");
        latLngPlot = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("result");
        String log = latLngPlot.toString();
        Log.d("TAG", "LATLNGPLOT IS" + log);
        final Boolean nextClicked = false;


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
            mMap.addMarker(new MarkerOptions().position(location).title("Marker in " + name));
            if (count == 0) {
                mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
                mMap.animateCamera((CameraUpdateFactory.newLatLngZoom(location, 15)));
                mMap.setBuildingsEnabled(true);
                mMap.setIndoorEnabled(true);
            }

            mMap.addMarker(new MarkerOptions().position(location).title("Marker in "+name));
            mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
            mMap.setBuildingsEnabled(true);
            mMap.setIndoorEnabled(true);

            latLngs.add(location);

        }


       nextButton = (Button) findViewById(R.id.nextButton);
       numBox = (EditText) findViewById(R.id.numBox);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButtonClicked = true;

                if (nextButtonClickedCount == latLngPlot.size())
                {
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
                    int count = Integer.parseInt(countToString);

                    LatLng location = new LatLng(latDouble, lngDouble);

                    Log.e(TAG, name);


                    nextButtonClickedCount++;
                    nextButtonClicked = true;

                nextButton.setVisibility(View.INVISIBLE);

                swapButtonAndInput(nextButton, numBox);

                numBox.setOnKeyListener(new View.OnKeyListener(){
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            timeTaken = Integer.parseInt(numBox.getText().toString());
                            new SendDeviceDetails().execute("https://201.team/time.php/?timespent="+timeTaken);

                            swapButtonAndInput(nextButton, numBox);

                            nextButton.setVisibility(View.VISIBLE);


                       //     Toast.makeText(HelloFormStuff.this, edittext.getText(), Toast.LENGTH_SHORT).show();
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
                return true;
            }
        });

//        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
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
    }

    public void hideButton()
    {


    }
    // mMap.setMyLocationEnabled(true);

    /*public void hideButton(Button b, R id)
    {
        b = (Button) findViewById(id);
    }*/
    public void swapButtonAndInput(Button b, EditText e)
    {
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
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.

    }

    @Override
    public void onResume() {
        super.onResume();
    }
    public class SendDeviceDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) { //what you want done in the thread

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                //make httpurlconnection object by casting url object to it. This is where the connection opens to url.
                httpURLConnection.setRequestMethod("POST");    // post request

                httpURLConnection.setDoOutput(true);          // connection outputs - true

                Log.d("test", params[0]);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                //make dataoutputstream using getOutputStream, we are going to write to it
                // getOutputStream opens a stream with intention to write to server
                wr.writeBytes("request="); // + params[1]
                //sending data
                wr.flush();
                wr.close(); //outputStream is closed, not connection

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                //read data from stream, from server

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) { //adding each character from input to data string
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
            Log.e("TAG", result); // receiving response code from server

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                timeTaken = data.getIntExtra("result",0);

                Log.d("DOUBLECLICK", "LINE BEFORE TIMETAKEN");
                //timeTaken = Integer.parseInt(result);
                Log.d("TAG", "Time Taken is"+timeTaken);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("TAG", "NO RESULT");
            }
        }

        new LoopMarker().execute();
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }


    private class LoopMarker extends AsyncTask<Void, Void, ArrayList<PolylineOptions>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsDisplay.this);
            pDialog.setIndeterminate(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage("Please wait...");
            pDialog.show();
        }

        @Override
        protected ArrayList<PolylineOptions> doInBackground(Void... stgr) {
            ArrayList<PolylineOptions> polylineOptions = new ArrayList<>();

            for (int i = 0; i <latLngs.size()-1 ; i++) {

                String origin = latLngs.get(i).latitude+","+latLngs.get(i).longitude;
                String destination = latLngs.get(i+1).latitude+","+latLngs.get(i+1).longitude;

                polylineOptions.add(addMarker("https://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+destination+"&avoid=highways&mode=bicycling&key=AIzaSyC-Qr_9Y10nFQMNzNtmOnuBf6QY3AuFCiw"));

            }
            return polylineOptions;
        }
        @Override
        protected void onPostExecute( ArrayList<PolylineOptions> result) {
            super.onPostExecute(result);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if(result != null) {
                for (int i = 0; i < result.size(); i++) {
                    mMap.addPolyline(result.get(i));
                }
            }
        }
    }

    public PolylineOptions addMarker(String url){
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                String data= client.newCall(request).execute().body().string();
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
                Log.d("parserTaskToPolyLine",jsonData);
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
                Log.e("parserTaskToPolyLine",e.toString());
                e.printStackTrace();
            }

        Log.d("onPostExecute","lineOptions result zone: "+lineOptions.getPoints().toString());
        return lineOptions;

    }
}

