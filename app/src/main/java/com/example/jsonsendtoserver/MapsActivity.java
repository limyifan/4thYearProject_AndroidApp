package com.example.jsonsendtoserver;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.jsonsendtoserver.Services.HttpHandler;
import com.example.jsonsendtoserver.Services.NetworkCall;
import com.google.android.gms.maps.GoogleMap;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;



public class MapsActivity extends AppCompatActivity {

    private String TAG = MapsActivity.class.getSimpleName();
    NetworkCall networkCall;
    private static String url = "https://www.201.team/tripit-http.php/";
    private ProgressDialog pDialog;
    private GoogleMap mMap;     
    ArrayList<HashMap<String, String>> candidateList;
    private ListView listView;
    String location;
    int time;

    ArrayList<HashMap<String, String>> result = new ArrayList<>();
   static ArrayList<HashMap<String, String>> resultNew = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.activity_maps);

        networkCall = new NetworkCall();

        Button goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText locationText = (EditText) findViewById(R.id.editText2);
                EditText timeNum = (EditText) findViewById(R.id.editText3);
                location = locationText.getText().toString();
                candidateList = new ArrayList<>();
                listView = (ListView) findViewById(R.id.list);

                time = Integer.parseInt(timeNum.getText().toString());
           //     new SendDeviceDetails().execute("https://www.201.team/tripit-http.php/?location=" + location + "&time=" + time);
                Log.d("TAG", "before execution"+resultNew.toString());
                try {
                  resultNew = new ParseJSON().execute().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "after execution"+resultNew.toString());

           Intent intent = new Intent(MapsActivity.this, MapsDisplay.class);
          intent.putExtra("result",(Serializable) resultNew);
          Log.d("TAG", "RESULT IS"+resultNew.toString());
          startActivity(intent);
            }

        });
    }

    private class SendDeviceDetails extends AsyncTask<String, Void, String> {

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
            new ParseJSON().execute();
        }
    }

    protected class ParseJSON extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute(); //before background task is run
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Void... arg0) {
            HttpHandler handler = new HttpHandler();

            HashMap<String, String> candidate = new HashMap<>();

            //String jsonString = handler.makeServiceCall(url +"?lat="+ location + "&time=" + time);
            String jsonString = handler.makeServiceCall(url+"?lat=53.9967788&lng=-6.4042121&time="+time);
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
            Log.d("TAG","onPost"+result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            ListAdapter adapter = new SimpleAdapter( //join together UI with DATA
                    MapsActivity.this, //reference to this activity
                    result,                              // list of Map objects| data
                    R.layout.list_item,                       // view layout (XML)
                    new String[]{"name", "lat", "lng"},  // key values used in the map //     match up key values from
                    new int[]{R.id.name, R.id.lat, R.id.lng});  // name of target TextViews   source data to name of fields //puts into view called "name, email"
            // in list
            listView.setAdapter(adapter);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
