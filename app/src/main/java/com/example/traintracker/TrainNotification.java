package com.example.traintracker;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class TrainNotification extends AsyncTask<Integer, Integer, String> {
    public static final String CHANNEL_ID = "150000";
    private String mTrainNum;
    private String mTrainShortCode;
    private Context mContext;
    TrainNotification(Context context, String trainNum, String trainShortCode){
        mContext = context;
        mTrainNum = trainNum;
        mTrainShortCode = trainShortCode;
    }
    @Override
    protected String doInBackground(Integer... integers) {
        //TODO: https://rata.digitraffic.fi/api/v1/trains/latest/114
        // Make asynctask/thread to get info from above API

        String url = "https://rata.digitraffic.fi/api/v1/trains/latest/" + mTrainNum;
        JSONObject departurePoint = new JSONObject();
        try {
            String JSONResponse = new HTTPGet().execute(url).get();
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
                Instant scheduledTime = Instant.parse(scheduledTimeString);
                String actualTimeString = departurePoint.getString("actualTime");
                Instant actualTime = Instant.parse(actualTimeString);

            }
            // format: JSONArray.JSONObject(0).JSONArray("timeTableRows").JSONObject(i).
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // fetch scheduledTime and liveEstimateTime as format 2019-04-15T13:27:00.000Z



        // TODO: Train departs in 60 minutes
        // TODO: Train departs in 40 minutes
        // TODO: Train departs in 15 minutes
        // TODO: Train is delayed more than 5 minutes
        return null;
    }
    private void notificationTrainDeparture(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_green)
                .setShowWhen(false)
                .setContentTitle("Train is departing")
                .setContentText("The train is departing at ");
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
