package com.example.queuehub;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.queuehub.MainActivity.currentSong;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private Context context;
    private ArrayList<Song> songsQueue;
    private OnItemClickListener listener;
    private Button btnPlay;
    private SeekBar seekBar;
    private MusicOnDB musicOnDB;
    private Boolean firstJoined = true;

    public interface  OnItemClickListener{
        void onItemClick(Song song);
    }


    public SongAdapter(Context context, ArrayList<Song> songs, Button myBtnPlay, SeekBar mySeekBar,
                       FirebaseDatabase myDatabaseRef, StorageReference myStorageRef) {
        this.context = context;
        this.songsQueue = songs;
        btnPlay = myBtnPlay;
        seekBar = mySeekBar;
        musicOnDB = new MusicOnDB(myStorageRef, myDatabaseRef);
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songsQueue.get(position);
        holder.tvSongTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        //holder.ivCoverArt.setImageResource(R.drawable.image);
        if (! song.getImageURL().equals("none")) {
            Glide.with(context)
                    .load(song.getImageURL())
                    .apply(new RequestOptions().placeholder(R.drawable.image))
                    .into(holder.ivCoverArt);
        }else{
            holder.ivCoverArt.setImageResource(R.drawable.image);
        }
        holder.bind(songsQueue.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return songsQueue.size();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.listed_song, parent, false);
        return new ViewHolder(view);
    }

    public void clear() {
        songsQueue.clear();
        notifyDataSetChanged();
    }

    public void addSongs(ArrayList<Song> songList){
        songsQueue.addAll(sortByTimestamp(songList));
        MainActivity.songList = songsQueue;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView ivCoverArt;
        public TextView tvSongTitle;
        public TextView tvArtist;
        public ConstraintLayout clSong;



        public ViewHolder(View itemView){
            super(itemView);
            ivCoverArt = itemView.findViewById(R.id.ivCoverArt);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            clSong = itemView.findViewById(R.id.clSong);
        }



        public void bind(final Song song, final OnItemClickListener listener){
            String title = song.getTitle();
            if(title.matches(currentSong.getTitle()))
            {
                clSong.setBackgroundColor(Color.GRAY);
            }else{
                clSong.setBackgroundColor(Color.TRANSPARENT);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    currentSong = song;
                    clSong.setBackgroundColor(Color.GRAY);
                    notifyDataSetChanged();

                    Toast.makeText(context,"Now playing: " + song.getTitle(), Toast.LENGTH_LONG).show();
                    final String selection = song.getTitle();
                    MainActivity.musicPlayer.playFile(selection);
                }
            });
        }
    }

    public ArrayList<Song> sortByTimestamp(List<Song> unsorted){
        if (unsorted.size() < 1) {
            return null;
        }
        ArrayList<Song> sorted = new ArrayList<>();
        ArrayList<Pair<Long, Song>> songPairs = new ArrayList<>();
        for (Song song : unsorted) {
            songPairs.add(new Pair<>(song.getTimestamp(), song));
        }

        Collections.sort(songPairs, new Comparator<Pair<Long, Song>>() {
            @Override
            public int compare(Pair<Long, Song> song1, Pair<Long, Song> song2) {
                if (song1.first < song2.first) {
                    return -1;
                }else if (song1.first.equals(song2.first)) {
                    return 0;
                }else{
                    return 1;
                }
            }
        });

        for (Pair pair : songPairs) {
            sorted.add((Song) pair.second);
        }

        if (currentSong.getTitle().equals("title")) {
            currentSong = sorted.get(0);
        }

        if (firstJoined) {
            firstJoined = false;
            if (! currentSong.getImageURL().equals("none")) {
                Glide.with(context)
                        .load(MainActivity.currentSong.getImageURL())
                        .apply(new RequestOptions().placeholder(R.drawable.image))
                        .into(MainActivity.ivCover);
            }
        }

        return sorted;
    }
}