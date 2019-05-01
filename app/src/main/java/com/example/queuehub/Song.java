package com.example.queuehub;

public class Song {
    private String title;
    private String artist;
    private String imageURL;


    public Song(String newTitle, String newArtist, String mediaURL) {
        title = newTitle;
        artist = newArtist;
        imageURL = mediaURL;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getImageURL() { return imageURL; }
}
