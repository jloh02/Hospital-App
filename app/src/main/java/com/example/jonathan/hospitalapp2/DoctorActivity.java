package com.example.jonathan.hospitalapp2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import java.util.Collections;
import java.util.Comparator;

public class DoctorActivity extends AppCompatActivity {

    String TAG = "DOCTOR ACTIVITY TESTING";
    String patientEmail = "";

    boolean walkIn = false;
    boolean present = false;

    ProgressDialog progressDialogDisplay;
    FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    final FirebaseUser u = authenticationInstance.getCurrentUser();
    GoogleSignInClient GsignInClient;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference docSchedule = database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())).child("schedule");
    DatabaseReference allSchedule = database.getReference().child("Appointments");
    DatabaseReference walkInSchedule = database.getReference().child("WalkInAppointments");
    DatabaseReference userAccRef;

    ValueEventListener schedVEL;

    ListView lv;

    private static Integer[] imageIconDatabase = {R.drawable.baseline_email_24px,  //TODO Setup for spinner
            R.drawable.baseline_vpn_key_24px, R.drawable.baseline_exit_to_app_24px, R.drawable.baseline_settings_20px};
    ArrayList<Integer> spinnerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions GsignInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GsignInClient = GoogleSignIn.getClient(this, GsignInOpt);

        userAccRef = database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail()));
        userAccRef.child("specialisation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                ArrayList<String> specs = (ArrayList<String>) dataSnapshot.getValue();
                for (String s : specs) {
                    if (s.contains("Walk In") || s.equals("Walk In")) walkIn = true;
                }
                Log.d(TAG, "walkIn: " + walkIn);

        /*-------------------------------------------------SPECIALISED DOCTOR-------------------------------------------------*/
                //TODO 2 views for 1 activity
                Log.d(TAG, "walkIn before check: " + walkIn);
                if (!walkIn) {
                    setContentView(R.layout.activity_doctor);
                    lv = findViewById(R.id.docActScheduleList);
                    /////////////////////////////////////POPUP WINDOW FOR INSTRUCTIONS/////////////////////////////////////
                    final CheckBox box = new CheckBox(DoctorActivity.this);
                    SharedPreferences pref = getSharedPreferences("DOCdoNotDisplay", MODE_PRIVATE);  //Read
                    int stopShowingChecked = Integer.parseInt(pref.getString("DOCdoNotDisplay", "0"));
                    if (stopShowingChecked != 1) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DoctorActivity.this);
                        final AlertDialog alert = alertBuilder.create();

                        alertBuilder.setTitle("Instructions");
                        alertBuilder.setMessage("Click on each appointment list item to view and save patient details\nTap on the relevant buttons below to view your patients or add new appointments");

                        box.setText("Do not show this message again");
                        alertBuilder.setView(box);

                        alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                alert.cancel();

                                if (box.isChecked()) {
                                    SharedPreferences prefs = getSharedPreferences("DOCdoNotDisplay", MODE_PRIVATE); //Store
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("DOCdoNotDisplay", "1");
                                    editor.commit();
                                }
                            }
                        });
                        alertBuilder.show();
                    }

                    /////////////////////////////////////SCHEDULE UI SETUP/////////////////////////////////////
                    schedVEL = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dbValues) {
                            Log.d(TAG, "DataSnapshot of docSchedule Item: " + dbValues.getValue());
                            final ArrayList<Long> docLongSched = (ArrayList<Long>) dbValues.getValue();
                            final ArrayList<scheduleItem> docSched = new ArrayList<>();
                            try {
                                for (final Long item : docLongSched) {
                                    allSchedule.child(item + "").addValueEventListener((new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            try {
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
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, e + "");
                                            }

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
                                                        Log.d(TAG, "userAccRef: " + userAccRef);
                                                        database.getReference().child("Patients").child(emailWithoutSuffix(schedItem.patientEmail)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                                                Log.d(TAG, "dataSnapshot: " + dataSnapshot.child("name").getValue().toString());
                                                                Log.d(TAG, "dataSnapshot: " + dataSnapshot.child("records").getValue());
                                                                String userName = dataSnapshot.child("name").getValue().toString();
                                                                String latestCondition;

                                                                if (dataSnapshot.child("records").getValue() != null) {
                                                                    ArrayList<patientCondition> allRec = new ArrayList<>();
                                                                    for (DataSnapshot snapshotItem : dataSnapshot.child("records").getChildren()) {
                                                                        patientCondition pc = new patientCondition();
                                                                        pc.condition = snapshotItem.getKey();
                                                                        Log.d(TAG, "pc.condition: " + pc.condition);
                                                                        for (DataSnapshot patientRecItem : snapshotItem.getChildren()) {
                                                                            ArrayList<String> condUp = patientRecItem.getValue(patientRecord.class).conditionUpdate;
                                                                            ArrayList<String> pre = patientRecItem.getValue(patientRecord.class).prescription;
                                                                            long t = patientRecItem.getValue(patientRecord.class).timestamp;
                                                                            patientRecord pr = new patientRecord(condUp, t, pre);
                                                                            Log.d(TAG, "pr.conditionUpdate: " + pr.conditionUpdate);
                                                                            Log.d(TAG, "pr.prescription: " + pr.prescription);
                                                                            Log.d(TAG, "pr.timestamp: " + pr.timestamp);
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
                                                                } else latestCondition = null;

                                                                LinearLayout alertLayout = new LinearLayout(DoctorActivity.this);
                                                                alertLayout.setOrientation(LinearLayout.VERTICAL);
                                                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DoctorActivity.this);
                                                                final AlertDialog alert = alertBuilder.create();

                                                                alertBuilder.setTitle("Appointment");

                                                                TextView nameTV = new TextView(DoctorActivity.this);
                                                                nameTV.setText("\nName: " + userName + "\n");
                                                                alertLayout.addView(nameTV);

                                                                TextView condTV = new TextView(DoctorActivity.this);
                                                                condTV.setText("Updates:");
                                                                alertLayout.addView(condTV);
                                                                final EditText condInput = new EditText(DoctorActivity.this);
                                                                condInput.setText("");
                                                                condInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                                                alertLayout.addView(condInput);

                                                                TextView presTV = new TextView(DoctorActivity.this);
                                                                presTV.setText("Prescription:");
                                                                alertLayout.addView(presTV);
                                                                final EditText presInput = new EditText(DoctorActivity.this);
                                                                presInput.setText("");
                                                                presInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                                                alertLayout.addView(presInput);

                                                                final EditText diagInput = new EditText(DoctorActivity.this);
                                                                diagInput.setText("");
                                                                diagInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                                                boolean isConditionEmpty = false;
                                                                if (latestCondition == null) {
                                                                    isConditionEmpty = true;
                                                                    TextView diagTV = new TextView(DoctorActivity.this);
                                                                    diagTV.setText("Diagnosis:");
                                                                    alertLayout.addView(diagTV);

                                                                    alertLayout.addView(diagInput);
                                                                }

                                                                final boolean conditionEmp = isConditionEmpty;
                                                                alertBuilder.setView(alertLayout);
                                                                final String lateCondition = latestCondition;
                                                                alertBuilder.setPositiveButton("Save &\nadd Appt", new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog,
                                                                                        int whichButton) {
                                                                        String condSet = "";
                                                                        if (conditionEmp)
                                                                            condSet = diagInput.getText().toString();
                                                                        else
                                                                            condSet = lateCondition;
                                                                        final ArrayList<String> condFinal = new ArrayList<String>(Arrays.asList(condInput.getText().toString().split("\n")));
                                                                        final ArrayList<String> presFinal = new ArrayList<String>(Arrays.asList(presInput.getText().toString().split("\n")));
                                                                        final String conditionSet = condSet;
                                                                        database.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
                                                                        database.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot ds) {
                                                                                patientRecord newRec = new patientRecord(condFinal, Long.parseLong(ds.getValue().toString()), presFinal);
                                                                                ArrayList<patientRecord> newUserRec = (ArrayList<patientRecord>) dataSnapshot.child("records").child(conditionSet).getValue();
                                                                                if (newUserRec == null)
                                                                                    newUserRec = new ArrayList<>();
                                                                                newUserRec.add(newRec);
                                                                                database.getReference().child("Patients").child(emailWithoutSuffix(schedItem.patientEmail)).child("records").child(conditionSet).setValue(newUserRec);

                                                                                database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String dEm = dataSnapshot.getValue().toString();
                                                                                        userAccRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(DataSnapshot ds) {
                                                                                                Intent intent = new Intent(DoctorActivity.this, addApptActivity.class);
                                                                                                intent.putExtra("patEm", ds.getValue().toString());
                                                                                                intent.putExtra("docEm", dEm);
                                                                                                intent.putExtra("task", "Follow Up:");
                                                                                                Toast.makeText(DoctorActivity.this,"Saved User Data",Toast.LENGTH_SHORT).show();
                                                                                                startActivity(intent);
                                                                                            }

                                                                                            @Override
                                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                                            }
                                                                                        });

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });
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
                                                                        String condSet = "";
                                                                        if (conditionEmp)
                                                                            condSet = diagInput.getText().toString();
                                                                        else
                                                                            condSet = lateCondition;
                                                                        final ArrayList<String> condFinal = new ArrayList<String>(Arrays.asList(condInput.getText().toString().split("\n")));
                                                                        final ArrayList<String> presFinal = new ArrayList<String>(Arrays.asList(presInput.getText().toString().split("\n")));
                                                                        final String conditionSet = condSet;
                                                                        database.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
                                                                        database.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot ds) {
                                                                                patientRecord newRec = new patientRecord(condFinal, Long.parseLong(ds.getValue().toString()), presFinal);
                                                                                ArrayList<patientRecord> newUserRec = (ArrayList<patientRecord>) dataSnapshot.child("records").child(conditionSet).getValue();
                                                                                if (newUserRec == null)
                                                                                    newUserRec = new ArrayList<>();
                                                                                newUserRec.add(newRec);
                                                                                database.getReference().child("Patients").child(emailWithoutSuffix(schedItem.patientEmail)).child("records").child(conditionSet).setValue(newUserRec);
                                                                                Toast.makeText(DoctorActivity.this,"Saved User Data",Toast.LENGTH_SHORT).show();
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
                                                        Log.d(TAG, "Error Occurred:" + e);
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
                            } catch (NullPointerException e){
                                Toast.makeText(DoctorActivity.this,"Empty Schedule",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "While Retrieiving Schedule: " + databaseError);
                        }
                    };
                    docSchedule.addValueEventListener(schedVEL);
                } else {

            /*-------------------------------------------------WALK IN DOCTOR-------------------------------------------------*/
                    setContentView(R.layout.activity_walk_in_doctor);
                    /////////////////////////////////////POPUP WINDOW FOR INSTRUCTIONS/////////////////////////////////////
                    final CheckBox box = new CheckBox(DoctorActivity.this);
                    SharedPreferences pref = getSharedPreferences("DOCWdoNotDisplay", MODE_PRIVATE);  //Read
                    int stopShowingChecked = Integer.parseInt(pref.getString("DOCWdoNotDisplay", "0"));
                    if (stopShowingChecked != 1) {
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DoctorActivity.this);
                        final AlertDialog alert = alertBuilder.create();

                        alertBuilder.setTitle("Instructions");
                        alertBuilder.setMessage("Enter details for each patient accordingly\nCheck the box below to signify your attendance");

                        box.setText("Do not show this message again");
                        alertBuilder.setView(box);

                        alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                alert.cancel();

                                if (box.isChecked()) {
                                    SharedPreferences prefs = getSharedPreferences("DOCWdoNotDisplay", MODE_PRIVATE); //Store
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("DOCWdoNotDisplay", "1");
                                    editor.commit();
                                }
                            }
                        });
                        alertBuilder.show();
                    }

                    /////////////////////////////////////SCHEDULE UI SETUP/////////////////////////////////////
                    schedVEL = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dbValues) {
                            Log.d(TAG, "DataSnapshot of walk in schedule: " + dbValues.getValue());
                            final ArrayList<walkInApptEntry> walkInSched = new ArrayList<>();
                            for (DataSnapshot d : dbValues.getChildren()) {
                                walkInSched.add(d.getValue(walkInApptEntry.class));
                            }

                            Collections.sort(walkInSched, new Comparator<walkInApptEntry>() {            //TODO sort array list of objects
                                public int compare(walkInApptEntry item1, walkInApptEntry item2) {
                                    return (item1.timestamp + "").compareToIgnoreCase(item2.timestamp + "");
                                }
                            });
                            if (walkInSched.size() != 0) {
                                patientEmail = walkInSched.get(0).email;
                                database.getReference().child("Patients").child(walkInSched.get(0).email).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot d) {
                                        TextView n = findViewById(R.id.patientNameLabel);
                                        n.setText("Name: " + d.getValue().toString());
                                        walkInSchedule.child(walkInSched.get(0).timestamp + "").removeValue();
                                        walkInSchedule.removeEventListener(schedVEL);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        Log.w(TAG, "onCancelled: ", error.toException());
                                    }
                                });
                            } else {
                                Toast.makeText(DoctorActivity.this, "No Patients in Queue", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.w(TAG, "onCancelled: ", error.toException());
                        }
                    };

                    CheckBox c = findViewById(R.id.attendanceButton);
                    c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            present = isChecked;
                            if (present) {
                                walkInSchedule.addValueEventListener(schedVEL);
                                database.getReference().child("numWalkInDoc").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int docs;
                                        if (dataSnapshot.getValue() != null)
                                            docs = Integer.parseInt(dataSnapshot.getValue().toString()) + 1;   //TODO to get wait time
                                        else
                                            docs = 1;
                                        database.getReference().child("numWalkInDoc").setValue(docs);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                database.getReference().child("numWalkInDoc").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int docs = Integer.parseInt(dataSnapshot.getValue().toString()) - 1;
                                        database.getReference().child("numWalkInDoc").setValue(docs);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
                }

                updateLowerInfo();

                /////////////////////////////////SPINNER UI/////////////////////////////////  //TODO PLACE ENTIRE THING IN A CLASS
                for (int i = 0; i < imageIconDatabase.length; i++) {   //TODO Spinner Dropdown
                    spinnerList.add(imageIconDatabase[i]);
                }
                final Spinner spinner = findViewById(R.id.accSpinner);
                CustomSpinnerAdapter spinAdap = new CustomSpinnerAdapter(DoctorActivity.this, R.layout.spinner_element, spinnerList);
                spinner.setAdapter(spinAdap);
                spinner.setSelection(3);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 1:
                                spinner.setSelection(3);
                                authenticationInstance.sendPasswordResetEmail(u.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(DoctorActivity.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;

                            case 0:
                                //TODO refer to manifest: android:launchMode = "singleInstance" to ensure no repeat
                                spinner.setSelection(3);
                                LinearLayout alertLayout = new LinearLayout(DoctorActivity.this);
                                alertLayout.setOrientation(LinearLayout.VERTICAL);
                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DoctorActivity.this);
                                final AlertDialog alert = alertBuilder.create();

                                alertBuilder.setTitle("Change Email");

                                TextView eTV = new TextView(DoctorActivity.this);
                                eTV.setText("New Email");
                                alertLayout.addView(eTV);
                                final EditText eET = new EditText(DoctorActivity.this);
                                eET.setText("");
                                eET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                alertLayout.addView(eET);

                                TextView pTV = new TextView(DoctorActivity.this);  //TODO ensure it is user who actually changing
                                pTV.setText("Confirm Password");
                                alertLayout.addView(pTV);
                                final EditText pET = new EditText(DoctorActivity.this);
                                pET.setText("");
                                pET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                alertLayout.addView(pET);

                                alertBuilder.setView(alertLayout);
                                alertBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (progressDialogDisplay == null) {
                                            progressDialogDisplay = new ProgressDialog(DoctorActivity.this);
                                            progressDialogDisplay.setMessage("Changing to new account");
                                            progressDialogDisplay.setIndeterminate(true);
                                        }
                                        if (!eET.getText().toString().equals("")) {
                                            if (!eET.getText().toString().equals(u.getEmail())) {
                                            AuthCredential credential = EmailAuthProvider
                                                    .getCredential(u.getEmail(), pET.getText().toString());
                                                u.reauthenticate(credential)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            Object allVal = dataSnapshot.getValue();
                                                                            Log.d(TAG, "Patient Data: " + allVal);
                                                                            Log.d(TAG, "SetValue Ref: " + database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())));
                                                                            database.getReference().child("Doctors").child(emailWithoutSuffix(eET.getText().toString())).setValue(allVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    database.getReference().child("Doctors").child(u.getEmail()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            database.getReference().child("Doctors").child(emailWithoutSuffix(eET.getText().toString())).child("email").setValue(eET.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {
                                                                                                    u.updateEmail(eET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {   //TODO JUST USE A ON SUCCESS LISTENER
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                hideLoadingView();
                                                                                                                Toast.makeText(DoctorActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();
                                                                                                                Log.d(TAG, "New Email: " + u.getEmail());
                                                                                                                updateLowerInfo();
                                                                                                                alert.dismiss();
                                                                                                            } else {
                                                                                                                Log.d(TAG, "Upon updating email: " + task.getException());
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                } else {
                                                                    Toast.makeText(DoctorActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(DoctorActivity.this, "Email cannot be the same email", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(DoctorActivity.this, "Email cannot be blank", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                alertBuilder.show();
                                break;

                            case 2:
                                spinner.setSelection(3);

                                signOut(null);
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        spinner.setSelection(3);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "get failed with ", databaseError.toException());
            }
        });
    }

    String emailWithoutSuffix(String input) {
        return input.substring(0, input.indexOf(".com")).toLowerCase();
    }

    public void addAppt(View view) {
        database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail())).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent = new Intent(DoctorActivity.this, addApptActivity.class);
                intent.putExtra("patEm", "");
                intent.putExtra("docEm", dataSnapshot.getValue().toString());
                intent.putExtra("task", "");
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void saveWalkIn(View view) {
        if (!patientEmail.equals("")) {
            EditText uET = findViewById(R.id.updateET);
            EditText pET = findViewById(R.id.presET);
            EditText dET = findViewById(R.id.diagnosisET);

            final ArrayList<String> condFinal = new ArrayList<String>(Arrays.asList(uET.getText().toString().split("\n")));
            final ArrayList<String> presFinal = new ArrayList<String>(Arrays.asList(pET.getText().toString().split("\n")));
            final String conditionSet = dET.getText().toString();
            database.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
            database.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot ds) {
                    database.getReference().child("Patients").child(patientEmail).child("records").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            patientRecord p = new patientRecord(condFinal, Long.parseLong(ds.getValue().toString()), presFinal);
                            ArrayList<patientRecord> pR = new ArrayList<>();
                            pR.add(p);
                            database.getReference().child("Patients").child(patientEmail).child("records").child(conditionSet).setValue(pR);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            uET.setText("");
            pET.setText("");
            dET.setText("");
            patientEmail = "";
            TextView n = findViewById(R.id.patientNameLabel);
            n.setText("Name: ");
            if (present) walkInSchedule.addValueEventListener(schedVEL);
        } else {
            Toast.makeText(DoctorActivity.this, "No Patients in Queue", Toast.LENGTH_SHORT).show();
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

    public void viewPastPatients(View view) {
        startActivity(new Intent(DoctorActivity.this, docPastPatActivity.class));
    }

    void updateLowerInfo() {
        try {
            Log.d(TAG, "Updating User Info:" + emailWithoutSuffix(u.getEmail()));
            userAccRef = database.getReference().child("Doctors").child(emailWithoutSuffix(u.getEmail()));
            userAccRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "userAccRef: " + userAccRef);
                    TextView nameLabel = findViewById(R.id.name);
                    TextView emailLabel = findViewById(R.id.email);
                    emailLabel.setText("Email: " + u.getEmail());
                    if (dataSnapshot.getValue() != null) {
                        String firstName = dataSnapshot.getValue().toString().split(" ")[0];   //TODO Only display first name
                        Log.d(TAG, "firstName: " + firstName);
                        nameLabel.setText("Name: " + firstName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "get failed with ", databaseError.toException());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "updateLowerInfo: ", e);
        }
    }

    @Override
    protected  void onStop() {
        try {
            CheckBox c = findViewById(R.id.attendanceButton);
            c.setChecked(false);
        }catch(NullPointerException e){
            Log.d(TAG, "Specialized Doctor, NPE for checkbox");
        }
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        docSchedule.removeEventListener(schedVEL);
        super.onDestroy();
    }   //TODO Remove listeners to prevent hogging of ram, constant updating and to reduce error messages from occurring
}