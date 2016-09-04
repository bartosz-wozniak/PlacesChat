package com.android.wut.placereviewer.models;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

import lombok.Value;

/**
 * Created by wozniakb on 28.04.2016.
 */
@Value
public class CommentResult implements Serializable {
    @Json(name ="Comments")
    public List<Comment> Comments;
}
