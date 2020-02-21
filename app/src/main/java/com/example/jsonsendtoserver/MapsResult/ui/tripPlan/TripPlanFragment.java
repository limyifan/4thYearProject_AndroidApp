package com.example.jsonsendtoserver.MapsResult.ui.tripPlan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jsonsendtoserver.R;
import com.example.jsonsendtoserver.Services.HttpHandler;
import com.example.jsonsendtoserver.UserPrefActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TripPlanFragment extends Fragment {
    public RecyclerView tripPlan;
    public TripPlanAdapter tripPlanAdapter;
    public ArrayList<HashMap<String, String>> latLngPlot;
    public ProgressDialog pDialog;
    public static String TAG = TripPlanFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        Log.d("TAG", "NEW INTENT");
        latLngPlot = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("result");

    }
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trip, container, false);
        tripPlan = root.findViewById(R.id.tripView);

        new ParseJSON(getActivity()).execute();

        return root;
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
            String url = "https://201.team/api/v2/GetTime_1.php/?";
            ArrayList<HashMap<String, String>> result = new ArrayList<>();

            for (int i = 0; i < latLngPlot.size()-1; i++) {
                HashMap<String, String> resultHashMap = latLngPlot.get(i);
                String lng1 = resultHashMap.get("lng");
                String lat1 = resultHashMap.get("lat");

                HashMap<String, String> resultHashMap2 = latLngPlot.get(i+1);
                String lng2 = resultHashMap2.get("lng");
                String lat2 = resultHashMap2.get("lat");


                try {

                    String url2 = url + "originLat=" + lat1 + "&originLng=" + lng1 + "&destinationLat="+lat2 + "&destinationLng="+lng2;
                    String jsonString = handler.makeServiceCall(url2);

                    JSONObject jsonObj = new JSONObject(jsonString);
                    JSONObject candidates = jsonObj.getJSONObject("TravelObject");

                    latLngPlot.get(i).put("timeTravel",candidates.getString("travelTime"));
                    Log.d(TAG,"size : "+ latLngPlot.get(i).get("name")+ " "+ latLngPlot.get(i).get("timeTravel"));

                    Log.d(TAG, "doInBackground: "+url2);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> result) { //running in uithread, only runinbackground runs in background
            super.onPostExecute(result);

            Log.d("TAG", "onPost" + result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();


            tripPlan.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
            tripPlan.setHasFixedSize(true);
            tripPlanAdapter = new TripPlanAdapter(getContext(), latLngPlot);
            tripPlan.setAdapter(tripPlanAdapter);
            // do we need to notify the RecyclerView that the list has been updated?
            tripPlanAdapter.notifyDataSetChanged();
        }
    }


}