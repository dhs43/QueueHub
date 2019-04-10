package com.example.queuehub;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class MainActivity extends AppCompatActivity {

    final private String TAG = "MainActivity";

    // Storage permissions parameter
    static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;
    Uri selectedFile;

    // Reference to FirebaseAuth object
    private FirebaseAuth mAuth;
    // Reference to Firebase Database
    private FirebaseDatabase mDatabaseRef;
    // Reference to Firebase Storage
    private StorageReference mStorageRef;


    MediaPlayer player;
    Button btnPlay;
    ImageView ivCover;
    SeekBar seekBar;
    TextView elapsedTime;
    TextView remainingTime;
    ProgressBar progressBar;
    int totalTime;

    //for the queue
    SongAdapter songsAdapter;
    RecyclerView rvSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = findViewById(R.id.btnPlay);
        elapsedTime = findViewById(R.id.elapsedTime);
        remainingTime = findViewById(R.id.remainingTime);
        seekBar = findViewById(R.id.seekBar);
        //also need to set cover art
        ivCover = findViewById(R.id.ivCover);
        progressBar = findViewById(R.id.loading_spinner);

        //for the queue
        List<Song> songs = new ArrayList<>();
        rvSongs = findViewById(R.id.rvSongs);
        songsAdapter = new SongAdapter(this, songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songsAdapter);

        Button btnSelectFile = findViewById(R.id.btnSelectFile);

        // Request access to local files
        requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        // Initializing reference to FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();
        // Initializing reference to Firebase Database
        mDatabaseRef = FirebaseDatabase.getInstance();
        // Initializing reference to Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // Register user anonymously with Firebase
        authenticateAnonymously();

        player = new MediaPlayer();

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });


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
                                                    if(fromUser){
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
                                            while(player != null) {
                                                try{
                                                    Message msg = new Message();
                                                    msg.what = player.getCurrentPosition();
                                                    handler.sendMessage(msg);
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {}
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Result of the music file selection
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    selectedFile = data.getData();

                    MusicOnDB musicOnDB = new MusicOnDB();
                    musicOnDB.uploadMusicFile(selectedFile, mStorageRef, mDatabaseRef, progressBar);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Authenticate with Firebase anonymously to allow users
    // to read/write from database without creating an account
    public void authenticateAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Signed in successfully");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.e(TAG, "Sign in failed");
                        }
                    }
                });
    }

    private void populateQueue( List<Song> songs) {
        List<Song> toAdd = new ArrayList<>();
        for(int i = 0; i < songs.size(); i++){
            toAdd.add(songs.get(i));
        }

        songsAdapter.clear();
        songsAdapter.addSongs(toAdd);
    }
}
