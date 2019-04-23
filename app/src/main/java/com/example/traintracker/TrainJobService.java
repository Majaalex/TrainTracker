package com.example.traintracker;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.time.Instant;
import java.time.ZoneId;
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
    private NotificationManagerCompat notificationManager;
    private static final int delayThreshold = 5;
    private String trainNum;
    private String startLocShortCode;
    private String depTime;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: ");
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params){
        // Extract all the extras
        trainNum = params.getExtras().getString(extraNum);
        startLocShortCode = params.getExtras().getString(extraDepLoc);
        depTime = params.getExtras().getString(extraDepTime);
        TrainTimeTable trainTimeTable = new TrainTimeTable(trainNum, startLocShortCode, depTime);
        try {
            // Compare the times that the trainTimeTable returns
            Log.d(TAG, "doBackgroundWork: second exec");
            ZonedDateTime time = trainTimeTable.execute().get();
            //TODO: Crash here when train is running
            //https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
            //https://medium.com/@arj.sna/android-multiple-asynctasks-78b2f847a2ec
            Log.d(TAG, "doBackgroundWork: " + time.getDayOfMonth() + time.getDayOfWeek());
            compareTimes(time);
            Log.d(TAG, "doBackgroundWork: 2");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        jobFinished(params, false);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;

    }
    private void notifyTrainDelayed(String departureTime){
            Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_train_green)
                    .setShowWhen(true)
                    .setContentTitle("Train has been delayed at least " + delayThreshold + " minutes.")
                    .setContentText("Train " + trainNum + " is delayed by at least " + delayThreshold + " minutes and will depart at " + departureTime + ".")
                    .build();
        notificationManager.notify(150001, builder);
    }

    // Compares the departure time to decide which notification to send out.
    // Will stop the notifications once the departure time has passed.
    private void compareTimes(ZonedDateTime actualDepartureTime) {
        String hhmmTime;
        Log.d(TAG, "compareTimes: ");
        if (actualDepartureTime != null && actualDepartureTime.isAfter(ZonedDateTime.now())){
            // Draw up different times to compare with
            ZonedDateTime timeTableTime = Instant.parse(depTime).atZone(ZoneId.of("Europe/Helsinki"));
            ZonedDateTime timeTableTimeDelayThreshold = timeTableTime.plus(delayThreshold, ChronoUnit.MINUTES);
            ZonedDateTime thirtyUntilDeparture = actualDepartureTime.minus(30, ChronoUnit.MINUTES);
            ZonedDateTime tenUntilDeparture = actualDepartureTime.minus(10, ChronoUnit.MINUTES);
            // HH:mm format of the actual time the train departs
            hhmmTime = DateTimeFormatter.ofPattern("HH:mm").format(actualDepartureTime);
            // Go through a set of if statements to decide how long it is until the train departs
            if (ZonedDateTime.now().isAfter(thirtyUntilDeparture)){ //1 If there is less than 30 minutes until departure
                if (ZonedDateTime.now().isAfter(tenUntilDeparture)){ //2 If the train is departing in under 10 minutes
                    if (timeTableTimeDelayThreshold.isAfter(actualDepartureTime)){//3If there is under 10 minutes left
                        notifyTrainDeprtingInSub10(hhmmTime);
                    } else {
                        notifyTrainDelayed(hhmmTime);
                    }
                } else {
                    //2 Notify that the train is departing in under 30 minutes
                    if (timeTableTimeDelayThreshold.isAfter(actualDepartureTime)){
                        notifyTrainDepartingIn30(hhmmTime);
                    } else {
                        notifyTrainDelayed(hhmmTime);
                    }
                }
            } else { //1 If there is more than 30 minutes until departure
                if (ZonedDateTime.now().isAfter(timeTableTime.minus(1, ChronoUnit.HOURS))){ //
                    if (timeTableTimeDelayThreshold.isAfter(actualDepartureTime)){ // Departure in less than an hour
                        notifyTrainDepartingInAnHour(hhmmTime);
                    } else { // If we know the train is delayed more than an hour before departure
                        notifyTrainDelayed(hhmmTime);
                    }
                }
            }
        } else { // If the current time has passed the trains departure time, stop the notifications
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancelAll();
        }
    }

    private void notifyTrainDepartingInAnHour(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(true)
                .setContentTitle("Train departing in under an hour.")
                .setContentText("Train " + trainNum + " is departing at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDepartingIn30(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(true)
                .setContentTitle("Train is departing in under 30 minutes.")
                .setContentText("Train " + trainNum + " is departing at " + departureTime  + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDeprtingInSub10(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(true)
                .setContentTitle("Train departing in under 10 minutes.")
                .setContentText("Train " + trainNum + " is departing at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }
}
