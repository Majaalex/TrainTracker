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
        TrainNotification trainNotification = new TrainNotification(getApplicationContext(), trainNum, startLocShortCode);
        trainNotification.execute();
        jobFinished(params, false);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;

    }
}
