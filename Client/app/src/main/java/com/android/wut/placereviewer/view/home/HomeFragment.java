package com.android.wut.placereviewer.view.home;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.models.PlaceType;
import com.android.wut.placereviewer.models.Result;
import com.android.wut.placereviewer.network.IGoogleService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.view.BaseActivity;
import com.android.wut.placereviewer.view.BaseFragment;
import com.android.wut.placereviewer.view.list.PlaceListFragment;
import com.android.wut.placereviewer.view.main.MainActivity;
import com.android.wut.placereviewer.view.map.MapFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import lombok.val;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomeFragment extends BaseFragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    private PlaceType placeType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void onGetPlacesListNext(Result result) {
        PlaceListFragment placeListFragment = new PlaceListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("result", result);
        placeListFragment.setArguments(bundle);

        List<Place> places1 = result.getResults();
        for(Place p : places1) {
            p.PlaceType = placeType;
        }
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.changeFragmentWithStack(placeListFragment, null);
        }
    }

    @OnClick(R.id.pubs_tile_map_btn)
    public void onPubsMapButtonClick() {
        placeType = PlaceType.Bar;
        provideFragmentWithMap();
    }

    @OnClick(R.id.restaurants_tile_map_btn)
    public void onRestaurantsMapButtonClick() {
        placeType = PlaceType.Restaurant;
        provideFragmentWithMap();
    }

    @OnClick(R.id.clubs_tile_map_btn)
    public void onClubsMapButtonClick() {
        placeType = PlaceType.Club;
        provideFragmentWithMap();
    }

    @OnClick(R.id.gym_tile_map_btn)
    public void onGymsMapButtonClick() {
        placeType = PlaceType.Gym;
        provideFragmentWithMap();
    }

    private void provideFragmentWithMap() {
        String type = "";
        switch(placeType) {
            case Bar:
                type = "bar";
                break;
            case Restaurant:
                type = "restaurant";
                break;
            case Club:
                type = "night_club";
                break;
            case Gym:
                type = "gym";
                break;
        }


        MapFragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("type",type);
        mapFragment.setArguments(bundle);



        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity)getActivity();
            activity.changeFragmentWithStack(mapFragment,null);
        }
    }


    @OnClick(R.id.pubs_tile_list_btn)
    public void onPubListButtonClick() {
        placeType = PlaceType.Bar;
        provideFragmentWithPlaceList();
    }

    @OnClick(R.id.restaurants_tile_list_btn)
    public void onRestaurantListButtonClick() {
        placeType = PlaceType.Restaurant;
        provideFragmentWithPlaceList();
    }
    @OnClick(R.id.clubs_tile_list_btn)
    public void onClubListButtonClick() {
        placeType = PlaceType.Club;
        provideFragmentWithPlaceList();
    }
    @OnClick(R.id.gym_tile_list_btn)
    public void onGymListButtonClick() {
        placeType = PlaceType.Gym;
        provideFragmentWithPlaceList();
    }

    private void provideFragmentWithPlaceList() {
        String type = "";
        switch(placeType) {
            case Bar:
                type = getString(R.string.google_location_type_bar);
                break;
            case Restaurant:
                type = getString(R.string.google_location_type_restaurant);
                break;
            case Club:
                type = getString(R.string.google_location_type_club);
                break;
            case Gym:
                type = getString(R.string.google_location_type_gym);
                break;
        }
        RetrofitProvider r = new RetrofitProvider();
        IGoogleService g = r.GetGoogleServices();
        val ret = g.GetPlaces(getString(R.string.warsaw_center), 5000, type, getString(R.string.google_places_key));
        ret.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this :: onGetPlacesListNext, throwable -> Log.e("homeFrafgment", throwable.toString()));
    }
    

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    protected int getTitleResId() {
        return R.string.home_fragment;
    }
}
