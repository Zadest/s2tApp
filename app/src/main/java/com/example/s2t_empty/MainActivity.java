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
    public class MainActivity2 extends FragmentActivity {

        private boolean State = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            AppBarConfiguration appBarConfiguration =
                    new AppBarConfiguration.Builder(navController.getGraph()).build();
            Toolbar toolbar = findViewById(R.id.toolbar);
            NavigationUI.setupWithNavController(
                    toolbar, navController, appBarConfiguration);
        }

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
}

