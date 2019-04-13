package com.example.traintracker;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.TrainViewHolder> {
    private ArrayList<TrainItem> mRouteList;
    private OnItemClickListener mListener;

    // Called on in another class to set on click functions
    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    // Called on in another class to set on click functions
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    // Viewholder for the RecyclerView
    public static class TrainViewHolder extends RecyclerView.ViewHolder{
        public TextView mName;
        public TextView mDepartureTime;
        public TextView mArrivalTime;

        public TrainViewHolder(View itemView, final OnItemClickListener listener){
            super(itemView);
            mName = itemView.findViewById(R.id.trainName);
            mDepartureTime = itemView.findViewById(R.id.trainDepartureTime);
            mArrivalTime = itemView.findViewById(R.id.trainArrivalTime);

            // When clicking the whole view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public TrainAdapter(ArrayList<TrainItem> routeList){
        mRouteList = routeList;
    }

    //----------------------------------------------------
    // Overrides for the ViewHolder class
    @NonNull
    @Override
    public TrainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.train_item, viewGroup, false);
        TrainViewHolder rvh = new TrainViewHolder(v, mListener);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull TrainViewHolder trainViewHolder, int i) {
        TrainItem currentItem = mRouteList.get(i);
        trainViewHolder.mName.setText(currentItem.getName());
        trainViewHolder.mDepartureTime.setText(currentItem.getDepartureTime());
        trainViewHolder.mArrivalTime.setText(currentItem.getArrivalTime());
    }

    @Override
    public int getItemCount() {
        if (mRouteList != null){
            return mRouteList.size();
        } else {
            return 0;
        }

    }
}
