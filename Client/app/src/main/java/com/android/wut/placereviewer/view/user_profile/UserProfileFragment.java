package com.android.wut.placereviewer.view.user_profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Response;
import com.android.wut.placereviewer.models.User;
import com.android.wut.placereviewer.network.IUsersService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.utils.BitmapUtils;
import com.android.wut.placereviewer.utils.HashUtils;
import com.android.wut.placereviewer.view.BaseFragment;

import java.io.FileDescriptor;
import java.io.IOException;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import lombok.val;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Augi_ on 2016-06-08.
 */
public class UserProfileFragment extends BaseFragment {

    @Bind(R.id.user_login)
    TextView userLoginTextView;

    @Bind(R.id.user_email)
    TextView userEmailTextView;

    @Bind(R.id.user_password)
    TextView userPasswordTextView;

    @Bind(R.id.user_image)
    CircleImageView userImageCircleView;

    @Bind(R.id.notification_switch)
    Switch notificationSwitch;

    @Bind(R.id.curtain)
    ImageView curtain;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    OnChangePhotoListener listener;

    private User user;
    private static final int RESULT_LOAD_CAMERA = 12345;
    private static final int RESULT_LOAD_GALLERY = 54321;
    private Bitmap userPhotoBitmap;

    public interface OnChangePhotoListener {
        void onChangePhoto();
        void onChangeLogin();
    }

    public static UserProfileFragment newInstance() {

        return new UserProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayUserProfileData();

        if (getActivity() instanceof OnChangePhotoListener)
            listener = (OnChangePhotoListener) getActivity();
        else {
            throw new IllegalStateException();
        }
        setSwitchState();
    }

