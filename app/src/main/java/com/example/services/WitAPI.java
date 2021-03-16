package com.example.services;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface WitAPI {
    String CLIENT_ACCESS_TOKEN = "HEUQMT6CGKPUTUCQFHUHM65F6H62WWLF";

    @Headers("Authorization: Bearer " + CLIENT_ACCESS_TOKEN)
    @POST("speech")
    Call<ResponseBody> getMessageFromAudio(@Header("Content-Type") String audioType, @Body RequestBody body);

}
