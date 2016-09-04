package com.android.wut.placereviewer.service;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.lang.Object;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Comment;
import com.android.wut.placereviewer.network.ICommentsService;
import com.android.wut.placereviewer.network.IUsersService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.view.main.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.common.data.Freezable;
import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by soive on 08.06.2016.
 */

public class NotificationsService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Timer timer;
    private TimerTask timerTask;
    private GoogleApiClient googleApiClient;
    private Map<String,Integer> lastPlacesComments;


    public static final int TYPE_BAR = 9;
    public static final int TYPE_RESTAURANT = 79;
    public static final int TYPE_GYM = 44;
    public static final int TYPE_NIGHT_CLUB = 67;
    public static final double placeLikehoodMin = 0.3;


    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.connect();

        lastPlacesComments = new HashMap<>();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        timer = new Timer();
        timerTask = new MyTimerTask();
        // CHANGE DELAY TO BIGGER IN FINAL VERSION
        timer.scheduleAtFixedRate(timerTask, 4 * 1000, 4 * 1000);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (sharedPref.getBoolean("showNotification", true))
                showNotificationIfNeeded();
        }
    }

    private void showNotificationIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(googleApiClient, null);
        result.setResultCallback(likelyPlaces -> {
            if (likelyPlaces.getCount() > 0) {
                Place bestPlace = choosePlace(likelyPlaces);
                if (bestPlace != null)
                    processPlace(bestPlace);
            }
            else {
                Log.i("a", "NO PLACE FOUND");
            }
            likelyPlaces.release();
        });
    }

    private Place choosePlace(PlaceLikelihoodBuffer likelyPlaces) {
        for(val place : likelyPlaces) {
            if (place.getLikelihood() < placeLikehoodMin)
                break;
            val placeTypes = place.getPlace().getPlaceTypes();
            if (placeTypes.contains(TYPE_GYM) || placeTypes.contains(TYPE_RESTAURANT)
                    || placeTypes.contains(TYPE_BAR) || placeTypes.contains(TYPE_NIGHT_CLUB))
            {
                Log.i("a", String.format("Chose '%s' has likelihood: %g",
                        place.getPlace().getName(),
                        place.getLikelihood()));
                return place.getPlace();
            }
        }
        Log.i("a", "NO PLACE FOUND");
        return null;
    }

    private void processPlace(Place place) {
        RetrofitProvider r = new RetrofitProvider();
        ICommentsService s = r.GetCommentsServices();
        val ret = s.GetLastComment(place.getId());
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::newComment , throwable -> Log.e("NotificationsService", throwable.toString()));
    }

    private void newComment(Comment comment) {
        if (comment == null)
            return;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean justAdded = false;
        if (!lastPlacesComments.containsKey(comment.getPlaceId())) {
            lastPlacesComments.put(comment.getPlaceId(),comment.getId());
            justAdded = true;
        }

        String login = sharedPref.getString("login",null);
        if (comment.getLogin().equals(login))
            return;

        int id = lastPlacesComments.get(comment.getPlaceId());
        if (id != comment.getId() || justAdded) {
            lastPlacesComments.put(comment.getPlaceId(),comment.getId());
                showNotification(comment);

        }
    }

    private void showNotification(Comment comment) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_menu_white_36dp)
                        .setLargeIcon(BitmapFactory.decodeResource( getResources(), R.drawable.ic_menu_white_36dp))
                        .setContentTitle("Nowy komentarz")
                        .setContentText(comment.getComment());
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("placeId",comment.getPlaceId());

        PendingIntent contentIntent = PendingIntent
                .getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification =  mBuilder.build();
        notification.contentIntent = contentIntent;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        mNotificationManager.notify(666,notification);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timerTask != null) {
            timerTask.cancel();
            timer.purge();
        }
    }


}
