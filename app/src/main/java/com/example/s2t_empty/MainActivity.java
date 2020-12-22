package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.content.ClipData;
import android.net.Uri;

public class MainActivity extends AppCompatActivity {

    private boolean State = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Intent ( ueberprueft die durch "Share" uebergebene Datei )
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null){
            // verarbeite den Intent
            TextView myText = (TextView) findViewById(R.id.textView5);
            myText.setText(type);
            handleSendVoice(intent);
        }
        else{
            // Do something else
        }
    }

    void handleSendVoice(Intent intent){
        Uri voiceUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (voiceUri != null){
            System.out.println("Yay! Sound da, alles gut!");
        }
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