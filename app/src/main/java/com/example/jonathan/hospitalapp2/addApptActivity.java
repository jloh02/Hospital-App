package com.example.jonathan.hospitalapp2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class addApptActivity extends AppCompatActivity {

    String TAG = "addApptActivity TESTING";

    long generatedTaskID;
    EditText tc, pEET, dEET, locET, taskET, durHo, durMi;
    long apptEpochTime;

    FirebaseDatabase doctorDB = FirebaseDatabase.getInstance();
    DatabaseReference docUsers = doctorDB.getReference().child("Doctors");
    DatabaseReference appts = doctorDB.getReference().child("Appointments");
    DatabaseReference patUsers = doctorDB.getReference().child("Patients");

    FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();

    final Calendar cal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appt);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width / 3, 150);  //TODO scale buttons to 1/3 of screen width
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        findViewById(R.id.cancelButton).setLayoutParams(params);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(width / 3, 150);
        params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params2.addRule(RelativeLayout.RIGHT_OF, findViewById(R.id.cancelButton).getId());
        findViewById(R.id.generateIDButton).setLayoutParams(params2);
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(width / 3, 150);
        params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params3.addRule(RelativeLayout.RIGHT_OF, findViewById(R.id.generateIDButton).getId());
        findViewById(R.id.addApptButton).setLayoutParams(params3);

        tc = findViewById(R.id.timeChosen);
        pEET = findViewById(R.id.patEmET);
        dEET = findViewById(R.id.docEmET);
        locET = findViewById(R.id.locET);
        taskET = findViewById(R.id.taskET);
        durHo = findViewById(R.id.durHour);
        durMi = findViewById(R.id.durMinute);

        Bundle extras = getIntent().getExtras();
        pEET.setText(extras.getString("patEm", ""));
        dEET.setText(extras.getString("docEm", ""));
        taskET.setText(extras.getString("task", ""));

        tc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doctorDB.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
                doctorDB.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long epochCurrentTime = Long.parseLong(dataSnapshot.getValue().toString());
                        final Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(epochCurrentTime);

                        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // TODO Auto-generated label for year
                                cal.set(Calendar.YEAR, year);
                                cal.set(Calendar.MONTH, monthOfYear);
                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                doctorDB.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
                                doctorDB.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        TimePickerDialog timePicker;
                                        timePicker = new TimePickerDialog(addApptActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                                cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                                                cal.set(Calendar.MINUTE, selectedMinute);
                                                cal.set(Calendar.SECOND, 0);
                                                cal.set(Calendar.MILLISECOND, 0);
                                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH mm");
                                                tc.setText(sdf.format(cal.getTime()));
                                            }
                                        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                                        timePicker.setTitle("Select Appointment Time");
                                        timePicker.show();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        };

                        new DatePickerDialog(addApptActivity.this, dateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void cancelReg(View view) {
        finish();
    }

    public void generateID(View view)
    {   //TODO Acts as primary key
        try {
            Date d = cal.getTime();
            apptEpochTime = d.getTime() / 1000;
            Log.d(TAG, "apptEpochTime: " + apptEpochTime);
            String apptEpochTimeStr = apptEpochTime + "";

            String locString = locET.getText().toString();
            String locNumString = "";
            for (int i = 0; i < locString.length(); i++) {
                char partStr = locString.charAt(i);
                if (partStr >= 48 && partStr <= 57)
                    locNumString += partStr;   //TODO ACSII value for 0-9
            }
            String stringID = apptEpochTimeStr + locNumString;

            long ID = Long.parseLong(stringID);

            generatedTaskID = ID;
            Log.d(TAG, "generateID: " + generatedTaskID);
            TextView apptIDTV = findViewById(R.id.taskIDTV);
            apptIDTV.setText("Generated Appointment ID:\n" + ID);
        } catch (NullPointerException e) {
            Toast.makeText(addApptActivity.this, "Fields cannot be left blank",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void addAppointment(View view) {
        generateID(null);
        boolean cancel1 = true;
        boolean cancel2 = true;
        boolean cancel3 = false;
        if (locET.getText().toString().equals("") || taskET.getText().toString().equals(""))
            cancel3 = true;

        long durationH = 0;
        Log.d(TAG, "durHo: " + durHo.getText().toString());
        if (!durHo.getText().toString().equals("")) {
            durationH = Long.parseLong(durHo.getText().toString());
            cancel1 = false;
        }
        long durationM = 0;
        Log.d(TAG, "durMi: " + durMi.getText().toString());
        if (!durMi.getText().toString().equals("")) {
            durationM = Long.parseLong(durMi.getText().toString());
            cancel2 = false;
        }
        final long dura = (durationH * 3600) + (durationM * 60);

        if (!(cancel1 && cancel2) && !cancel3) {
            docUsers.child(emailWithoutSuffix(u.getEmail())).child("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnap) {
                    final ArrayList<Long> docApptList;
                    if (dataSnap.getValue() != null) {
                        docApptList = (ArrayList<Long>) dataSnap.getValue();
                        Collections.sort(docApptList);   //Ensure ascending order
                    } else {
                        docApptList = new ArrayList<>();
                        Log.d(TAG, "dataSnap.getValue() == null");
                    }
                    final boolean[] acceptable = new boolean[1];
                    acceptable[0] = true;
                    Log.d(TAG, "docApptList.size(): " + docApptList.size());
                    if (docApptList.size() != 0) {
                        for (int i = 0; i < docApptList.size(); i++) {
                            final int i2 = i;
                            appts.child(docApptList.get(i) + "").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    scheduleItem appt = dataSnapshot.getValue(scheduleItem.class);
                                    long timeOfAppt = appt.timestamp;
                                    long durOfAppt = appt.duration;
                                    Log.d(TAG, "timeOfAppt: " + timeOfAppt);
                                    Log.d(TAG, "durOfAppt: " + durOfAppt);
                                    Log.d(TAG, "apptEpochTime: " + apptEpochTime);
                                    Log.d(TAG, "dura: " + dura);
                                    if (apptEpochTime >= timeOfAppt && apptEpochTime <= timeOfAppt + durOfAppt) { //startTime Within
                                        Log.d(TAG, "Start Time Within");
                                        Toast.makeText(addApptActivity.this, "Doctor unavailable:\nPlease change start time", Toast.LENGTH_SHORT).show();
                                        acceptable[0] = false;
                                    }
                                    if (apptEpochTime + dura >= timeOfAppt && apptEpochTime + dura <= timeOfAppt + durOfAppt) { //endTime Within
                                        Log.d(TAG, "End Time Within");
                                        Toast.makeText(addApptActivity.this, "Doctor unavailable:\nPlease change duration", Toast.LENGTH_SHORT).show();
                                        acceptable[0] = false;
                                    }

                                    Log.d(TAG, "acceptable[0] && i2 == docApptList.size() - 1: " + (acceptable[0] && i2 == docApptList.size() - 1));
                                    if (acceptable[0] && i2 >= docApptList.size() - 1) {
                                        //TODO HANDLE INVALID USERS
                                        try {
                                            patUsers.child(emailWithoutSuffix(pEET.getText().toString()).toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    try {
                                                        String eTest = dataSnapshot.child("email").getValue().toString();
                                                        if (eTest.equals(""))
                                                            throw new NullPointerException("Empty email");
                                                        ArrayList<Long> patSched = new ArrayList<>();
                                                        if (dataSnapshot.child("appointments").getValue() != null)
                                                            patSched = (ArrayList<Long>) dataSnapshot.child("appointments").getValue();
                                                        patSched.add(generatedTaskID);
                                                        patUsers.child(emailWithoutSuffix(pEET.getText().toString()).toLowerCase()).child("appointments").setValue(patSched);

                                                        Log.d(TAG, "Going to register to DB");
                                                        appts.child(generatedTaskID + "").setValue(new scheduleItem(apptEpochTime, dura, pEET.getText().toString(), dEET.getText().toString(), locET.getText().toString(), taskET.getText().toString(), generatedTaskID));

                                                        docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                ArrayList<Long> docSched = new ArrayList<>();
                                                                if (dataSnapshot.getValue() != null)
                                                                    docSched = (ArrayList<Long>) dataSnapshot.getValue();
                                                                docSched.add(generatedTaskID);
                                                                docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("schedule").setValue(docSched);
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                Log.d(TAG, "docUsers sched read failed");
                                                            }
                                                        });

                                                        docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("pastPatients").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                String enteredPatEm = emailWithoutSuffix(pEET.getText().toString().toLowerCase());
                                                                ArrayList<String> allPast = new ArrayList<>();
                                                                if (dataSnapshot.getValue() != null)
                                                                    allPast = (ArrayList<String>) dataSnapshot.getValue();
                                                                boolean repeated = false;
                                                                for (String singleEm : allPast)
                                                                    if (singleEm.equals(enteredPatEm))
                                                                        repeated = true;
                                                                if (!repeated) allPast.add(enteredPatEm);
                                                                docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("pastPatients").setValue(allPast);

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                                Log.d(TAG, "docUsers pastPatients read failed");
                                                            }
                                                        });


                                                        finish();

                                                    } catch (NullPointerException e) {
                                                        Toast.makeText(addApptActivity.this, "Invalid patient email", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "Invalid Patient: " + e);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(addApptActivity.this, "Invalid patient email", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (DatabaseException e) {
                                            Toast.makeText(addApptActivity.this, "Invalid patient email", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "Invalid Patient: " + e);
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "Appts read failed");
                                }
                            });
                            if (!acceptable[0]) break;
                        }
                    } else {   //TODO Skip time validation
                        //TODO handle invalid users
                        try {
                            patUsers.child(emailWithoutSuffix(pEET.getText().toString()).toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        String eTest = dataSnapshot.child("email").getValue().toString();
                                        if (eTest.equals(""))
                                            throw new NullPointerException("Empty email");
                                        ArrayList<Long> patSched = new ArrayList<>();
                                        if (dataSnapshot.child("appointments").getValue() != null)
                                            patSched = (ArrayList<Long>) dataSnapshot.child("appointments").getValue();
                                        patSched.add(generatedTaskID);
                                        patUsers.child(emailWithoutSuffix(pEET.getText().toString()).toLowerCase()).child("appointments").setValue(patSched);

                                        Log.d(TAG, "Going to register to DB");
                                        appts.child(generatedTaskID + "").setValue(new scheduleItem(apptEpochTime, dura, pEET.getText().toString(), dEET.getText().toString(), locET.getText().toString(), taskET.getText().toString(), generatedTaskID));

                                        docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                ArrayList<Long> docSched = new ArrayList<>();
                                                if (dataSnapshot.getValue() != null)
                                                    docSched = (ArrayList<Long>) dataSnapshot.getValue();
                                                docSched.add(generatedTaskID);
                                                docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("schedule").setValue(docSched);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(TAG, "docUsers sched read failed");
                                            }
                                        });

                                        docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("pastPatients").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String enteredPatEm = emailWithoutSuffix(pEET.getText().toString().toLowerCase());
                                                ArrayList<String> allPast = new ArrayList<>();
                                                if (dataSnapshot.getValue() != null)
                                                    allPast = (ArrayList<String>) dataSnapshot.getValue();
                                                boolean repeated = false;
                                                for (String singleEm : allPast)
                                                    if (singleEm.equals(enteredPatEm))
                                                        repeated = true;
                                                if (!repeated) allPast.add(enteredPatEm);
                                                docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("pastPatients").setValue(allPast);

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(TAG, "docUsers pastPatients read failed");
                                            }
                                        });


                                        finish();

                                    } catch (NullPointerException e) {
                                        Toast.makeText(addApptActivity.this, "Invalid patient email", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Invalid Patient: " + e);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(addApptActivity.this, "Invalid patient email", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (DatabaseException e) {
                            Toast.makeText(addApptActivity.this, "Invalid patient email", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Invalid Patient: " + e);
                        }
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "large loop docUsers sched read failed");
                }
            });
        } else {
            Toast.makeText(addApptActivity.this, "Fields cannot be empty",
                    Toast.LENGTH_SHORT).show();
        }
    }


    String emailWithoutSuffix(String input) {
        if (!input.contains(".com")) return input;
        else return input.substring(0, input.indexOf(".com"));
    }
}
