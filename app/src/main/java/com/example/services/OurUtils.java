package com.example.services;

import android.os.Build;

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
        //String.join requires API level 26 (current min is 16)-- > better solution ?
        return String.join(".", dayOfFile, monthOfFile, yearOfFile);
    }

}
