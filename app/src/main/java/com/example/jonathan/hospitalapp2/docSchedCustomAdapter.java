package com.example.jonathan.hospitalapp2;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class docSchedCustomAdapter extends BaseAdapter {
    private Activity activity;
    ArrayList<scheduleItem> data;
    Context context;
    private static LayoutInflater inflater = null;
    FirebaseDatabase fd = FirebaseDatabase.getInstance();
    DatabaseReference patInfoRef = fd.getReference().child("Patients");

    public docSchedCustomAdapter(Activity activity, int textViewResourceId, ArrayList<scheduleItem> dataItems) {
        try {
            this.activity = activity;
            this.data = dataItems;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                view = inflater.inflate(R.layout.doc_sched_list_layout, null);
            }
            final TextView timeTV = (TextView) view.findViewById(R.id.timeTV);
            final TextView nameTV = (TextView) view.findViewById(R.id.nameTV);
            final TextView conditionTV = (TextView) view.findViewById(R.id.cTV);

            final scheduleItem item = data.get(position);

            Date d = new Date(item.timestamp * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH mm");
            String ddmmyyyyhhmmTime = sdf.format(d);
            String[] times = ddmmyyyyhhmmTime.split(" ", 2);
            final String outputddmmyyyyhhmmTime = times[0] + "\n" + times[1];

            final String dbUserRef = item.patientEmail.contains(".com")?item.patientEmail.substring(0, item.patientEmail.indexOf(".com")).toLowerCase():item.patientEmail;

            patInfoRef.child(dbUserRef).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String latestCondition = "";
                    try {
                        if (dataSnapshot.child("records").getValue() != null) {
                            ArrayList<patientCondition> allRec = new ArrayList<>();
                            for(DataSnapshot snapshotItem : dataSnapshot.child("records").getChildren()) {
                                patientCondition pc = new patientCondition();
                                pc.condition = snapshotItem.getKey();
                                Log.d(TAG, "pc.condition: " + pc.condition);
                                for(DataSnapshot patientRecItem : snapshotItem.getChildren()){
                                    ArrayList<String> condUp = patientRecItem.getValue(patientRecord.class).conditionUpdate;
                                    ArrayList<String> pre = patientRecItem.getValue(patientRecord.class).prescription;
                                    long t = patientRecItem.getValue(patientRecord.class).timestamp;
                                    patientRecord pr = new patientRecord(condUp,t,pre);
                                    Log.d(TAG, "pr.conditionUpdate: "+pr.conditionUpdate);
                                    Log.d(TAG, "pr.prescription: "+pr.prescription);
                                    Log.d(TAG, "pr.timestamp: "+pr.timestamp);
                                    pc.prevUpdates.add(pr);
                                }
                                allRec.add(pc);
                            }
                            Collections.sort(allRec, new Comparator<patientCondition>() {            //TODO array list
                                public int compare(patientCondition item1, patientCondition item2) {
                                    return Long.compare(item2.getLatestTimestamp(), item1.getLatestTimestamp());
                                }
                            });
                            latestCondition = allRec.get(0).condition;
                        }
                    } catch (NullPointerException e) {
                        Log.w(TAG, "User Records Empty");
                    }

                    /*Log.d(TAG, "dbUserRef: " + item.patientEmail);
                    Log.d(TAG, "ddmmyyyyhhmmTime: " + outputddmmyyyyhhmmTime);
                    Log.d(TAG, "conditionOutput: " + latestCondition);*/
                    nameTV.setText(dataSnapshot.child("name").getValue().toString());
                    timeTV.setText(outputddmmyyyyhhmmTime);
                    conditionTV.setText(latestCondition);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
        return view;
    }
}