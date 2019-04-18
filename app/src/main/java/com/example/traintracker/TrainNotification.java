package com.example.traintracker;

import android.app.Notification;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static com.example.traintracker.App.CHANNEL_1_ID;

public class TrainNotification extends AsyncTask<Integer, Integer, ZonedDateTime> {
    private static final String TAG = "TrainNotification";
    private NotificationManagerCompat notificationManager;
    private String mTrainNum;
    private String mDepShortCode;
    private String mFullDepTime;
    private Context mContext;
    TrainNotification(Context context, String trainNum, String trainShortCode, String fullDepTime){
        mContext = context;
        mTrainNum = trainNum;
        mDepShortCode = trainShortCode;
        mFullDepTime = fullDepTime;
        notificationManager = NotificationManagerCompat.from(mContext);
    }
    @Override
    protected ZonedDateTime doInBackground(Integer... integers) {
        return apiCallAndNotification();
    }

    @Override
    protected void onPostExecute(ZonedDateTime zonedDateTime) {
        Log.d(TAG, "onPostExecute: " + zonedDateTime);
        super.onPostExecute(zonedDateTime);
    }

    private ZonedDateTime apiCallAndNotification() {
        //TODO: https://rata.digitraffic.fi/api/v1/trains/latest/114
        // Make asynctask/thread to get info from above API

        String url = "https://rata.digitraffic.fi/api/v1/trains/latest/" + mTrainNum;
        JSONObject departurePoint = new JSONObject();
        Log.d(TAG, url);
        Instant originalTime = Instant.parse(mFullDepTime);
        ZonedDateTime zonedTime = originalTime.atZone(ZoneId.of("Europe/Helsinki"));
        // Since TrainJobService is run every 15 minutes, we want a 15 minute window for the hour until departure
        ZonedDateTime oneHourUntilDeparture = zonedTime.minus(1, ChronoUnit.HOURS);
        oneHourUntilDeparture = oneHourUntilDeparture.minus(15, ChronoUnit.MINUTES);
        ZonedDateTime actualDepartureTime = null;
        // If current time is later than oneHourUntilDeparture
        if (oneHourUntilDeparture.compareTo(ZonedDateTime.now()) < 0){
            Log.d(TAG, "apiCallAndNotification: after first compare");
            departurePoint = fetchTrainInfo(url, departurePoint);
            actualDepartureTime = fetchTrainDepartureTime(departurePoint);
            //compareTimes(actualDepartureTime);
            Log.d(TAG, "apiCallAndNotification: " + actualDepartureTime);
        }
        return actualDepartureTime;
    }

    private void compareTimes(ZonedDateTime actualDepartureTime) {
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

    private ZonedDateTime fetchTrainDepartureTime(JSONObject departurePoint) {
        String actualTime = "";
        String scheduledtime = "";
        try {
            scheduledtime = departurePoint.getString("scheduledTime");
            Log.d(TAG, "fetchTrainDepartureTime: scheduledtime " + scheduledtime);
            actualTime = departurePoint.getString("actualTime");
            Log.d(TAG, "fetchTrainDepartureTime: actualTime " + actualTime);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (actualTime.equals("undefined") || actualTime.equals("")){
            Log.d(TAG, "fetchTrainDepartureTime: going by scheduledtime");
            return Instant.parse(scheduledtime).atZone(ZoneId.of("Europe/Helsinki"));
        } else {
            Log.d(TAG, "fetchTrainDepartureTime: going by actual Time");
            return Instant.parse(actualTime).atZone(ZoneId.of("Europe/Helsinki"));
        }
    }

    private JSONObject fetchTrainInfo(String url, JSONObject departurePoint){
        try {
            String JSONResponse = HTTPRequest(url);
            JSONArray jArr = new JSONArray(JSONResponse);
            JSONArray timeTableJArr = jArr.getJSONObject(0).getJSONArray("timeTableRows");
            for (int i = 0; i < timeTableJArr.length(); i++){
                if (timeTableJArr.getJSONObject(i).getString("stationShortCode").equals(mDepShortCode) && timeTableJArr.getJSONObject(i).get("type").toString().equals("DEPARTURE")){
                    departurePoint = timeTableJArr.getJSONObject(i);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return departurePoint;
    }

    private void notifyTrainDelayed(String departureTime){
        Notification builder = new NotificationCompat.Builder(mContext, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train " + mTrainNum + " has been delayed.")
                .setContentText("The train is delayed by at least 15 minutes and will depart at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDepartingInAnHour(String departureTime){
        Notification builder = new NotificationCompat.Builder(mContext, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in about 60 minutes.")
                .setContentText("Train " + mTrainNum + " will depart in about 60 minutes, at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDepartingIn35(String departureTime){
        Notification builder = new NotificationCompat.Builder(mContext, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in about 35 minutes.")
                .setContentText("Train " + mTrainNum + " will depart in about 35 minutes, at " + departureTime  + ".")
                .build();
        notificationManager.notify(150001, builder);
    }

    private void notifyTrainDeprtingInSub15(String departureTime){
        Notification builder = new NotificationCompat.Builder(mContext, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in less than 15 minutes.")
                .setContentText("Train " + mTrainNum + " will depart in under 15 minutes, at " + departureTime + ".")
                .build();
        notificationManager.notify(150001, builder);
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
