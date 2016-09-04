package com.android.wut.placereviewer.models;

import com.squareup.moshi.Json;

import lombok.Value;

/**
 * Created by Augi_ on 2016-06-06.
 */
@Value
public class Response {
    @Json(name="Success")
    private int Success;
}
