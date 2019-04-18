package com.example.traintracker;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class TrainItem implements Serializable {

    private String mName;
    private String mTrainNum;
    private String mDepartureTime;
    private String mArrivalTime;
    private String mColour;
    private String mFullDepTime;

    public TrainItem(String name, String depTime, String arrTime, String trainNum, String fullDepTime){
        mName = name;
        mDepartureTime = depTime;
        mArrivalTime = arrTime;
        mTrainNum = trainNum;
        mFullDepTime = fullDepTime;
    }
    public String getFullDepTime() {
        return mFullDepTime;
    }
    public String getName() {
        return mName;
    }

    public String getDepartureTime() {
        return mDepartureTime;
    }

    public String getArrivalTime() {
        return mArrivalTime;
    }

    public String getTrainNum() {
        return mTrainNum;
    }
    public String getColour(){
        return mColour;
    }
}
