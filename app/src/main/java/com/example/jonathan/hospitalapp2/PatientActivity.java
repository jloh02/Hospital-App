package com.example.jonathan.hospitalapp2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Collections;

public class PatientActivity extends AppCompatActivity {

    String TAG = "PATIENT ACTIVITY TESTING";

    ProgressDialog progressDialogDisplay;
    FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    final FirebaseUser u = authenticationInstance.getCurrentUser();
    GoogleSignInClient GsignInClient;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference patAccRef = database.getReference().child("Patients").child(emailWithoutSuffix(u.getEmail()));
    DatabaseReference allSchedule = database.getReference().child("Appointments");

    ValueEventListener presVEL, apptVEL, walkInVEL;
    String conditionList;
    ArrayList<String> presList = new ArrayList<>();
    ArrayList<scheduleItem> apptList = new ArrayList<>();
    ListView preslv, apptlv;
    TextView conds;

    int recurCount;
    boolean dataReceived;

    private static Integer[] imageIconDatabase = {R.drawable.baseline_email_24px,  //TODO Setup for spinner
            R.drawable.baseline_vpn_key_24px, R.drawable.baseline_exit_to_app_24px, R.drawable.baseline_settings_20px};
    ArrayList<Integer> spinnerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        /////////////////////////////////SPINNER UI/////////////////////////////////
        for (int i = 0; i < imageIconDatabase.length; i++) {   //TODO Spinner Dropdown
            spinnerList.add(imageIconDatabase[i]);
        }
        final Spinner spinner = findViewById(R.id.accSpinner);
        CustomSpinnerAdapter spinAdap = new CustomSpinnerAdapter(this, R.layout.spinner_element, spinnerList);
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
                                Toast.makeText(PatientActivity.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;

                    case 0:
                        //TODO refer to manifest: android:launchMode = "singleInstance" to ensure no repeat
                        spinner.setSelection(3);
                        LinearLayout alertLayout = new LinearLayout(PatientActivity.this);
                        alertLayout.setOrientation(LinearLayout.VERTICAL);
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PatientActivity.this);
                        final AlertDialog alert = alertBuilder.create();

                        alertBuilder.setTitle("Change Email");

                        TextView eTV = new TextView(PatientActivity.this);
                        eTV.setText("New Email");
                        alertLayout.addView(eTV);
                        final EditText eET = new EditText(PatientActivity.this);
                        eET.setText("");
                        eET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        alertLayout.addView(eET);

                        TextView pTV = new TextView(PatientActivity.this);  //TODO ensure it is user who actually changing
                        pTV.setText("Confirm Password");
                        alertLayout.addView(pTV);
                        final EditText pET = new EditText(PatientActivity.this);
                        pET.setText("");
                        pET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        alertLayout.addView(pET);

