package com.app.pyxller.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
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

import static android.content.Context.MODE_PRIVATE;

public class ProfileUserFragment extends Fragment {

    private ImageView coverImage, chat;
    private TextView name, username, bio;
    private RecyclerView postsRecyclerView;
    private ProfilePostsAdapter profilePostsAdapter;
    private List<Posts> posts;
    private ImageButton options;
    private Context mContext;
    private AppCompatButton followBtn;
    private TextView txtFollow, txtFollowing;

    private FirebaseAuth mAuth;
    private String currentUserId, uId, u;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore usersDb, postsDb;
    private CollectionReference userReference, postsReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_user_fragment, container, false);

        coverImage = view.findViewById(R.id.cp_coverImg);
        name = view.findViewById(R.id.cp_name);
        username = view.findViewById(R.id.cp_username);
        bio = view.findViewById(R.id.cp_bio);
        postsRecyclerView = view.findViewById(R.id.cp_recyclerview);
        chat = view.findViewById(R.id.cp_chat);
        followBtn = view.findViewById(R.id.cp_follow_btn);
        txtFollow = view.findViewById(R.id.cp_followTxt);
        txtFollowing = view.findViewById(R.id.cp_followingTxt);

        mContext = view.getContext();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        uId = prefs.getString("profileid", "none");

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

        getUserInfo();

        /*
        Profile Fragment user all posts showing on RecyclerView
         */
        profilePosts();

        /*
        click to go to chat fragment
         */
        if (currentUserId.equals(uId)){
            chat.setVisibility(View.GONE);
        }else {
            chat.setVisibility(View.VISIBLE);
        }

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("uid", uId);
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MessageChatFragment())
                        .addToBackStack(null)
                        .commit();

            }
        });

        /*
        Follow UnFollow
         */
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fBtn = followBtn.getText().toString();

                if (fBtn.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUserId)
                            .child("following").child(uId).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(uId)
                            .child("followers").child(currentUserId).setValue(true);
                }else if (fBtn.equals("following")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUserId)
                            .child("following").child(uId).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(uId)
                            .child("followers").child(currentUserId).removeValue();
                }
            }
        });

        if (uId.equals(currentUserId)){
            followBtn.setVisibility(View.GONE);
        }else {
            /*
            follow checking
             */
            checkFollow();
        }

        getFollowers();

        getActivity().getIntent().removeExtra("");

        return view;
    }

    private void getFollowers() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(uId).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtFollow.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Follow").child(uId).child("following");
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

    private void checkFollow() {
        DatabaseReference f_reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(currentUserId).child("following");

        f_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(uId).exists()){
                    followBtn.setText("following");
                }else {
                    followBtn.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo() {

        usersDb.collection("Users").document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                        Glide.with(getContext())
                                .load(strProfile)
                                .into(coverImage);

                    }
                }
            }
        });

    }


    /**
     * User Posts on Profile Fragment
     */
    private void profilePosts() {

        postsDb = FirebaseFirestore.getInstance();
        postsReference = postsDb.collection("Posts");

        postsReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        posts.clear();
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : list) {
                            Posts model = snapshot.toObject(Posts.class);

                            if (model.getUid().equals(uId)) {
                                posts.add(model);
                            }

                        }
                        Collections.reverse(posts);
                        profilePostsAdapter.notifyDataSetChanged();
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