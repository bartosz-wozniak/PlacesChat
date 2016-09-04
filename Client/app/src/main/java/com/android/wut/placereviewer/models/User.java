package com.android.wut.placereviewer.models;

import com.squareup.moshi.Json;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by Augi_ on 2016-06-06.
 */
@Data
public class User implements Serializable {
    @Json(name ="Id")
    public int Id;
    @Json(name ="Password")
    public String Password;
    @Json(name ="Email")
    public String Email;
    @Json(name ="Image")
    public String Image;
    @Json(name ="Login")
    public String Login;
}
