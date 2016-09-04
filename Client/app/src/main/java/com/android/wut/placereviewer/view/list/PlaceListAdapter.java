package com.android.wut.placereviewer.view.list;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.view.BaseFragment;
import com.android.wut.placereviewer.view.main.MainActivity;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by soive on 28.04.2016.
 */
public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceListViewHolder> {

    private List<Place> places;
    private BaseFragment fragment;

    public PlaceListAdapter(BaseFragment fragment) {
        places = Collections.emptyList();
        this.fragment = fragment;
    }

    @Override
    public PlaceListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_place_list, parent, false);
        return new PlaceListViewHolder(itemView,fragment);
    }

    @Override
    public void onBindViewHolder(PlaceListViewHolder holder, int position) {
        holder.bind(places.get(position));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void setPlaceList(List<Place> places) {
        this.places = places;
    }

    public static class PlaceListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.place_item_name)
        TextView placeName;

        @Bind(R.id.place_item_location)
        TextView placeLocation;

        @Bind(R.id.place_type_photo)
        CircleImageView placeTypePhoto;

        private Place place;

        private BaseFragment fragment;

        public PlaceListViewHolder(View itemView, BaseFragment fragment) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            this.fragment = fragment;
        }

        public void bind(Place place) {
            this.place = place;
            placeName.setText(place.Name);
            placeLocation.setText(place.Address);
            int res = 0;
            switch(place.PlaceType) {
                case Bar:
                    res = R.drawable.image_pub_logo;
                    break;
                case Restaurant:
                    res = R.drawable.image_restaurant_logo;
                    break;
                case Club:
                    res = R.drawable.image_club_logo;
                    break;
                case Gym:
                    res = R.drawable.image_gym_logo;
                    break;
            }
            placeTypePhoto.setImageResource(res);
        }

        @Override
        public void onClick(View v) {
            CommentListFragment commentListFragment = new CommentListFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("place", place);
            commentListFragment.setArguments(bundle);

            if (fragment.getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity)fragment.getActivity();
                activity.changeFragmentWithStack(commentListFragment,null);
            }
        }
    }
}
