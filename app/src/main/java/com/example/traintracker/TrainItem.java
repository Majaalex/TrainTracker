package com.example.traintracker;

import java.io.Serializable;

public class TrainItem implements Serializable {

    private String mName;
    private String mDepartureTime;
    private String mArrivalTime;

    public TrainItem(String name,String depTime,String arrTime){
        mName = name;
        mDepartureTime = depTime;
        mArrivalTime = arrTime;
    }

    public String getName() {
        return mName;
    }

    public String getmDepartureTime() {
        return mDepartureTime;
    }

    public String getmArrivalTime() {
        return mArrivalTime;
    }
}
