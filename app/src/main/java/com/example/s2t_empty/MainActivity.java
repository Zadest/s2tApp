package com.example.s2t_empty;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
//import androidx.navigation.NavController;
//import androidx.navigation.fragment.NavHostFragment;
//import androidx.navigation.ui.AppBarConfiguration;
//import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.IOException;

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
    ImageView play_pause_icon;
    ImageView stop_icon;
    TextView file_info;
    TextView myText;

    Button speechtotext;
    Button namedentity;
    Button savetext;

    String fileName;

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

        file_info = findViewById(R.id.file_info);

        //MediaPlayer
        play_pause_icon = findViewById(R.id.play_pause);
        stop_icon = findViewById(R.id.stop_play);

        //Text
        myText = findViewById(R.id.textView5);

        //Buttons
        speechtotext = findViewById(R.id.button_speechtotext);
        //TODO: add functionality to these buttons
        namedentity = findViewById(R.id.button_namedentity);
        savetext = findViewById(R.id.button_savetext);

        //disable buttons that need text for now
        namedentity.setEnabled(false);
        savetext.setEnabled(false);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // verarbeite den Intent
            myUri = handleSendVoice(intent);
            try {
                //prepare MediaPlayer
                mp.setDataSource(getApplicationContext(), myUri);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //display info about the current audio file
            String currentFilename = getFileInfo(myUri);
            file_info.setText(currentFilename);

            //enable speechtotext button
            speechtotext.setEnabled(true);
        } else {
            file_info.setText(R.string.no_file);
            //disable speechtotext button
            speechtotext.setEnabled(false);
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

        //change play-button when audiofile ended
        mp.setOnCompletionListener(mediaPlayer -> {
            // Do something when media player end playing
            play_pause_icon.setImageResource(R.drawable.ic_baseline_play_arrow_24);
        });


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

    public void changeTextWithWit(View myView) {
        //show that text is loading
        ProgressBar progress = findViewById(R.id.progressBar);
        progress.setVisibility(View.VISIBLE);
        //prepare wit call
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
        //handle call
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsn = new JSONObject(response.body().string());
                    progress.setVisibility(View.INVISIBLE);
                    myText.setText(jsn.getString("text")); //TODO: show text completely, layout cuts parts
                    namedentity.setEnabled(true);
                    savetext.setEnabled(true);
                    state = !(state);
                } catch (JSONException |  IOException | NullPointerException e){ //TODO: improve error handling
                    progress.setVisibility(View.INVISIBLE);
                    e.printStackTrace();
                    myText.setText(e.getMessage());
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("fail!");
                progress.setVisibility(View.INVISIBLE);
                t.printStackTrace();
                myText.setText(R.string.wit_error);
                call.cancel();
            }
        });
    }

    //TODO: use Alertdialog instead:
    //https://developer.android.com/guide/topics/ui/dialogs
    public void openSavingPopup(View myView){
        Intent i = new Intent(getApplicationContext(), SavingPopup.class);
        i.putExtra("text", myText.getText().toString());
        i.putExtra("fileName", fileName);
        startActivityForResult(i, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1 && data != null){
            Toast.makeText(getApplicationContext(), "Nachricht von " + data.getStringExtra("name") + "gespeichert", Toast.LENGTH_SHORT).show();
        }
    }

    //just for testing
    public void showSavedText(View myView){
        SharedPreferences sp = getApplicationContext().getSharedPreferences("MySavedTexts", Context.MODE_PRIVATE);
        String text = sp.getString("test123", "");
        this.myText.setText(text);
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
        fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        //file from whatsApp?
        if (fileName.startsWith("PTT-")) {
            String[] parts = fileName.split("-");
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
            String infoString = "Aktuelle Datei: ".concat(fileName);
            return infoString;
        }
    }


}