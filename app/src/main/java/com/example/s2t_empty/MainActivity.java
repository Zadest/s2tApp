package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
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

    private boolean state = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

}