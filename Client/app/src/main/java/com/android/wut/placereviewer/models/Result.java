package com.android.wut.placereviewer.models;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Value;

/**
 * Created by soive on 27.04.2016.
 */
@Data
public class Result implements Serializable {
    @Json(name ="next_page_token")
    public String NextPageToken;
    @Json(name ="error_message")
    public String Result;
    @Json(name ="results")
    public List<Place> Results;
    @Json(name ="status")
    public String Status;
}
