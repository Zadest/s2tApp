package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean State = false;
    Button pause, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pause =(Button)findViewById(R.id.button2);
        stop =(Button)findViewById(R.id.button3);
        //'create': no need to prepare MediaPlayer
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.test);

        //Clicklistener Play/Pause
        pause.setOnClickListener(v -> {
            if (mp.isPlaying()) {
                mp.pause();
            }else{
                mp.start();
            }
        });

        //Clicklistener Stop
        stop.setOnClickListener(v -> {
            mp.pause();
            mp.seekTo(0);
        });
    }

    public void changeText(View myView) {
        System.out.println("!Test");
        TextView myText = (TextView) findViewById(R.id.textView5);
        System.out.println(myText.getText());
        // TODO : Request
        if (State) {
            myText.setText("Neuer Text");
            State = !(State);
        }
        else{
            myText.setText("Anderer Text");
            State = !(State);
        }
    }

}