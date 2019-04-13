package com.example.traintracker;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TrainTracker extends AsyncTask<Integer, LatLng, String> {
    private static final String TAG = "TrainTracker";
    private WeakReference<MapActivity> mapActivityWeakReference;
    private Boolean trainRunning;

    //
    TrainTracker(MapActivity activity){
        mapActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected String doInBackground(Integer... integers) {
        trainRunning = true;
        while (trainRunning) {
            LatLng coords = fetchTrainCoordinates();
            if (coords != null){
                publishProgress(coords);
            } else {
                trainRunning = false;
                return "The train is not running.";
            }
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "The train is no longer running.";
    }
    @Override
    protected void onProgressUpdate(LatLng... values) {
        super.onProgressUpdate(values);
        updateMarker(values[0]);
    }

    private void updateMarker(LatLng latLng){
        MapActivity activity = mapActivityWeakReference.get();
        if (activity == null  || activity.isFinishing()){
            return;
        }
        activity.trainMO = new MarkerOptions();
        activity.trainMO.position(latLng)
                .title(activity.trainNum)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        if (activity.trainMarker != null){
            activity.trainMarker.remove();
        }
        activity.trainMarker = activity.gMap.addMarker(activity.trainMO);
        activity.gMap.moveCamera(CameraUpdateFactory.newLatLng(activity.trainMarker.getPosition()));
    }

    private LatLng fetchTrainCoordinates() {
        MapActivity activity = mapActivityWeakReference.get();
        if (activity == null  || activity.isFinishing()){
            return null;
        }
        String url = "https://rata.digitraffic.fi/api/v1/train-locations/latest/" + activity.trainNum;
        String jResponse = "";
        LatLng position;
        try {
            Log.d(TAG, "fetchTrainCoordinates: url: " + url);
            jResponse = HTTPRequest(url);
            if (!jResponse.equals("")){
                JSONArray jArr = new JSONArray(jResponse);
                double v2 = (double) jArr.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").get(0);
                double v1 = (double) jArr.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").get(1);
                position = new LatLng(v1, v2);
                return position;
            } else {
                Log.d(TAG, "fetchTrainCoordinates: no jResponse: " + jResponse);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String HTTPRequest(String... strings){
        String response = "";
        try
        {
            System.out.println("Making an API call");
            URL url = new URL(strings[0]);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String nextLine;

            while ((nextLine = reader.readLine()) != null)
            {
                response += nextLine;
            }

            return response;
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            return response;
        }
    }


}
