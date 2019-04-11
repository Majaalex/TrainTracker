package com.example.traintracker;

import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class TrainTracker extends AsyncTask<Integer, Integer, LatLng> {

    MapActivity ma;
    public TrainTracker(MapActivity ma){
        this.ma = ma;
    }

    @Override
    protected LatLng doInBackground(Integer... integers) {
        LatLng position = new LatLng(60,24);
        //TODO: https://rata.digitraffic.fi/api/v1/train-locations/latest/977
        // Make api call with the above +
        ma.updateMarker(position);
        return position;
    }
}
