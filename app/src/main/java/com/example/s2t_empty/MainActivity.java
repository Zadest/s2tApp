package com.example.s2t_empty;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                new StartScreen(), "startScreen").commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                String selectedFragmentTag = "";

                switch (item.getItemId()) {
                    case R.id.start_screen:
                        selectedFragmentTag = "startScreen";
                        selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);
                        if(selectedFragment == null) {
                            selectedFragment = new StartScreen();
                        }
                        break;
                    case R.id.saved_text:
                        selectedFragmentTag = "saveText";
                        selectedFragment = getSupportFragmentManager().findFragmentByTag(selectedFragmentTag);
                        if(selectedFragment == null) {
                            selectedFragment = new SavedText();
                        }
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                        selectedFragment, selectedFragmentTag).addToBackStack(null).commit();
                return true;
            };

}