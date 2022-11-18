package com.example.jonathan.hospitalapp2;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jonathan on 3/7/2018.
 */

public class patientCondition {
    String condition;
    ArrayList<patientRecord> prevUpdates;

    public patientCondition(){
        prevUpdates = new ArrayList<>();
    }

    long getLatestTimestamp(){
        ArrayList<Long> allTS = new ArrayList<>();
        for (patientRecord p:prevUpdates) {
            allTS.add(p.timestamp);
        }
        Collections.sort(allTS);
        return(allTS.get(0));
    }
}
