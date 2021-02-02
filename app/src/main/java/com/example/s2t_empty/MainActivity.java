package com.example.s2t_empty;

import android.os.Bundle;
//import android.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button ChangeText = findViewById(R.id.button_speechtotext);
        Button SaveText = findViewById(R.id.button_savetext);
        Button NamedEntity = findViewById(R.id.button_namedentity);

        ChangeText.setOnClickListener(v -> {
            TextView Text = findViewById(R.id.textView5);
            Text.setText("Sprachnachricht umwandeln.");
        });
        SaveText.setOnClickListener(v -> {
            TextView Text = findViewById(R.id.textView5);
            Text.setText("Text speichern.");
        });
        NamedEntity.setOnClickListener(v -> {
            TextView Text = findViewById(R.id.textView5);
            Text.setText("Named Entity anzeigen.");
        });
    }
}