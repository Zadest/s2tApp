package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.widget.ImageView;
import android.content.Intent;
import android.net.Uri;

import java.io.IOException;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.example.services.WitAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    private boolean state = true;

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

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // verarbeite den Intent
            myUri = handleSendVoice(intent);
            try {
                mp.setDataSource(getApplicationContext(), myUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Do something else
        }

        //MediaPlayer
        play_pause_icon = findViewById(R.id.play_pause);
        stop_icon = findViewById(R.id.stop_play);

        //'create': no need to prepare MediaPlayer --> possible when using uri?
        //final MediaPlayer mp = MediaPlayer.create(this, R.raw.test);

        // play from local URI

        try {
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();

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

        }
    }

    //TODO: maybe fix warning  Accessing hidden method Ldalvik/system/CloseGuard --> Android Version 27?
    public void changeTextWithWit(View myView) {
        TextView myText = findViewById(R.id.textView5);
        if(state){
            WitAPI witApi = prepareRetrofit();
            try{
                Call<ResponseBody> call = witApi.getMessageFromAudio(prepareAudio());
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            JSONObject jsn = new JSONObject(response.body().string());
                            myText.setText(jsn.getString("text"));
                            state = !(state);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e){
                            e.printStackTrace();
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
            }catch (IOException e){
                e.printStackTrace();
            }
        } else {
            myText.setText("Anderer Text");
            state = !(state);
        }
    }

    private RequestBody prepareAudio() throws IOException {
        System.out.println("preparing audio");
        InputStream is = this.getResources().openRawResource(R.raw.testo3);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //TODO maybe find better way, try to improve performance
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        byte[] byteArray = buffer.toByteArray();
        return RequestBody.create(MediaType.parse("audio/ogg"), byteArray);//TODO find way to work with opus!!!
    }

    private WitAPI prepareRetrofit(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.wit.ai/").build();
        return retrofit.create(WitAPI.class);
    }

    Uri handleSendVoice(Intent intent){
        Uri voiceUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (voiceUri != null){
            System.out.println("Yay! Sound da, alles gut!");
        }
        return voiceUri;
    }

}