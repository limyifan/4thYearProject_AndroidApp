package com.example.jsonsendtoserver;

import androidx.fragment.app.FragmentActivity;

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
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private ProgressDialog pDialog;

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
