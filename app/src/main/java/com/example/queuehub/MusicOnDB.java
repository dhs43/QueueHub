package com.example.queuehub;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

class MusicOnDB {

    final private String TAG = "MusicOnDB";

    // Expects a reference to the main Firebase storage and file to upload
    void uploadMusicFile(final Uri file, StorageReference storageRef, final FirebaseDatabase databaseRef, final ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        // Get id of file
        String[] segments = Objects.requireNonNull(file.getPath()).split("/");
        final String idStr = segments[segments.length - 1];

        // Upload file with id as name.
        final String filename;
        if (idStr.contains(".")){
            filename = idStr.substring(0, idStr.indexOf("."));
        }else{
            filename = idStr;
        }

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio")
                .setCustomMetadata("name", "test")
                .build();

        StorageReference musicRef;
        musicRef = storageRef.child("music/" + filename);
        musicRef.putFile(file, metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        DatabaseReference queueRef = databaseRef.getReference("queue");

                        if (idStr != null) {
                            Log.d(TAG, filename);
                            queueRef.child(filename).setValue("0");
                            Calendar calendar = Calendar.getInstance();
                            queueRef.child(filename).setValue(calendar.getTimeInMillis());
                            progressBar.setVisibility(View.GONE);

                            Log.d(TAG, "File uploaded");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });
    }


    void getFileUrl(String filename, StorageReference storageRef, final DatabaseCallback databaseCallback) {

        storageRef.child("music").child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String fileURL = uri.toString();
                databaseCallback.onCallback(fileURL);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    public interface DatabaseCallback {
        void onCallback(String thisURL);
    }


    // To get the names of the songs in the queue
    public void getSongs(FirebaseDatabase database, final songNamesCallback songsCallback){
        Log.d("fetchingFirebase0", "getSongs");
        database.getInstance().getReference().child("queue")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> songNames = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String song = snapshot.getKey();
                            songNames.add(song);
                        }
                        songsCallback.onCallback(songNames);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public interface songNamesCallback {
        void onCallback(List<String> songNames);
    }

    /*public String getNextSong(FirebaseDatabase database, String currentSong, final nextSongCallback songsCallback){
        Log.d("fetchingFirebase0", "getSongs");
        final List<String> songNames = new ArrayList<>();
        database.getInstance().getReference().child("queue")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String song = snapshot.getKey();
                            songNames.add(song);
                        }
                        songsCallback.onCallback(songNames);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        String next = songNames.get(0);
        for(int i = 0; i < songNames.size(); i++)
        {
            if(songNames.get(i).equals(currentSong))
            {
                next = songNames.get(i+1);
                break;
            }
        }
        return next;
    }

    public interface nextSongCallback {
        void onCallback(List<String> songNames);
    }
    */
}
