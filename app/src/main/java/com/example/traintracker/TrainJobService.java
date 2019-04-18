package com.example.traintracker;

import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import static com.example.traintracker.App.CHANNEL_1_ID;
import static com.example.traintracker.MainActivity.extraDepLoc;
import static com.example.traintracker.MainActivity.extraDepTime;
import static com.example.traintracker.MainActivity.extraNum;

public class TrainJobService extends JobService {
    public static final String TAG = "TrainJobService";
    private boolean jobCancelled = false;
    private NotificationManagerCompat notificationManager;

    private String trainNum;
    private String startLocShortCode;
    private String deptime;

    @Override
    public boolean onStartJob(JobParameters params) {
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params){
        Log.d(TAG, "job started");
        trainNum = params.getExtras().getString(extraNum);
        startLocShortCode = params.getExtras().getString(extraDepLoc);
        deptime = params.getExtras().getString(extraDepTime);
        TrainNotification trainNotification = new TrainNotification(getApplicationContext(), trainNum, startLocShortCode, deptime);
        try {
            compareTimes(trainNotification.execute().get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jobFinished(params, true);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;

    }
    private void notifyTrainDelayed(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train " + trainNum + " has been delayed.")
                .setContentText("The train is delayed by at least 15 minutes and will depart at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void compareTimes(ZonedDateTime actualDepartureTime) {
        Log.d(TAG, "compareTimes: " + actualDepartureTime);
        String hhmmTime;
        if (actualDepartureTime != null){
            ZonedDateTime thirtyUntilDeparture = actualDepartureTime.minus(40, ChronoUnit.MINUTES);
            ZonedDateTime tenUntilDeparture = actualDepartureTime.minus(15, ChronoUnit.MINUTES);
            hhmmTime = DateTimeFormatter.ofPattern("HH:mm").format(actualDepartureTime);
            if (thirtyUntilDeparture.compareTo(ZonedDateTime.now()) < 0){ // If there is less than 30 minutes until departure
                Log.d(TAG, "apiCallAndNotification: depart in under 30");
                if (tenUntilDeparture.compareTo(ZonedDateTime.now()) < 0){ // If the train is departing in under 10 minutes
                    Log.d(TAG, "apiCallAndNotification: depart in under 10");
                    // Notify that there is under 10 minutes left
                    notifyTrainDeprtingInSub15(hhmmTime);
                } else {
                    // Notify that the train is departing in under 30 minutes
                    notifyTrainDepartingIn35(hhmmTime);
                }
            } else {
                // Notify that there is an hour left
                notifyTrainDepartingInAnHour(hhmmTime);
            }
        }
    }

    private void notifyTrainDepartingInAnHour(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in about 60 minutes.")
                .setContentText("Train " + trainNum + " will depart in about 60 minutes, at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDepartingIn35(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in about 35 minutes.")
                .setContentText("Train " + trainNum + " will depart in about 35 minutes, at " + departureTime  + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDeprtingInSub15(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in less than 15 minutes.")
                .setContentText("Train " + trainNum + " will depart in under 15 minutes, at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }
}
