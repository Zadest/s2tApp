package com.example.services;

import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.Arrays;

public class OurUtils {

    public OurUtils(){
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getSplittedDate(String fileName){
        String[] parts = fileName.split("-");
        String dateOfFile = parts[1];
        String yearOfFile = dateOfFile.substring(0, 4);
        String monthOfFile = dateOfFile.substring(4, 6);
        String dayOfFile = dateOfFile.substring(6, 8);
        return String.join(".", dayOfFile, monthOfFile, yearOfFile);
    }

    public int timeStringToMilliSecs(String timeString){
        int ms = 0;
        if(timeString.matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{2}")){
            String[] parts = timeString.split(":");
            String hours = parts[0];
            String minutes = parts[1];
            String[] secondsAndMilli = parts[2].split("\\.");
            String seconds = secondsAndMilli[0];
            String milli = secondsAndMilli[1];
            ms = ms + (Integer.parseInt(hours) * 3600000) + (Integer.parseInt(minutes) * 60000) +
                    (Integer.parseInt(seconds) * 1000) + Integer.parseInt(milli);
        }
        return ms;
    }


    public static void enableScroll(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setMovementMethod(new ScrollingMovementMethod());
        }

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
    }
}


