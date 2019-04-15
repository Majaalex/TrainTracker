package com.example.traintracker;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TrainJobService extends JobService {
    public static final String TAG = "TrainJobService";
    private boolean jobCancelled = false;
    public static final String CHANNEL_ID = "150000";
    private String trainNum;
    private String startLocShortCode;

    @Override
    public boolean onStartJob(JobParameters params) {
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params){
        Log.d(TAG, "job started");
        trainNum = params.getExtras().getString("trainNum");
        startLocShortCode = params.getExtras().getString("departure");
        //TODO: https://rata.digitraffic.fi/api/v1/trains/latest/114
        // Make asynctask/thread to get info from above API
        // format: JSONArray.JSONObject(0).JSONArray("timeTableRows").JSONObject(i).
        // check stationShortCode == startLocShortCode && type : DEPARTURE
        // fetch scheduledTime and liveEstimateTime as format 2019-04-15T13:27:00.000Z

        jobFinished(params, false);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;

    }

    private void notificationTrainDeparture(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train is departing")
                .setContentText("The train is departing at ");
    }
}
