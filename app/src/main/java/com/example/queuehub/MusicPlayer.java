package com.example.queuehub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.queuehub.MainActivity.currentSong;
import static com.example.queuehub.MainActivity.isCreator;
import static com.example.queuehub.MainActivity.musicPlayer;
import static com.example.queuehub.MainActivity.songList;

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
    private Song nowPlaying;


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

//        btnToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (MainActivity.isTunedIn) {
//                    MainActivity.isTunedIn = false;
//                    seekBar.setVisibility(View.GONE);
//                    elapsedTime.setVisibility(View.GONE);
//                    remainingTime.setVisibility(View.GONE);
//                    btnToggle.setBackgroundResource(R.drawable.rounder_button_yahdig);
//                    btnToggle.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.colorAccent));
//                    MainActivity.player.pause();
//                } else {
//                    MainActivity.isTunedIn = true;
//                    seekBar.setVisibility(View.GONE);
//                    elapsedTime.setVisibility(View.GONE);
//                    remainingTime.setVisibility(View.GONE );
//                    btnToggle.setBackgroundResource(R.drawable.rounder2);
//                    btnToggle.setTextColor(ContextCompat.getColor(MainActivity.context, R.color.purp));
//                    // Play the first song in the queue
//                    musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
//                        @Override
//                        public void onCallback(ArrayList<Song> songNames) {
//                            songNames = songsAdapter.sortByTimestamp(songNames);
//                            if (! MainActivity.player.isPlaying()) {
//                                playFile(songNames.get(0).getTitle());
//                            }
//                        }
//                    });
//                }
//            }
//        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.isCreator) {

                    if (MainActivity.player.isPlaying()) {
                        MainActivity.player.pause();
                        btnPlay.setBackgroundResource(R.drawable.play);
                    } else {
                        MainActivity.player.start();
                        seekBar.setBackgroundColor(Color.TRANSPARENT);
                        btnPlay.setBackgroundResource(R.drawable.stop);
                    }
                }
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });
    }

    public void playNextSong() {
        MainActivity.player.pause();
        final Song thisCurrentSong = currentSong;

        // Remove first song if there are at least two songs in queue
        if (mDatabaseRef.getReference().child(MainActivity.sessionID)
                .child(MainActivity.currentSong.getTitle()).getKey() != null) {

            musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
                @Override
                public void onCallback(ArrayList<Song> songNames) {
                    if ((songNames.size() > 1) && (MainActivity.isCreator) && (currentSong == thisCurrentSong)) {
                        mDatabaseRef.getReference().child(MainActivity.sessionID)
                                .child(MainActivity.currentSong.getTitle()).removeValue();
                    }
                }
            });
        }

        // Play first song in queue
        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
            @Override
            public void onCallback(ArrayList<Song> songList) {
                nowPlaying = currentSong;
                songList = songsAdapter.sortByTimestamp(songList);
                MainActivity.currentSong = songList.get(0);
                songsAdapter.notifyDataSetChanged();

                if (!MainActivity.player.isPlaying()) {
                    playFile(MainActivity.currentSong.getTitle());
                }
                populateQueue(songList);
            }
        });
    }

    public void playCurrentSong() {
        MainActivity.player.pause();
        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
            @Override
            public void onCallback(ArrayList<Song> songList) {
                songList = songsAdapter.sortByTimestamp(songList);

                nowPlaying = currentSong;
                MainActivity.currentSong = songList.get(0);
                songsAdapter.notifyDataSetChanged();

                if (! MainActivity.player.isPlaying()) {
                    playFile(MainActivity.currentSong.getTitle());
                }
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

                    if (! isCreator) {
                        musicOnDB.getSongs(mDatabaseRef, new MusicOnDB.songNamesCallback() {
                            @Override
                            public void onCallback(ArrayList<Song> songList) {
                                nowPlaying = currentSong;
                                songList = songsAdapter.sortByTimestamp(songList);
                                MainActivity.currentSong = songList.get(0);
                                songsAdapter.notifyDataSetChanged();
                                populateQueue(songList);
                            }
                        });
                    }

                    if (! currentSong.getImageURL().equals("none")) {
                        Glide.with(context)
                                .load(currentSong.getImageURL())
                                .apply(new RequestOptions().placeholder(R.drawable.image))
                                .into(MainActivity.ivCover);
                    } else {
                        MainActivity.ivCover.setImageResource(R.drawable.image);
                    }

                    MainActivity.tvTitle.setText(currentSong.getTitle());
                    MainActivity.tvArtist.setText(currentSong.getArtist());

                    if(! isCreator) {
                        updateQueue(mDatabaseRef);
                        return;
                    }

                    // Release memory from previously-playing player
                    MainActivity.player.pause();
                    MainActivity.player.release();
                    MainActivity.player = new MediaPlayer();
                    MainActivity.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            btnPlay.setBackgroundResource(R.drawable.play);

                            playNextSong();
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
                    songList.clear();
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

    public void populateQueue(ArrayList<Song> songs) {
        songsAdapter.clear();

        if(songs.size() > 1) {
            ArrayList<Song> tempSongsList = songsAdapter.sortByTimestamp(songs);
            tempSongsList.remove(0);
            songsAdapter.addSongs(tempSongsList);
        }
        songsAdapter.notifyDataSetChanged();
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
                    while ((MainActivity.player != null) && (MainActivity.isCreator)) {
                        int currentPosition;
                        try {
                            Message msg = new Message();
                            currentPosition = MainActivity.player.getCurrentPosition();
                            msg.what = currentPosition;
                            handler.sendMessage(msg);
                            Thread.sleep(1000);
                        } catch (final Exception e) {
                            // Try to get current position 3 times before throwing exception
                            if (e instanceof IllegalStateException) {
                                boolean checkAgain = true;
                                int counter = 0;
                                for (int i = 0; i < 2; i++) {
                                    if(checkAgain) {
                                        if((MainActivity.player != null) && (MainActivity.player.isPlaying())) {
                                            currentPosition = MainActivity.player.getCurrentPosition();
                                        }else{
                                            currentPosition = 0;
                                        }
                                        if (currentPosition > 0) {
                                            checkAgain = false;
                                            counter++;
                                        }
                                    }else{
                                        if (counter == 0) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }).start();

            return null;
        }
    }
}