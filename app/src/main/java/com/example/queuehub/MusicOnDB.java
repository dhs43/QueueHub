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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

        // Upload file with id as name
        StorageReference musicRef;
        musicRef = storageRef.child("music/" + idStr);

        musicRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        DatabaseReference queueRef = databaseRef.getReference("queue");

                        if (idStr != null) {
                            Log.d(TAG, idStr);
                            queueRef.child(idStr).setValue("0");
                            Calendar calendar = Calendar.getInstance();
                            queueRef.child(idStr).setValue(calendar.getTimeInMillis());
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
