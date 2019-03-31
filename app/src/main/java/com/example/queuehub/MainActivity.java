package com.example.queuehub;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


//okay

public class MainActivity extends AppCompatActivity {

    // Reference to FirebaseAuth object
    private FirebaseAuth mAuth;
    // Reference to Firebase Database
    private FirebaseDatabase mDatabaseRef;
    // Reference to Firebase Storage
    private StorageReference mStorageRef;

    // In the future we will get URI from a user-selected file
    Uri file = Uri.parse("android.resource://com.hsuproject.queuehub/" + R.raw.harrison_ford);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing reference to FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();
        // Initializing reference to Firebase Database
        mDatabaseRef = FirebaseDatabase.getInstance();
        // Initializing reference to Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // Register user anonymously with Firebase
        authenticateAnonymously();

        // Initialize class for music upload/download
        MusicOnDB musicOnDB = new MusicOnDB();
        musicOnDB.uploadMusicFile(mStorageRef, mDatabaseRef, file);  // file is the input URI.
        musicOnDB.downloadMusicFile(mStorageRef, mDatabaseRef);
    }



    // Authenticate with Firebase anonymously to allow users
    // to read/write from database without creating an account
    public void authenticateAnonymously(){
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("SignIn", "Signed in successfully");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.e("SignIn", "Sign in failed");
                        }
                    }
                });
    }
}
