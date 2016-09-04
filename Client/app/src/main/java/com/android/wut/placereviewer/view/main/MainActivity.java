package com.android.wut.placereviewer.view.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.wut.placereviewer.models.Place;
import com.android.wut.placereviewer.models.PlaceType;
import com.android.wut.placereviewer.models.SinglePlaceResult;
import com.android.wut.placereviewer.network.IGoogleService;
import com.android.wut.placereviewer.network.IUsersService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.service.NotificationsService;
import com.android.wut.placereviewer.utils.BitmapUtils;
import com.android.wut.placereviewer.view.list.CommentListFragment;
import com.android.wut.placereviewer.view.list.PlaceListFragment;
import com.android.wut.placereviewer.view.login.LoginActivity;
import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.view.BaseActivity;
import com.android.wut.placereviewer.view.home.HomeFragment;
import com.android.wut.placereviewer.view.map.MapFragment;
import com.android.wut.placereviewer.view.user_profile.UserProfileFragment;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import de.hdodenhof.circleimageview.CircleImageView;
import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static butterknife.ButterKnife.findById;

public class MainActivity extends BaseActivity implements UserProfileFragment.OnChangePhotoListener {

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view)
    public NavigationView navigationView;

    int PERMISSON_LOCATION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        requestForLocationPermission();

        initializeMenu();

        String placeId = getIntent().getStringExtra("placeId");
        if (placeId != null)
        {
            goToPlaceView(placeId);
        }
        else
        {
            changeFragment(HomeFragment.newInstance(), null);
        }


    }

    private void goToPlaceView(String placeId) {

        RetrofitProvider r = new RetrofitProvider();
        IGoogleService g = r.GetGoogleServices();
        val ret = g.GetPlace(placeId, getString(R.string.google_places_key));
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetPlace , throwable -> Log.e("homeFrafgment", throwable.toString()));


    }

    private void onGetPlace(SinglePlaceResult singlePlaceResult) {
        CommentListFragment commentListFragment = new CommentListFragment();
        setPlaceType(singlePlaceResult);
        Bundle bundle = new Bundle();
        bundle.putSerializable("place", singlePlaceResult.getPlace());
        commentListFragment.setArguments(bundle);
        runOnUiThread(() -> {
            cleanBackStack();
            changeFragmentWithStack(commentListFragment,null);
        });

    }

    private void setPlaceType(SinglePlaceResult singlePlaceResult) {
        val types = singlePlaceResult.getPlace().getTypes();
        singlePlaceResult.getPlace().setPlaceType(PlaceType.Restaurant);
        if (types.contains("bar"))
            singlePlaceResult.getPlace().setPlaceType(PlaceType.Bar);
        if (types.contains("restaurant"))
            singlePlaceResult.getPlace().setPlaceType(PlaceType.Restaurant);
        if (types.contains("gym"))
            singlePlaceResult.getPlace().setPlaceType(PlaceType.Gym);
        if (types.contains("night_club"))
            singlePlaceResult.getPlace().setPlaceType(PlaceType.Club);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeMenu() {
        navigationView.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            changeFragment(HomeFragment.newInstance(), null);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        navigationView.getMenu().getItem(1).setOnMenuItemClickListener(item -> {
            changeFragment(UserProfileFragment.newInstance(), null);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        navigationView.getMenu().getItem(2).setOnMenuItemClickListener(item -> logoutUser());
        loadUserProfile();
    }

    private boolean logoutUser() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("login", "");
        editor.commit();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        drawerLayout.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }

    private void loadUserProfile(){
        CircleImageView userPhoto = findById(navigationView.getHeaderView(0), R.id.drawer_photo);

        Picasso.with(this)
                .load("https://www.youtube.com/yt/space/media/images/yt-space-icon-newyork.png")
                .placeholder(R.drawable.image_placeholder)
                .into(userPhoto);

        TextView username = findById(navigationView.getHeaderView(0), R.id.drawer_username);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = sharedPref.getString("login", "");
        username.setText("Witaj " + login);

        CircleImageView userImage = findById(navigationView.getHeaderView(0), R.id.drawer_photo);
        RetrofitProvider r = new RetrofitProvider();
        IUsersService s = r.GetUsersServices();
        val ret = s.GetUser(login);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(u -> {
                    String photo = u.getImage();
                    if(photo != null && !photo.isEmpty())
                        userImage.setImageBitmap(BitmapUtils.stringToBitMap(photo));
                }, throwable -> Log.e("Drawer", throwable.toString()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSON_LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        requestForLocationPermission();
                    } else {
                        Intent intent = new Intent(getBaseContext(), NotificationsService.class);
                        startService(intent);
                    }
                }
            }
        }
    }

    private void requestForLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSON_LOCATION_REQUEST);
        } else {
            Intent intent = new Intent(getBaseContext(), NotificationsService.class);
            startService(intent);
        }
    }

    @Override
    public void onChangePhoto() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = sharedPref.getString("login", "");
        CircleImageView userImage = findById(navigationView.getHeaderView(0), R.id.drawer_photo);
        RetrofitProvider r = new RetrofitProvider();
        IUsersService s = r.GetUsersServices();
        val ret = s.GetUser(login);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(u -> {
                    String photo = u.getImage();
                    if(photo != null && !photo.isEmpty())
                        userImage.setImageBitmap(BitmapUtils.stringToBitMap(photo));
                }, throwable -> Log.e("Drawer", throwable.toString()));
    }

    @Override
    public void onChangeLogin() {
        TextView username = findById(navigationView.getHeaderView(0), R.id.drawer_username);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = sharedPref.getString("login", "");
        username.setText("Witaj " + login);
    }

}
