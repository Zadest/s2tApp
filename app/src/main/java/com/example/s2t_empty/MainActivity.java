package com.example.s2t_empty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.services.WitAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    public void changeText(View myView) {
        System.out.println("!Test");
        TextView myText = findViewById(R.id.textView5);
        System.out.println(myText.getText());
        if (state) {
            myText.setText("Neuer Text");
            state = !(state);
        }
        else{
            myText.setText("Anderer Text");
            state = !(state);
        }
    }

    //TODO: maybe move method (where?) --> find best practices
    //TODO: fix warning  Accessing hidden method Ldalvik/system/CloseGuard
    public void changeTextWithWit(View myView) {
        TextView myText = findViewById(R.id.textView5);
        if(state){
        WitAPI witApi = prepareRetrofit();
        Call<ResponseBody> call = witApi.getMessageFromTestText();
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
                }
                call.cancel();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("fail!");
                myText.setText("Wit does not want to play with you");
                call.cancel();
            }
        });
        } else {
            myText.setText("Anderer Text");
            state = !(state);
        }
    }

    private WitAPI prepareRetrofit(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.wit.ai/").build();
        return retrofit.create(WitAPI.class);
    }

}