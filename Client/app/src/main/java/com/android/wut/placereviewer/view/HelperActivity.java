package com.android.wut.placereviewer.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.wut.placereviewer.view.login.LoginActivity;
import com.android.wut.placereviewer.view.main.MainActivity;

public class HelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String login = sharedPref.getString("login", "");
        if (login == "")
        {
            startActivity(new Intent(this, LoginActivity.class));
        }
        else
        {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
