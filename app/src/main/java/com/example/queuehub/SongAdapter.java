package com.example.queuehub;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.queuehub.MainActivity.btnPlay;
import static com.example.queuehub.MainActivity.player;
import static com.example.queuehub.MainActivity.seekBar;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

        private Context context;
        private List<Song> songs;
        private OnItemClickListener listener;


        public interface  OnItemClickListener{
            void onItemClick(Song song);
        }


        public SongAdapter(Context context, List<Song> songs) {
            this.context = context;
            this.songs = songs;
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


                                // Release memory from previously-playing player
                                player.stop();
                                player.release();
                                btnPlay.setBackgroundResource(R.drawable.play);

                                player = new MediaPlayer();
                                //player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                  //  @Override
                                    //public void onCompletion(MediaPlayer mp) {
                                      //  btnPlay.setBackgroundResource(R.drawable.play);
                                    //}
                                //});
                                try {
                                    player.setDataSource(thisURL);
                                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            //adding seek bar in here
                                            int totalTime = player.getDuration();
                                            seekBar.setMax(totalTime);
                                            //seek bar
                                            seekBar.setOnSeekBarChangeListener(
                                                    new SeekBar.OnSeekBarChangeListener() {
                                                        @Override
                                                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                            if(fromUser){
                                                                player.seekTo(progress);
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
                                                    while(player != null) {
                                                        try{
                                                            Message msg = new Message();
                                                            msg.what = player.getCurrentPosition();
                                                            //handler.sendMessage(msg);
                                                            Thread.sleep(1000);
                                                        } catch (InterruptedException e) {}
                                                    }
                                                }
                                            }).start();
                                            //end seek bar addition
                                            player.start();
                                            seekBar.setBackgroundColor(Color.LTGRAY); // Temporary to show when player is ready
                                            btnPlay.setBackgroundResource(R.drawable.stop);
                                        }
                                    });
                                    player.prepare();
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