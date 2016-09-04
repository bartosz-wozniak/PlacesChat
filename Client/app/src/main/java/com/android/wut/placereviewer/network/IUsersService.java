package com.android.wut.placereviewer.network;

import com.android.wut.placereviewer.models.Response;
import com.android.wut.placereviewer.models.User;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Augi_ on 2016-06-05.
 */
public interface IUsersService {

    @GET("GetUser")
    Observable<User> GetUser(@Query("login") String login);
    @GET("SaveUser")
    Observable<Response> SaveUser(@Query("login") String login, @Query("id") int id, @Query("email") String email, @Query("password") String password, @Query("image") String image);
    @POST("SaveUser")
    Observable<Response> SaveUser(@Body User user);
}
