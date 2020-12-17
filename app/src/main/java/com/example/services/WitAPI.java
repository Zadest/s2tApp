package com.example.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface WitAPI {
    String CLIENT_ACCESS_TOKEN = "HEUQMT6CGKPUTUCQFHUHM65F6H62WWLF";
    String testString = "Hey, my name is Wit";

    @Headers("Authorization: Bearer " + CLIENT_ACCESS_TOKEN)
    @GET("message?q=" + testString)
    Call<ResponseBody> getMessageFromTestText();
    //TODO: next step: enable entering message with audio file (where to get it from? how to transfer it to parameter?)

}
