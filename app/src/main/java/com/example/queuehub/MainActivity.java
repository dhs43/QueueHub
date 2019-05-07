package com.example.queuehub;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

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

    static Boolean isHost = false;
    static String sessionID;
    static Boolean isSession;
    static MediaPlayer player;
    static Song currentSong = new Song("title", "artist", "url", 0L,0);
    static ArrayList<Song> songList;
    static MusicPlayer musicPlayer;
    Button btnPlay;
    static ImageView ivCover;
    SeekBar seekBar;
    TextView elapsedTime;
    TextView remainingTime;
    ProgressBar uploadProgressBar;
    Button btnSkip;
    Button btnToggle;
    int totalTime;
    MusicOnDB musicOnDB;
    static Context context;

    //for the queue
    SongAdapter songsAdapter;
    RecyclerView rvSongs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //also need to set cover art
        ivCover = findViewById(R.id.ivCover);
        seekBar = findViewById(R.id.seekBar);
        btnPlay = findViewById(R.id.btnPlay);
        btnToggle = findViewById(R.id.btnToggle);
        elapsedTime = findViewById(R.id.elapsedTime);
        remainingTime = findViewById(R.id.remainingTime);
        btnSkip = findViewById(R.id.btnSkip);
        uploadProgressBar = findViewById(R.id.determinateBar);
        player = new MediaPlayer();
        songList = new ArrayList<>();
        context = this;

        //for the queue
        ArrayList<Song> songsQueue = new ArrayList<>();
        rvSongs = findViewById(R.id.rvSongs);
        songsAdapter = new SongAdapter(this, songsQueue, btnPlay, seekBar, mDatabaseRef, mStorageRef);
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

        musicOnDB = new MusicOnDB(mStorageRef, mDatabaseRef);

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });


        // Instantiate a MusicPlayer

        musicPlayer = new MusicPlayer(seekBar, btnPlay, btnToggle, remainingTime, elapsedTime,
                songsAdapter, btnSkip, mStorageRef, mDatabaseRef, musicOnDB, context);

        // Play last song
        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
            @Override
            public void onCallback(ArrayList<Song> songNames) {
                if (songNames.size() > 0) {
                    songNames = songsAdapter.sortByTimestamp(songNames);
                    if (MainActivity.player.isPlaying()) { return; }
                    musicPlayer.playFile(songNames.get(0).getTitle());
                }else{
                    return;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Result of the music file selection
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    selectedFile = data.getData();
                    musicOnDB.uploadMusicFile(selectedFile, uploadProgressBar, selectedFile);
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
}