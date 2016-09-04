package com.android.wut.placereviewer.view.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.models.PlaceType;
import com.android.wut.placereviewer.models.Result;
import com.android.wut.placereviewer.network.ICommentsService;
import com.android.wut.placereviewer.network.IGoogleService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.view.BaseFragment;
import com.android.wut.placereviewer.view.main.MainActivity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by soive on 28.04.2016.
 */
public class PlaceListFragment extends BaseFragment {

    @Bind(R.id.place_list_recycleview)
    RecyclerView placeListRecycleview;

    @Bind(R.id.btn_more)
    Button buttonMore;

    @Bind(R.id.et_search)
    EditText textSearch;

    private PlaceListAdapter adapter;

    private PlaceType placeType;

    private String nextToken;

    public static PlaceListFragment newInstance() {
        return new PlaceListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PlaceListAdapter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        placeListRecycleview.setAdapter(adapter);
        placeListRecycleview.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle bundle = getArguments();
        buttonMore.setEnabled(false);
        if (bundle.containsKey("result")) {
            val result = (Result) bundle.getSerializable("result");
            nextToken = result.getNextPageToken();
            if (nextToken != null) buttonMore.setEnabled(true);
            if (result.getResults() != null && result.getResults().size() > 0)
                placeType = (result.getResults()).get(0).PlaceType;
            else {
                placeType = PlaceType.Bar;
            }
            adapter.setPlaceList(result.getResults());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected int getTitleResId() {
        return R.string.place_list_fragment;
    }

    @OnClick(R.id.btn_search)
    public void Search(){
        String search = textSearch.getText().toString();
        if(search.isEmpty()) {
            Toast.makeText(getContext(),"Podaj słowo kluczowe", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitProvider r = new RetrofitProvider();
        IGoogleService g = r.GetGoogleServices();
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
        val ret = g.GetPlaces(getString(R.string.warsaw_center),5000, type, getString(R.string.google_places_key), search);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetPlacesListNext , throwable -> Log.e("placeListFrafgment", throwable.toString()));
    }

    private void onGetPlacesListNext(Result result) {
        PlaceListFragment placeListFragment = new PlaceListFragment();
        Bundle bundle = new Bundle();
        if (result.getResults() == null || result.getResults().size() == 0) {
            buttonMore.setEnabled(true);
        }
        bundle.putSerializable("result",result);
        placeListFragment.setArguments(bundle);

        List<Place> places = result.getResults();
        for(Place p : places) {
            p.PlaceType = placeType;
        }
        nextToken = result.getNextPageToken();
        if (nextToken != null)
            buttonMore.setEnabled(true);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity)getActivity();
            activity.changeFragmentWithStack(placeListFragment,null);
        }
    }

    @OnClick(R.id.btn_more)
    public void More(){
        RetrofitProvider r = new RetrofitProvider();
        IGoogleService g = r.GetGoogleServices();
        if (nextToken != null) {
            val ret = g.GetPlacesNext(getString(R.string.google_places_key), nextToken);
            ret.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this :: onGetPlacesListNext, throwable -> Log.e("placeListFrafgment", throwable.toString()));
            nextToken = null;
            buttonMore.setEnabled(false);
        }
    }
}
