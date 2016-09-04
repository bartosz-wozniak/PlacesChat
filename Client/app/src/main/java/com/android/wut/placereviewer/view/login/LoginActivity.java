package com.android.wut.placereviewer.view.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.widget.EditText;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Response;
import com.android.wut.placereviewer.models.User;
import com.android.wut.placereviewer.network.ICommentsService;
import com.android.wut.placereviewer.network.IUsersService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.utils.HashUtils;
import com.android.wut.placereviewer.view.BaseActivity;
import com.android.wut.placereviewer.view.main.MainActivity;

import java.security.MessageDigest;

import butterknife.Bind;
import butterknife.OnClick;
import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    @Bind(R.id.input_login) EditText _loginText;
    @Bind(R.id.input_password) EditText _passwordText;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
    }

    @OnClick(R.id.btn_login)
    public void onLoginButtonClick() {
        login();
    }

    @OnClick(R.id.link_signup)
    public void onSignupLinkClick() {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    public void login() {
        Log.d(TAG, "Login");
        progressDialog.setMessage("Logowanie...");
        progressDialog.show();

        if (!validate()) {
            onLoginFailed();
            return;
        }
        authenticateUser();
    }

    private void authenticateUser() {
        String login = _loginText.getText().toString();
        RetrofitProvider r = new RetrofitProvider();
        IUsersService s = r.GetUsersServices();
        val ret = s.GetUser(login);
        ret.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGetUser , throwable -> Log.e("LoginActivity", throwable.toString()));
    }

    private void onGetUser(User user) {
        String login = _loginText.getText().toString();
        String password = _passwordText.getText().toString();
        if(user.Id == -1 || !user.Password.equals(HashUtils.HashText(password))) {
            onLoginFailed();
            return;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("login", login);
        editor.commit();
        onLoginSuccess();
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), R.string.login_failed, Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;

        String login = _loginText.getText().toString();
        String password = _passwordText.getText().toString();

        if (login.isEmpty() ||  login.length() < 4) {
            _loginText.setError(getString(R.string.login_restriction));
            valid = false;
        } else {
            _loginText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError(getString(R.string.password_restriction));
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }

    public static class SignUpActivity extends BaseActivity {
        private static final String TAG = "SignupActivity";

        @Bind(R.id.input_name) EditText _nameText;
        @Bind(R.id.input_email) EditText _emailText;
        @Bind(R.id.input_password) EditText _passwordText;

        private ProgressDialog progressDialog;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.activity_sign_up);
            super.onCreate(savedInstanceState);
            progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
        }

        @OnClick(R.id.link_login)
        public void onLoginButtonClick() {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            finish();
        }

        @OnClick(R.id.btn_signup)
        public void onSignupButtonClick() {
            signup();
        }

        public void signup() {
            Log.d(TAG, "Signup");

            if (!validate()) {
                onSignupFailed();
                return;
            }
            progressDialog.setMessage("Tworzenie konta...");
            progressDialog.show();
            signUpUser();
        }

        private void signUpUser() {
            String name = _nameText.getText().toString();
            String email = _emailText.getText().toString();
            String password = _passwordText.getText().toString();

            String login = _nameText.getText().toString();
            RetrofitProvider r = new RetrofitProvider();
            IUsersService s = r.GetUsersServices();
            val ret = s.SaveUser(name, -1, email, HashUtils.HashText(password), "");
            ret.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onSaveUserTry , throwable -> Log.e("LoginActivity", throwable.toString()));
        }

        private void onSaveUserTry(Response ret) {
            if(ret == null || ret.getSuccess() == 0) {
                progressDialog.dismiss();
                return;
            }
            String name = _nameText.getText().toString();
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("login", name);
            editor.commit();

            onSignupSuccess();
            progressDialog.dismiss();
        }

        public void onSignupSuccess() {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }

        public void onSignupFailed() {
            Toast.makeText(getBaseContext(), R.string.registration_failed, Toast.LENGTH_LONG).show();
        }

        public boolean validate() {
            boolean valid = true;

            String name = _nameText.getText().toString();
            String email = _emailText.getText().toString();
            String password = _passwordText.getText().toString();

            if (name.isEmpty() || name.length() < 4) {
                _nameText.setError(getString(R.string.login_restriction));
                valid = false;
            } else {
                _nameText.setError(null);
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _emailText.setError(getString(R.string.wrong_email));
                valid = false;
            } else {
                _emailText.setError(null);
            }

            if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                _passwordText.setError(getString(R.string.password_restriction));
                valid = false;
            } else {
                _passwordText.setError(null);
            }

            return valid;
        }


    }
}
