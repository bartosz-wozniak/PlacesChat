package com.android.wut.placereviewer.network;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Created by soive on 27.04.2016.
 */
public class RetrofitProvider {
    public IGoogleService GetGoogleServices(){
        return new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getOkHttpClient())
                .build()
                .create(IGoogleService.class);
    }

    public ICommentsService GetCommentsServices(){
        return new Retrofit.Builder()
                .baseUrl("http://placeschat.azurewebsites.net/api/Comment/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getOkHttpClient())
                .build()
                .create(ICommentsService.class);
    }

    public IUsersService GetUsersServices(){
        return new Retrofit.Builder()
                .baseUrl("http://placeschat.azurewebsites.net/api/User/")
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getOkHttpClient())
                .build()
                .create(IUsersService.class);
    }


    private HttpLoggingInterceptor getLoggingInterceptor () {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> Log.d("Retrofit Provider",message));
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return interceptor;
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder().addInterceptor(getLoggingInterceptor()).build();
    }

}
