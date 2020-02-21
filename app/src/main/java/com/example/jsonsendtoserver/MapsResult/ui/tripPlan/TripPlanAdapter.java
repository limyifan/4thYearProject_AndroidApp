package com.example.jsonsendtoserver.MapsResult.ui.tripPlan;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jsonsendtoserver.R;

import java.util.ArrayList;
import java.util.HashMap;


public class TripPlanAdapter extends RecyclerView.Adapter<TripPlanAdapter.ViewHolder> {

    private static String TAG = TripPlanAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<HashMap<String, String>> places;
    private Context context;

    // data is passed into the constructor
    TripPlanAdapter(Context context, ArrayList<HashMap<String, String>> places) {
        this.mInflater = LayoutInflater.from(context);
        this.places = places;
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.tripview_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Log.d(TAG, "Adapter Run: "+ TAG);
        holder.placeName.setText(places.get(position).get("name"));
        Log.d(TAG, "Places element at "+position+"is "+places.get(position));
        if (!places.get(position).get("rating").equals("No Rating")) {
            holder.ratingBar.setRating(Float.valueOf(places.get(position).get("rating")));
        }
        else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        if (!places.get(position).get("img").contains("No Photos Provided")) {
            String img = places.get(position).get("img").replaceAll("\\\\", "");
            Glide.with(context).load(img).into(holder.coverImg);
        }

        String txt = places.get(position).get("timeTravel")+" approx";
        holder.timeTravel.setText(txt);
        holder.placeCategory.setText(places.get(position).get("place_type"));

        Log.d(TAG,"size2 : "+ places.size()+ " "+ places.get(position).get("timeTravel"));


        if(position == places.size()-1 ) {
            holder.view1.setVisibility(View.GONE);
            holder.timeTaken.setVisibility(View.GONE);
            holder.view2.setVisibility(View.GONE);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return places.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView placeName, timeTravel,ratingBar, placeCategory;
        View view1,view2;
        RatingBar ratingBar;
        LinearLayout timeTaken;
        ImageView coverImg;

        ViewHolder(View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.placeName);
            view1 = itemView.findViewById(R.id.view1);
            view2 = itemView.findViewById(R.id.view2);
            timeTaken = itemView.findViewById(R.id.timeTaken);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            coverImg = itemView.findViewById(R.id.coverImg);
            timeTravel = itemView.findViewById(R.id.travelTime);
            placeCategory = itemView.findViewById(R.id.placeCategory);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public void update(ArrayList<HashMap<String, String>> places) {
        this.places.clear();
        this.places.addAll(places);
        notifyDataSetChanged();
    }
}