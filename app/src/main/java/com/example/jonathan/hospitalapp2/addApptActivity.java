package com.example.jonathan.hospitalapp2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class addApptActivity extends AppCompatActivity {

    String TAG = "addApptActivity TESTING";

    long generatedTaskID;
    EditText ye, mo, da, ho, minu, pEET, dEET, locET, taskET, durHo, durMi;
    long apptEpochTime;

    FirebaseDatabase doctorDB = FirebaseDatabase.getInstance();
    DatabaseReference docUsers = doctorDB.getReference().child("Doctors");
    DatabaseReference appts = doctorDB.getReference().child("Appointments");
    DatabaseReference patUsers = doctorDB.getReference().child("Patients");

    //TODO AUTO FILL CERTAIN PARTICULARS IF CERTAIN USERS

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

        ye = findViewById(R.id.year);
        mo = findViewById(R.id.month);
        da = findViewById(R.id.day);
        ho = findViewById(R.id.hour);
        minu = findViewById(R.id.minute);
        pEET = findViewById(R.id.patEmET);
        dEET = findViewById(R.id.docEmET);
        locET = findViewById(R.id.locET);
        taskET = findViewById(R.id.taskET);
        durHo = findViewById(R.id.durHour);
        durMi = findViewById(R.id.durMinute);
    }

    public void cancelReg(View view) {
        finish();
    }

    public void generateID(View view) {   //TODO Acts as primary key
        try {
            int y = Integer.parseInt(ye.getText().toString());
            int m = Integer.parseInt(mo.getText().toString());
            int d = Integer.parseInt(da.getText().toString());
            int h = Integer.parseInt(ho.getText().toString());
            int min = Integer.parseInt(minu.getText().toString());

            apptEpochTime = yymmddhhmmToEpoch(y, m, d, h, min);
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
            TextView apptIDTV = findViewById(R.id.taskIDTV);
            apptIDTV.setText("Generated Appointment ID:\n" + ID);
        } catch (NullPointerException e) {
            Toast.makeText(addApptActivity.this, "Fields cannot be left blank",
                    Toast.LENGTH_SHORT).show();
        }
    }

    long yymmddhhmmToEpoch(int year, int month, int day, int hour, int minute) {
        String outputDateTime = String.format("%4d%2d%2d%2d%2d", year, month, day, hour, minute);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        long epochTime = 0;
        try {
            Date dt = sdf.parse(outputDateTime);
            epochTime = dt.getTime();
        } catch (ParseException e) {
            Log.d(TAG, "yymmddhhmmToEpoch: e");
        }
        return (long) (epochTime / 1000);
    }

    public void addAppointment(View view) {
        try {
            generateID(null);
            int dura = (Integer.parseInt(durHo.getText().toString()) * 3600) + (Integer.parseInt(durMi.getText().toString()) * 60);
            appts.child(generatedTaskID + "").setValue(new scheduleItem(apptEpochTime, dura, pEET.getText().toString(), dEET.getText().toString(), locET.getText().toString(), taskET.getText().toString(),generatedTaskID));

            //TODO HANDLE INVALID USERS

            docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("schedule").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Long> docSched = new ArrayList<>();
                    if(dataSnapshot.getValue() != null) docSched = (ArrayList<Long>) dataSnapshot.getValue();
                    docSched.add(generatedTaskID);
                    docUsers.child(emailWithoutSuffix(dEET.getText().toString()).toLowerCase()).child("schedule").setValue(docSched);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            patUsers.child(emailWithoutSuffix(pEET.getText().toString()).toLowerCase()).child("appointments").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Long> patSched = new ArrayList<>();
                    if(dataSnapshot.getValue() != null) patSched = (ArrayList<Long>) dataSnapshot.getValue();
                    patSched.add(generatedTaskID);
                    patUsers.child(emailWithoutSuffix(pEET.getText().toString()).toLowerCase()).child("appointments").setValue(patSched);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            finish();
        } catch (NullPointerException e) {
            Toast.makeText(addApptActivity.this, "Fields cannot be left blank",
                    Toast.LENGTH_SHORT).show();
        }
    }

    String emailWithoutSuffix(String input) {
        return input.substring(0, input.indexOf(".com"));
    }
}
