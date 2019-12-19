package com.example.jsonsendtoserver;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.jsonsendtoserver.Services.DataParser;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsDisplay extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsDisplay.class.getSimpleName();
    private GoogleMap mMap;
    private ArrayList<LatLng> latLngs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Intent intent = getIntent();
        Log.d("TAG", "NEW INTENT");
        ArrayList<HashMap<String, String>> latLngPlot = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("result");
        String log = latLngPlot.toString();
        Log.d("TAG", "LATLNGPLOT IS" +log);


        for (int i = 0; i < latLngPlot.size(); i++) {

            HashMap<String, String> resultHashMap = latLngPlot.get(i);

            String name = resultHashMap.get("name");
            String lng = resultHashMap.get("lng");
            String lat = resultHashMap.get("lat");

            Double latDouble = Double.parseDouble(lat);
            Double lngDouble = Double.parseDouble(lng);

            LatLng location = new LatLng(latDouble, lngDouble);

            mMap.addMarker(new MarkerOptions().position(location).title("Marker in "+name));
            mMap.animateCamera((CameraUpdateFactory.newLatLng(location)));
            mMap.setBuildingsEnabled(true);
            mMap.setIndoorEnabled(true);

            latLngs.add(location);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
                return true;
            }
        });
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

//        PolylineOptions lineOptions = new PolylineOptions().addAll(latLngs).color(Color.BLUE).width(5);
//
//        if(lineOptions != null) {
//            mMap.addPolyline(lineOptions);
//        }

        String origin = latLngs.get(0).latitude+","+latLngs.get(0).longitude;
        String destination = latLngs.get(1).latitude+","+latLngs.get(1).longitude;

        new FetchUrl().execute("https://maps.googleapis.com/maps/api/directions/json?origin="+origin+"&destination="+destination+"&avoid=highways&mode=bicycling&key=AIzaSyC-Qr_9Y10nFQMNzNtmOnuBf6QY3AuFCiw");

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
// For storing data from web service
            String data = "";
            try {
// Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
// Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
// Connecting to url
            urlConnection.connect();
// Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());
// Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());
            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
// Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
// Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
// Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
// Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
                Log.d("onPostExecute","onPostExecute lineoptions decoded");
            }
// Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