                        alertBuilder.setView(alertLayout);
                        alertBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                if (progressDialogDisplay == null) {
                                    progressDialogDisplay = new ProgressDialog(PatientActivity.this);
                                    progressDialogDisplay.setMessage("Changing to new account");
                                    progressDialogDisplay.setIndeterminate(true);
                                }
                                progressDialogDisplay.show();
                                if (!eET.getText().toString().equals("")) {
                                    if (!eET.getText().toString().equals(u.getEmail())) {
                                        AuthCredential credential = EmailAuthProvider
                                                .getCredential(u.getEmail(), pET.getText().toString());
                                        u.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            database.getReference().child("Patients").child(emailWithoutSuffix(u.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    Object allVal = dataSnapshot.getValue();
                                                                    Log.d(TAG, "Patient Data: " + allVal);
                                                                    Log.d(TAG, "SetValue Ref: " + database.getReference().child("Patients").child(emailWithoutSuffix(u.getEmail())));
                                                                    database.getReference().child("Patients").child(emailWithoutSuffix(eET.getText().toString())).setValue(allVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            database.getReference().child("Patients").child(u.getEmail()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    Log.d(TAG, "patAccRef set email ref: " + patAccRef.child("email"));
                                                                                    database.getReference().child("Patients").child(emailWithoutSuffix(eET.getText().toString())).child("email").setValue(eET.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            u.updateEmail(eET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        hideLoadingView();
                                                                                                        Toast.makeText(PatientActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();
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
                                                            Toast.makeText(PatientActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(PatientActivity.this, "Email cannot be the same email", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(PatientActivity.this, "Email cannot be blank", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        alertBuilder.show();
                        break;

                    case 2:
                        spinner.setSelection(3);
                        signOut();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner.setSelection(3);
            }
        });


        preslv = findViewById(R.id.prescriptionList);

        apptlv = findViewById(R.id.apptList);

        conds = findViewById(R.id.conditionTV);

        GoogleSignInOptions GsignInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GsignInClient = GoogleSignIn.getClient(this, GsignInOpt);

        updateLowerInfo();

        /////////////////////////////////////POPUP WINDOW FOR INSTRUCTIONS/////////////////////////////////////
        final CheckBox box = new CheckBox(this);
        SharedPreferences pref = getSharedPreferences("PATdoNotDisplay", MODE_PRIVATE);  //Read
        int stopShowingChecked = Integer.parseInt(pref.getString("PATdoNotDisplay", "0"));
        if (stopShowingChecked != 1)

        {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PatientActivity.this);
            final AlertDialog alert = alertBuilder.create();

            alertBuilder.setTitle("Instructions");
            alertBuilder.setMessage("Each item in your prescription can be clicked to do a Google Search on the relevant drug\nYour appointments listed below can be clicked on to request to modify");

            box.setText("Do not show this message again");
            alertBuilder.setView(box);

            alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    alert.cancel();

                    if (box.isChecked()) {
                        SharedPreferences prefs = getSharedPreferences("PATdoNotDisplay", MODE_PRIVATE); //Store
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("PATdoNotDisplay", "1");
                        editor.commit();
                    }
                }
            });
            alertBuilder.show();
        }


        /////////////////////////////////////USER UI SETUP/////////////////////////////////////
        presVEL = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dbValues) {
                        Log.d(TAG, "DataSnapshot of Items: " + dbValues.getValue());
                        ArrayList<String> allCond = new ArrayList<>();
                        ArrayList<String> allPres = new ArrayList<>();
                        try {
                            for (DataSnapshot snapshotItem : dbValues.getChildren()) {
                                String condition = snapshotItem.getKey();
                                allCond.add(condition);
                                Log.d(TAG, "condition: " + condition);
                                for (DataSnapshot patientRecItem : snapshotItem.getChildren()) {
                                    ArrayList<String> pre = patientRecItem.getValue(patientRecord.class).prescription;
                                    Log.d(TAG, "prescription: " + pre);
                                    allPres.addAll(pre);
                                }
                            }

                            Collections.sort(allCond);
                            Collections.sort(allPres);

                            for (int i = 0; i < allPres.size(); i++) {
                                if (allPres.get(i).equals("")) {
                                    allPres.remove(i);
                                    i--;
                                }
                            }
                            presList = allPres;

                            conditionList = allCond.get(0);
                            for (int i = 1; i < allCond.size(); i++)
                                conditionList += "\n" + allCond.get(i);

                            conds.setText(conditionList);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(PatientActivity.this, android.R.layout.simple_list_item_1, presList);
                            preslv.setAdapter(adapter);
                            preslv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, ((TextView) view).getText().toString());
                                    startActivity(intent);
                                }
                            });
                        } catch (NullPointerException e) {
                            Log.d(TAG, "Empty Patient Record");
                        } catch (IndexOutOfBoundsException e) {
                            Log.d(TAG, "Empty Patient Record");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError e) {
                        Log.d(TAG, "onCancelled:" + e);
                    }
                };
        patAccRef.child("records").addValueEventListener(presVEL);

