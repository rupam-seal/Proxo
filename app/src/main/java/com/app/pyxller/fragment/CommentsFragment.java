package com.app.pyxller.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.model.Comments;
import com.app.pyxller.adapter.CommentsAdapter;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class CommentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText edtComment;
    private CircleImageView imgProfile;
    private TextView btnPost;

    private String uId, strProfile;

    private FirebaseUser firebaseUser;
    private DatabaseReference uploadReference;
    private String postid;
    private String commentId, comment;

    private List<Comments> commentsList;
    private CommentsAdapter commentsAdapter;

    private ShimmerFrameLayout shimmerFrameLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.comments_fragment, container, false);

        recyclerView = view.findViewById(R.id.comment_act_recycler_view);
        edtComment = view.findViewById(R.id.edt_comment);
        imgProfile = view.findViewById(R.id.img_profile);
        btnPost = view.findViewById(R.id.btn_post);

        // Getting current user id
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uId = firebaseUser.getUid();
        }

        // get current post id
        // from postAdapter
        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        postid = prefs.getString("postid", "none");

        // Start shimmer on starting
        // if recyclerview not loaded view show shimmer effect
        // by using this we will not see any black screen
        // when recyclerview not loaded
        shimmerFrameLayout = view.findViewById(R.id.shimmer_comment);
        shimmerFrameLayout.startShimmer();

        /*
        recycler view for showing all the user comment
         */
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        commentsList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getContext(), commentsList, postid);
        recyclerView.setAdapter(commentsAdapter);


        onClick();
        customizeToolbar(view);
        // getting profile image from FireStore
        getImage();
        // show comments
        showComments();

        return view;
    }

    private void onClick() {
        // post comment on a current post
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtComment.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "You can't send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                    postComment();
                }
            }
        });
    }

    /**
     * set title of toolbar
     * back press
     *
     * @param view
     */
    private void customizeToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Comments");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * getting image profile from FireStore
     * showing image on lefOf editText circleImageView
     */
    private void getImage() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection("Users");

        /*
        Get the profile, name, username from database
        Set info on PostActivity
         */
        reference.document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        strProfile = document.getString("profileimage");
                    /*
                    Set profile from database
                     */
                        Glide.with(getContext())
                                .load(strProfile)
                                .into(imgProfile);

                    }
                }
            }
        });
    }

    /**
     * Showing all the comments on a post on commentsActivity
     */
    private void showComments() {
        DatabaseReference showReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        showReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comments comments = snapshot.getValue(Comments.class);
                    commentsList.add(comments);
                    shimmerFrameLayout.setVisibility(View.INVISIBLE);
                    shimmerFrameLayout.stopShimmer();
                    recyclerView.setVisibility(View.VISIBLE);
                }

                if (commentsList.isEmpty()) {
                    shimmerFrameLayout.setVisibility(View.INVISIBLE);
                    shimmerFrameLayout.stopShimmer();
                }
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Comment activity upload button click
     * click to upload comment to a current post
     */
    private void postComment() {
        /*
        upload comment to firebase database
         */
        uploadReference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        /*
        unique comment id
         */
        commentId = uploadReference.push().getKey();

        /*
        get user input comment text
         */
        comment = edtComment.getText().toString();

        /*
        put value to firebase database
         */
        HashMap<String, Object> map = new HashMap<>();
        map.put("comment", comment);
        map.put("publisher", uId);
        map.put("commentid", commentId);

        uploadReference.child(commentId).setValue(map);

        /*
        remove text from editText after upload comment to database
         */
        edtComment.setText("");
    }

}