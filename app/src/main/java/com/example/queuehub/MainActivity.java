package com.example.queuehub;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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


    static MediaPlayer player;
    Button btnPlay;
    ImageView ivCover;
    SeekBar seekBar;
    TextView elapsedTime;
    TextView remainingTime;
    ProgressBar progressBar;
    Button btnSkip;
    int totalTime;
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
        elapsedTime = findViewById(R.id.elapsedTime);
        remainingTime = findViewById(R.id.remainingTime);
        progressBar = findViewById(R.id.loading_spinner);
        btnSkip = findViewById(R.id.btnSkip);
        player = new MediaPlayer();
        context = this;

        //for the queue
        List<Song> songs = new ArrayList<>();
        rvSongs = findViewById(R.id.rvSongs);
        songsAdapter = new SongAdapter(this, songs, btnPlay, seekBar);
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
        MusicPlayer mMusicPlayer = new MusicPlayer(seekBar, btnPlay, remainingTime, elapsedTime, songsAdapter, btnSkip);
        // This line can be moved to wherever we need to play the song.
        mMusicPlayer.playFile(mStorageRef,mDatabaseRef);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Result of the music file selection
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    selectedFile = data.getData();

                    MusicOnDB musicOnDB = new MusicOnDB();
                    musicOnDB.uploadMusicFile(selectedFile, mStorageRef, mDatabaseRef, progressBar, selectedFile);
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