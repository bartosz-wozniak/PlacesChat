package com.android.wut.placereviewer.models;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

import lombok.Value;

/**
 * Created by soive on 09.06.2016.
 */
@Value
public class SinglePlaceResult implements Serializable {
    @Json(name ="error_message")
    public String Result;
    @Json(name ="result")
    public Place Place;
    @Json(name ="status")
    public String Status;
}
