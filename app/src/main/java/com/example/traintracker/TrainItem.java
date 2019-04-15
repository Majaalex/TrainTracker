package com.example.traintracker;

import java.io.Serializable;

public class TrainItem implements Serializable {

    private String mName;
    private String mTrainNum;
    private String mDepartureTime;
    private String mArrivalTime;
    private String mColour;

    public TrainItem(String name,String depTime,String arrTime, String trainNum){
        mName = name;
        mDepartureTime = depTime;
        mArrivalTime = arrTime;
        mTrainNum = trainNum;
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
