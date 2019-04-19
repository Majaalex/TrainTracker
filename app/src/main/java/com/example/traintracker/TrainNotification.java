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
    private String mTrainNum;
    private String mDepShortCode;
    private String mFullDepTime;
    TrainNotification(String trainNum, String trainShortCode, String fullDepTime){
        mTrainNum = trainNum;
        mDepShortCode = trainShortCode;
        mFullDepTime = fullDepTime;
    }
    @Override
    protected ZonedDateTime doInBackground(Integer... integers) {
        return apiCallAndNotification();
    }

    @Override
    protected void onPostExecute(ZonedDateTime zonedDateTime) {
        super.onPostExecute(zonedDateTime);
    }

    private ZonedDateTime apiCallAndNotification() {
        String url = "https://rata.digitraffic.fi/api/v1/trains/latest/" + mTrainNum;
        JSONObject departurePoint = new JSONObject();
        Instant originalTime = Instant.parse(mFullDepTime);
        ZonedDateTime zonedTime = originalTime.atZone(ZoneId.of("Europe/Helsinki"));
        // Since TrainJobService is run every 15 minutes, we want a 15 minute window for the hour until departure
        ZonedDateTime oneHourUntilDeparture = zonedTime.minus(1, ChronoUnit.HOURS);
        oneHourUntilDeparture = oneHourUntilDeparture.minus(15, ChronoUnit.MINUTES);
        ZonedDateTime actualDepartureTime = null;
        // If current time is later than oneHourUntilDeparture
        if (oneHourUntilDeparture.compareTo(ZonedDateTime.now()) < 0){
            departurePoint = fetchTrainInfo(url, departurePoint);
            actualDepartureTime = fetchTrainDepartureTime(departurePoint);
        }
        return actualDepartureTime;
    }

    private ZonedDateTime fetchTrainDepartureTime(JSONObject departurePoint) {
        String actualTime = "";
        String scheduledtime = "";
        try {
            scheduledtime = departurePoint.getString("scheduledTime");
            actualTime = departurePoint.getString("actualTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (actualTime.equals("undefined") || actualTime.equals("")){
            return Instant.parse(scheduledtime).atZone(ZoneId.of("Europe/Helsinki"));
        } else {
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

    private String HTTPRequest(String... strings){
        String response = "";
        try
        {
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
