package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean State = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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