package com.example.queuehub;

public class Song {
    private String title;
    private String artist;
    private String coverArt;


    public Song(String newTitle, String newArtist) {
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