        apptVEL = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            final ArrayList<Long> appts = (ArrayList<Long>) dataSnapshot.getValue();
                            for (long l : appts) {
                                allSchedule.child(l + "").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot ds) {
                                        apptList.add(ds.getValue(scheduleItem.class));
                                        patientApptCustomAdapter paca = new patientApptCustomAdapter(PatientActivity.this, apptList);
                                        apptlv.setAdapter(paca);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.w(TAG, "Database Error in allSchedule Listener: ", databaseError.toException());
                                    }
                                });
                            }
                        } catch (NullPointerException e) {
                            Toast.makeText(PatientActivity.this, "No Appointments", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "Database Error on apptVEL: ", databaseError.toException());
                    }
                };
        patAccRef.child("appointments").addValueEventListener(apptVEL);

        walkInVEL = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final long queueSize = dataSnapshot.getChildrenCount();
                        database.getReference().child("numWalkInDoc").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int numDoc;
                                if (dataSnapshot.getValue() == null) numDoc = 0;
                                else numDoc = Integer.parseInt(dataSnapshot.getValue().toString());
                                Button walkInApptButton = findViewById(R.id.newApptButton);
                                if (numDoc != 0) {
                                    int waitTime = (int) (queueSize - numDoc) / numDoc * 15;
                                    if (waitTime < 0) waitTime = 0;
                                    walkInApptButton.setText("Register Walk In (est. wait time: " + waitTime + " min)");
                                } else {
                                    walkInApptButton.setText("Register Walk In (est. wait time: Unknown)");
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
        database.getReference().child("WalkInAppointments").addValueEventListener(walkInVEL);
    }

    void updateLowerInfo() {
        try {
            final FirebaseUser fU = authenticationInstance.getCurrentUser();
            Log.d(TAG, "Updating User Info:" + emailWithoutSuffix(fU.getEmail()));
            patAccRef = database.getReference().child("Patients").child(emailWithoutSuffix(fU.getEmail()));
            patAccRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TextView nameLabel = findViewById(R.id.name);
                    TextView emailLabel = findViewById(R.id.email);
                    if (dataSnapshot.getValue() != null) {
                        String firstName = dataSnapshot.getValue().toString().split(" ")[0];   //TODO Only display first name
                        nameLabel.setText("Name: " + firstName);
                    }
                    emailLabel.setText("Email: " + fU.getEmail());
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

    String emailWithoutSuffix(String input) {
        return input.substring(0, input.indexOf(".com")).toLowerCase();
    }

    public void signOut() {
        showLogoutLoading();
        if (authenticationInstance.getCurrentUser() != null) {
            authenticationInstance.signOut();
            GsignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideLoadingView();
                            patAccRef.child("records").removeEventListener(presVEL);
                            patAccRef.child("appointments").removeEventListener(apptVEL);
                            finish();
                        }
                    });
        }
    }

    public void regWalkIn(View view) {
        database.getReference().child("currentTime").setValue(ServerValue.TIMESTAMP);
        database.getReference().child("currentTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot ds) {
                long t = Long.parseLong(ds.getValue().toString());
                //t = 123456789L;                                   //For testing
                Log.d(TAG, "t initial: " + t);
                String e = emailWithoutSuffix(u.getEmail());
                recurCount = 0;
                regNextAvailableSlot(e, t);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void displayNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
    }

    void regNextAvailableSlot(final String em, final long a) {  //TODO Recursive Method
        recurCount++;
        if (recurCount <= 30) {  //TODO Cutoff
            dataReceived = false;
            database.getReference().child("WalkInAppointments").child(a + "").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.getValue() != null) {
                            Log.d(TAG, "Datasnapshot Val Taken: " + dataSnapshot.getValue());
                            long b = a;
                            b++;
                            regNextAvailableSlot(em, b);
                        } else {
                            final walkInApptEntry w = new walkInApptEntry(a, em);
                            Log.d(TAG, "Walk In Appt saved: " + w.timestamp);

                            final ValueEventListener notifyVEL = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() == null) {
                                        Log.d(TAG, "Datasnapshot: " + dataSnapshot.getValue());
                                        displayNotification();
                                        database.getReference().child("WalkInAppointments").child(a + "").removeEventListener(this);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            };

                            database.getReference().child("WalkInAppointments").child(a + "").setValue(w).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    database.getReference().child("WalkInAppointments").child(a + "").addValueEventListener(notifyVEL);
                                    Toast.makeText(PatientActivity.this, "Walk In Appointment Registered", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    } catch (NullPointerException e) {
                        Log.d(TAG, "NPE Caught: " + e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else

        {
            Toast.makeText(PatientActivity.this, "Register failed. Slots are currently full. Please try again later", Toast.LENGTH_SHORT).show();
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
