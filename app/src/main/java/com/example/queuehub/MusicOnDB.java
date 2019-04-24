package com.example.queuehub;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
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

        //get file metadata
        String songTitles;
        String songArtists;
        byte[] songBitMaps;
        MetadataParser parser = new MetadataParser();
        songTitles = parser.getSongTitle(idStr);
        //songArtists = parser.getSongArtist(filename);
        //try {
        //songBitMaps = parser.getSongBtyeArray(filename);
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}


        final StorageReference musicRef;
        musicRef = storageRef.child("music/" + filename);

        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                //.setContentType("image/jpg")
                .setCustomMetadata("Song Title", songTitles)
                //.setCustomMetadata("Song Artist", songArtists)
                .build();

        // Update metadata properties
        musicRef.updateMetadata(metadata);

        String Title = metadata.getCustomMetadata("Song Title");
        //String Artist = metadata.getCustomMetadata("Song Artist");
        musicRef.putFile(file)
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


}
