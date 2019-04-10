package com.example.queuehub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{
    private Context context;
    private List<Song> songs;

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        //replace unknown with the song.getTitle
        holder.tvSongTitle.setText(song.getTitle());
        //replace unknown with the song.getArtist
        holder.tvArtist.setText(song.getArtist());
        holder.ivCoverArt.setImageResource(R.drawable.image);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.listed_song, parent, false);
        return new ViewHolder(view);
    }

    public void clear() {
        songs.clear();
        notifyDataSetChanged();
    }

    public void addSongs(List<Song> songList){
        songs.addAll(songList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView ivCoverArt;
        public TextView tvSongTitle;
        public TextView tvArtist;

        public ViewHolder(View itemView){
            super(itemView);
            ivCoverArt = itemView.findViewById(R.id.ivCoverArt);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
        }
    }

}
