package com.example.jonathan.hospitalapp2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class loginActivity extends AppCompatActivity {

    //TODO SIGNUP NEW USERS
    //TODO FORGOT PASSWORD

    GoogleSignInClient GsignInClient;
    private FirebaseAuth authenticationInstance = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userDBRef;
    ProgressDialog progressDialogDisplay;
    String[] pastLogins;

    //TODO Realtime Database faster than Firestore (<1s vs 3-5s delay)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AutoCompleteTextView actv = findViewById(R.id.em);
        actv.setThreshold(0);
        SharedPreferences pref = getSharedPreferences("successfulLogins", MODE_PRIVATE);  //Read
        pastLogins = pref.getString("successfulLogins", "").split("\n");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pastLogins);
        actv.setAdapter(adapter);

        GoogleSignInOptions GsignInOpt = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GsignInClient = GoogleSignIn.getClient(this, GsignInOpt);

        FirebaseUser currentUser = authenticationInstance.getCurrentUser();
        if (currentUser != null) {   //TODO Auto sign in
            try { //TODO Try to prevent unexpected errors from improper/incomplete sign out
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                showLoginLoading();
                databaseSignIn(true);
            } catch (Exception e) {
                Log.w("LOGIN TEST", "onStart: ", e);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideLoadingView();
    }

    void databaseSignIn(boolean autoLogin) {
        final String currentUserEmail = authenticationInstance.getCurrentUser().getEmail();
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
                break;

            case 4:
                userDBRef = database.getReference().child("Patients");   //TODO Flexibility for public
                placeholderAct = PatientActivity.class;
                break;*/
        }

        if (!skipRemainder) {
            final Class nextAct = placeholderAct;
            if (userType < 4) {
                userDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hideLoadingView();
                        if (dataSnapshot.exists())
                            startActivity(new Intent(loginActivity.this, nextAct));
                        else {
                            signOut();
                            Toast.makeText(loginActivity.this, "User not found",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        signOut();
                        Toast.makeText(loginActivity.this, "User not found",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {   //TODO Flexibility of public users
                userDBRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hideLoadingView();
                        if (dataSnapshot.exists())
                            startActivity(new Intent(loginActivity.this, nextAct));
                        else {
                            signOut();
                            Toast.makeText(loginActivity.this, "User not found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        userDBRef = database.getReference().child("Caretakers");
                        userDBRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                hideLoadingView();
                            /*if (dataSnapshot.exists()) startActivity(new Intent(loginActivity.this, CaretakerActivity.class)); TODO REMOVE COMEMNT
                            else {
                                signOut();
                                Toast.makeText(loginActivity.this, "User not found",
                                        Toast.LENGTH_SHORT).show();
                            }*/
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                signOut();
                                Toast.makeText(loginActivity.this, "User not found",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }

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

    void showAccessingData() {
        if (progressDialogDisplay == null) {
            progressDialogDisplay = new ProgressDialog(this);
            progressDialogDisplay.setMessage("Accessing User Data...");
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
        //final AutoCompleteTextView e = (AutoCompleteTextView) findViewById(R.id.em);
        final EditText e = findViewById(R.id.em);
        final String email = e.getText().toString();
        final EditText p = (EditText) findViewById(R.id.pw);
        final String password = p.getText().toString();

        try {
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
                                        hideLoadingView();
                                        showAccessingData();  //TODO Give Users Sense of Progress
                                        boolean repeated = false;
                                        if (pastLogins != null) for (String s : pastLogins)
                                            if (s.equals(email)) repeated = true;
                                        String[] newPastLogins;
                                        if (!repeated) {
                                            if (pastLogins != null)
                                                newPastLogins = new String[pastLogins.length + 1];
                                            else newPastLogins = new String[1];
                                            newPastLogins[newPastLogins.length - 1] = email;
                                        } else newPastLogins = pastLogins;
                                        String savedLogins = newPastLogins[0];
                                        for (int i = 1; i < newPastLogins.length; i++) {
                                            savedLogins += "\n" + newPastLogins[i];
                                        }
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

        } catch (Exception err) {
            hideLoadingView();
            Toast.makeText(loginActivity.this, "Fields cannot be empty",
                    Toast.LENGTH_SHORT).show();
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
