package com.example.s2t_empty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.services.WitAPI;

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

public class StartScreen extends Fragment {

    ImageView play_pause_icon;
    ImageView stop_icon;
    TextView file_info;
    TextView myText;

    int duration;
    ProgressBar progress;
    TextView progressState;

    Button speechtotext;
    Button namedentity;
    Button savetext;

    String witText = "";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_start_screen, container, false);

        // Get Intent ( ueberprueft die durch "Share" uebergebene Datei )
        //TODO: make parts with getActivity() null safe!
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri myUri = null;

        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        file_info = root.findViewById(R.id.file_info);
        myText = root.findViewById(R.id.textView5);

        progress = root.findViewById(R.id.progressBar);
        progressState = root.findViewById(R.id.progressState);

        //MediaPlayer
        play_pause_icon = root.findViewById(R.id.play_pause);
        stop_icon = root.findViewById(R.id.stop_play);

        //Buttons
        speechtotext = root.findViewById(R.id.button_speechtotext);
        namedentity = root.findViewById(R.id.button_namedentity);
        savetext = root.findViewById(R.id.button_savetext);

        speechtotext.setOnClickListener(this::changeTextWithWit);
        //disable buttons that need text for now
        namedentity.setEnabled(false);
        savetext.setEnabled(false);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            // verarbeite den Intent
            myUri = handleSendVoice(intent);
            try {
                //prepare MediaPlayer
                mp.setDataSource(getActivity().getApplicationContext(), myUri);
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            duration = mp.getDuration();
            //enable speech to text button as file is shared
            speechtotext.setEnabled(true);

            //display info about the current audio file
            String currentFilename = getFileInfo(myUri);
            file_info.setText(currentFilename);

            //Prepare Audio for wit.ai (convert from opus to mp3)
            String FileIn = getInternalDirectory() + "/original.opus";
            File CopyFile = new File(FileIn);
            //Copy content from Uri to File "original.opus"
            try {
                copyInputStreamToFile(myUri, CopyFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

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
        return root;
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
        Cursor returnCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
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

    private String getInternalDirectory(){
        return getActivity().getApplicationContext().getFilesDir().getAbsolutePath();
    }

    private void copyInputStreamToFile(Uri uri, File file) throws FileNotFoundException {
        InputStream ins = getActivity().getContentResolver().openInputStream(uri);
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

    public void changeTextWithWit(View myView) {
        //check for internet connection before starting workflow
        ConnectivityManager cm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null || !ni.isConnectedOrConnecting()){
            Toast.makeText(getActivity().getApplicationContext(), "No internet connection!", Toast.LENGTH_LONG).show();//TODO maybe customize, show as warning
        } else {
            speechtotext.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            progressState.setVisibility(View.VISIBLE);
            progressState.setText(R.string.progressState_before);
            //Convert ".opus" to ".mp3" with ffmpeg
            String FileIn = getInternalDirectory() + "/original.opus";
            String FileOut = getInternalDirectory() + "/converted.mp3";
            File convertedFile = new File(FileOut);
            if(convertedFile.exists()){
                convertedFile.delete();
            }
            ConvertFromOpusToMp3(FileIn, FileOut);
            //further functionality happens when converting is finished
        }
    }

    public void ConvertFromOpusToMp3(String In, String Out){
        //show progress
        progressState.setText(R.string.progressState_mp3);
        FFmpeg ffmpeg = FFmpeg.getInstance(getActivity().getApplicationContext());
        if (FFmpeg.getInstance(getActivity()).isSupported()) {
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
                String[] splitStart = message.split("time=");
                if(splitStart.length > 1){
                    String[] splitEnd = splitStart[1].split(" bitrate");
                    int ms = timeStringToMilliSecs(splitEnd[0]);
                    int prog = ms < duration ? Math.round(((float)ms/duration)*60) : 60;
                    progress.setProgress(prog);
                }
                Log.w(null, message);
            }

            public void onFailure(String message) {
                Log.w(null, message);
                progress.setVisibility(View.INVISIBLE);
            }

            public void onFinish() {
                Log.w(null, "finished");
                //delete opus file after conversion
                new File(In).delete();
                progress.setProgress(60);
                SplitAudioFile(Out);
                //further functionality happens when splitting is finished
            }
        });
    }

    //TODO: maybe move to utils?
    private int timeStringToMilliSecs(String timeString){
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
        } else {
            //TODO: add error handling
        }
        return ms;
    }

    public void SplitAudioFile(String In){
        progressState.setText(R.string.progressState_split);
        String outDirectory = getInternalDirectory() + "/out%03d.mp3";
        //ffmpeg -i somefile.mp3 -f segment -segment_time 3 -c copy out%03d.mp3
        String[] cmd = new String[]{"-i", In, "-f", "segment", "-segment_time", "19", "-c", "copy", outDirectory};
        FFmpeg ffmpeg = FFmpeg.getInstance(getActivity().getApplicationContext());
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
                progress.setProgress(61);
                progressState.setText(R.string.progressState_wit);
                callWit(witApi, mp3Files.get(0), mp3Files);
            }
        });
    }

    private WitAPI prepareRetrofit(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.wit.ai/").build();
        return retrofit.create(WitAPI.class);
    }

    private void callWit(WitAPI api, File file, List<File> mp3Files){
        Call<ResponseBody> call = api.getMessageFromAudio("audio/mpeg3", RequestBody.create(MediaType.parse("audio/mpeg3"), file));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsn = new JSONObject(response.body().string());
                    if(jsn.has("text")) {
                        witText = witText.concat(jsn.getString("text") + " ");
                        //TODO: maybe find smoother way to show progress here
                        progress.setProgress(progress.getProgress() + Math.round(((float)20000/duration) * 40));
                    }
                    //after last file: show result & clean up
                    if(file == mp3Files.get(mp3Files.size() - 1)){
                        progress.setProgress(100);
                        myText.setVisibility(View.VISIBLE);
                        myText.setText(witText);

                        namedentity.setEnabled(true);
                        savetext.setEnabled(true);

                        progress.setVisibility(View.INVISIBLE);
                        progressState.setVisibility(View.INVISIBLE);
                        for (File f: mp3Files){
                            f.delete();
                        }
                    } else{
                        //call method recursively as long as there are files to transcribe
                        callWit(api,  mp3Files.get(mp3Files.indexOf(file) + 1), mp3Files);
                    }


                } catch (JSONException |  IOException | NullPointerException e){ //TODO: improve error handling further, e.g. show message if there is no internet connection
                    e.printStackTrace();
                    progress.setVisibility(View.INVISIBLE);
                    progressState.setText(e.getMessage()); //TODO: show warning as toast?
                    speechtotext.setEnabled(true);
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myText.setText("Call failed: "+ t.getMessage());
                t.printStackTrace();
                progress.setVisibility(View.INVISIBLE);

                for(File f: mp3Files){
                    f.delete();
                }
                call.cancel();
            }
        });
    }

}