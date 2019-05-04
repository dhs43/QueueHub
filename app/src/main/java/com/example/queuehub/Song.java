package com.example.queuehub;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class Song {
    private String title;
    private String artist;
    private String imageURL;
    private Long timestamp;
    private Integer vote;


    public Song(String newTitle, String newArtist, String mediaURL, Long myTimestamp, Integer myVote) {
        title = newTitle;
        artist = newArtist;
        imageURL = mediaURL;
        timestamp = myTimestamp;
        vote = myVote;
    }



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getVote() {
        return vote;
    }

    public void setVote(Integer vote) {
        this.vote = vote;
    }
}
