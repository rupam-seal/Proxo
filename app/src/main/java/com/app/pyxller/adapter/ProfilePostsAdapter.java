package com.app.pyxller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.model.Posts;
import com.bumptech.glide.Glide;

import java.util.List;

public class ProfilePostsAdapter extends RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder> {

    public Context mContext;
    public List<Posts> mPosts;

    public ProfilePostsAdapter(Context mContext, List<Posts> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ProfilePostsAdapter.ProfilePostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_posts_item, parent, false);

        return new ProfilePostsAdapter.ProfilePostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfilePostsAdapter.ProfilePostsViewHolder holder, int i) {
        Posts model = mPosts.get(i);

        Glide.with(mContext).load(model.getPostImage()).into(holder.postImage);

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ProfilePostsViewHolder extends RecyclerView.ViewHolder {

        private ImageView postImage;

        public ProfilePostsViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.p_postsItem);
        }
    }
}
