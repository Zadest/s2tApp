package com.example.s2t_empty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.OpenableColumns;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.services.OurUtils;
import com.example.services.WitAPI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

public class StartScreen extends Fragment implements SavingPopup.SavingPopupListener {
    private static final String WIT_TEXT_VARIABLE_KEY = "witTextKey";

    ImageView play_pause_icon;
    ImageView stop_icon;
    TextView file_info;
    TextView myText;
    TextView myTextViewNotEditable;
    MenuItem menuSavedText;

    int duration;
    ProgressBar progress;
    TextView progressState;

    Button speechtotext;
    Button savetext;

    SpannableString witText = new SpannableString("");
    List<Integer> startHighlight = new ArrayList<>();
    List<Integer> endHighlight = new ArrayList<>();
    String fileName;

    public StartScreen(){
        setArguments(new Bundle());
    }

    @RequiresApi(api = Build.VERSION_CODES.O) //TODO: remove requiresAPI annotations by setting API in general to max necessary for our code
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_start_screen, container, false);

        //actionbar
        setHasOptionsMenu(true);

        // Get Intent ( ueberprueft die durch "Share" uebergebene Datei )
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Uri myUri = null;

        MediaPlayer mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        file_info = root.findViewById(R.id.file_info);

        myText = (EditText) root.findViewById(R.id.textView5);
        OurUtils.enableScroll(myText);
        myTextViewNotEditable = (TextView) root.findViewById(R.id.textView2);
        OurUtils.enableScroll(myTextViewNotEditable);
        myText.setVisibility(View.INVISIBLE);
        myText.setEnabled(false);

        myText.setSingleLine(false);
        myText.setImeOptions(6);
        myText.setOnEditorActionListener((v, actionId, event) -> {
            System.out.println("Called by"+ actionId);
            if(actionId == EditorInfo.IME_ACTION_DONE){
                System.out.println("Fertig mit bearbeiten");
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(myText.getWindowToken(), 0);
                myTextViewNotEditable.setText(myText.getText());
                myTextViewNotEditable.setEnabled(true);
                myTextViewNotEditable.setVisibility(View.VISIBLE);
                myText.setEnabled(false);
                myText.setVisibility(View.INVISIBLE);
                return true;
            }
            return false;
        });

        myTextViewNotEditable.setOnLongClickListener(v -> {
            myText.setVisibility(View.VISIBLE);
            myTextViewNotEditable.setVisibility(View.INVISIBLE);
            myText.setEnabled(true);
            myTextViewNotEditable.setEnabled(false);
            return false;
        });

        progress = root.findViewById(R.id.progressBar);
        progressState = root.findViewById(R.id.progressState);

        //MediaPlayer
        play_pause_icon = root.findViewById(R.id.button_play_pause);
        stop_icon = root.findViewById(R.id.button_stop_play);

        //Buttons
        speechtotext = root.findViewById(R.id.button_speechtotext);
        savetext = root.findViewById(R.id.button_savetext);

        // Navigation zum Archiv
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
        menuSavedText = bottomNav.getMenu().getItem(1);

        speechtotext.setOnClickListener(this::changeTextWithWit);
        //enable button only when text exists
        savetext.setEnabled(witText.length() > 0 && !witText.toString().equals(getResources().getString(R.string.textdisplay)) && !witText.toString().equals(getResources().getString(R.string.tipp_for_file)));
        savetext.setOnClickListener(this::openSavingPopup);

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

            //display info about the current audio file
            String currentFilename = getFileInfo(myUri);
            file_info.setText(currentFilename);
            //only when there is no text so far
            if(witText.length() == 0) {
                myTextViewNotEditable.setText(R.string.tipp_for_file);
            } else {
                myTextViewNotEditable.setText(witText);
                myText.setText(witText);
            }
            duration = mp.getDuration();
            //enable speech to text button as file is shared
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

        return root;
    }

    // action-button in action bar (help)
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.button_hilfe:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Die Speech2Text App");
                builder.setMessage("Hilfe");
                //builder.setIcon(R.drawable.testpic); TODO: eventuell app-icon einfuegen?
                builder.setView(R.layout.help_start_screen);
                builder.setPositiveButton("ok", null);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            case R.id.button_about:
                AlertDialog.Builder builder_about = new AlertDialog.Builder(requireContext());
                builder_about.setTitle("Die Speech2Text App");
                builder_about.setMessage("About");
                builder_about.setView(R.layout.about);
                //builder.setIcon(R.drawable.testpic); TODO: eventuell app-icon einfuegen?
                builder_about.setPositiveButton("ok", null);
                AlertDialog dialog_about = builder_about.create();
                dialog_about.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // inflate actionbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_actionbar, menu);
    }

    Uri handleSendVoice(Intent intent){
        Uri voiceUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (voiceUri != null) {
            System.out.println("Yay! Sound da, alles gut!");
        }
        return voiceUri;
    }

    //persists witText when navigating
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onPause() {
        super.onPause();
        witText = new SpannableString(myText.getText());
        getArguments().putString(WIT_TEXT_VARIABLE_KEY, Html.toHtml(witText, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE) );
        //TODO: maybe also save open/close state of edit
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFileInfo(Uri uri) {
        //Info about current audio file
        Cursor returnCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        String infoString;
        //file from whatsApp?
        if (fileName.startsWith("PTT-")) {
            OurUtils utils = new OurUtils();
            String splittedDate = utils.getSplittedDate(fileName);
            infoString = "Sprachnachricht vom ".concat(splittedDate);
            return infoString;
            //audio file from other source
        }else{
            infoString = "Aktuelle Datei: ".concat(fileName);
            return infoString;
        }
    }

    private String getInternalDirectory(){
        return getActivity().getApplicationContext().getFilesDir().getAbsolutePath();
    }

    public void changeTextWithWit(View myView) {
        //check for internet connection before starting workflow
        ConnectivityManager cm = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null || !ni.isConnectedOrConnecting()){
            Toast.makeText(getActivity().getApplicationContext(), "No internet connection!", Toast.LENGTH_LONG).show();
        } else {
            //disable navigation to prevent bug
            menuSavedText.setEnabled(false);
            //Prepare Audio for wit.ai
            String FileIn = getInternalDirectory() + "/original.opus";
            File CopyFile = new File(FileIn);
            //Copy content from Uri to File "original.opus"
            try {
                copyInputStreamToFile(handleSendVoice(getActivity().getIntent()), CopyFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            speechtotext.setEnabled(false);
            savetext.setEnabled(false);
            witText = new SpannableString("");
            myTextViewNotEditable.setText(witText);
            myText.setText(witText);
            progress.setVisibility(View.VISIBLE);
            progressState.setVisibility(View.VISIBLE);
            progressState.setText(R.string.progressState_before);
            //Convert ".opus" to ".mp3" with ffmpeg
            String FileOut = getInternalDirectory() + "/converted.mp3";
            File convertedFile = new File(FileOut);
            if(convertedFile.exists()){
                convertedFile.delete();
            }
            ConvertFromOpusToMp3(FileIn, FileOut);
            //further functionality happens when converting is finished
        }
    }

    private void copyInputStreamToFile(Uri uri, File file) throws FileNotFoundException {
        InputStream ins = getActivity().getContentResolver().openInputStream(uri);
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = ins.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if (out != null) {
                    out.close();
                }

                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    OurUtils utils = new OurUtils();
                    int ms = utils.timeStringToMilliSecs(splitEnd[0]);
                    int prog = ms < duration ? Math.round(((float)ms/duration)*60) : 60;
                    progress.setProgress(prog);
                }
                Log.w(null, message);
            }

            public void onFailure(String message) {
                Log.w(null, message);
                myTextViewNotEditable.setText("Umwandlung fehlgeschlagen: " + message);
                new File(In).delete();
                new File(Out).delete();
                progress.setVisibility(View.INVISIBLE);
                progressState.setVisibility(View.INVISIBLE);
                speechtotext.setEnabled(true);
                menuSavedText.setEnabled(true);
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

    private WitAPI prepareRetrofit() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.wit.ai/").build();
        return retrofit.create(WitAPI.class);
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

            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onFailure(String message) {
                Log.w(null, message);
                myTextViewNotEditable.setText("Umwandlung fehlgeschlagen: " + message);
                new File(In).delete();
                File audioFolder = new File(getInternalDirectory());
                List<File> mp3Files = Arrays.stream(audioFolder.listFiles()).filter(f -> f.getName().endsWith(".mp3")).collect(Collectors.toList());
                mp3Files.forEach(f -> f.delete());
                progress.setVisibility(View.INVISIBLE);
                progressState.setVisibility(View.INVISIBLE);
                speechtotext.setEnabled(true);
                menuSavedText.setEnabled(true);
                progress.setVisibility(View.INVISIBLE);
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onFinish() {
                Log.w(null, "finished");

                //filter files in audiofolder for only mp3
                File audioFolder = new File(getInternalDirectory());
                List<File> mp3Files = Arrays.stream(audioFolder.listFiles()).filter(f -> f.getName().endsWith(".mp3")).collect(Collectors.toList());
                if (mp3Files.size() > 1) {
                    File converted = new File(getInternalDirectory() + "/converted.mp3");
                    mp3Files.remove(converted);
                    converted.delete();
                }

                //call wit for first mp3 file, others are called recursively if necessary
                WitAPI witApi = prepareRetrofit();
                progress.setProgress(61);
                progressState.setText(R.string.progressState_wit);
                int currentLengthWitText = 0;
                callWit(witApi, mp3Files.get(0), mp3Files, currentLengthWitText);
            }
        });
    }

    private void callWit(WitAPI api, File file, List<File> mp3Files, int currentLengthWitText){
        Call<ResponseBody> call = api.getMessageFromAudio("audio/mpeg3", RequestBody.create(MediaType.parse("audio/mpeg3"), file));

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsn = new JSONObject(response.body().string());
                    if(jsn.has("text")) {
                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(witText);
                        spannableStringBuilder.append(jsn.getString("text") + " ");
                        witText = SpannableString.valueOf(spannableStringBuilder);
                        progress.setProgress(progress.getProgress() + Math.round(((float)20000/duration) * 40));
                    }
                    //Named Entities
                    if (jsn.has("entities")) {
                        JSONObject entities = jsn.getJSONObject("entities");
                        //dates (wit$datetime:datetime)
                        if (entities.has("wit$datetime:datetime")) {
                            JSONArray dates = entities.getJSONArray("wit$datetime:datetime");
                            for (int i = 0; i < dates.length(); ++i) {
                                JSONObject entity = dates.getJSONObject(i);
                                int start = entity.getInt("start") + currentLengthWitText;
                                int end = entity.getInt("end") + currentLengthWitText;
                                startHighlight.add(start);
                                endHighlight.add(end);
                            }
                        }
                        //Persons (contact)
                        if (entities.has("wit$contact:contact")) {
                            JSONArray contacts = entities.getJSONArray("wit$contact:contact");
                            for (int i = 0; i < contacts.length(); ++i) {
                                JSONObject contact = contacts.getJSONObject(i);
                                int start = contact.getInt("start") + currentLengthWitText;
                                int end = contact.getInt("end") + currentLengthWitText;
                                startHighlight.add(start);
                                endHighlight.add(end);
                            }
                        }
                    }
                    //after last file: show result & clean up
                    if (file == mp3Files.get(mp3Files.size() - 1)) {
                        if (startHighlight.isEmpty()) {
                            myText.setText(witText);
                            myTextViewNotEditable.setText(witText);
                        } else {
                            for (int i = 0; i < startHighlight.size(); i++) {
                                witText.setSpan(new ForegroundColorSpan(Color.rgb(3,218,197)), startHighlight.get(i), endHighlight.get(i), 0);
                            }
                            myText.setText(witText);
                            myTextViewNotEditable.setText(witText);
                        }

                        savetext.setEnabled(true);
                        menuSavedText.setEnabled(true);
                        progress.setProgress(100);
                        myText.setText(witText);

                        progress.setVisibility(View.INVISIBLE);
                        progressState.setVisibility(View.INVISIBLE);
                        for (File f: mp3Files){
                            f.delete();
                        }
                        startHighlight = new ArrayList<>();
                        endHighlight = new ArrayList<>();
                    } else{
                        //call method recursively as long as there are files to transcribe
                        callWit(api,  mp3Files.get(mp3Files.indexOf(file) + 1), mp3Files, witText.length());
                    }


                } catch (JSONException |  IOException | NullPointerException e){ //TODO: improve error handling further
                    e.printStackTrace();
                    myTextViewNotEditable.setText("Exception: "+ e.getMessage());
                    progress.setVisibility(View.INVISIBLE);
                    progressState.setVisibility(View.INVISIBLE);
                    startHighlight = new ArrayList<>();
                    endHighlight = new ArrayList<>();
                    speechtotext.setEnabled(true);
                    menuSavedText.setEnabled(true);
                    for (File f: mp3Files){
                        f.delete();
                    }
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                myTextViewNotEditable.setText("Call fehlgeschlagen: "+ t.getMessage());
                t.printStackTrace();
                progress.setVisibility(View.INVISIBLE);
                progressState.setVisibility(View.INVISIBLE);

                for(File f: mp3Files){
                    f.delete();
                }
                startHighlight = new ArrayList<>();
                endHighlight = new ArrayList<>();
                speechtotext.setEnabled(true);
                menuSavedText.setEnabled(true);
                call.cancel();
            }
        });
    }

    //open popup to save text
    public void openSavingPopup(View myView){
        if(myText.getText().length() > 0) {
            DialogFragment newFragment = new SavingPopup();
            newFragment.show(getChildFragmentManager(), "savingPopup");
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Kann keine leere Nachricht speichern", Toast.LENGTH_SHORT).show();
        }
    }

    //handle saving when "save" is clicked in popup
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void saveText(DialogFragment dialogFragment) {
        //extract name that was entered in dialog
        String personName = "";
        Dialog dialog = dialogFragment.getDialog();
        if (dialog != null) {
            personName = ((EditText) dialog.findViewById(R.id.editTextTextPersonName)).getText().toString();
        }

        //initialize sp
        SharedPreferences sp = getActivity().getSharedPreferences(String.valueOf(R.string.sp_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        //generate key to save text with
        String key = "savedText1";
        if (fileName != null && !fileName.isEmpty()) {
            if (!personName.isEmpty()) {
                key = fileName + "_" + personName;
            } else {
                key = fileName;
            }
        }

        String value = Html.toHtml(new SpannableString(myText.getText()), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        //save text in sp
        editor.putString(key, value);
        editor.apply();

        //mirror success to user
        String toastMessage = "Nachricht gespeichert";
        if (!personName.isEmpty()) {
            toastMessage = "Nachricht von " + personName + " gespeichert";
        }
        Toast.makeText(getActivity().getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }


}