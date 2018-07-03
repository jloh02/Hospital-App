package com.example.jonathan.hospitalapp2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class DoctorActivity extends AppCompatActivity {

    String TAG = "DOCTOR ACTIVITY TESTING";
    String patientEmail;

    ProgressDialog progressDialogDisplay;
    FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    final FirebaseUser u = authenticationInstance.getCurrentUser();
    GoogleSignInClient GsignInClient;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference docSchedule = database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())).child("schedule");
    DatabaseReference allSchedule = database.getReference().child("Appointments");
    DatabaseReference userAccRef;

    ListView lv;

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);

        lv = findViewById(R.id.docActScheduleList);

        GoogleSignInOptions GsignInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GsignInClient = GoogleSignIn.getClient(this, GsignInOpt);

        try {
            final FirebaseUser u = authenticationInstance.getCurrentUser();
            Log.d(TAG, "Updating User Info:" + emailWithoutSuffix(u.getEmail()));
            DatabaseReference userRef = database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail()));  //TODO Email as Primary Key
            userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TextView nameLabel = findViewById(R.id.name);
                    String firstName = dataSnapshot.getValue().toString().split(" ")[0];   //TODO Only display first name
                    nameLabel.setText("Name: " + firstName + " (" + u.getEmail() + ")");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "get failed with ", databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "onCreate: ", e);
        }

        /////////////////////////////////////SCHEDULE UI SETUP/////////////////////////////////////
        docSchedule.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                Log.d(TAG, "DataSnapshot of docSchedule Item: " + dbValues.getValue());
                final ArrayList<Long> docLongSched = (ArrayList<Long>) dbValues.getValue();
                final ArrayList<scheduleItem> docSched = new ArrayList<>();
                for (final Long item : docLongSched) {
                    allSchedule.child(item + "").addValueEventListener((new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "DataSnapshot of allSchedule Item: " + dataSnapshot.getValue());
                            scheduleItem s = dataSnapshot.getValue(scheduleItem.class);
                            String locString = s.location;
                            String locNumString = "";
                            for (int i = 0; i < locString.length(); i++) {
                                char partStr = locString.charAt(i);
                                if (partStr >= 48 && partStr <= 57)
                                    locNumString += partStr;   //TODO ACSII value for 0-9
                            }
                            String stringID = s.timestamp + locNumString;
                            s.taskID = Long.parseLong(stringID);
                            docSched.add(s);

                            for (int i = 0; i < docSched.size(); i++) {
                                Log.d(TAG, "docSched[" + i + "]: " + docSched.get(i));
                            }

                            docSchedCustomAdapter adap = new docSchedCustomAdapter(DoctorActivity.this, R.layout.doc_sched_list_layout, docSched);
                            lv.setAdapter(adap);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                    Object itemInfo = lv.getItemAtPosition(position);
                                    final scheduleItem schedItem = (scheduleItem) itemInfo;
                                    try {
                                        patientEmail = schedItem.patientEmail;
                                        userAccRef = database.getReference().child("Patients").child(emailWithoutSuffix(patientEmail));
                                        Log.d(TAG, "userAccRef: "+ userAccRef);
                                        userAccRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                                Log.d(TAG, "dataSnapshot: "+ dataSnapshot.child("name").getValue().toString());
                                                Log.d(TAG, "dataSnapshot: "+ dataSnapshot.child("records").getValue());
                                                String userName = dataSnapshot.child("name").getValue().toString();

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
                                                String conditionStr = "", presStr = "";
                                                try {
                                                    conditionStr = recs.get(0).condition.get(0);
                                                    presStr = recs.get(0).prescription.get(0);
                                                    for (int j = 1; j < recs.get(0).condition.size(); j++) {
                                                        conditionStr += "\n" + recs.get(0).condition.get(j);
                                                        presStr += "\n" + recs.get(0).prescription.get(j);
                                                    }
                                                    for (int i = 1; i < recs.size(); i++) {
                                                        ArrayList<String> conditionArr = recs.get(i).condition;
                                                        ArrayList<String> presArr = recs.get(i).prescription;
                                                        for (int j = 0; j < conditionArr.size(); j++) {
                                                            conditionStr += "\n" + conditionArr.get(j);
                                                            presStr += "\n" + presArr.get(j);
                                                        }
                                                    }
                                                } catch (NullPointerException e){
                                                    Log.d(TAG, "Empty User Record");
                                                }

                                                LinearLayout alertLayout = new LinearLayout(DoctorActivity.this);
                                                alertLayout.setOrientation(LinearLayout.VERTICAL);
                                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DoctorActivity.this);
                                                final AlertDialog alert = alertBuilder.create();

                                                alertBuilder.setTitle("Appointment");

                                                TextView nameTV = new TextView(DoctorActivity.this);
                                                nameTV.setText("\nName: " + userName +"\n");
                                                alertLayout.addView(nameTV);

                                                TextView condTV = new TextView(DoctorActivity.this);
                                                condTV.setText("Condition:");
                                                alertLayout.addView(condTV);
                                                final EditText condInput = new EditText(DoctorActivity.this);
                                                condInput.setText(conditionStr);
                                                condInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                                alertLayout.addView(condInput);

                                                TextView presTV = new TextView(DoctorActivity.this);
                                                presTV.setText("Prescription:");
                                                alertLayout.addView(presTV);
                                                final EditText presInput = new EditText(DoctorActivity.this);
                                                presInput.setText(presStr);
                                                presInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                                alertLayout.addView(presInput);

                                                alertBuilder.setView(alertLayout);

                                                alertBuilder.setPositiveButton("Save &\nadd Appt", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {
                                                        final ArrayList<String> condFinal = new ArrayList<String>(Arrays.asList(condInput.getText().toString().split("\n")));
                                                        final ArrayList<String> presFinal = new ArrayList<String>(Arrays.asList(presInput.getText().toString().split("\n")));
                                                        database.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
                                                        database.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot ds) {
                                                                patientRecord newRec = new patientRecord(condFinal,Long.parseLong(ds.getValue().toString()), presFinal);
                                                                ArrayList<patientRecord> newUserRec = (ArrayList<patientRecord>)  dataSnapshot.child("records").getValue();
                                                                if(newUserRec == null) newUserRec = new ArrayList<>();
                                                                newUserRec.add(newRec);
                                                                userAccRef.child("records").setValue(newUserRec);
                                                                startActivity(new Intent(DoctorActivity.this,addApptActivity.class));
                                                                alert.dismiss();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }
                                                });

                                                alertBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {
                                                        alert.cancel();
                                                    }
                                                });

                                                alertBuilder.setNegativeButton("Save", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {
                                                        final ArrayList<String> condFinal = new ArrayList<String>(Arrays.asList(condInput.getText().toString().split("\n")));
                                                        final ArrayList<String> presFinal = new ArrayList<String>(Arrays.asList(presInput.getText().toString().split("\n")));
                                                        database.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
                                                        database.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot ds) {
                                                                patientRecord newRec = new patientRecord(condFinal,Long.parseLong(ds.getValue().toString()), presFinal);
                                                                ArrayList<patientRecord> newUserRec = (ArrayList<patientRecord>)  dataSnapshot.child("records").getValue();
                                                                if(newUserRec == null) newUserRec = new ArrayList<>();
                                                                newUserRec.add(newRec);
                                                                userAccRef.child("records").setValue(newUserRec);
                                                                alert.dismiss();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }
                                                });

                                                alertBuilder.show();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                    } catch (Exception e) {
                                        Log.d(TAG, "Error Occurred:"+ e);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "While Retrieiving Schedule: " + databaseError);
                        }
                    }));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "While Retrieiving Schedule: " + databaseError);
            }
        });

    }

    String emailWithoutSuffix(String input) {
        return input.substring(0, input.indexOf(".com")).toLowerCase();
    }

    public void addAppt(View view) {
        startActivity(new Intent(this, addApptActivity.class));
    }

    public void signOut(View view) {
        showLogoutLoading();
        if (authenticationInstance.getCurrentUser() != null) {
            authenticationInstance.signOut();
            GsignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideLoadingView();
                            finish();
                        }
                    });
        }
    }

    void showLogoutLoading() {
        if (progressDialogDisplay == null) {
            progressDialogDisplay = new ProgressDialog(this);
            progressDialogDisplay.setMessage("Signing Out...");
            progressDialogDisplay.setIndeterminate(true);
        }

        progressDialogDisplay.show();
    }

    void hideLoadingView() {
        if (progressDialogDisplay != null && progressDialogDisplay.isShowing()) {
            progressDialogDisplay.dismiss();
        }
    }
}