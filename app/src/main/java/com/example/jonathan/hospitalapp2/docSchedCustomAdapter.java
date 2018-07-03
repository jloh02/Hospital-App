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
            String[] times = ddmmyyyyhhmmTime.split(" ",2);
            final String outputddmmyyyyhhmmTime = times[0] + "\n" + times[1];

            final String dbUserRef = item.patientEmail.substring(0, item.patientEmail.indexOf(".com")).toLowerCase();

            patInfoRef.child(dbUserRef).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String outputRecs = "";
                    try {
                        ArrayList<patientRecord> recs = new ArrayList<>();
                        if(dataSnapshot.child("records").getValue() != null)
                        {
                            Log.d(TAG, "dataSnapshot: "+dataSnapshot.child("records").getValue());
                            Log.d(TAG, "childrenCount: "+dataSnapshot.child("records").getChildrenCount());
                            for(DataSnapshot snapshotItem : dataSnapshot.child("records").getChildren()) {
                                Log.d(TAG, "Indiv snapshotItem: "+snapshotItem.child("records").getValue());
                                ArrayList<String> co = snapshotItem.getValue(patientRecord.class).condition;
                                ArrayList<String> pr = snapshotItem.getValue(patientRecord.class).prescription;
                                long t = snapshotItem.getValue(patientRecord.class).timestamp;
                                patientRecord p = new patientRecord(co,t,pr);
                                Log.d(TAG, "Indiv patientRecord: "+p);
                                recs.add(p);
                            }
                        }

                        if(recs!=null)
                            outputRecs = recs.get(0).condition.get(0);
                        for (int j = 1; j < recs.get(0).condition.size(); j++) {
                            outputRecs += "\n" + recs.get(0).condition.get(j);
                        }
                        for (int i = 1; i < recs.size(); i++) {
                            ArrayList<String> conditionArr = recs.get(i).condition;
                            for (int j = 0; j < conditionArr.size(); j++) {
                                outputRecs += "\n" + conditionArr.get(j);
                            }
                        }
                    } catch (NullPointerException e) {
                        Log.w(TAG, "User Records Empty");
                    }

                    Log.d(TAG, "dbUserRef: " + item.patientEmail);
                    Log.d(TAG, "ddmmyyyyhhmmTime: " + outputddmmyyyyhhmmTime);
                    Log.d(TAG, "outputRecs: " + outputRecs);
                    nameTV.setText(dataSnapshot.child("name").getValue().toString());
                    timeTV.setText(outputddmmyyyyhhmmTime);
                    conditionTV.setText(outputRecs);
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