package com.android.wut.placereviewer.view.map;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.CommentResult;
import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.models.PlaceType;
import com.android.wut.placereviewer.models.Result;
import com.android.wut.placereviewer.network.ICommentsService;
import com.android.wut.placereviewer.network.IGoogleService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.view.BaseFragment;
import com.android.wut.placereviewer.view.list.CommentListFragment;
import com.android.wut.placereviewer.view.main.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;

import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by soive on 21.04.16.
 */
public class MapFragment extends BaseFragment implements OnMapReadyCallback , GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraChangeListener {


    public static MapFragment newInstance() {
        return new MapFragment();
    }
    private HashMap<Marker, Place> places;
    private GoogleMap googleMap;
    private PlaceType placeType;
    private String placeTypeStr;

    public MapFragment() {
        super();
        this.places = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment supportMapFragment = (SupportMapFragment) fragmentManager
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    protected int getTitleResId() {
        return R.string.map_fragment;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnCameraChangeListener(this);
        val bundle =  getArguments();
        switch (bundle.getString("type")){
            case "bar":
                placeType = PlaceType.Bar;
                break;
            case "restaurant":
                placeType = PlaceType.Restaurant;
                break;
            case "night_club":
                placeType = PlaceType.Club;
                break;
            case "gym":
                placeType = PlaceType.Gym;
                break;
        }
        placeTypeStr = bundle.getString("type");
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.234183,20.996994),15.0f));
        RetrofitProvider r = new RetrofitProvider();
        IGoogleService g = r.GetGoogleServices();
        val ret = g.GetPlaces(getString(R.string.warsaw_center),5000, placeTypeStr, getString(R.string.google_places_key));
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetPlacesNext , throwable -> Log.e("homeFrafgment", throwable.toString()));

    }

    private void onGetPlacesNext(Result result) {
        Marker lastMarker = null;
        googleMap.clear();
        for (Place place : result.getResults()) {
            place.setPlaceType(placeType);
            lastMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(
                    place.geometry.getLocation().getLat(), place.geometry.getLocation().getLng()
            )).title(place.Name));
            places.put(lastMarker,place);
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        CommentListFragment commentListFragment = new CommentListFragment();
        Bundle bundle = new Bundle();
        Place p = places.get(marker);
        bundle.putSerializable("place", p);
        commentListFragment.setArguments(bundle);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity)getActivity();
            activity.changeFragment(commentListFragment,null);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        RetrofitProvider r = new RetrofitProvider();
        IGoogleService g = r.GetGoogleServices();
        String latlng = cameraPosition.target.latitude + "," + cameraPosition.target.longitude;
        val ret = g.GetPlaces(latlng,(int)getRadius(), placeTypeStr, getString(R.string.google_places_key));
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetPlacesNext , throwable -> Log.e("homeFrafgment", throwable.toString()));

    }

    private float zoom(double distance) {
        byte zoom = 1;
        WindowManager windowManager = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int widthInPixels = windowManager.getDefaultDisplay().getWidth();
        double equatorLength = 6378136.28;//in meters
        double metersPerPixel = equatorLength / 256;
        while ((metersPerPixel * widthInPixels) > distance) {
            metersPerPixel /= 2;
            ++zoom;
        }
        if (zoom > 21)
            zoom = 21;
        if (zoom < 1)
            zoom = 1;
        return zoom;
    }

    public double getRadius() {
        double latitudeSpan = 0;
        VisibleRegion vr = googleMap.getProjection().getVisibleRegion();
        float [] results = new float[3];
        Location.distanceBetween(vr.farLeft.latitude, vr.farLeft.longitude, vr.farRight.latitude, vr.farRight.longitude, results);
        latitudeSpan = results[0];
        return latitudeSpan;
    }
}
