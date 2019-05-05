package com.example.queuehub;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Session extends AppCompatActivity {

    private FirebaseDatabase database;

    public Session(FirebaseDatabase myDatabase)
    {
        database = myDatabase;
    }

    //create random 4 digit code = random number from 0 to 9999
    public void randomNum(final randomNumCallback randomNumCallbacks)
    {
        int posID = (int)(Math.random()*9000)+1000;
        final String stringID = Integer.toString(posID);
        database.getInstance().getReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(stringID))
                        {
                            Log.d("henlo", "found");
                            randomNum(new randomNumCallback() {
                                @Override
                                public void onCallback(String ID) {
                                    randomNumCallbacks.onCallback(stringID);
                                }
                            });
                        }
                        else
                        {
                            randomNumCallbacks.onCallback(stringID);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void sessionExists(String ID, final sessionExistsCallback sessionExistsCallaback)
    {
        final String stringID = ID;
        database.getInstance().getReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(stringID))
                        {
                            //MainActivity.isAHost = true;
                            sessionExistsCallaback.onCallback(true, stringID);
                        }
                        else
                        {
                            //MainActivity.isAHost = false;
                            sessionExistsCallaback.onCallback(false, stringID);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public interface sessionExistsCallback {
        void onCallback(Boolean yee, String ID);
    }


    public void createSession()
    {
        //get working sessionID
        this.randomNum(new randomNumCallback() {
            @Override
            public void onCallback(String ID) {
                //set code to sessionID
                MainActivity.sessionID = ID;
            }
        });

        //isHost = T
        MainActivity.isHost = true;
    }

    public interface randomNumCallback {
        void onCallback(String ID);
    }
}
