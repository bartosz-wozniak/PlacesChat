package com.android.wut.placereviewer.network;

import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.models.Result;
import com.android.wut.placereviewer.models.SinglePlaceResult;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by soive on 27.04.2016.
 */
public interface IGoogleService  {
    @GET("nearbysearch/json")
    Observable<Result> GetPlaces(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key) ;

    @GET("details/json")
    Observable<SinglePlaceResult> GetPlace(@Query("placeid") String placeId, @Query("key") String key) ;

    @GET("nearbysearch/json")
    Observable<Result> GetPlaces(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key, @Query("keyword") String keyword);

    @GET("nearbysearch/json")
    Observable<Result> GetPlacesNext( @Query("key") String key, @Query("pagetoken") String token);

}
