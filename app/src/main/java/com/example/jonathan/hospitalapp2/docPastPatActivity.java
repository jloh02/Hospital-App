package com.example.jonathan.hospitalapp2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class docPastPatActivity extends AppCompatActivity {

    String TAG = "docPastPatActivity Testing";

    FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    final FirebaseUser u = authenticationInstance.getCurrentUser();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference docPastPatRef = database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())).child("pastPatients");
    DatabaseReference patRefs = database.getReference().child("Patients");

    ValueEventListener patVEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_past_pat);

        patVEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                Log.d(TAG, "DataSnapshot of pastPatient Item: " + dbValues.getValue());
                try {
                    final ArrayList<String> docPastPatAll = (ArrayList<String>) dbValues.getValue();
                    final ArrayList<String> allNames = new ArrayList<>();
                    final ArrayList<String> allCond = new ArrayList<>();
                    for (final String item : docPastPatAll) {
                        patRefs.child(item).child("name").addValueEventListener((new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "DataSnapshot of name: " + dataSnapshot.getValue());
                                final String nm = dataSnapshot.getValue().toString();
                                patRefs.child(item).child("records").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot ds) {
                                        Log.d(TAG, "DataSnapshot of records: "+ds.getValue());
                                        Iterable<DataSnapshot> patCond = ds.getChildren();
                                        ArrayList<String> allPatCond = new ArrayList<>();
                                        for (DataSnapshot con : patCond) {
                                            Log.d(TAG, "con.getKey: " + con.getKey());
                                            allPatCond.add(con.getKey());
                                        }
                                        if(allPatCond.size()>0) {
                                            String combCond = allPatCond.get(0);
                                            if (allPatCond.size() > 1) {
                                                for (int i = 1; i < allPatCond.size(); i++) {
                                                    combCond += ", " + allPatCond.get(i);
                                                }
                                            }
                                            Log.d(TAG, "combCond: " + combCond);
                                            allNames.add(nm);
                                            allCond.add(combCond);
                                            ListView lv = findViewById(R.id.docPastPatList);
                                            docPastPatListAdapter adap = new docPastPatListAdapter(docPastPatActivity.this, R.layout.docpastpatlistlayout, allNames, allCond);
                                            lv.setAdapter(adap);
                                        } else Toast.makeText(docPastPatActivity.this,"No Past Patients",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "While Retrieiving Schedule: " + databaseError);
                            }
                        }));
                    }

                } catch (NullPointerException e) {
                    Toast.makeText(docPastPatActivity.this, "No Patients", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "While Retrieiving Schedule: " + databaseError);
            }
        };
        docPastPatRef.addValueEventListener(patVEL);
    }

    String emailWithoutSuffix(String input) {
        return input.substring(0, input.indexOf(".com")).toLowerCase();
    }

    public void closeWindow(View view) {
        docPastPatRef.removeEventListener(patVEL);
        finish();
    }
}
