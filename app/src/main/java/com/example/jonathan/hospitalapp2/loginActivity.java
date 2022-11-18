package com.example.jonathan.hospitalapp2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class loginActivity extends AppCompatActivity {

    GoogleSignInClient GsignInClient;
    private FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userDBRef;
    DatabaseReference patAccRef = database.getReference().child("Patients");
    ProgressDialog progressDialogDisplay;
    String[] pastLogins;
    String userEmShort;

    //TODO Realtime Database faster than Firestore (<1s vs 3-5s delay)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){  //TODO Restrict max API to 26
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(loginActivity.this);
            final AlertDialog alert = alertBuilder.create();

            alertBuilder.setTitle("Reset Password");

            alertBuilder.setMessage("Max API is 25, Current API is " + Build.VERSION.SDK_INT + "\nSome features may not be present");

            alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            alertBuilder.show();
        }

        GoogleSignInOptions GsignInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GsignInClient = GoogleSignIn.getClient(this, GsignInOpt);

        //TODO Auto sign in
        //TODO Try to prevent unexpected errors from improper/incomplete sign out

        FirebaseUser currentUser = authenticationInstance.getCurrentUser();
        if (currentUser != null) {
            try {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                showLoginLoading();
                databaseSignIn(true);
            } catch (Exception e) {
                Log.w("LOGIN TEST", "onStart: ", e);
            }
        }

        TextView forgotPwHyperlink = findViewById(R.id.forgotPw);
        TextView registerNewAcc = findViewById(R.id.regNew);
        forgotPwHyperlink.setOnClickListener(new TextView.OnClickListener() {  //TODO Password reset email
            @Override
            public void onClick(View v) {
                LinearLayout alertLayout = new LinearLayout(loginActivity.this);
                alertLayout.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(loginActivity.this);
                final AlertDialog alert = alertBuilder.create();

                alertBuilder.setTitle("Reset Password");

                TextView eTV = new TextView(loginActivity.this);
                eTV.setText("Email");
                alertLayout.addView(eTV);
                final EditText eET = new EditText(loginActivity.this);
                eET.setText("");
                eET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                alertLayout.addView(eET);

                alertBuilder.setView(alertLayout);
                alertBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        if (!eET.getText().toString().equals("")) {
                            authenticationInstance.sendPasswordResetEmail(eET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(loginActivity.this, "Password Reset Email Sent", Toast.LENGTH_SHORT).show();
                                        alert.dismiss();
                                    } else {
                                        Log.d("LOGIN TEST", "Error: " + task.getException().toString());
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(loginActivity.this, "Email cannot be blank", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertBuilder.show();
            }
        });
        registerNewAcc.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout alertLayout = new LinearLayout(loginActivity.this);
                alertLayout.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(loginActivity.this);
                final AlertDialog alert = alertBuilder.create();

                alertBuilder.setTitle("Register New Account");

                TextView nameTV = new TextView(loginActivity.this);
                nameTV.setText("Name:");
                alertLayout.addView(nameTV);
                final EditText nameInput = new EditText(loginActivity.this);
                alertLayout.addView(nameInput);

                TextView contactTV = new TextView(loginActivity.this);
                contactTV.setText("Phone Number:");
                alertLayout.addView(contactTV);
                final EditText contactInput = new EditText(loginActivity.this);
                alertLayout.addView(contactInput);

                TextView emailTV = new TextView(loginActivity.this);
                emailTV.setText("Email:");
                alertLayout.addView(emailTV);
                final EditText emailInput = new EditText(loginActivity.this);
                alertLayout.addView(emailInput);

                TextView addTV = new TextView(loginActivity.this);
                addTV.setText("Address:");
                alertLayout.addView(addTV);
                final EditText addInput = new EditText(loginActivity.this);
                alertLayout.addView(addInput);

                TextView passwordTV = new TextView(loginActivity.this);
                passwordTV.setText("Password:");
                alertLayout.addView(passwordTV);
                final EditText pwInput = new EditText(loginActivity.this);
                alertLayout.addView(pwInput);

                TextView pwTV2 = new TextView(loginActivity.this);
                pwTV2.setText("Confirm Password:");
                alertLayout.addView(pwTV2);
                final EditText pwInput2 = new EditText(loginActivity.this);
                alertLayout.addView(pwInput2);

                alertBuilder.setView(alertLayout);

                alertBuilder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        showRegisteringLoading();
                        if (pwInput.getText().toString().equals(pwInput2.getText().toString())) {
                            try {

                                authenticationInstance.createUserWithEmailAndPassword(emailInput.getText().toString(), pwInput.getText().toString())
                                        .addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    signOut();
                                                    Map<String, Object> patUser = new HashMap<>();
                                                    patUser.put("name", nameInput.getText().toString());
                                                    patUser.put("contactNumber", Integer.parseInt(contactInput.getText().toString()));
                                                    patUser.put("email", emailInput.getText().toString());
                                                    patUser.put("address", addInput.getText().toString());
                                                    patUser.put("lastSeen", ServerValue.TIMESTAMP); //TODO writing last seen for functions

                                                    patAccRef.child(emailInput.getText().toString().substring(0, emailInput.getText().toString().indexOf(".com")).toLowerCase())
                                                            .setValue(patUser)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void successVoid) {
                                                                    Toast.makeText(loginActivity.this, "User Registered",
                                                                            Toast.LENGTH_LONG).show();
                                                                    hideLoadingView();
                                                                }
                                                            });
                                                } else {
                                                    String arrSplit[] = task.getException().toString().split(":");
                                                    String errorMessage = arrSplit[arrSplit.length - 1];
                                                    Toast.makeText(loginActivity.this, errorMessage,
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                hideLoadingView();
                                            }
                                        });
                            } catch (Exception e) {
                                hideLoadingView();
                                Toast.makeText(loginActivity.this, "Invalid data entered",
                                        Toast.LENGTH_SHORT).show();
                            }

                            hideLoadingView();

                        } else {
                            hideLoadingView();
                            Toast.makeText(loginActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AutoCompleteTextView actv = findViewById(R.id.em);
        actv.setThreshold(0);
        SharedPreferences pref = getSharedPreferences("successfulLogins", MODE_PRIVATE);  //Read
        String fullPref = pref.getString("successfulLogins", "");
        pastLogins = fullPref.split("\n");
        for (int i = 0; i < pastLogins.length; i++)
            Log.d("LOGIN TESTING", "pastLogins[" + i + "]: " + pastLogins[i]);
        Arrays.sort(pastLogins);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pastLogins);
        actv.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideLoadingView();
    }

    void databaseSignIn(boolean autoLogin) {
        final String currentUserEmail = authenticationInstance.getCurrentUser().getEmail();
        userEmShort = currentUserEmail.substring(0, currentUserEmail.indexOf(".com")).toLowerCase();
        Log.d("LOGIN DEBUGGING", "User Email: " + currentUserEmail);
        int atSymbolPos = currentUserEmail.indexOf("@") + 1;  //TODO +1 to start at next position
        String userEmailAfterAt = currentUserEmail.substring(atSymbolPos);
        int userType;                                         //TODO userType based on int (1:admin,2:doc,3:nur,4:pat/care)
        if (userEmailAfterAt.contains("dba")) userType = 1;
        else if (userEmailAfterAt.contains("doc")) userType = 2;
        else if (userEmailAfterAt.contains("nur")) userType = 3;
        else userType = 4;

        boolean skipRemainder = false;
        Class placeholderAct = loginActivity.class;  //Just to initialize    //TODO faster login time compared to nested checks
        switch (userType) {   //TODO faster processing than nesting multiple checks from database (20s vs 5s)
            case 1:
                if (!autoLogin) {
                    Log.d("AUTON LOGIN TEST", "Auto Login Disabled");
                    userDBRef = database.getReference().child("Administrators"); //TODO Logout for important users (2 approaches)
                    placeholderAct = AdminActivity.class;
                } else {
                    Log.d("AUTON LOGIN TEST", "Auto Login Enabled");
                    signOut();
                    skipRemainder = true;
                    Log.d("AUTON LOGIN TEST", "skipRemainder:" + skipRemainder);
                }
                break;

            case 2:
                if (!autoLogin) {
                    Log.d("AUTON LOGIN TEST", "Auto Login Disabled");
                    userDBRef = database.getReference().child("Doctors"); //TODO Logout for important users (2 approaches)
                    placeholderAct = DoctorActivity.class;
                } else {
                    Log.d("AUTON LOGIN TEST", "Auto Login Enabled");
                    signOut();
                    skipRemainder = true;
                    Log.d("AUTON LOGIN TEST", "skipRemainder:" + skipRemainder);
                }
                break;

            /*case 3:  //TODO REMOVE COMMENT
                userDBRef = database.getReference().child("Nurses");
                placeholderAct = NurseActivity.class;
                break;*/

            case 4:
                userDBRef = database.getReference().child("Patients");   //TODO Flexibility for public
                placeholderAct = PatientActivity.class;
                break;
        }

        if (!skipRemainder) {
            final Class nextAct = placeholderAct;
            if (userType < 4) {
                userDBRef.child(userEmShort).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hideLoadingView();
                        if (dataSnapshot.getValue() != null) {
                            userDBRef.child(userEmShort).child("lastSeen").setValue(ServerValue.TIMESTAMP);
                            startActivity(new Intent(loginActivity.this, nextAct));
                        } else {
                            signOut();
                            Toast.makeText(loginActivity.this, "Database User Not Found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        signOut();
                        //Toast.makeText(loginActivity.this, "User not found",Toast.LENGTH_SHORT).show();
                    }
                });
            } else {   //TODO Flexibility of public users
                userDBRef.child(userEmShort).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            userDBRef.child(userEmShort).child("lastSeen").setValue(ServerValue.TIMESTAMP);
                            startActivity(new Intent(loginActivity.this, nextAct));
                        } else {
                            signOut();
                            Toast.makeText(loginActivity.this, "Database User Not Found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        userDBRef = database.getReference().child("Caretakers");
                        userDBRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {   //TODO REMOVE COMMENT
                                /*hideLoadingView();
                                if (dataSnapshot.getValue() != null) {
                                    userDBRef.child(userEmShort).child("lastSeen").setValue(ServerValue.TIMESTAMP);
                                    startActivity(new Intent(loginActivity.this, CaretakerActivity.class));
                                } else {
                                    signOut();
                                    Toast.makeText(loginActivity.this, "Database User Not Found",
                                            Toast.LENGTH_SHORT).show();
                                }*/
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                signOut();
                                //Toast.makeText(loginActivity.this, "Database User Not Found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }

    }

    void showRegisteringLoading() {
        if (progressDialogDisplay == null) {
            progressDialogDisplay = new ProgressDialog(this);
            progressDialogDisplay.setMessage("Registering...");
            progressDialogDisplay.setIndeterminate(true);
        }

        progressDialogDisplay.show();
    }   //TODO Progress Dialogs

    void showLoginLoading() {
        if (progressDialogDisplay == null) {
            progressDialogDisplay = new ProgressDialog(this);
            progressDialogDisplay.setMessage("Signing In...");
            progressDialogDisplay.setIndeterminate(true);
        }

        progressDialogDisplay.show();
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

    void signOut() {
        showLogoutLoading();
        authenticationInstance.signOut();
        GsignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideLoadingView();
                    }
                });
    }

    public void signIn(View view) {
        final EditText e = findViewById(R.id.em);
        final String email = e.getText().toString();
        final EditText p = findViewById(R.id.pw);
        final String password = p.getText().toString();

        if (e.getText().toString().trim().equals("") || e.getText().toString().trim().equals("")) {
            Toast.makeText(loginActivity.this,"Email and password cannot be empty",Toast.LENGTH_SHORT).show();
        } else {
            authenticationInstance.signOut();
            GsignInClient.signOut().addOnCompleteListener(this,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            showLoginLoading();
                            authenticationInstance.signInWithEmailAndPassword(email, password).addOnCompleteListener(loginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        boolean repeated = false;
                                        if (pastLogins != null) for (String s : pastLogins)
                                            if (s.equals(email)) repeated = true;
                                        String[] newPastLogins;
                                        if (!repeated) {
                                            if (pastLogins != null) {
                                                newPastLogins = new String[pastLogins.length + 1];
                                                for (int i = 0; i < pastLogins.length; i++)
                                                    newPastLogins[i] = pastLogins[i];
                                            } else newPastLogins = new String[1];

                                            newPastLogins[newPastLogins.length - 1] = email;
                                        } else newPastLogins = pastLogins;
                                        String savedLogins = newPastLogins[0];
                                        for (int i = 1; i < newPastLogins.length; i++) {
                                            savedLogins += "\n" + newPastLogins[i];
                                        }
                                        Log.d("SIGN IN PREV", "pastLogins: " + pastLogins);
                                        Log.d("SIGN IN PREV", "savedLogins: " + savedLogins);
                                        SharedPreferences prefs = getSharedPreferences("successfulLogins", MODE_PRIVATE); //Store
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("successfulLogins", savedLogins);
                                        editor.commit();

                                        databaseSignIn(false);
                                    } else {
                                        hideLoadingView();
                                        NetworkInfo info = getNetworkInfo(loginActivity.this);
                                        if (info != null && info.isConnected()) {
                                            if (isConnectionFast(loginActivity.this)) {
                                                Toast.makeText(loginActivity.this, "Invalid email address or password",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(loginActivity.this, "Connection timed out, Please check your internet connection",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(loginActivity.this, "Not connected to internet",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    });
        }
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnectionFast(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        int type = info.getType();
        int subType = info.getSubtype();
        if (info != null && info.isConnected()) {
            if (type == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return false; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return false; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return false; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return true; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return true; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return false; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return true; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return true; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return true; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return true; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return true; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return true; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return false; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return true; // ~ 10+ Mbps
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

}
