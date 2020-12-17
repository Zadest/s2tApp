package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //TODO: read in Whatsapp-Filenames (with special characters)
    //TODO: foward/rewind?
    //TODO: possible to read in shared file?
    ImageView play_pause_icon;
    ImageView stop_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play_pause_icon = findViewById(R.id.play_pause);
        stop_icon = findViewById(R.id.stop_play);

        //'create': no need to prepare MediaPlayer
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.test);

        //Click Listener for Playbutton
        play_pause_icon.setOnClickListener(v -> {
            if(!mp.isPlaying()){
                mp.start();
                play_pause_icon.setImageResource(R.drawable.ic_baseline_pause_24);
            }else{
                mp.pause();
                play_pause_icon.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }
        });

        //Clicklistener for Stopbutton
        stop_icon.setOnClickListener(v -> {
                mp.pause();
                mp.seekTo(0);
                play_pause_icon.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        });
    }


}