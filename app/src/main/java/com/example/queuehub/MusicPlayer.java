package com.example.queuehub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.queuehub.MainActivity.currentSong;

public class MusicPlayer {

    private int totalTime;
    private SeekBar seekBar;
    private Button btnPlay;
    private Button btnSkip;
    private Button btnToggle;
    private TextView remainingTime;
    private TextView elapsedTime;
    private SongAdapter songsAdapter;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabaseRef;
    private MusicOnDB musicOnDB;
    final private String TAG = "MusicPlayer";
    private Context context;


    public MusicPlayer(SeekBar mySeekBar, Button myButtonPlay, Button myButtonToggle, TextView myRemainingTime,
                       TextView myElapsedTime, SongAdapter mySongAdapter, final Button myBtnSkip,
                       StorageReference myStorageRef, FirebaseDatabase myDatabaseRef, MusicOnDB myMusicOnDB, Context myContext) {
        totalTime = 0;
        seekBar = mySeekBar;
        btnPlay = myButtonPlay;
        btnSkip = myBtnSkip;
        remainingTime = myRemainingTime;
        elapsedTime = myElapsedTime;
        songsAdapter = mySongAdapter;
        mStorageRef = myStorageRef;
        mDatabaseRef = myDatabaseRef;
        musicOnDB = myMusicOnDB;
        context = myContext;
        btnToggle = myButtonToggle;

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isHost) {
                    MainActivity.isHost = false;
                    seekBar.setVisibility(View.GONE);
                    elapsedTime.setVisibility(View.GONE);
                    remainingTime.setVisibility(View.GONE);
                    btnToggle.setBackgroundResource(R.drawable.rounder_button_yahdig);
                    btnToggle.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.colorAccent));
                    MainActivity.player.stop();
                } else {
                    MainActivity.isHost = true;
                    seekBar.setVisibility(View.GONE);
                    elapsedTime.setVisibility(View.GONE);
                    remainingTime.setVisibility(View.GONE );
                    btnToggle.setBackgroundResource(R.drawable.rounder2);
                    btnToggle.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.purp));
                    // Play the first song in the queue
                    musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
                        @Override
                        public void onCallback(ArrayList<Song> songNames) {
                            songNames = songsAdapter.sortByTimestamp(songNames);
                            if (!MainActivity.player.isPlaying()) {
                                playFile(songNames.get(0).getTitle());
                            }
                        }
                    });
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isHost) {

                    if (!MainActivity.player.isPlaying()) {
                        //stopping
                        MainActivity.player.start();
                        seekBar.setBackgroundColor(Color.TRANSPARENT);
                        btnPlay.setBackgroundResource(R.drawable.stop);
                    } else {
                        //playing
                        MainActivity.player.pause();
                        btnPlay.setBackgroundResource(R.drawable.play);
                    }
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isHost) {
                    playNextSong();
                }
            }
        });
    }

    public void playNextSong() {
        MainActivity.player.stop();

        if (mDatabaseRef.getReference().child(MainActivity.sessionID)
                .child(MainActivity.currentSong.getTitle()).getKey() != null) {

            musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
                @Override
                public void onCallback(ArrayList<Song> songNames) {
                    if ((MainActivity.songList.size() > 1) && (MainActivity.isHost)) {
                        mDatabaseRef.getReference().child(MainActivity.sessionID)
                                .child(MainActivity.currentSong.getTitle()).removeValue();
                    }
                }
            });
        }

        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
            @Override
            public void onCallback(ArrayList<Song> songList) {
                songList = songsAdapter.sortByTimestamp(songList);
                MainActivity.currentSong = songList.get(0);
                songsAdapter.notifyDataSetChanged();

                // Get URL from fileName
                playFile(MainActivity.currentSong.getTitle());
            }
        });
    }

    public void playCurrentSong() {
        MainActivity.player.stop();
        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
            @Override
            public void onCallback(ArrayList<Song> songList) {
                songList = songsAdapter.sortByTimestamp(songList);

                MainActivity.currentSong = songList.get(0);
                songsAdapter.notifyDataSetChanged();

                playFile(MainActivity.currentSong.getTitle());
            }
        });
    }

    public void playFile(String filename) {
        new playFileAsync().execute(filename);
    }

    private class playFileAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... filename) {
            musicOnDB.getFileUrl(filename[0], new MusicOnDB.DatabaseCallback() {
                @Override
                public void onCallback(String fileURL) {
                    for (Song song : MainActivity.songList) {
                        if (song.getTitle().equals(filename[0])) {
                            MainActivity.currentSong = song;
                        }
                    }

                    if (!MainActivity.currentSong.getImageURL().equals("none")) {
                        Glide.with(context)
                                .load(MainActivity.currentSong.getImageURL())
                                .apply(new RequestOptions().placeholder(R.drawable.image))
                                .into(MainActivity.ivCover);
                    } else {
                        MainActivity.ivCover.setImageResource(R.drawable.image);
                    }

                    MainActivity.tvTitle.setText(currentSong.getTitle());
                    MainActivity.tvArtist.setText(currentSong.getArtist());

                    if (!MainActivity.isHost) {
                        updateQueue(mDatabaseRef);
                        return;
                    }

                    // Release memory from previously-playing player
                    MainActivity.player.stop();
                    MainActivity.player.release();
                    MainActivity.player = new MediaPlayer();
                    MainActivity.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            btnPlay.setBackgroundResource(R.drawable.play);

                            if (mDatabaseRef.getReference().child(MainActivity.sessionID)
                                    .child(MainActivity.currentSong.getTitle()).getKey() != null) {

                                musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
                                    @Override
                                    public void onCallback(ArrayList<Song> songNames) {
                                        if ((MainActivity.songList.size() > 1) && (MainActivity.isHost)) {
                                            mDatabaseRef.getReference().child(MainActivity.sessionID)
                                                    .child(MainActivity.currentSong.getTitle()).removeValue();
                                        }
                                    }
                                });
                            }
                            playCurrentSong();
                        }
                    });
                    try {
                        MainActivity.player.setDataSource(fileURL);
                        MainActivity.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                setupSeekbar();
                            }
                        });
                        MainActivity.player.prepare();
                        MainActivity.player.start();
                        btnPlay.setBackgroundResource(R.drawable.stop);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateQueue(mDatabaseRef);
                }
            });
            return null;
        }
    }

    //seek bar helper functions
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            int currentPosition = msg.what;
            seekBar.setProgress(currentPosition);

            String elapsed = createTimeLabel(currentPosition);
            elapsedTime.setText(elapsed);

            String remaining = createTimeLabel(totalTime - currentPosition);
            remainingTime.setText("- " + remaining);
        }
    };

    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
    //end seek bar helper functions


    public void updateQueue(FirebaseDatabase mDatabaseRef) {
        songsAdapter.clear();
        songsAdapter.notifyDataSetChanged();
        new updateQueueAsync().execute(mDatabaseRef);
    }

    private class updateQueueAsync extends AsyncTask<FirebaseDatabase, Void, Void> {

        @Override
        protected Void doInBackground(FirebaseDatabase... firebaseDatabases) {
            MusicOnDB musicOnDB = new MusicOnDB(mStorageRef, mDatabaseRef);
            final ArrayList<Song> songList = new ArrayList<>();
            musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
                @Override
                public void onCallback(ArrayList<Song> songNames) {
                    for (Song thisSong : songNames) {
                        Song tempSong = new Song(thisSong.getTitle(), thisSong.getArtist(),
                                thisSong.getImageURL(), thisSong.getTimestamp(), thisSong.getVote());

                        songList.add(tempSong);

                    }
                    populateQueue(songList);
                }
            });

            return null;
        }
    }

    private void populateQueue(ArrayList<Song> songs) {
        songsAdapter.clear();

        if(songs.size() > 1) {
            ArrayList<Song> tempSongsList = songsAdapter.sortByTimestamp(songs);
            tempSongsList.remove(0);
            songsAdapter.addSongs(tempSongsList);
        }
    }

    private void setupSeekbar() {
        new setupSeekbarAsync().execute();
    }

    private class setupSeekbarAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            totalTime = MainActivity.player.getDuration();
            seekBar.setMax(totalTime);
            seekBar.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
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
                    while ((MainActivity.player != null) && (MainActivity.player.isPlaying())) {
                        try {
                            Message msg = new Message();
                            msg.what = MainActivity.player.getCurrentPosition();
                            handler.sendMessage(msg);
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }).start();

            return null;
        }
    }
}