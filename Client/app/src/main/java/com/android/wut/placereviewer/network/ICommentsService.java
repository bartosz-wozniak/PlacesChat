package com.android.wut.placereviewer.network;

import com.android.wut.placereviewer.models.Comment;
import com.android.wut.placereviewer.models.CommentResult;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by wozniakb on 28.04.2016.
 */
public interface ICommentsService {
    @GET("GetComments/json")
    Observable<CommentResult> GetComments(@Query("placeId") String placeId);

    @GET("SaveComment/json")
    Observable<CommentResult> AddComment(
              @Query("comment") String comment
            , @Query("id") int id
            , @Query("login") String login
            , @Query("placeId") String placeId);

    @GET("GetNewestComment")
    Observable<Comment> GetLastComment(@Query("placeId") String placeId);
}