    private void setSwitchState() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean wantNotification = sharedPref.getBoolean("showNotification", true);

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            if(isChecked)
                editor.putBoolean("showNotification", true);
            else
                editor.putBoolean("showNotification", false);
            editor.commit();
        });

        if(wantNotification)
            notificationSwitch.setChecked(true);
    }

    private void displayUserProfileData() {
        RetrofitProvider r = new RetrofitProvider();
        IUsersService s = r.GetUsersServices();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String login = sharedPref.getString("login", "");

        val ret = s.GetUser(login);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    curtain.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                })
                .subscribe(this::displayUserProfile , throwable -> Log.e("UserProfileFragment", throwable.toString()));
    }

    @OnClick(R.id.user_image)
    public void takeFoto() {
        AlertDialog changePhotoDialogBox = new AlertDialog.Builder(getActivity())
                .setTitle("Zmiana zdjęcia")
                .setMessage("Wybierz źródło")
                .setPositiveButton("Aparat", (dialog, whichButton) -> {
                    takePhotoFromCamera();
                    dialog.dismiss();
                })
                .setNeutralButton("Galeria", (dialog, whichButton) -> {
                    takePhotoFromGallery();
                    dialog.dismiss();
                })
                .create();
        changePhotoDialogBox.show();
    }

    private void takePhotoFromGallery() {
        Intent takePictureIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, RESULT_LOAD_GALLERY);
        }
    }

    private void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, RESULT_LOAD_CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap imageBitmap = null;
        if (requestCode == RESULT_LOAD_CAMERA && resultCode == -1 && null != data) {
            Bundle extras = data.getExtras();
            imageBitmap = BitmapUtils.getResizedBitmap((Bitmap) extras.get("data"), 200, 200);
            userImageCircleView.setImageBitmap(imageBitmap);
        }
        if (requestCode == RESULT_LOAD_GALLERY && resultCode == -1 && null != data) {
            try {
                imageBitmap = BitmapUtils.getResizedBitmap(getBitmapFromUri(data.getData()), 200, 200);
                userImageCircleView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        if(imageBitmap != null) {
            storeImageInDb(imageBitmap);
        }
    }

    private void storeImageInDb(Bitmap imageBitmap) {
        RetrofitProvider r = new RetrofitProvider();
        IUsersService s = r.GetUsersServices();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String login = sharedPref.getString("login", "");
        userPhotoBitmap = imageBitmap;
        val ret = s.GetUser(login);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onStoreUserImage , throwable -> Log.e("UserProfileFragment", throwable.toString()));
    }

    private void onStoreUserImage(User user) {
        if (userPhotoBitmap == null)
            return;
        String imageString = BitmapUtils.bitMapToString(userPhotoBitmap);
        RetrofitProvider r = new RetrofitProvider();
        IUsersService s = r.GetUsersServices();
        user.setImage(imageString);
        val ret = s.SaveUser(user);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if(response.getSuccess() == 1)
                        Toast.makeText(getContext(), "Zaktualizowano", Toast.LENGTH_SHORT);
                        listener.onChangePhoto();
                },
                        throwable -> Log.e("UserProfileFragment", throwable.toString()));
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContext().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @OnClick(R.id.user_password)
    public void changePassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Zmiana hasła");
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setView(input);
        alert.setPositiveButton("Zapisz", (dialog, whichButton) -> {
            RetrofitProvider r = new RetrofitProvider();
            IUsersService s = r.GetUsersServices();
            String password = input.getText().toString();
            if(!password.isEmpty() && password.length() > 4 && password.length() < 10) {

                user.setPassword(HashUtils.HashText(password));
                Observable<Response> ret = s.SaveUser(user);
                ret.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> userPasswordTextView.setText(user.getPassword()), throwable -> Log.e("UserProfileFragment", throwable.toString()));
            }
            else
                Toast.makeText(getContext(), "Hasło musi mieć od 4 do 10 znaków!", Toast.LENGTH_SHORT).show();
        });
        alert.setNegativeButton("Powrót", null);
        alert.show();
    }

    @OnClick(R.id.user_email)
    public void changeEmail() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Zmiana adresu Email");
        final EditText input = new EditText(getContext());
        input.setText(user.getEmail());
        alert.setView(input);
        alert.setPositiveButton("Zapisz", (dialog, whichButton) -> {
            RetrofitProvider r = new RetrofitProvider();
            IUsersService s = r.GetUsersServices();
            String email = input.getText().toString();
            if(!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                user.setEmail(email);
                Observable<Response> ret = s.SaveUser(user);
                ret.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> userEmailTextView.setText(user.getEmail()), throwable -> Log.e("UserProfileFragment", throwable.toString()));
            }
            else
                Toast.makeText(getContext(), "Niepoprawny adres Email!", Toast.LENGTH_SHORT).show();
        });
        alert.setNegativeButton("Powrót", null);
        alert.show();
    }


    @OnClick(R.id.user_login)
    public void changeLogin() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Zmiana loginu");
        final EditText input = new EditText(getContext());
        input.setText(user.getLogin());
        alert.setView(input);
        alert.setPositiveButton("Zapisz", (dialog, whichButton) -> {
            RetrofitProvider r = new RetrofitProvider();
            IUsersService s = r.GetUsersServices();
            if(input.getText().toString().isEmpty() == false) {
                user.setLogin(input.getText().toString());
                Observable<Response> ret = s.SaveUser(user);
                ret.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onLoginChanged, throwable -> Log.e("UserProfileFragment", throwable.toString()));
            }
        });
        alert.setNegativeButton("Powrót", null);
        alert.show();
    }

    private void onLoginChanged(Response response) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("login", user.getLogin());
        editor.commit();
        userLoginTextView.setText(user.getLogin());
        listener.onChangeLogin();
    }

    private void displayUserProfile(User user) {
        this.user = user;
        userLoginTextView.setText(user.getLogin());
        userEmailTextView.setText(user.getEmail());
        userPasswordTextView.setText("*****");
        String imgString = user.getImage();
        if(imgString != null)
            userImageCircleView.setImageBitmap(BitmapUtils.stringToBitMap(imgString));
    }
    @Override
    protected int getTitleResId() {
        return R.string.user_profile_fragment;
    }
}
