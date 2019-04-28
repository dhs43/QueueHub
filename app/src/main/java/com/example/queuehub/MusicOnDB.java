package com.example.queuehub;

import android.content.Context;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

class MusicOnDB {

    final private String TAG = "MusicOnDB";

    // Expects a reference to the main Firebase storage and file to upload
    void uploadMusicFile(final Uri file, StorageReference storageRef, final FirebaseDatabase databaseRef, final ProgressBar progressBar, Uri fileUri) {

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


        //get file metadata
        String songTitle;
        String songArtist;
        byte[] songBitMap;
        MetadataParser parser = new MetadataParser();
        songTitle = parser.getSongTitle(fileUri);
        songArtist = parser.getSongArtist(fileUri);
        songBitMap = parser.getSongBtyeArray(fileUri);

        // Upload album art to Firebase
        final StorageReference albumArtRef;
        albumArtRef = storageRef.child("album_art/" + songTitle);
        albumArtRef.putBytes(songBitMap)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Bitmap uploaded");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                });


        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                //.setContentType("image/jpg")
                .setCustomMetadata("Song Title", songTitle)
                .setCustomMetadata("Song Artist", songArtist)
                .build();

        metadata.getCustomMetadata("Song Title");
        metadata.getCustomMetadata("Song Artist");


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


    void getAlbumArtUrl(String filename, StorageReference storageRef, final DatabaseCallback databaseCallback) {

        storageRef.child("album_art").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String fileURL = uri.toString();
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
}
