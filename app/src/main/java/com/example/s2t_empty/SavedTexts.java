package com.example.s2t_empty;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
//import androidx.navigation.NavController;
//import androidx.navigation.fragment.NavHostFragment;
//import androidx.navigation.ui.AppBarConfiguration;
//import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.CollapsingToolbarLayout;


public class SavedTexts extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //giving toolbar navi-powers//TODO: fix NullPointer at navHostFragment.getNavController()
        //CollapsingToolbarLayout layout = findViewById(R.id.collapsing_toolbar_layout);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        //assert navHostFragment != null;
        //NavController navController = navHostFragment.getNavController();
        //AppBarConfiguration appBarConfiguration =
        //      new AppBarConfiguration.Builder(navController.getGraph()).build();
        //NavigationUI.setupWithNavController(layout, toolbar, navController, appBarConfiguration);
    }

    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_texts, container, false);
    }
}