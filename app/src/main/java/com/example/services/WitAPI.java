package com.example.services;

import java.io.InputStream;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface WitAPI {
    String CLIENT_ACCESS_TOKEN = "HEUQMT6CGKPUTUCQFHUHM65F6H62WWLF";
    String testString = "Hey, my name is Wit";

    @Headers("Authorization: Bearer " + CLIENT_ACCESS_TOKEN)
    @GET("message?q=" + testString)
    Call<ResponseBody> getMessageFromTestText();

    @Headers({"Authorization: Bearer " + CLIENT_ACCESS_TOKEN, "Content-Type: audio/ogg"})
    @POST("speech")
    Call<ResponseBody> getMessageFromAudio(@Body RequestBody body);

}
