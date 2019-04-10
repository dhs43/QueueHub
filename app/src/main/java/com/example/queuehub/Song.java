package com.example.queuehub;

import com.google.android.gms.common.util.Strings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Song {
    private String title;
    private String artist;
    private String coverArt;


    public Song(Song newTitle, String newArtist) {
        title = newTitle;
        artist = newArtist;
    }

    public String getTitle() {
        return title;
    }


    public String getArtist() {
        return artist;
    }

}


