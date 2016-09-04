package com.android.wut.placereviewer.view.list;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.CommentResult;
import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.network.ICommentsService;
import com.android.wut.placereviewer.network.IUsersService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.view.BaseFragment;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.Bind;
import butterknife.OnClick;
import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by soive on 28.04.2016.
 */
public class CommentListFragment extends BaseFragment {

    @Bind(R.id.comment_list_recycleview)
    RecyclerView commentListRecyclerview;

    @Bind(R.id.place_image)
    ImageView placeImageView;

    @Bind(R.id.et_new_comment)
    EditText newComment;

    @Bind(R.id.curtain)
    ImageView curtain;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    private Place place;
    private CommentListAdapter adapter;

    public static CommentListFragment newInstance() {

        return new CommentListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new CommentListAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commentListRecyclerview.setAdapter(adapter);
        commentListRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle bundle = getArguments();
        if (bundle.containsKey("place")) {
            place = (Place) bundle.getSerializable("place");
            getComments();
        }
        getActionBar().setTitle(place.getName());
        setPlaceImageView();
    }

    private void getComments() {
        RetrofitProvider r = new RetrofitProvider();
        ICommentsService s = r.GetCommentsServices();
        val ret1 = s.GetComments(place.getId2());
        ret1.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetCommentsListNext , throwable -> Log.e("PlaceListFragment", throwable.toString()));
    }

    @OnClick(R.id.btn_add_comment)
    public void addComment(){
        String comment = newComment.getText().toString();
        if(comment.isEmpty()) {
            Toast.makeText(getContext(), R.string.comment_restriction, Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitProvider r = new RetrofitProvider();
        ICommentsService s = r.GetCommentsServices();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String login = preferences.getString("login", "Default");
        val ret1 = s.AddComment(comment, -1, login, place.getId2());
        ret1.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> getComments() , throwable -> Log.e("PlaceListFragment", throwable.toString()));
        newComment.setText("");
    }




    private void setPlaceImageView() {
    String photoRef = "";
    if(place.Photos != null && !place.Photos.isEmpty())
        photoRef = place.Photos.get(0).PhotoRef;
        int placeholderRes = 0;
        switch(place.PlaceType) {
            case Bar:
                placeholderRes = R.drawable.image_pub_placeholder;
                break;
            case Restaurant:
                placeholderRes = R.drawable.image_restaurant_placeholder;
                break;
            case Club:
                placeholderRes = R.drawable.image_club_placeholder;
                break;
            case Gym:
                placeholderRes = R.drawable.image_gym_placeholder;
                break;
        }
    Picasso.with(this.getContext())
            .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoRef + "&key=AIzaSyAbTSDsXIPJHu73Qz04Be0y2p5MlA79eFw")
            .placeholder(placeholderRes)
            .into(placeImageView, new Callback() {
                @Override
                public void onSuccess() {
                    curtain.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                @Override
                public void onError() {
                    curtain.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
    }

    private void onGetCommentsListNext(CommentResult result) {
        adapter.setCommentList(result.getComments());
        adapter.notifyDataSetChanged();
        commentListRecyclerview.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    protected int getTitleResId() {
        return R.string.comment_list_fragment;
    }

}
