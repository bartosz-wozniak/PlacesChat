package com.android.wut.placereviewer.models;

import android.support.annotation.XmlRes;

import com.squareup.moshi.Json;

import org.joda.time.DateTime;

import java.io.Serializable;

import lombok.Value;

/**
 * Created by wozniakb on 28.04.2016.
 */
@Value
public class Comment  implements Serializable {
    @Json(name ="Comment")
    public String Comment;
    @Json(name ="Date")
    public String Date;
    @Json(name ="Id")
    public int Id;
    @Json(name ="Login")
    public String Login;
    @Json(name ="PlaceId")
    public String PlaceId;
}
