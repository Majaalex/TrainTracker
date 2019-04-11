package com.example.traintracker;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class FileIO {

    public static void saveStations(HashMap<String, String> trainStations, Activity activity){
        System.out.println("Saving routes");
        try {
            FileOutputStream fos = activity.openFileOutput("routes", Context.MODE_PRIVATE);
            ObjectOutput oop = new ObjectOutputStream(fos);
            oop.writeObject(trainStations);
            oop.close();
            fos.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> loadStations(Activity activity){
        System.out.println("Loading routes");
        HashMap<String, String> trainStations = new HashMap<>();

        try {
            FileInputStream fis = activity.openFileInput("routes");
            ObjectInputStream oip = new ObjectInputStream(fis);
            trainStations = (HashMap<String, String>)oip.readObject();
            oip.close();
            fis.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return trainStations;
    }

}
