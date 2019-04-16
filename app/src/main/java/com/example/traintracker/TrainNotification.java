package com.example.traintracker;

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
import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class TrainNotification extends AsyncTask<Integer, Integer, String> {
    public static final String TAG = "TrainNotification";
    public static final String CHANNEL_ID = "150000";
    private NotificationManagerCompat notificationManager;
    private String mTrainNum;
    private String mTrainShortCode;
    private Context mContext;
    TrainNotification(Context context, String trainNum, String trainShortCode){
        mContext = context;
        mTrainNum = trainNum;
        mTrainShortCode = trainShortCode;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
    }
    @Override
    protected String doInBackground(Integer... integers) {
        //TODO: https://rata.digitraffic.fi/api/v1/trains/latest/114
        // Make asynctask/thread to get info from above API

        String url = "https://rata.digitraffic.fi/api/v1/trains/latest/" + mTrainNum;
        JSONObject departurePoint = new JSONObject();
        Log.d(TAG, url);
        try {
            String JSONResponse = HTTPRequest(url);
            JSONArray jArr = new JSONArray(JSONResponse);
            JSONArray timeTableJArr = jArr.getJSONObject(0).getJSONArray("timeTableRows");
            for (int i = 0; i < timeTableJArr.length(); i++){
                if (timeTableJArr.getJSONObject(i).getString("stationShortCode").equals(mTrainShortCode) && timeTableJArr.getJSONObject(i).get("type").toString().equals("DEPARTURE")){
                    departurePoint = timeTableJArr.getJSONObject(i);
                    break;
                }
            }
            if (departurePoint != null){

                String scheduledTimeString = departurePoint.getString("scheduledTime");
                Instant scheduledITime = Instant.parse(scheduledTimeString);
                ZonedDateTime scheduledTime = scheduledITime.atZone(ZoneId.of("Europe/Helsinki"));
                String actualTimeString = departurePoint.getString("actualTime");
                Instant actualITime = Instant.parse(actualTimeString);
                ZonedDateTime actualTime = actualITime.atZone(ZoneId.of("Europe/Helsinki"));
                if (actualTime == null){
                    actualTime = scheduledTime;
                }
                Instant now = Instant.now();
                // Convert all times to minutes so 2:30 becomes 150
                double currentHour = Double.parseDouble(DateTimeFormatter.ofPattern("HH").format(now));
                double currentMinutes = Double.parseDouble(DateTimeFormatter.ofPattern("mm").format(now)) + currentHour * 60;
                double scheduledHour = Double.parseDouble(DateTimeFormatter.ofPattern("HH").format(scheduledTime));
                double scheduledMinutes = Double.parseDouble(DateTimeFormatter.ofPattern("mm").format(scheduledTime)) + scheduledHour * 60;
                double actualHour = Double.parseDouble(DateTimeFormatter.ofPattern("HH").format(actualTime));
                double actualMinutes = Double.parseDouble(DateTimeFormatter.ofPattern("mm").format(actualTime)) + actualHour * 60;
                // Train is delayed more than 15 minutes
                if (actualMinutes - scheduledMinutes >= 15 && actualMinutes - currentMinutes >= 75 ){
                    notifyTrainDelayed(DateTimeFormatter.ofPattern("HH:mm").format(actualTime));
                }
                // Train departing in about an hour
                if (actualMinutes - currentMinutes <= 70 && actualMinutes - currentMinutes >= 55){
                    notifyTrainDepartingInAnHour(DateTimeFormatter.ofPattern("HH:mm").format(actualTime));
                }
                // Train departing in about 35 minutes
                if (actualMinutes - currentMinutes < 45 && actualMinutes - currentMinutes >= 25){
                    notifyTrainDepartingIn35(DateTimeFormatter.ofPattern("HH:mm").format(actualTime));
                }
                // Train departing within 15 minutes
                if (actualMinutes - currentMinutes < 15){
                    notifyTrainDeprtingInSub15(DateTimeFormatter.ofPattern("HH:mm").format(actualTime));
                }
            }
            // format: JSONArray.JSONObject(0).JSONArray("timeTableRows").JSONObject(i).
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // fetch scheduledTime and liveEstimateTime as format 2019-04-15T13:27:00.000Z




        return null;
    }

    private void notifyTrainDelayed(String departureTime){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train " + mTrainNum + " has been delayed.")
                .setContentText("The train is delayed by at least 15 minutes and will depart at " + departureTime + ".");
        notificationManager.notify(150001, builder.build());
    }

    private void notifyTrainDepartingInAnHour(String departureTime){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in about 60 minutes.")
                .setContentText("Train " + mTrainNum + " will depart in about 60 minutes, at " + departureTime + ".");
        notificationManager.notify(150001, builder.build());
    }

    private void notifyTrainDepartingIn35(String departureTime){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in about 35 minutes.")
                .setContentText("Train " + mTrainNum + " will depart in about 35 minutes, at " + departureTime  + ".");
        notificationManager.notify(150001, builder.build());
    }

    private void notifyTrainDeprtingInSub15(String departureTime){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Departure in less than 15 minutes.")
                .setContentText("Train " + mTrainNum + " will depart in under 15 minutes, at " + departureTime + ".");
        notificationManager.notify(150001, builder.build());
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
