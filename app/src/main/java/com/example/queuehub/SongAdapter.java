package com.example.queuehub;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private Context context;
    private List<Song> songs;
    private OnItemClickListener listener;
    private Button btnPlay;
    private SeekBar seekBar;


    public interface  OnItemClickListener{
        void onItemClick(Song song);
    }


    public SongAdapter(Context context, List<Song> songs, Button myBtnPlay, SeekBar mySeekBar) {
        this.context = context;
        this.songs = songs;
        btnPlay = myBtnPlay;
        seekBar = mySeekBar;
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.tvSongTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.ivCoverArt.setImageResource(R.drawable.image);

        holder.bind(songs.get(position), listener);
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
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
        }

        public void bind(final Song song, final OnItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context,"Now playing: " + song.getTitle(), Toast.LENGTH_LONG).show();
                    final String selection = song.getTitle();


                    MusicOnDB musicOnDB = new MusicOnDB();
                    StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

                    musicOnDB.getFileUrl(selection, mStorageRef, new MusicOnDB.DatabaseCallback() {
                        @Override
                        public void onCallback(String thisURL) {


                            MainActivity.player.stop();
                            btnPlay.setBackgroundResource(R.drawable.play);

                            MainActivity.player = new MediaPlayer();
                            //player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            //  @Override
                            //public void onCompletion(MediaPlayer mp) {
                            //  btnPlay.setBackgroundResource(R.drawable.play);
                            //}
                            //});
                            try {
                                Log.d("fetchingFirebase3", "getSongs");
                                MainActivity.player.setDataSource(thisURL);
                                MainActivity.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        //adding seek bar in here
                                        int totalTime = MainActivity.player.getDuration();
                                        seekBar.setMax(totalTime);
                                        //seek bar
                                        seekBar.setOnSeekBarChangeListener(
                                                new SeekBar.OnSeekBarChangeListener() {
                                                    @Override
                                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                        if(fromUser){
                                                            MainActivity.player.seekTo(progress);
                                                            seekBar.setProgress(progress);
                                                        }
                                                    }

                                                    @Override
                                                    public void onStartTrackingTouch(SeekBar seekBar) {

                                                    }

                                                    @Override
                                                    public void onStopTrackingTouch(SeekBar seekBar) {

                                                    }
                                                }
                                        );

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                while(MainActivity.player != null) {
                                                    try{
                                                        Message msg = new Message();
                                                        msg.what = MainActivity.player.getCurrentPosition();
                                                        //handler.sendMessage(msg);
                                                        Thread.sleep(1000);
                                                    } catch (InterruptedException e) {}
                                                }
                                            }
                                        }).start();
                                        //end seek bar addition
                                        MainActivity.player.start();
                                        seekBar.setBackgroundColor(Color.LTGRAY); // Temporary to show when player is ready
                                        btnPlay.setBackgroundResource(R.drawable.stop);
                                    }
                                });
                                MainActivity.player.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

        }
    }
}