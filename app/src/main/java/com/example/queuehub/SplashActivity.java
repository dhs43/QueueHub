package com.example.queuehub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import static android.os.Build.ID;

public class SplashActivity extends AppCompatActivity {

    private Button btnStart;
    private Button btnJoin;
    private TextInputEditText etSession;
    private FirebaseDatabase mDatabaseRef;
    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnStart = findViewById(R.id.btnCreate);
        btnJoin = findViewById(R.id.btnJoin);
        etSession = findViewById(R.id.etSession);
        context = this;

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //will use the etSession variable

                mDatabaseRef = FirebaseDatabase.getInstance();
                Session mySession = new Session(mDatabaseRef);
                mySession.createSession(new Session.createSessionCallback() {
                    @Override
                    public void onCallback(String ID) {
                        MainActivity.sessionID = ID;
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                });
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sessionID = etSession.getText().toString();
                
                if(sessionID.length() < 4)
                {
                    Toast.makeText(SplashActivity.this, "Invalid Session ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Session mySession = new Session(mDatabaseRef);
                mySession.sessionExists(sessionID, new Session.sessionExistsCallback() {
                    @Override
                    public void onCallback(Boolean myBool, String ID) {
                        if(myBool)
                        {
                            MainActivity.isCreator = false;
                            MainActivity.sessionID = ID;
                            Intent i = new Intent(context, MainActivity.class);
                            startActivity(i);
                        }
                        else
                        {
                            Toast.makeText(context, "Invalid Session ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
