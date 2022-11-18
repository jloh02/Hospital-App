package com.example.jonathan.hospitalapp2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
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
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    String TAG = "ADMIN ACTIVITY TESTING";

    ProgressDialog progressDialogDisplay;
    FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    final FirebaseUser u = authenticationInstance.getCurrentUser();
    GoogleSignInClient GsignInClient;

    ValueEventListener adminVEL, docVEL, nurVEL, patVEL, careVEL;

    ArrayList<DBAfieldItem> dbaFields;
    ArrayList<docFieldItem> docFields;
    ArrayList<nurFieldItem> nurFields;
    ArrayList<patFieldItem> patFields;
    ArrayList<careFieldItem> careFields;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbaAccRef = database.getReference().child("Administrators");
    DatabaseReference docAccRef = database.getReference().child("Doctors");
    DatabaseReference nurAccRef = database.getReference().child("Nurses");
    DatabaseReference patAccRef = database.getReference().child("Patients");
    DatabaseReference careAccRef = database.getReference().child("Caretakers");

    private static Integer[] imageIconDatabase = {R.drawable.baseline_email_24px,  //TODO Setup for spinner
            R.drawable.baseline_vpn_key_24px, R.drawable.baseline_exit_to_app_24px, R.drawable.baseline_settings_20px};
    ArrayList<Integer> spinnerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

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
                                Toast.makeText(AdminActivity.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;

                    case 0:
                        //TODO refer to manifest: android:launchMode = "singleInstance" to ensure no repeat
                        spinner.setSelection(3);
                        LinearLayout alertLayout = new LinearLayout(AdminActivity.this);
                        alertLayout.setOrientation(LinearLayout.VERTICAL);
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
                        final AlertDialog alert = alertBuilder.create();

                        alertBuilder.setTitle("Change Email");

                        TextView eTV = new TextView(AdminActivity.this);
                        eTV.setText("New Email");
                        alertLayout.addView(eTV);
                        final EditText eET = new EditText(AdminActivity.this);
                        eET.setText("");
                        eET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        alertLayout.addView(eET);

                        TextView pTV = new TextView(AdminActivity.this);  //TODO ensure it is user who actually changing
                        pTV.setText("Confirm Password");
                        alertLayout.addView(pTV);
                        final EditText pET = new EditText(AdminActivity.this);
                        pET.setText("");
                        pET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        alertLayout.addView(pET);

                        alertBuilder.setView(alertLayout);
                        alertBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                if (!eET.getText().toString().equals("")) {
                                    if (!eET.getText().toString().equals(u.getEmail())) {
                                        if (progressDialogDisplay == null) {
                                            progressDialogDisplay = new ProgressDialog(AdminActivity.this);
                                            progressDialogDisplay.setMessage("Changing to new account");
                                            progressDialogDisplay.setIndeterminate(true);
                                        }
                                        AuthCredential credential = EmailAuthProvider
                                                .getCredential(u.getEmail(), pET.getText().toString());
                                        u.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            database.getReference().child("Administrators").child(emailWithoutSuffix(u.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    Object allVal = dataSnapshot.getValue();
                                                                    Log.d(TAG, "Patient Data: " + allVal);
                                                                    Log.d(TAG, "SetValue Ref: " + database.getReference().child("Administrators").child(emailWithoutSuffix(u.getEmail())));
                                                                    database.getReference().child("Administrators").child(emailWithoutSuffix(eET.getText().toString())).setValue(allVal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            database.getReference().child("Administrators").child(u.getEmail()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    Log.d(TAG, "patAccRef set email ref: " + patAccRef.child("email"));
                                                                                    database.getReference().child("Administrators").child(emailWithoutSuffix(eET.getText().toString())).child("email").setValue(eET.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            u.updateEmail(eET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        hideLoadingView();
                                                                                                        Toast.makeText(AdminActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();
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
                                                            Toast.makeText(AdminActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(AdminActivity.this, "Email cannot be the same email", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(AdminActivity.this, "Email cannot be blank", Toast.LENGTH_SHORT).show();
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


        /////////////////////////////////SIGN IN INTERFACE/////////////////////////////////
        GoogleSignInOptions GsignInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GsignInClient = GoogleSignIn.getClient(this, GsignInOpt);

        updateLowerInfo();

        /////////////////////////////////////POPUP WINDOW FOR INSTRUCTIONS/////////////////////////////////////
        final CheckBox box = new CheckBox(this);
        SharedPreferences pref = getSharedPreferences("DBAdoNotDisplay", MODE_PRIVATE);  //Read
        int stopShowingChecked = Integer.parseInt(pref.getString("DBAdoNotDisplay", "0"));
        if (stopShowingChecked != 1) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
            final AlertDialog alert = alertBuilder.create();

            alertBuilder.setTitle("Instructions");
            alertBuilder.setMessage("There are 5 tabs to modify or add users\nTap on a account to modify\nA popup window will open when the button at the bottom of the screen is tapped to create a new account");

            box.setText("Do not show this message again");
            alertBuilder.setView(box);

            alertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    alert.cancel();

                    if (box.isChecked()) {
                        SharedPreferences prefs = getSharedPreferences("DBAdoNotDisplay", MODE_PRIVATE); //Store
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("DBAdoNotDisplay", "1");
                        editor.commit();
                    }
                }
            });
            alertBuilder.show();
        }

        /////////////////////////////////////SETUP TAB HOST/////////////////////////////////////
        TabHost host = (TabHost) findViewById(R.id.tab_host);
        host.setup();

        TabHost.TabSpec spec = host.newTabSpec("Admin");
        spec.setContent(R.id.Admin);
        spec.setIndicator("Admin");
        host.addTab(spec);

        spec = host.newTabSpec("Doctor");
        spec.setContent(R.id.Doctor);
        spec.setIndicator("Doctor");
        host.addTab(spec);

        spec = host.newTabSpec("Nurse");
        spec.setContent(R.id.Nurse);
        spec.setIndicator("Nurse");
        host.addTab(spec);

        spec = host.newTabSpec("Patient");
        spec.setContent(R.id.Patient);
        spec.setIndicator("Patient");
        host.addTab(spec);

        spec = host.newTabSpec("Caretaker");
        spec.setContent(R.id.Caretaker);
        spec.setIndicator("Caretaker");
        host.addTab(spec);

        for (int i = 0; i < host.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) host.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
        }

        /////////////////////////////////////DBA ACC DISPLAY SETUP/////////////////////////////////////
        dbaFields = new ArrayList<>();
        adminVEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                dbaFields.clear();
                for (DataSnapshot document : dbValues.getChildren()) {
                    try {
                        String ne = document.child("name").getValue().toString();
                        long cn = Long.parseLong(document.child("contactNumber").getValue().toString());
                        String el = document.child("email").getValue().toString();
                        DBAfieldItem entry = new DBAfieldItem(ne, cn + "", el);
                        dbaFields.add(entry);
                    } catch (Exception error) {
                        Log.w(TAG, "Error occured while retrieving:", error);
                    }
                }

                Collections.sort(dbaFields, new Comparator<DBAfieldItem>() {            //TODO array list
                    public int compare(DBAfieldItem item1, DBAfieldItem item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });

                DBACustomAdapter adapter = new DBACustomAdapter(AdminActivity.this, R.layout.dbapatnurfielditemlayout, dbaFields);
                final ListView lv = findViewById(R.id.adminList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Object itemInfo = lv.getItemAtPosition(position);
                        final DBAfieldItem dbaItemInfo = (DBAfieldItem) itemInfo;
                        final String emailSearch = emailWithoutSuffix(dbaItemInfo.email);
                        dbaAccRef.child(emailSearch).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dbValues) {
                                String nameField;
                                String phoneNumberField;
                                try {
                                    nameField = dbValues.child("name").getValue().toString();
                                    phoneNumberField = dbValues.child("contactNumber").getValue().toString();

                                    LinearLayout alertLayout = new LinearLayout(AdminActivity.this);
                                    alertLayout.setOrientation(LinearLayout.VERTICAL);
                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
                                    final AlertDialog alert = alertBuilder.create();

                                    alertBuilder.setTitle("Update Existing DBA");

                                    TextView nameTV = new TextView(AdminActivity.this);
                                    nameTV.setText("Name:");
                                    alertLayout.addView(nameTV);
                                    final EditText nameInput = new EditText(AdminActivity.this);
                                    nameInput.setText(nameField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                    alertLayout.addView(nameInput);

                                    TextView contactTV = new TextView(AdminActivity.this);
                                    contactTV.setText("Phone Number:");
                                    alertLayout.addView(contactTV);
                                    final EditText contactInput = new EditText(AdminActivity.this);
                                    contactInput.setText(phoneNumberField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    alertLayout.addView(contactInput);

                                    TextView emailpwTV = new TextView(AdminActivity.this);
                                    emailpwTV.setText("Email and password can only reset by user");
                                    alertLayout.addView(emailpwTV);

                                    alertBuilder.setView(alertLayout);

                                    alertBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Map<String, Object> dbaUser = new HashMap<>();
                                            dbaUser.put("name", nameInput.getText().toString());
                                            dbaUser.put("contactNumber", contactInput.getText().toString());
                                            dbaUser.put("email", dbaItemInfo.email);

                                            dbaAccRef.child(emailSearch)
                                                    .setValue(dbaUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "DBA User Updated",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });
                                        }
                                    });

                                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            alert.cancel();
                                        }
                                    });

                                    alertBuilder.show();
                                } catch (Exception e) {
                                    Log.w(TAG, "Error Occurred:", e);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(AdminActivity.this, "Unable to Update Details",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Unable to Retrieve Users",
                        Toast.LENGTH_LONG).show();
            }
        };
        dbaAccRef.addValueEventListener(adminVEL);
        /////////////////////////////////////DOCTOR ACC DISPLAY SETUP/////////////////////////////////////
        docFields = new ArrayList<>();
        docVEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                docFields.clear();
                for (DataSnapshot document : dbValues.getChildren()) {
                    try {
                        String ne = document.child("name").getValue().toString();
                        long cn = Long.parseLong(document.child("contactNumber").getValue().toString());
                        String el = document.child("email").getValue().toString();
                        Log.d(TAG, "Special: " + document.child("specialisation").getValue());
                        ArrayList<String> sp = (ArrayList<String>) document.child("specialisation").getValue();
                        docFieldItem entry = new docFieldItem(ne, cn + "", el, sp);
                        docFields.add(entry);
                    } catch (Exception error) {
                        Log.w(TAG, "Error occured while retrieving:", error);
                    }
                }

                Collections.sort(docFields, new Comparator<docFieldItem>() {            //TODO sort array list
                    public int compare(docFieldItem item1, docFieldItem item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });

                docCustomAdapter adapter = new docCustomAdapter(AdminActivity.this, R.layout.docfielditemlayout, docFields);
                final ListView lv = findViewById(R.id.doctorList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Object itemInfo = lv.getItemAtPosition(position);
                        final docFieldItem docItemInfo = (docFieldItem) itemInfo;
                        final String emailSearch = emailWithoutSuffix(docItemInfo.email);
                        docAccRef.child(emailSearch).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dbValues) {
                                String nameField;
                                String phoneNumberField;
                                ArrayList<String> specialArr;
                                String specialisationField = "";
                                try {
                                    nameField = dbValues.child("name").getValue().toString();
                                    phoneNumberField = dbValues.child("contactNumber").getValue().toString();
                                    specialArr = (ArrayList<String>) dbValues.child("specialisation").getValue();
                                    Log.d(TAG, "Specialisation: " + specialArr.get(0));
                                    specialisationField = specialArr.get(0);
                                    for (int i = 1; i < specialArr.size(); i++) {
                                        specialisationField += "\n" + specialArr.get(i);
                                    }

                                    LinearLayout alertLayout = new LinearLayout(AdminActivity.this);
                                    alertLayout.setOrientation(LinearLayout.VERTICAL);
                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
                                    final AlertDialog alert = alertBuilder.create();

                                    alertBuilder.setTitle("Update Existing Doctor");

                                    TextView nameTV = new TextView(AdminActivity.this);
                                    nameTV.setText("Name:");
                                    alertLayout.addView(nameTV);
                                    final EditText nameInput = new EditText(AdminActivity.this);
                                    nameInput.setText(nameField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                    alertLayout.addView(nameInput);

                                    TextView contactTV = new TextView(AdminActivity.this);
                                    contactTV.setText("Phone Number:");
                                    alertLayout.addView(contactTV);
                                    final EditText contactInput = new EditText(AdminActivity.this);
                                    contactInput.setText(phoneNumberField);
                                    contactInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                    alertLayout.addView(contactInput);

                                    TextView specialTV = new TextView(AdminActivity.this);
                                    specialTV.setText("Specialisation:");
                                    alertLayout.addView(specialTV);
                                    final EditText specialInput = new EditText(AdminActivity.this);
                                    specialInput.setText(specialisationField);
                                    specialInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    alertLayout.addView(specialInput);

                                    TextView emailpwTV = new TextView(AdminActivity.this);
                                    emailpwTV.setText("Email and password can only reset by user");
                                    alertLayout.addView(emailpwTV);

                                    alertBuilder.setView(alertLayout);

                                    alertBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            ArrayList<Long> sched = new ArrayList<>();
                                            try {
                                                sched = (ArrayList<Long>) dbValues.child("schedule").getValue();
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "Empty Schedule");
                                            }
                                            ArrayList<String> pastPat = new ArrayList<>();
                                            try {
                                                pastPat = (ArrayList<String>) dbValues.child("pastPatients").getValue();
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "Empty Past");
                                            }
                                            ArrayList<String> inputStringSp = new ArrayList<String>(Arrays.asList(specialInput.getText().toString().split("\n")));

                                            Map<String, Object> docUser = new HashMap<>();
                                            docUser.put("name", nameInput.getText().toString());
                                            docUser.put("contactNumber", contactInput.getText().toString());
                                            docUser.put("email", docItemInfo.email);
                                            docUser.put("specialisation", inputStringSp);
                                            docUser.put("schedule", sched);
                                            docUser.put("pastPatients", pastPat);

                                            docAccRef.child(emailSearch)
                                                    .setValue(docUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Doctor User Updated",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });
                                            ;
                                        }
                                    });

                                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            alert.cancel();
                                        }
                                    });

                                    alertBuilder.show();
                                } catch (Exception e) {
                                    Log.w(TAG, "Error Occurred:", e);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(AdminActivity.this, "Unable to Update Details",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Unable to Retrieve Users",
                        Toast.LENGTH_LONG).show();
            }
        };
        docAccRef.addValueEventListener(docVEL);
        /////////////////////////////////////NURSE ACC DISPLAY SETUP/////////////////////////////////////
        nurFields = new ArrayList<>();
        nurVEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                nurFields.clear();
                for (DataSnapshot document : dbValues.getChildren()) {
                    try {
                        String ne = document.child("name").getValue().toString();
                        long cn = Long.parseLong(document.child("contactNumber").getValue().toString());  //TODO Integer.parseInt to confirm integer
                        String el = document.child("email").getValue().toString();
                        nurFieldItem entry = new nurFieldItem(ne, cn + "", el);
                        nurFields.add(entry);
                    } catch (Exception error) {
                        Log.w(TAG, "Error occured while retrieving:", error);
                    }
                }

                Collections.sort(nurFields, new Comparator<nurFieldItem>() {            //TODO sort array list of objects
                    public int compare(nurFieldItem item1, nurFieldItem item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });

                nurCustomAdapter adapter = new nurCustomAdapter(AdminActivity.this, R.layout.dbapatnurfielditemlayout, nurFields);
                final ListView lv = findViewById(R.id.nurseList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Object itemInfo = lv.getItemAtPosition(position);
                        final nurFieldItem nurItemInfo = (nurFieldItem) itemInfo;
                        final String emailSearch = emailWithoutSuffix(nurItemInfo.email);
                        nurAccRef.child(emailSearch).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dbValues) {
                                String nameField;
                                String phoneNumberField;
                                try {
                                    nameField = dbValues.child("name").getValue().toString();
                                    phoneNumberField = dbValues.child("contactNumber").getValue().toString();

                                    LinearLayout alertLayout = new LinearLayout(AdminActivity.this);
                                    alertLayout.setOrientation(LinearLayout.VERTICAL);
                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
                                    final AlertDialog alert = alertBuilder.create();

                                    alertBuilder.setTitle("Update Existing Nurse");

                                    TextView nameTV = new TextView(AdminActivity.this);
                                    nameTV.setText("Name:");
                                    alertLayout.addView(nameTV);
                                    final EditText nameInput = new EditText(AdminActivity.this);
                                    nameInput.setText(nameField);
                                    alertLayout.addView(nameInput);

                                    TextView contactTV = new TextView(AdminActivity.this);
                                    contactTV.setText("Phone Number:");
                                    alertLayout.addView(contactTV);
                                    final EditText contactInput = new EditText(AdminActivity.this);
                                    contactInput.setText(phoneNumberField);
                                    alertLayout.addView(contactInput);

                                    TextView emailpwTV = new TextView(AdminActivity.this);
                                    emailpwTV.setText("Email and password can only reset by user");
                                    alertLayout.addView(emailpwTV);

                                    alertBuilder.setView(alertLayout);

                                    alertBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            ArrayList<Long> sched = new ArrayList<>();
                                            try {
                                                sched = (ArrayList<Long>) dbValues.child("schedule").getValue();
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "Empty Schedule");
                                            }

                                            Map<String, Object> nurUser = new HashMap<>();
                                            nurUser.put("name", nameInput.getText().toString());
                                            nurUser.put("contactNumber", contactInput.getText().toString());
                                            nurUser.put("email", nurItemInfo.email);
                                            nurUser.put("schedule", sched);

                                            nurAccRef.child(emailSearch)
                                                    .setValue(nurUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Nurse User Updated",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });
                                            ;
                                        }
                                    });

                                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            alert.cancel();
                                        }
                                    });

                                    alertBuilder.show();
                                } catch (Exception e) {
                                    Log.w(TAG, "Error Occurred:", e);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(AdminActivity.this, "Unable to Update Details",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Unable to Retrieve Users",
                        Toast.LENGTH_LONG).show();
            }
        };
        nurAccRef.addValueEventListener(nurVEL);
        /////////////////////////////////////PATIENT ACC DISPLAY SETUP/////////////////////////////////////
        patFields = new ArrayList<>();
        patVEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                patFields.clear();
                for (DataSnapshot document : dbValues.getChildren()) {
                    try {
                        String ne = document.child("name").getValue().toString();
                        long cn = Long.parseLong(document.child("contactNumber").getValue().toString());  //TODO Long.parseLong to confirm number
                        String el = document.child("email").getValue().toString();
                        String ad = document.child("address").getValue().toString();
                        patFieldItem entry = new patFieldItem(ne, cn + "", el, ad);
                        patFields.add(entry);
                    } catch (Exception error) {
                        Log.w(TAG, "Error occured while retrieving:", error);
                    }
                }

                Collections.sort(patFields, new Comparator<patFieldItem>() {            //TODO array list sort
                    public int compare(patFieldItem item1, patFieldItem item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });

                patCustomAdapter adapter = new patCustomAdapter(AdminActivity.this, R.layout.dbapatnurfielditemlayout, patFields);
                final ListView lv = findViewById(R.id.patientList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Object itemInfo = lv.getItemAtPosition(position);
                        final patFieldItem patItemInfo = (patFieldItem) itemInfo;
                        final String emailSearch = emailWithoutSuffix(patItemInfo.email);
                        patAccRef.child(emailSearch).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dbValues) {
                                String nameField;
                                String phoneNumberField;
                                String addressField;
                                try {
                                    nameField = dbValues.child("name").getValue().toString();
                                    phoneNumberField = dbValues.child("contactNumber").getValue().toString();
                                    addressField = dbValues.child("address").getValue().toString();

                                    LinearLayout alertLayout = new LinearLayout(AdminActivity.this);
                                    alertLayout.setOrientation(LinearLayout.VERTICAL);
                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
                                    final AlertDialog alert = alertBuilder.create();

                                    alertBuilder.setTitle("Update Existing DBA");

                                    TextView nameTV = new TextView(AdminActivity.this);
                                    nameTV.setText("Name:");
                                    alertLayout.addView(nameTV);
                                    final EditText nameInput = new EditText(AdminActivity.this);
                                    nameInput.setText(nameField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                    alertLayout.addView(nameInput);

                                    TextView contactTV = new TextView(AdminActivity.this);
                                    contactTV.setText("Phone Number:");
                                    alertLayout.addView(contactTV);
                                    final EditText contactInput = new EditText(AdminActivity.this);
                                    contactInput.setText(phoneNumberField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    alertLayout.addView(contactInput);

                                    TextView addTV = new TextView(AdminActivity.this);
                                    addTV.setText("Address:");
                                    alertLayout.addView(addTV);
                                    final EditText addInput = new EditText(AdminActivity.this);
                                    addInput.setText(addressField);
                                    alertLayout.addView(addInput);


                                    TextView emailpwTV = new TextView(AdminActivity.this);
                                    emailpwTV.setText("Email and password can only reset by user");
                                    alertLayout.addView(emailpwTV);

                                    alertBuilder.setView(alertLayout);

                                    alertBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Object rec = "", appts = "";
                                            try {
                                                rec = dbValues.child("records").getValue();
                                            } catch (NullPointerException e) {
                                                Log.w(TAG, "Patient Record Null");
                                            }
                                            try {
                                                appts = dbValues.child("appointments").getValue();
                                            } catch (NullPointerException e) {
                                                Log.w(TAG, "Patient Appt Null");
                                            }
                                            long ls = (long) dbValues.child("lastSeen").getValue();

                                            Map<String, Object> patUser = new HashMap<>();
                                            patUser.put("name", nameInput.getText().toString());
                                            patUser.put("contactNumber", contactInput.getText().toString());
                                            patUser.put("email", patItemInfo.email);
                                            patUser.put("address", addInput.getText().toString());
                                            patUser.put("appointments", appts);
                                            patUser.put("records", rec);
                                            patUser.put("lastSeen", ls);

                                            patAccRef.child(emailSearch)
                                                    .setValue(patUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Patient User Updated",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });
                                        }
                                    });

                                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            alert.cancel();
                                        }
                                    });

                                    alertBuilder.show();
                                } catch (Exception e) {
                                    Log.w(TAG, "Error Occurred:", e);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(AdminActivity.this, "Unable to Update Details",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Unable to Retrieve Users",
                        Toast.LENGTH_LONG).show();
            }
        };
        patAccRef.addValueEventListener(patVEL);
        /////////////////////////////////////CARETAKER ACC DISPLAY SETUP/////////////////////////////////////
        careFields = new ArrayList<>();
        careVEL = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dbValues) {
                careFields.clear();
                for (DataSnapshot document : dbValues.getChildren()) {
                    try {
                        String ne = document.child("name").getValue().toString();
                        long cn = Long.parseLong(document.child("contactNumber").getValue().toString());  //TODO Long.parseLong to confirm number
                        String el = document.child("email").getValue().toString();
                        String ad = document.child("address").getValue().toString();
                        String pe = document.child("patientEmail").getValue().toString();
                        careFieldItem entry = new careFieldItem(ne, cn + "", el, ad, pe);
                        careFields.add(entry);
                    } catch (Exception error) {
                        Log.w(TAG, "Error occured while retrieving:", error);
                    }
                }

                Collections.sort(careFields, new Comparator<careFieldItem>() {            //TODO array list sort
                    public int compare(careFieldItem item1, careFieldItem item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });

                careCustomAdapter adapter = new careCustomAdapter(AdminActivity.this, R.layout.carefielditemlayout, careFields);
                final ListView lv = findViewById(R.id.caretakerList);
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        Object itemInfo = lv.getItemAtPosition(position);
                        final careFieldItem careItemInfo = (careFieldItem) itemInfo;
                        Log.d(TAG, "caretaker careItemInfo.email: " + careItemInfo.email);
                        Log.d(TAG, "caretaker careItemInfo.name: " + careItemInfo.name);
                        Log.d(TAG, "caretaker careItemInfo.patientEmail: " + careItemInfo.patientEmail);
                        final String emailSearch = emailWithoutSuffix(careItemInfo.email);
                        careAccRef.child(emailSearch).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dbValues) {
                                String nameField;
                                String phoneNumberField;
                                String addressField;
                                try {
                                    nameField = dbValues.child("name").getValue().toString();
                                    phoneNumberField = dbValues.child("contactNumber").getValue().toString();
                                    addressField = dbValues.child("address").getValue().toString();
                                    final String patientEmailField = dbValues.child("patientEmail").getValue().toString();

                                    LinearLayout alertLayout = new LinearLayout(AdminActivity.this);
                                    alertLayout.setOrientation(LinearLayout.VERTICAL);
                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AdminActivity.this);
                                    final AlertDialog alert = alertBuilder.create();

                                    alertBuilder.setTitle("Update Existing DBA");

                                    TextView nameTV = new TextView(AdminActivity.this);
                                    nameTV.setText("Name:");
                                    alertLayout.addView(nameTV);
                                    final EditText nameInput = new EditText(AdminActivity.this);
                                    nameInput.setText(nameField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                                    alertLayout.addView(nameInput);

                                    TextView contactTV = new TextView(AdminActivity.this);
                                    contactTV.setText("Phone Number:");
                                    alertLayout.addView(contactTV);
                                    final EditText contactInput = new EditText(AdminActivity.this);
                                    contactInput.setText(phoneNumberField);
                                    nameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                                    alertLayout.addView(contactInput);

                                    TextView addTV = new TextView(AdminActivity.this);
                                    addTV.setText("Address:");
                                    alertLayout.addView(addTV);
                                    final EditText addInput = new EditText(AdminActivity.this);
                                    addInput.setText(addressField);
                                    alertLayout.addView(addInput);

                                    TextView peTV = new TextView(AdminActivity.this);
                                    peTV.setText("Patient's Email:");
                                    alertLayout.addView(peTV);
                                    final EditText peInput = new EditText(AdminActivity.this);
                                    peInput.setText(patientEmailField);
                                    alertLayout.addView(peInput);

                                    TextView emailpwTV = new TextView(AdminActivity.this);
                                    emailpwTV.setText("Email and password can only reset by user");
                                    alertLayout.addView(emailpwTV);

                                    alertBuilder.setView(alertLayout);

                                    alertBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            long ls = (long) dbValues.child("lastSeen").getValue();

                                            Map<String, Object> careUser = new HashMap<>();
                                            careUser.put("name", nameInput.getText().toString());
                                            careUser.put("contactNumber", contactInput.getText().toString());
                                            careUser.put("email", patientEmailField);
                                            careUser.put("address", addInput.getText().toString());
                                            careUser.put("patientEmail", peInput.getText().toString());
                                            careUser.put("lastSeen", ls);

                                            careAccRef.child(emailSearch)
                                                    .setValue(careUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Caretaker User Updated",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });
                                        }
                                    });

                                    alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            alert.cancel();
                                        }
                                    });

                                    alertBuilder.show();
                                } catch (Exception e) {
                                    Log.w(TAG, "Error Occurred:", e);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(AdminActivity.this, "Unable to Update Details",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Unable to Retrieve Users",
                        Toast.LENGTH_LONG).show();
            }
        };
        careAccRef.addValueEventListener(careVEL);
    }

    //TODO var name cannot be full email
    String emailWithoutSuffix(String input) {
        return input.substring(0, input.indexOf(".com")).toLowerCase();
    }

    void showRegisteringLoading() {
        if (progressDialogDisplay == null) {
            progressDialogDisplay = new ProgressDialog(this);
            progressDialogDisplay.setMessage("Registering...");
            progressDialogDisplay.setIndeterminate(true);
        }

        progressDialogDisplay.show();
    }   //TODO Progress Dialogs

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

    public void registerDBA(View view) {
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final AlertDialog alert = alertBuilder.create();

        alertBuilder.setTitle("Register New DBA");

        TextView nameTV = new TextView(this);
        nameTV.setText("Name:");
        alertLayout.addView(nameTV);
        final EditText nameInput = new EditText(this);
        alertLayout.addView(nameInput);

        TextView contactTV = new TextView(this);
        contactTV.setText("Phone Number:");
        alertLayout.addView(contactTV);
        final EditText contactInput = new EditText(this);
        alertLayout.addView(contactInput);

        TextView emailTV = new TextView(this);
        emailTV.setText("Email:");
        alertLayout.addView(emailTV);
        final EditText emailInput = new EditText(this);
        alertLayout.addView(emailInput);

        TextView passwordTV = new TextView(this);
        passwordTV.setText("Password:");
        alertLayout.addView(passwordTV);
        final EditText pwInput = new EditText(this);
        alertLayout.addView(pwInput);

        TextView pwTV2 = new TextView(this);
        pwTV2.setText("Confirm Password:");
        alertLayout.addView(pwTV2);
        final EditText pwInput2 = new EditText(this);
        alertLayout.addView(pwInput2);

        alertBuilder.setView(alertLayout);

        alertBuilder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showRegisteringLoading();
                if (pwInput.getText().toString().equals(pwInput2.getText().toString())) {
                    try {
                        authenticationInstance.createUserWithEmailAndPassword(emailInput.getText().toString(), pwInput.getText().toString())
                                .addOnCompleteListener(AdminActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> dbaUser = new HashMap<>();
                                            dbaUser.put("name", nameInput.getText().toString());
                                            dbaUser.put("contactNumber", Integer.parseInt(contactInput.getText().toString()));
                                            dbaUser.put("email", emailInput.getText().toString());
                                            dbaAccRef.child(emailWithoutSuffix(emailInput.getText().toString()))
                                                    .setValue(dbaUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "DBA User Registered",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });

                                        } else {
                                            String arrSplit[] = task.getException().toString().split(":");
                                            String errorMessage = arrSplit[arrSplit.length - 1];
                                            Toast.makeText(AdminActivity.this, errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        hideLoadingView();
                                    }
                                });
                    } catch (Exception e) {
                        hideLoadingView();
                        Toast.makeText(AdminActivity.this, "Invalid data entered",
                                Toast.LENGTH_SHORT).show();
                    }

                    hideLoadingView();

                } else {
                    hideLoadingView();
                    Toast.makeText(AdminActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alert.cancel();
                hideLoadingView();
            }
        });

        alertBuilder.show();
    }

    public void registerDoc(View view) {
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final AlertDialog alert = alertBuilder.create();

        alertBuilder.setTitle("Register New Doctor");

        TextView nameTV = new TextView(this);
        nameTV.setText("Name:");
        alertLayout.addView(nameTV);
        final EditText nameInput = new EditText(this);
        alertLayout.addView(nameInput);

        TextView contactTV = new TextView(this);
        contactTV.setText("Phone Number:");
        alertLayout.addView(contactTV);
        final EditText contactInput = new EditText(this);
        alertLayout.addView(contactInput);

        TextView emailTV = new TextView(this);
        emailTV.setText("Email:");
        alertLayout.addView(emailTV);
        final EditText emailInput = new EditText(this);
        alertLayout.addView(emailInput);

        TextView spTV = new TextView(this);
        spTV.setText("Specialisation: (1 entry per line)");
        alertLayout.addView(spTV);
        final EditText spInput = new EditText(this);
        alertLayout.addView(spInput);

        TextView passwordTV = new TextView(this);
        passwordTV.setText("Password:");
        alertLayout.addView(passwordTV);
        final EditText pwInput = new EditText(this);
        alertLayout.addView(pwInput);

        TextView pwTV2 = new TextView(this);
        pwTV2.setText("Confirm Password:");
        alertLayout.addView(pwTV2);
        final EditText pwInput2 = new EditText(this);
        alertLayout.addView(pwInput2);

        alertBuilder.setView(alertLayout);

        alertBuilder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showRegisteringLoading();
                if (pwInput.getText().toString().equals(pwInput2.getText().toString())) {
                    final ArrayList<String> spSplit = new ArrayList<>(Arrays.asList(spInput.getText().toString().split("\n")));
                    try {
                        authenticationInstance.createUserWithEmailAndPassword(emailInput.getText().toString(), pwInput.getText().toString())
                                .addOnCompleteListener(AdminActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            Map<String, Object> docUser = new HashMap<>();
                                            docUser.put("name", nameInput.getText().toString());
                                            docUser.put("contactNumber", Integer.parseInt(contactInput.getText().toString()));
                                            docUser.put("email", emailInput.getText().toString());
                                            docUser.put("specialisation", spSplit);

                                            docAccRef.child(emailWithoutSuffix(emailInput.getText().toString()))
                                                    .setValue(docUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Doctor User Registered",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });

                                        } else {
                                            String arrSplit[] = task.getException().toString().split(":");
                                            String errorMessage = arrSplit[arrSplit.length - 1];
                                            Toast.makeText(AdminActivity.this, errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        hideLoadingView();
                                    }
                                });
                    } catch (Exception e) {
                        hideLoadingView();
                        Toast.makeText(AdminActivity.this, "Invalid data entered",
                                Toast.LENGTH_SHORT).show();
                    }

                    hideLoadingView();
                } else {
                    hideLoadingView();
                    Toast.makeText(AdminActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();  //TODO Data Validation/Verification
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alert.cancel();
                hideLoadingView();
            }
        });

        alertBuilder.show();
    }

    public void registerNur(View view) {
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final AlertDialog alert = alertBuilder.create();

        alertBuilder.setTitle("Register New Nurse");

        TextView nameTV = new TextView(this);
        nameTV.setText("Name:");
        alertLayout.addView(nameTV);
        final EditText nameInput = new EditText(this);
        alertLayout.addView(nameInput);

        TextView contactTV = new TextView(this);
        contactTV.setText("Phone Number:");
        alertLayout.addView(contactTV);
        final EditText contactInput = new EditText(this);
        alertLayout.addView(contactInput);

        TextView emailTV = new TextView(this);
        emailTV.setText("Email:");
        alertLayout.addView(emailTV);
        final EditText emailInput = new EditText(this);
        alertLayout.addView(emailInput);

        TextView lvlTV = new TextView(this);
        lvlTV.setText("Level Allocated (Split by ,):");
        alertLayout.addView(lvlTV);
        final EditText lvlInput = new EditText(this);
        alertLayout.addView(lvlInput);

        TextView passwordTV = new TextView(this);
        passwordTV.setText("Password:");
        alertLayout.addView(passwordTV);
        final EditText pwInput = new EditText(this);
        alertLayout.addView(pwInput);

        TextView pwTV2 = new TextView(this);
        pwTV2.setText("Confirm Password:");
        alertLayout.addView(pwTV2);
        final EditText pwInput2 = new EditText(this);
        alertLayout.addView(pwInput2);

        alertBuilder.setView(alertLayout);

        alertBuilder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showRegisteringLoading();
                if (pwInput.getText().toString().equals(pwInput2.getText().toString())) {
                    final ArrayList<String> lvlSplit = new ArrayList<>(Arrays.asList(lvlInput.getText().toString().split(",")));
                    try {
                        authenticationInstance.createUserWithEmailAndPassword(emailInput.getText().toString(), pwInput.getText().toString())
                                .addOnCompleteListener(AdminActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            Map<String, Object> nurUser = new HashMap<>();
                                            nurUser.put("name", nameInput.getText().toString());
                                            nurUser.put("contactNumber", Integer.parseInt(contactInput.getText().toString()));
                                            nurUser.put("email", emailInput.getText().toString());
                                            nurUser.put("level", lvlSplit);
                                            nurAccRef.child(emailWithoutSuffix(emailInput.getText().toString()))
                                                    .setValue(nurUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Nurse User Registered",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });

                                        } else {
                                            String arrSplit[] = task.getException().toString().split(":");
                                            String errorMessage = arrSplit[arrSplit.length - 1];
                                            Toast.makeText(AdminActivity.this, errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        hideLoadingView();
                                    }
                                });
                    } catch (Exception e) {
                        hideLoadingView();
                        Toast.makeText(AdminActivity.this, "Invalid data entered",
                                Toast.LENGTH_SHORT).show();
                    }

                    hideLoadingView();

                } else {
                    hideLoadingView();
                    Toast.makeText(AdminActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alert.cancel();
                hideLoadingView();
            }
        });

        alertBuilder.show();
    }

    public void registerPat(View view) {
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final AlertDialog alert = alertBuilder.create();

        alertBuilder.setTitle("Register New Patient");

        TextView nameTV = new TextView(this);
        nameTV.setText("Name:");
        alertLayout.addView(nameTV);
        final EditText nameInput = new EditText(this);
        alertLayout.addView(nameInput);

        TextView contactTV = new TextView(this);
        contactTV.setText("Phone Number:");
        alertLayout.addView(contactTV);
        final EditText contactInput = new EditText(this);
        alertLayout.addView(contactInput);

        TextView emailTV = new TextView(this);
        emailTV.setText("Email:");
        alertLayout.addView(emailTV);
        final EditText emailInput = new EditText(this);
        alertLayout.addView(emailInput);

        TextView addTV = new TextView(this);
        addTV.setText("Address:");
        alertLayout.addView(addTV);
        final EditText addInput = new EditText(this);
        alertLayout.addView(addInput);

        TextView passwordTV = new TextView(this);
        passwordTV.setText("Password:");
        alertLayout.addView(passwordTV);
        final EditText pwInput = new EditText(this);
        alertLayout.addView(pwInput);

        TextView pwTV2 = new TextView(this);
        pwTV2.setText("Confirm Password:");
        alertLayout.addView(pwTV2);
        final EditText pwInput2 = new EditText(this);
        alertLayout.addView(pwInput2);

        alertBuilder.setView(alertLayout);

        alertBuilder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showRegisteringLoading();
                if (pwInput.getText().toString().equals(pwInput2.getText().toString())) {
                    try {

                        authenticationInstance.createUserWithEmailAndPassword(emailInput.getText().toString(), pwInput.getText().toString())
                                .addOnCompleteListener(AdminActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> patUser = new HashMap<>();
                                            patUser.put("name", nameInput.getText().toString());
                                            patUser.put("contactNumber", Integer.parseInt(contactInput.getText().toString()));
                                            patUser.put("email", emailInput.getText().toString());
                                            patUser.put("address", addInput.getText().toString());
                                            patUser.put("lastSeen", ServerValue.TIMESTAMP); //TODO writing last seen for functions

                                            patAccRef.child(emailWithoutSuffix(emailInput.getText().toString()))
                                                    .setValue(patUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Patient User Registered",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });
                                        } else {
                                            String arrSplit[] = task.getException().toString().split(":");
                                            String errorMessage = arrSplit[arrSplit.length - 1];
                                            Toast.makeText(AdminActivity.this, errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        hideLoadingView();
                                    }
                                });
                    } catch (Exception e) {
                        hideLoadingView();
                        Toast.makeText(AdminActivity.this, "Invalid data entered",
                                Toast.LENGTH_SHORT).show();
                    }

                    hideLoadingView();

                } else {
                    hideLoadingView();
                    Toast.makeText(AdminActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alert.cancel();
                hideLoadingView();
            }
        });

        alertBuilder.show();
    }

    public void registerCare(View view) {
        LinearLayout alertLayout = new LinearLayout(this);
        alertLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final AlertDialog alert = alertBuilder.create();

        alertBuilder.setTitle("Register New Caretaker");

        TextView nameTV = new TextView(this);
        nameTV.setText("Name:");
        alertLayout.addView(nameTV);
        final EditText nameInput = new EditText(this);
        alertLayout.addView(nameInput);

        TextView contactTV = new TextView(this);
        contactTV.setText("Phone Number:");
        alertLayout.addView(contactTV);
        final EditText contactInput = new EditText(this);
        alertLayout.addView(contactInput);

        TextView emailTV = new TextView(this);
        emailTV.setText("Email:");
        alertLayout.addView(emailTV);
        final EditText emailInput = new EditText(this);
        alertLayout.addView(emailInput);

        TextView addTV = new TextView(this);
        addTV.setText("Address:");
        alertLayout.addView(addTV);
        final EditText addInput = new EditText(this);
        alertLayout.addView(addInput);

        TextView peTV = new TextView(this);
        peTV.setText("Patient's Email:");
        alertLayout.addView(peTV);
        final EditText peInput = new EditText(this);
        alertLayout.addView(peInput);

        TextView passwordTV = new TextView(this);
        passwordTV.setText("Password:");
        alertLayout.addView(passwordTV);
        final EditText pwInput = new EditText(this);
        alertLayout.addView(pwInput);

        TextView pwTV2 = new TextView(this);
        pwTV2.setText("Confirm Password:");
        alertLayout.addView(pwTV2);
        final EditText pwInput2 = new EditText(this);
        alertLayout.addView(pwInput2);

        alertBuilder.setView(alertLayout);

        alertBuilder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showRegisteringLoading();
                if (pwInput.getText().toString().equals(pwInput2.getText().toString())) {  //TODO data verification
                    try {

                        authenticationInstance.createUserWithEmailAndPassword(emailInput.getText().toString(), pwInput.getText().toString())
                                .addOnCompleteListener(AdminActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> careUser = new HashMap<>();
                                            careUser.put("name", nameInput.getText().toString());
                                            careUser.put("contactNumber", Integer.parseInt(contactInput.getText().toString()));
                                            careUser.put("email", emailInput.getText().toString());
                                            careUser.put("address", addInput.getText().toString());
                                            careUser.put("patientEmail", peInput.getText().toString());   //TODO VALIDATE PATIENT EMAIL
                                            careUser.put("lastSeen", ServerValue.TIMESTAMP); //TODO writing last seen for functions

                                            careAccRef.child(emailWithoutSuffix(emailInput.getText().toString()))
                                                    .setValue(careUser)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void successVoid) {
                                                            Toast.makeText(AdminActivity.this, "Caretaker User Registered",
                                                                    Toast.LENGTH_LONG).show();
                                                            hideLoadingView();
                                                        }
                                                    });

                                            hideLoadingView();
                                        } else {
                                            String arrSplit[] = task.getException().toString().split(":");
                                            String errorMessage = arrSplit[arrSplit.length - 1];
                                            Toast.makeText(AdminActivity.this, errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        hideLoadingView();
                        Toast.makeText(AdminActivity.this, "Invalid data entered",
                                Toast.LENGTH_SHORT).show();
                    }

                    hideLoadingView();

                } else {
                    hideLoadingView();
                    Toast.makeText(AdminActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

        {
            public void onClick(DialogInterface dialog, int whichButton) {
                alert.cancel();
                hideLoadingView();
            }
        });

        alertBuilder.show();
    }

    void updateLowerInfo() {
        try {
            final FirebaseUser fU = authenticationInstance.getCurrentUser();
            Log.d(TAG, "Updating User Info:" + emailWithoutSuffix(fU.getEmail()));
            dbaAccRef = database.getReference().child("Administrators");
            dbaAccRef.child(emailWithoutSuffix(fU.getEmail())).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    TextView nameLabel = findViewById(R.id.name);
                    TextView emailLabel = findViewById(R.id.email);
                    if (dataSnapshot.getValue() != null) {
                        String firstName = dataSnapshot.getValue().toString().split(" ")[0];   //TODO Only display first name
                        nameLabel.setText("Name: " + firstName);
                    }
                    Log.d(TAG, "Datasnapshot of name: " + dataSnapshot.getValue());
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

    @Override  //TODO remove all listeners so as not to hog CPU
    protected void onDestroy() {
        dbaAccRef.removeEventListener(adminVEL);
        docAccRef.removeEventListener(docVEL);
        nurAccRef.removeEventListener(nurVEL);
        patAccRef.removeEventListener(patVEL);
        careAccRef.removeEventListener(careVEL);
        super.onDestroy();
    }
}