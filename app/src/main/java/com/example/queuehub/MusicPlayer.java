package com.example.queuehub;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends MainActivity {

    private MediaPlayer player;
    private int totalTime;
    private SeekBar seekBar;
    final private String TAG = "MusicPlayer";



    public MusicPlayer(SeekBar mySeekBar) {
        player = new MediaPlayer();
        totalTime = 0;
        player = new MediaPlayer();
    }


    public void playFile(final StorageReference mStorageRef, final FirebaseDatabase mDatabaseRef, final Button btnPlay, final SeekBar mySeekBar) {
        seekBar = mySeekBar;
        final DatabaseReference queueRef = mDatabaseRef.getReference("queue");
        Query lastQuery = queueRef.orderByValue().limitToLast(1);
        lastQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MusicOnDB musicOnDB = new MusicOnDB();
                String filename = (dataSnapshot.getKey());
                musicOnDB.getFileUrl(filename, mStorageRef, new MusicOnDB.DatabaseCallback() {
                    @Override
                    public void onCallback(String fileURL) {
                        // Release memory from previously-playing player
                        player.release();
                        player = new MediaPlayer();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                btnPlay.setBackgroundResource(R.drawable.play);
                            }
                        });
                        try {
                            player.setDataSource(fileURL);
                            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    //adding seek bar in here
                                    totalTime = player.getDuration();
                                    seekBar.setMax(totalTime);
                                    //seek bar
                                    seekBar.setOnSeekBarChangeListener(
                                            new SeekBar.OnSeekBarChangeListener() {
                                                @Override
                                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                    if (fromUser) {
                                                        player.seekTo(progress);
                                                        seekBar.setProgress(progress);
                                                    }
                                                }

                                                @Override
                                                public void onStartTrackingTouch(SeekBar seekBar) {

                                                }

                                                @Override
                                                public void onStopTrackingTouch(SeekBar seekBar) {

                                                }
                                            }
                                    );

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            while (player != null) {
                                                try {
                                                    Message msg = new Message();
                                                    msg.what = player.getCurrentPosition();
                                                    handler.sendMessage(msg);
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                }
                                            }
                                        }
                                    }).start();
                                    //end seek bar addition
                                    //player.start();
                                    seekBar.setBackgroundColor(Color.LTGRAY); // Temporary to show when player is ready
                                    btnPlay.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!player.isPlaying()) {
                                                //stopping
                                                player.start();
                                                seekBar.setBackgroundColor(Color.TRANSPARENT);
                                                btnPlay.setBackgroundResource(R.drawable.stop);
                                            } else {
                                                //playing
                                                player.pause();
                                                btnPlay.setBackgroundResource(R.drawable.play);
                                            }
                                        }
                                    });
                                }
                            });
                            player.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }
    //seek bar helper functions
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            remainingTime = findViewById(R.id.remainingTime);

            int currentPosition = msg.what;
            seekBar.setProgress(currentPosition);

            String elapsed = createTimeLabel(currentPosition);
            elapsedTime.setText(elapsed);

            String remaining = createTimeLabel(totalTime - currentPosition);
            remainingTime.setText("- " + remaining);
        }
    };

    public String createTimeLabel(int time){
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if(sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
    //end seek bar helper functions




    public void updateQueue (FirebaseDatabase mDatabaseRef) {
        //adding in queue here
        MusicOnDB musicOnDB = new MusicOnDB();
        final List<Song> songList = new ArrayList<>();
        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
            @Override
            public void onCallback(List<String> songNames) {
                for(String name : songNames){
                    songList.add(new Song(name, "Unknown"));
                }
                populateQueue(songList);
            }
        });
    }

    private void populateQueue(List<Song> songs) {
        List<Song> toAdd = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            toAdd.add(songs.get(i));
        }

        songsAdapter.clear();
        songsAdapter.addSongs(toAdd);
    }
}