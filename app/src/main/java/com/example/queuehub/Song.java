package com.example.queuehub;

public class Song {
    private String title;
    private String artist;
    private String imageURL;
    private int time;


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

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
