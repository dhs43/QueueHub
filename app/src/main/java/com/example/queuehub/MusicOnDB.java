package com.example.queuehub;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;

class MusicOnDB {

    private StorageReference storageRef;
    private FirebaseDatabase databaseRef;
    private String filename;

    final private String TAG = "MusicOnDB";

    public MusicOnDB(StorageReference myStorageRef, FirebaseDatabase myDatabaseRef) {
        storageRef = myStorageRef;
        databaseRef = myDatabaseRef;
    }

    // Expects a reference to the main Firebase storage and file to upload
    void uploadMusicFile(final Uri file, final ProgressBar uploadProgressBar, Uri fileUri) {

        uploadProgressBar.setVisibility(View.VISIBLE);

        //get file metadata
        final String songTitle;
        final String songArtist;
        byte[] songBitMap;
        final String[] albumArtURL = new String[1];
        MetadataParser parser = new MetadataParser();
        songTitle = parser.getSongTitle(fileUri);
        songArtist = parser.getSongArtist(fileUri);
        songBitMap = parser.getSongBtyeArray(fileUri);

        // Upload album art to Firebase
        if (songBitMap != null) {
            final StorageReference albumArtRef;
            albumArtRef = storageRef.child("album_art/" + songTitle);
            albumArtRef.putBytes(songBitMap)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            albumArtRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    albumArtURL[0] = uri.toString();

                                    // Create file metadata including the content type
                                    StorageMetadata metadata = new StorageMetadata.Builder()
                                            //.setContentType("image/jpg")
                                            .setCustomMetadata("Song Title", songTitle)
                                            .setCustomMetadata("Song Artist", songArtist)
                                            .setCustomMetadata("album_art", albumArtURL[0])
                                            .build();

                                    StorageReference musicRef;
                                    musicRef = storageRef.child("music/" + songTitle);
                                    musicRef.putFile(file, metadata)
                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    DatabaseReference queueRef = databaseRef.getReference("queue");

                                                    if (songTitle != null) {
                                                        Log.d(TAG, songTitle);
                                                        queueRef.child(songTitle).setValue("0");
                                                        Calendar calendar = Calendar.getInstance();
                                                        queueRef.child(songTitle).setValue(calendar.getTimeInMillis());
                                                        queueRef.child(songTitle).child("title").setValue(songTitle);
                                                        queueRef.child(songTitle).child("artist").setValue(songArtist);
                                                        queueRef.child(songTitle).child("image").setValue(albumArtURL[0]);
                                                        uploadProgressBar.setVisibility(View.GONE);

                                                        Log.d(TAG, "File uploaded");
                                                    }
                                                }
                                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            int currentProgress = (int) progress;
                                            uploadProgressBar.setProgress(currentProgress);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    });
                                }
                            });
                            Log.d(TAG, "Bitmap uploaded");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            });
        } else {
            // This is for when no album art could be extracted.
            // Same code as above, but copied b/c of difficulties with
            //  Firebase's asynchronous functions.

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    //.setContentType("image/jpg")
                    .setCustomMetadata("Song Title", songTitle)
                    .setCustomMetadata("Song Artist", songArtist)
                    .build();

            StorageReference musicRef;
            musicRef = storageRef.child("music/" + songTitle);
            musicRef.putFile(file, metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            DatabaseReference queueRef = databaseRef.getReference("queue");

                            if (songTitle != null) {
                                Log.d(TAG, songTitle);
                                queueRef.child(songTitle).setValue("0");
                                Calendar calendar = Calendar.getInstance();
                                queueRef.child(songTitle).setValue(calendar.getTimeInMillis());
                                queueRef.child(songTitle).child("title").setValue(songTitle);
                                queueRef.child(songTitle).child("artist").setValue(songArtist);
                                uploadProgressBar.setVisibility(View.GONE);

                                Log.d(TAG, "File uploaded");
                            }
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            });
        }
    }


    void getFileUrl(String thisFilename, final DatabaseCallback databaseCallback) {

        filename = thisFilename;

        new getFileUrlAsync().execute(new DatabaseCallback() {
            @Override
            public void onCallback(String thisURL) {
                databaseCallback.onCallback(thisURL);
            }
        });
    }

    private class getFileUrlAsync extends AsyncTask<DatabaseCallback, Void, Uri> {

        @Override
        protected Uri doInBackground(final DatabaseCallback... databaseCallbacks) {
            storageRef.child("music").child(filename).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    databaseCallbacks[0].onCallback(uri.toString());
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            });

            return null;
        }
    }

    public interface DatabaseCallback {
        void onCallback(String thisURL);
    }

    // To get the names of the songs in the queue
    public void getSongs(FirebaseDatabase database, final songNamesCallback songsCallback) {
        Log.d("fetchingFirebase0", "getSongs");
        database.getInstance().getReference().child("queue")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Song> songNames = new ArrayList<>();
                        for (DataSnapshot songChild : dataSnapshot.getChildren()) {
                            String title = songChild.child("title").getValue().toString();
                            String artist = songChild.child("artist").getValue().toString();
                            String imageURL = songChild.child("image").getValue().toString();

                            Song thisSong = new Song(title, artist, imageURL);

                            songNames.add(thisSong);
                        }
                        songsCallback.onCallback(songNames);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public interface songNamesCallback {
        void onCallback(ArrayList<Song> songNames);
    }

}
