package com.example.s2t_empty;

import android.os.Bundle;
//import android.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity extends AppCompatActivity {

    private boolean State = false;

        public void changeText(View myView) {
            System.out.println("!Test");
            TextView myText = (TextView) findViewById(R.id.textView5);
            System.out.println(myText.getText());
            // TODO : Request
            if (State) {
                myText.setText("Neuer Text");
                State = !(State);
            } else {
                myText.setText("Anderer Text");
                State = !(State);
            }
        }
}