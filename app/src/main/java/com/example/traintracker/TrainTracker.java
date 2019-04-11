package com.example.traintracker;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class TrainTracker extends AsyncTask<Integer, Integer, Marker> {

    MapActivity ma;
    public TrainTracker(MapActivity ma){
        this.ma = ma;
    }

    @Override
    protected Marker doInBackground(Integer... integers) {
        LatLng position = new LatLng(0,0);
        ma.updateMarker(position);
        return null;
    }
}
