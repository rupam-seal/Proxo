package com.app.pyxller.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.app.pyxller.R;
import com.app.pyxller.adapter.FeedPostsAdapter;
import com.app.pyxller.model.Posts;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FeedHomeFragment extends Fragment {
    private Context mContext;

    // home post recyclerView
    private RecyclerView recyclerView;
    // adapter for recyclerView post
    private FeedPostsAdapter feedPostsAdapter;
    private RelativeLayout emptyScreen;
    // all posts list
    private ArrayList<Posts> posts;
    // linear swipe posts
    private LinearLayoutManager layoutManager;

    // FireStore Database
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;

    private List<String> followingList;

    private Parcelable recyclerViewState;
    DatabaseReference databaseReference;
    private ShimmerFrameLayout shimmer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_home_fragment, container, false);

        /*
        getContext
         */
        mContext = getContext();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        /*
        Get Current User From Database
         */
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        shimmer = view.findViewById(R.id.shimmer);
        shimmer.startShimmer();

        emptyScreen = view.findViewById(R.id.empty_screen);

        /*
        Home posts recycler view
         */
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);


        if (recyclerViewState != null) {
            recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }

//        SnapHelper snapHelper = new PagerSnapHelper();
//        snapHelper.attachToRecyclerView(recyclerView);

        /*
        Layout manager for home posts
         */
        layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        /*
        posts list
         */
        posts = new ArrayList<>();

        feedPostsAdapter = new FeedPostsAdapter(mContext, posts);

        recyclerView.setAdapter(feedPostsAdapter);

        checkFollowing();

        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(currentUserId)
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                followingList.add(currentUserId);

                /*
                showing all posts to home fragment
                */
                showingPosts();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * showing all posts to home fragment
     */
    private void showingPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                posts.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Posts post = snapshot.getValue(Posts.class);
                        for (String id : followingList){
                            if (post.getUid().equals(id)){
                                posts.add(post);
                                shimmer.setVisibility(View.INVISIBLE);
                                shimmer.stopShimmer();
                                recyclerView.setVisibility(View.VISIBLE);
                            } else {
                                shimmer.setVisibility(View.INVISIBLE);
                                shimmer.stopShimmer();
                            }
                        }
                    }
                }else {
                    shimmer.setVisibility(View.INVISIBLE);
                    shimmer.stopShimmer();
                    emptyScreen.setVisibility(View.VISIBLE);
                }

                feedPostsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}