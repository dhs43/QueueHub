package com.example.queuehub;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//okay

public class MainActivity extends AppCompatActivity {
    MediaPlayer player;
    Button playBtn;
    ImageView ivCover;
    SeekBar seekBar;
    TextView elapsedTime;
    TextView remainingTime;
    int totalTime;
    //Uri song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playBtn = findViewById(R.id.btnPlay);
        elapsedTime = findViewById(R.id.elapsedTime);
        remainingTime = findViewById(R.id.remainingTime);
        seekBar = findViewById(R.id.seekBar);
        //also need to set cover art
        ivCover = findViewById(R.id.ivCover);


        //song = newUriFromSearch;
        player = MediaPlayer.create(this, R.raw.song);
        //replace R.raw.song with the above song variable
        //prob set cover art here
        //ie: ivCover.setImageblahblahblah
        totalTime = player.getDuration();
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
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();

    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            int currentPosition = msg.what;
            seekBar.setProgress(currentPosition);

            String elapsed = createTimeLabel(currentPosition);
            elapsedTime.setText(elapsed);

            String remaining = createTimeLabel(totalTime - currentPosition);
            remainingTime.setText("- " + remaining);
        }
    };

    public String createTimeLabel(int time){
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if(sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    public void play(View view) {

        if(!player.isPlaying()){
            //stopping
            player.start();
            playBtn.setBackgroundResource(R.drawable.stop);
        } else {
            //playing
            player.pause();
            playBtn.setBackgroundResource(R.drawable.play);
        }
    }



}
