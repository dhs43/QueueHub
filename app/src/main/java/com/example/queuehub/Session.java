package com.example.queuehub;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Session {

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
        database.getInstance().getReference().child("queue")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(stringID))
                        {
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
        database.getInstance().getReference().child("queue")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(stringID))
                        {
                            //MainActivity.isAHost = true;
                            sessionExistsCallaback.onCallback(true);
                        }
                        else
                        {
                            //MainActivity.isAHost = false;
                            sessionExistsCallaback.onCallback(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public interface sessionExistsCallback {
        void onCallback(Boolean yee);
    }

    public void joinSession(String ID)
    {
        this.sessionExists(ID, new sessionExistsCallback() {
            @Override
            public void onCallback(Boolean yee) {
               MainActivity.isAHost = yee;
            }
        });
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
