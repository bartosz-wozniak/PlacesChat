package com.android.wut.placereviewer.models;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Setter;
import lombok.Value;

/**
 * Created by soive on 27.04.2016.
 */
@Data
public class Place implements Serializable {
    @Json(name ="place_id")
    public String Id2;
    @Json(name ="id")
    public String Id;
    @Json(name ="name")
    public String Name;
    @Json(name ="geometry")
    public Geometry geometry;
    @Json(name ="photos")
    public List<PlacePhoto> Photos;
    @Json(name="vicinity")
    public String Address;
    @Json(name="types")
    public List<String> Types;

    public PlaceType PlaceType;

    @Value
    public static class PlacePhoto implements Serializable {
        @Json(name="height")
        public int Height;
        @Json(name="width")
        public int Width;
        @Json(name="photo_reference")
        public String PhotoRef;
    }

    @Value
    public static class Geometry implements Serializable {
        @Json(name ="location")
        public Location location;
        @Value
        public static class Location implements Serializable {
            @Json(name ="lat")
            public double lat;
            @Json(name ="lng")
            public double lng;
        }
    }
}
