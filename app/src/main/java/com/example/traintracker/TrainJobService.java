package com.example.traintracker;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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

    private String trainNum;
    private String startLocShortCode;
    private String deptime;
    private Boolean continueRunning = true;

    @Override
    public boolean onStartJob(JobParameters params) {
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(JobParameters params){
        trainNum = params.getExtras().getString(extraNum);
        startLocShortCode = params.getExtras().getString(extraDepLoc);
        deptime = params.getExtras().getString(extraDepTime);
        TrainNotification trainNotification = new TrainNotification(trainNum, startLocShortCode, deptime);
        try {
            compareTimes(trainNotification.execute().get());
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
                .setShowWhen(false)
                .setContentTitle("Train has been delayed at least 15 minutes.")
                .setContentText("Train " + trainNum + " is delayed by at least 15 minutes and will depart at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void compareTimes(ZonedDateTime actualDepartureTime) {
        String hhmmTime;
        if (actualDepartureTime != null && actualDepartureTime.isAfter(ZonedDateTime.now())){
            ZonedDateTime timeTableTime = Instant.parse(deptime).atZone(ZoneId.of("Europe/Helsinki"));
            ZonedDateTime timeTableTimePlusFifteen = timeTableTime.plus(15, ChronoUnit.MINUTES);
            ZonedDateTime thirtyUntilDeparture = actualDepartureTime.minus(30, ChronoUnit.MINUTES);
            ZonedDateTime tenUntilDeparture = actualDepartureTime.minus(10, ChronoUnit.MINUTES);
            hhmmTime = DateTimeFormatter.ofPattern("HH:mm").format(actualDepartureTime);
            if (ZonedDateTime.now().isAfter(thirtyUntilDeparture)){ // If there is less than 30 minutes until departure
                if (ZonedDateTime.now().isAfter(tenUntilDeparture)){ // If the train is departing in under 10 minutes
                    // Notify that there is under 10 minutes left
                    notifyTrainDeprtingInSub10(hhmmTime);
                } else {
                    // Notify that the train is departing in under 30 minutes
                    notifyTrainDepartingIn30(hhmmTime);
                }
            } else {
                if (ZonedDateTime.now().isAfter(timeTableTime.minus(1, ChronoUnit.HOURS))){
                    if (timeTableTimePlusFifteen.isAfter(actualDepartureTime)){
                        notifyTrainDepartingInAnHour(hhmmTime);
                    } else {
                        notifyTrainDelayed(hhmmTime);
                    }
                }
            }
        } else {
            continueRunning = false;
        }
    }

    private void notifyTrainDepartingInAnHour(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train departing in under an hour.")
                .setContentText("Train " + trainNum + " is departing at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDepartingIn30(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train is departing in under 30 minutes.")
                .setContentText("Train " + trainNum + " is departing at " + departureTime  + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDeprtingInSub10(String departureTime){
        Notification builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train departing in under 10 minutes.")
                .setContentText("Train " + trainNum + " is departing at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }
}
