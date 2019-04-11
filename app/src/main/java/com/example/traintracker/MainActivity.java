package com.example.traintracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    // RecyclerView variables
    private TrainAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<TrainItem> mTrainList;
    AutoCompleteTextView textViewDep;
    AutoCompleteTextView textViewDest;
    private Button mButtonSet;
    private HashMap<String, String> trainStations;
    private ArrayList<String> stationList;

    private TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtonsAndFields();

        loadData();
        // is only run if loadData returns an empty set of values
        buildTrainStationList(trainStations);
        buildAutoCompleteTextViews();
        buildRecyclerView();
    }

    private void loadData() {
        trainStations = FileIO.loadStations(this);
        stationList.addAll(trainStations.keySet());
    }
    private void saveData() {
        FileIO.saveStations(trainStations, this);
    }

    // Connect an adapter with the stationList arraylist to the autocompleteTextViews
    private void buildAutoCompleteTextViews() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, stationList);
        textViewDep = findViewById(R.id.acDepartureLoc);
        textViewDest = findViewById(R.id.acDestinationLoc);
        textViewDep.setAdapter(adapter);
        textViewDest.setAdapter(adapter);
    }

    private void setButtonsAndFields() {
        mTrainList = new ArrayList<>();
        trainStations = new HashMap<>();
        stationList = new ArrayList<>();
        tv = findViewById(R.id.currentDate);
        updateTime();
        mButtonSet = findViewById(R.id.setStations);
        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTrainList();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateTime(){
        LocalDateTime localTime = LocalDateTime.now();
        tv.setText(DateTimeFormatter.ofPattern("dd-MM-yyyy").format(localTime));
    }

    private void createTrainList(){
        mTrainList.clear();
        String start = trainStations.get(textViewDep.getText().toString());
        String dest = trainStations.get(textViewDest.getText().toString());
        LocalDateTime localTime = LocalDateTime.now();
        String url = "https://rata.digitraffic.fi/api/v1/live-trains/station/" + start + "/" + dest + "?departure_date=" + DateTimeFormatter.ofPattern("yyyy-dd-MM").format(localTime);
        try {
            String JSONResponse = new HTTPGet().execute(url).get();
            JSONArray jArr = new JSONArray(JSONResponse);
            // Loop through each train
            for (int i = 0; i < jArr.length(); i++){
                // Name, departureTime, ArrivalTime
                String name = jArr.getJSONObject(i).get("trainType").toString() + " " + jArr.getJSONObject(i).getString("trainNumber");
                String depTime = "";
                String destTime = "";
                // Loop through each station the train passes by
                for (int j = 0; j < jArr.getJSONObject(i).getJSONArray("timeTableRows").length(); j++){
                    // Only check stations that the train actually stops at
                    if (jArr.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(j).getBoolean("trainStopping")){
                        // Pick out the object at the current position
                        JSONObject currentItem = jArr.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(j);
                        String checkStart = currentItem.getString("stationShortCode");
                        String checkDest = currentItem.getString("stationShortCode");
                        // Find the correct departure point
                        if (start.equals(checkStart) && currentItem.get("type").toString().equals("DEPARTURE")){
                            LocalDateTime time = LocalDateTime.parse(currentItem.get("scheduledTime").toString().substring(0,23));
                            depTime = DateTimeFormatter.ofPattern("HH:mm").format(time);
                        }
                        // Find the correct arrival point
                        if (dest.equals(checkDest) && currentItem.get("type").toString().equals("ARRIVAL")){
                            LocalDateTime time = LocalDateTime.parse(currentItem.get("scheduledTime").toString().substring(0,23));
                            destTime = DateTimeFormatter.ofPattern("HH:mm").format(time);
                        }
                    }

                }
                mTrainList.add(new TrainItem(name, depTime, destTime));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "You are missing a station or the stations are not connected.", Toast.LENGTH_SHORT).show();
        }
    }

    // Build a hashmap with all stations that are for passengerTraffic
    // Is only run if trainStations hashmap is empty
    private void buildTrainStationList(HashMap<String, String> trainStations) {
        if (trainStations.isEmpty()){
            String url = "https://rata.digitraffic.fi/api/v1/metadata/stations";
            String JSONResponse = "";
            try {
                JSONResponse = new HTTPGet().execute(url).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!JSONResponse.equals("")){
                try {
                    JSONArray jArr = new JSONArray(JSONResponse);
                    for (int i = 0; i < jArr.length(); i++){
                        if (jArr.getJSONObject(i).getBoolean("passengerTraffic")){
                            trainStations.put(jArr.getJSONObject(i).getString("stationName"), jArr.getJSONObject(i).getString("stationShortCode"));
                        }
                    }
                    stationList.addAll(trainStations.keySet());
                    saveData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new TrainAdapter(mTrainList);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(new TrainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openInMap(position);
            }
        });
    }

    private void openInMap(int position){
        String number = mTrainList.get(position).getName();
        Intent map = new Intent(this, MapActivity.class);
        map.putExtra("number", number);
        startActivity(map);
        finish();
    }
}
