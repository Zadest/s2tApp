package com.example.s2t_empty;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.services.WitAPI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;
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

    ProgressBar progress;

    Button speechtotext;
    Button namedentity;
    Button savetext;

    String witText = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        StartScreen startScreen = new StartScreen();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
               startScreen, "startScreen").commit();

        // Get Intent ( ueberprueft die durch "Share" uebergebene Datei )
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri myUri = null;

        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Fragment stSc = getSupportFragmentManager().findFragmentByTag("startScreen");
        file_info = stSc.getView().findViewById(R.id.file_info);
        myText = stSc.getView().findViewById(R.id.textView5);

        progress = stSc.getView().findViewById(R.id.progressBar);

        //MediaPlayer
        play_pause_icon = stSc.getView().findViewById(R.id.play_pause);
        stop_icon = stSc.getView().findViewById(R.id.stop_play);

        //Buttons
        speechtotext = stSc.getView().findViewById(R.id.button_speechtotext);
        //TODO: add functionality to these buttons
        namedentity = stSc.getView().findViewById(R.id.button_namedentity);
        savetext = stSc.getView().findViewById(R.id.button_savetext);

        //disable speechtotext button until converting is done
        speechtotext.setEnabled(false);

        speechtotext.setOnClickListener(this::changeTextWithWit);
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

            //Prepare Audio for wit.ai (convert from opus to mp3)
            File CopyFile = new File(getInternalDirectory() + "/original.opus");
            String FileIn = CopyFile.getPath();
            String FileOut = getInternalDirectory() + "/converted.mp3";
            //Copy content from Uri to File "original.opus"
            try {
                copyInputStreamToFile(myUri, CopyFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Convert ".opus" to ".mp3" with ffmpeg
            ConvertFromOpusToMp3(FileIn, FileOut);

            //TODO: delete original mp3 after splitting if >1 mp3 files after splitting

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
    }


    BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.start_screen:
                        selectedFragment = new StartScreen();
                        break;
                    case R.id.saved_text:
                        selectedFragment = new SavedText();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                        selectedFragment).commit();
                return true;
            };

    private String getInternalDirectory(){
        return getApplicationContext().getFilesDir().getAbsolutePath();
    }

    public void changeTextWithWit(View myView) {
        progress.setVisibility(View.VISIBLE);
        String FileOut = getInternalDirectory() + "/converted.mp3";
        SplitAudioFile(FileOut);
        //further functionality happens when splitting is finished
    }


    private void callWit(WitAPI api, File file, List<File> mp3Files){
        Call<ResponseBody> call = api.getMessageFromAudio("audio/mpeg3", RequestBody.create(MediaType.parse("audio/mpeg3"), file));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsn = new JSONObject(response.body().string());
                    if(jsn.has("text")) {
                        witText = witText.concat(" " + jsn.getString("text"));
                    }
                    //after last file: show result & clean up
                    if(file == mp3Files.get(mp3Files.size() - 1)){
                        myText.setText(witText); //TODO: show text completely, layout cuts parts

                        namedentity.setEnabled(true);
                        savetext.setEnabled(true);

                        progress.setVisibility(View.INVISIBLE);
                        for (File f: mp3Files){
                            f.delete();
                        }
                    } else{
                        //call method recursively as long as there are files to transcribe
                        callWit(api,  mp3Files.get(mp3Files.indexOf(file) + 1), mp3Files);
                    }


                } catch (JSONException |  IOException | NullPointerException e){ //TODO: improve error handling further
                    e.printStackTrace();
                    progress.setVisibility(View.INVISIBLE);
                    myText.setText(response.message());
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("fail!");
                t.printStackTrace();
                progress.setVisibility(View.INVISIBLE);

                for(File f: mp3Files){
                    f.delete();
                }
                call.cancel();
            }
        });

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
    private void copyInputStreamToFile(Uri uri, File file) throws FileNotFoundException {
        InputStream ins = getContentResolver().openInputStream(uri);
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=ins.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                ins.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    public void ConvertFromOpusToMp3(String In, String Out){
        //show progress
        progress.setVisibility(View.VISIBLE);
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        if (FFmpeg.getInstance(this).isSupported()) {
            // ffmpeg is supported (binary ffmpeg is automatically loaded)
        } else {
            // ffmpeg is not supported
        }
        //convert sent file from ogg to mp3
        //ffmpeg -i audio.ogg -acodec libmp3lame audio.mp3
        String[] cmd = new String[]{"-i", In, "-acodec", "libmp3lame", Out};
        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
            public void onStart() {
                Log.w(null, "started");
            }

            public void onProgress(String message) {
                Log.w(null, message);
            }

            public void onFailure(String message) {
                Log.w(null, message);
                progress.setVisibility(View.INVISIBLE);
            }

            public void onFinish() {
                Log.w(null, "finished");
                //show that progress is finished, enable speechtotext button
                progress.setVisibility(View.INVISIBLE);
                speechtotext.setEnabled(true);
                //delete opus file after conversion
                new File(In).delete();
            }
        });
    }

    public void SplitAudioFile(String In){
        String outDirectory = getInternalDirectory() + "/out%03d.mp3";
        //ffmpeg -i somefile.mp3 -f segment -segment_time 3 -c copy out%03d.mp3
        String[] cmd = new String[]{"-i", In, "-f", "segment", "-segment_time", "19", "-c", "copy", outDirectory};
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
            public void onStart() {
                Log.w(null, "started");
            }

            public void onProgress(String message) {
                Log.w(null, message);

            }

            public void onFailure(String message) {
                Log.w(null, message);
                progress.setVisibility(View.INVISIBLE);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onFinish() {
                Log.w(null, "finished");

                //filter files in audiofolder for only mp3
                File audioFolder = new File(getInternalDirectory());
                List<File> mp3Files = Arrays.stream(audioFolder.listFiles()).filter(f -> f.getName().endsWith(".mp3")).collect(Collectors.toList());
                if(mp3Files.size() > 1) {
                    File converted = new File(getInternalDirectory() + "/converted.mp3");
                    mp3Files.remove(converted);
                    converted.delete();
                }

                //call wit for first mp3 file, others are called recursively if necessary
                WitAPI witApi = prepareRetrofit();
                callWit(witApi, mp3Files.get(0), mp3Files);
            }
        });
    }
}