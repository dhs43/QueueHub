package com.example.queuehub;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

class MusicOnDB {
    // Expects a reference to the main Firebase storage and file to upload
    void uploadMusicFile(StorageReference storageRef, final FirebaseDatabase databaseRef, final Uri file) {
        StorageReference musicRef;
        musicRef = storageRef.child("music/" + file.getPath());
        musicRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        DatabaseReference queueRef = databaseRef.getReference("queue");
                        queueRef.child(Objects.requireNonNull(file.getPath())).setValue("0");
                        Log.d("FileUpload", "File uploaded");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FileUpload", e.getMessage());
                    }
                });

    }

    private String filename;
    private File downloadFile;

    File downloadMusicFile(final StorageReference storageRef, final FirebaseDatabase databaseRef) {
        DatabaseReference queueRef = databaseRef.getReference("queue");

        Query lastQuery = queueRef.orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                filename = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                filename = filename.substring(1 ,filename.indexOf('='));

                Log.d("Filename", filename);

                try {
                    downloadFile = File.createTempFile(filename, ".mp3");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                storageRef.child("music").child(filename).getFile(downloadFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.d("FileDownload", "File was downloaded successfully");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FileDownload", "File download failed: " + e.getMessage());
                    }
                });

                Log.d("DL FILE", downloadFile.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                filename = "Cancelled";
            }
        });

        return downloadFile;
    }
}
