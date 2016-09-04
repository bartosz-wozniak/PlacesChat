package com.android.wut.placereviewer.view.list;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.wut.placereviewer.R;
import com.android.wut.placereviewer.models.Comment;
import com.android.wut.placereviewer.network.IUsersService;
import com.android.wut.placereviewer.network.RetrofitProvider;
import com.android.wut.placereviewer.utils.BitmapUtils;


import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import lombok.val;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by soive on 28.04.2016.
 */
public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CommentListViewHolder> {

    private List<Comment> comments;

    public CommentListAdapter() {
        comments = Collections.emptyList();
    }

    @Override
    public CommentListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_comment_list, parent, false);
        return new CommentListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CommentListViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setCommentList(List<Comment> comments) {
        this.comments = comments;
    }

    public static class CommentListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.comment_item_name)
        TextView commentName;
        @Bind(R.id.comment_user_name)
        TextView userName;
        @Bind(R.id.comment_item_date)
        TextView commentDate;

        @Bind(R.id.comment_user_image)
        CircleImageView commentUserImage;

        public CommentListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(Comment comment) {
            commentName.setText(comment.getComment());
            userName.setText(comment.getLogin());
            commentDate.setText(comment.getDate());

            RetrofitProvider r = new RetrofitProvider();
            IUsersService s = r.GetUsersServices();
            val ret = s.GetUser(comment.getLogin());
            ret.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(u -> {
                                String imageStr = u.getImage();
                                if(imageStr != null && !imageStr.isEmpty())
                                    commentUserImage.setImageBitmap(BitmapUtils.stringToBitMap(imageStr));
                            },
                            throwable -> Log.e("CommentListAdapter", throwable.toString()));
        }

        @Override
        public void onClick(View v) {

        }

    }


}
