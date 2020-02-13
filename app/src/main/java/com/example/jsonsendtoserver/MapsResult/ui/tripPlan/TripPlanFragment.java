package com.example.jsonsendtoserver.MapsResult.ui.tripPlan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jsonsendtoserver.R;

import java.util.ArrayList;
import java.util.HashMap;

public class TripPlanFragment extends Fragment {
    public RecyclerView tripPlan;
    public TripPlanAdapter tripPlanAdapter;
    public ArrayList<HashMap<String, String>> latLngPlot;

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
        tripPlan.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        tripPlan.setHasFixedSize(true);
        tripPlanAdapter = new TripPlanAdapter(getContext(), latLngPlot);
        tripPlan.setAdapter(tripPlanAdapter);
        // do we need to notify the RecyclerView that the list has been updated?
        tripPlanAdapter.notifyDataSetChanged();
        return root;
    }
}