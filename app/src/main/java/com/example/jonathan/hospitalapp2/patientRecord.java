package com.example.jonathan.hospitalapp2;

import java.util.ArrayList;

/**
 * Created by Jonathan on 14/5/2018.
 */

public class patientRecord {
    ArrayList<String> condition;
    long timestamp;
    ArrayList<String> prescription;

    public patientRecord(){}
    public patientRecord(ArrayList<String> c, long t, ArrayList<String> p){
        this.condition = c;
        this.timestamp = t;
        this.prescription = p;
    }
}
