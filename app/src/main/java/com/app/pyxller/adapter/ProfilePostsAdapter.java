package com.app.pyxller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.model.Posts;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        if (model.getPostid() != null){
            numLikes(holder.profile_likes_num, model.getPostid());
        }

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ProfilePostsViewHolder extends RecyclerView.ViewHolder {

        private ImageView postImage;
        private TextView profile_likes_num;

        public ProfilePostsViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.p_postsItem);
            profile_likes_num = itemView.findViewById(R.id.profile_likes_num);
        }
    }

    private void numLikes(TextView numLikes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numLikes.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
