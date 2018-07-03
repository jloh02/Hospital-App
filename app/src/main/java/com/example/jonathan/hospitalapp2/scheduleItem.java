package com.example.jonathan.hospitalapp2;

public class scheduleItem {
    long timestamp;
    long duration;
    String patientEmail;
    String doctorEmail;
    String location;
    String taskAction;
    long taskID;

    public scheduleItem(){}

    public scheduleItem(long ts, long du, String p, String d, String loc, String tA, long tID) {
        this.duration = du;
        this.doctorEmail = d;
        this.patientEmail = p;
        this.timestamp = ts;
        this.location = loc;
        this.taskID = tID;
        this.taskAction = tA;
    }
}