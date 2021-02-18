package com.example.services;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.util.Arrays;

public class OurUtils {

    public OurUtils(){
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String makeListEntryTitle(String fileName){
        String infoString = "Unbekannte Herkunft";
        //file from whatsApp?
        if (fileName.startsWith("PTT-")) {
            String splittedDate = getSplittedDate(fileName);
            String nameOfSpeaker ="";
            String[] parts = fileName.split("_");
            if (parts.length > 1){
                nameOfSpeaker = parts[parts.length-1].concat(" ");
            }
            infoString = "".concat(nameOfSpeaker).concat("am ").concat(splittedDate);
        }
        return  infoString;
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

    //TODO: maybe use to sort entries by date
    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDate getLocalDate(String ourDate){
        String[] parts = ourDate.split(".");
        if(parts.length == 3){
            Integer[] intParts = Arrays.stream(parts).map(Integer::parseInt).toArray(Integer[]::new) ;
            return LocalDate.of(intParts[2], intParts[1], intParts[0]);
        }
        return null;
    }
}
