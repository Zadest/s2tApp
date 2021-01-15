package com.example.s2t_empty;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.view.View;
import android.widget.TextView;

import com.example.services.WitAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {
    //TODO: read in Whatsapp-Filenames (with special characters)
    //TODO: foward/rewind?
    //TODO: possible to read in shared file?
    ImageView play_pause_icon;
    ImageView stop_icon;
    TextView file_info;

    private boolean state = true;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Intent ( ueberprueft die durch "Share" uebergebene Datei )
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri myUri = null;

        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //MediaPlayer
        play_pause_icon = findViewById(R.id.play_pause);
        stop_icon = findViewById(R.id.stop_play);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // verarbeite den Intent
            myUri = handleSendVoice(intent);
            try {
                mp.setDataSource(getApplicationContext(), myUri);

                //'create': no need to prepare MediaPlayer --> possible when using uri?
                // play from local URI
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Do something else
        }

            //Click Listener for Playbutton
        play_pause_icon.setOnClickListener(v -> {
            if (!mp.isPlaying()) {
                mp.start();
                play_pause_icon.setImageResource(R.drawable.ic_baseline_pause_24);
            } else {
                mp.pause();
                play_pause_icon.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }
        });
        //Clicklistener for Stopbutton
        stop_icon.setOnClickListener(v -> {
            mp.pause();
            mp.seekTo(0);
            play_pause_icon.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        });

        //display info about the current audio file
        String currentFilename = getFileInfo(myUri);
        file_info.setText(currentFilename);
    }

    public void changeTextWithWit(View myView) {
        TextView myText = findViewById(R.id.textView5);
        if(state){
            WitAPI witApi = prepareRetrofit();
            Call<ResponseBody> call = witApi.getMessageFromTestText();
            try{
                String audioType = getIntent().getType();
                //in order to remove additional info, like codecs
                if(audioType.length() > 9){
                    audioType = audioType.substring(0, 10); //TODO if possible, resolve/remove codecs
                }
                call = witApi.getMessageFromAudio(audioType, prepareAudio(audioType));
            }catch (IOException e){
                e.printStackTrace();
            }
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        JSONObject jsn = new JSONObject(response.body().string());
                        myText.setText(jsn.getString("text"));
                        state = !(state);
                    } catch (JSONException |  IOException | NullPointerException e){ //TODO: improve error handling
                        e.printStackTrace();
                        myText.setText(e.getMessage());
                    }
                    call.cancel();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    System.out.println("fail!");
                    t.printStackTrace();
                    myText.setText("Wit does not want to play with you");
                    call.cancel();
                }
            });
        } else {
            myText.setText("Anderer Text");
            state = !(state);
        }
    }

    private RequestBody prepareAudio(String audioType) throws IOException{
        Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        InputStream is = getApplicationContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();
        return RequestBody.create(MediaType.parse(audioType), byteArray);
    }

    private WitAPI prepareRetrofit(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.wit.ai/").build();
        return retrofit.create(WitAPI.class);
    }

    Uri handleSendVoice(Intent intent){
        Uri voiceUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (voiceUri != null){
            System.out.println("Yay! Sound da, alles gut!");
        }
        return voiceUri;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFileInfo(Uri uri){
        //Info about current audio file
        file_info = findViewById(R.id.file_info);
        Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        //file from whatsApp?
        if (name.startsWith("PTT-")) {
            String[] parts = name.split("-");
            String dateOfFile = parts[1];
            String yearOfFile = dateOfFile.substring(0, 4);
            String monthOfFile = dateOfFile.substring(4, 6);
            String dayOfFile = dateOfFile.substring(6, 8);
            //String.join requires API level 26 (current min is 16)-- > better solution ?
            String splittedDate = String.join(".", dayOfFile, monthOfFile, yearOfFile);
            String infoString = "Sprachnachricht vom ".concat(splittedDate);
            return infoString;
            //audio file from other source
        }else{
            String infoString = "Aktuelle Datei: ".concat(name);
            return infoString;
        }
    }


}