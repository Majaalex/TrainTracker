package com.example.traintracker;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.traintracker.MainActivity.extraDepLoc;
import static com.example.traintracker.MainActivity.extraDepTime;
import static com.example.traintracker.MainActivity.extraNum;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private static final int JobId = 2000;
    // Google map vars
    private MapView mapView;
    private String gKey;
    GoogleMap gMap;
    Marker trainMarker;
    MarkerOptions trainMO;
    String trainNum;
    String trainDepTime;
    private TextView mTrainName;
    private Boolean notifierRunning = false;

    private Button mButtonNotify;
    private Button mButtonReturn;
    private Button mButtonStopTrack;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        buildMapView(savedInstanceState);
        buildViews();
        getTrain();
        startTrainMarker();
    }

    private void buildViews() {
        mTrainName = findViewById(R.id.mapTrainName);
        mButtonReturn = findViewById(R.id.mapReturn);
        mButtonNotify = findViewById(R.id.mapTrack);
        mButtonStopTrack = findViewById(R.id.mapRemoveTrack);
        mButtonStopTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTrackerJob();
            }
        });
        mButtonNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleTrainJob();
            }
        });
        mButtonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }
        });
    }

    // Cancel the JobService notifying about train departures
    private void cancelTrackerJob(){
        if (notifierRunning){
            notifierRunning = false;
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancelAll();
            //scheduler.cancel(JobId);
        }

    }

    // Start the JobService to notify about the train departures
    private void scheduleTrainJob() {
        if (!notifierRunning){
            notifierRunning = true;
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString(extraNum, trainNum);
            bundle.putString(extraDepLoc, getIntent().getStringExtra(extraDepLoc));
            bundle.putString(extraDepTime, getIntent().getStringExtra(extraDepTime));
            ComponentName trainComponent = new ComponentName(this, TrainJobService.class);
            JobInfo info = new JobInfo.Builder(JobId, trainComponent)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(15 * 60 * 1000)
                    .setExtras(bundle)
                    .build();

            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if(resultCode == JobScheduler.RESULT_SUCCESS){
                Log.d(TAG, "Job Scheduled");
            } else {
                Log.d(TAG, "Job scheduling failed");
                notifierRunning = false;
            }
        }
    }

    // Start AsyncTask for tracking the trains position
    private void startTrainMarker() {
        TrainTracker tt = new TrainTracker(this);
        tt.execute();
    }

    // Extract the train number and departure time from the intent
    private void getTrain() {
        trainNum = getIntent().getStringExtra(extraNum);
        trainDepTime = getIntent().getStringExtra(extraDepTime);
        mTrainName.setText(trainNum);
    }

    /*-----------------------------------------
    Google map methods
    Build the map, and override the methods necessary
     */
    private void buildMapView(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setMinZoomPreference(8);

        LatLng hki = new LatLng(60.1699, 24.9384);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(hki));
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
