package com.app.pyxller.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.activity.EditProfileActivity;
import com.app.pyxller.adapter.ProfilePostsAdapter;
import com.app.pyxller.model.Posts;
import com.app.pyxller.activity.start.StartupActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {
    public static final int TAB_POSITION = 4;

    private Button btnLogout;
    private ImageView coverImage, btnOptions;
    private TextView name, username, bio;
    private RecyclerView postsRecyclerView;
    private ProfilePostsAdapter profilePostsAdapter;
    private List<Posts> posts;
    private AppCompatButton btnEditProfile;

    private TextView txtFollow, txtFollowing;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore usersDb, postsDb;
    private CollectionReference userReference, postsReference;

    //region newInstance
    public static Fragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

//        btnLogout = view.findViewById(R.id.btnLogOut);
        coverImage = view.findViewById(R.id.coverImg);
        name = view.findViewById(R.id.p_name);
        btnOptions = view.findViewById(R.id.btn_options);
        username = view.findViewById(R.id.p_username);
        bio = view.findViewById(R.id.p_bio);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        postsRecyclerView = view.findViewById(R.id.p_recyclerview);
        txtFollow = view.findViewById(R.id.p_followTxt);
        txtFollowing = view.findViewById(R.id.p_followingTxt);

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileIntent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(editProfileIntent);
            }
        });

        /*
        Button Logout
         */
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Logout from current account
                 */
                logout();
            }
        });

        /*
        Get Current User From Database
         */
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        /*
        Profile Posts
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);
        posts = new ArrayList<>();
        profilePostsAdapter = new ProfilePostsAdapter(getContext(), posts);
        postsRecyclerView.setAdapter(profilePostsAdapter);

        /*
        User Info on Profile Fragment
        Profile Picture
        Name
        UserName
        Bio
         */
        usersDb = FirebaseFirestore.getInstance();

        usersDb.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String strFullName = document.getString("fullname");
                        String strUserName = document.getString("username");
                        String strProfile = document.getString("profileimage");
                        String strBio = document.getString("bio");

                        if (strBio == null) {
                            bio.setVisibility(View.GONE);
                        } else {
                            bio.setVisibility(View.VISIBLE);
                        }

                    /*
                    Set full name from database
                     */
                        name.setText(strFullName);

                    /*
                    Set user name from database
                     */
                        username.setText(strUserName);

                    /*
                    Set user bio from database
                     */
                        bio.setText(strBio);

                    /*
                    Set profile from database
                     */
                        if (getContext() != null) {
                            Glide.with(getContext())
                                    .load(strProfile)
                                    .into(coverImage);
                        }

                    }
                }
            }
        });

        /*
        Profile Fragment user all posts showing on RecyclerView
         */
        profilePosts();

        getFollowers();

        return view;
    }

    private void getFollowers() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(currentUserId).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtFollow.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow").child(currentUserId).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtFollowing.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * LogOut From Current Account
     */
    private void logout() {
        mAuth.signOut();
        sendUserToStartActivity();
    }


    /**
     * User Posts on Profile Fragment
     */
    private void profilePosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
//        Toast.makeText(getContext(), "reference"+ reference, Toast.LENGTH_SHORT).show();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Posts post = snapshot.getValue(Posts.class);
                        if (post.getUid().equals(currentUserId)){
                            posts.add(post);
                        }
                    }
                    Collections.reverse(posts);
                    profilePostsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Send User To Start Activity After LogOut
     */
    private void sendUserToStartActivity() {
        FirebaseAuth.getInstance().signOut();
        Intent startIntent = new Intent(getActivity(), StartupActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        getActivity().finish();
        getActivity().getIntent().removeExtra("");
    }

}