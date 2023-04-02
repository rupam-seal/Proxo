package com.app.pyxller.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.pyxller.R;
import com.app.pyxller.model.Users;
import com.app.pyxller.adapter.MessageConnectionsAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

public class MessageConnectionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Users> usersList;
    private MessageConnectionsAdapter userAdapter;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference userReference;
    private List<String> followingList;

    private ShimmerFrameLayout shimmer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.message_connections_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerview);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<>();
        userAdapter = new MessageConnectionsAdapter(usersList, getContext());
        recyclerView.setAdapter(userAdapter);

        shimmer = view.findViewById(R.id.shimmer);
        shimmer.startShimmer();

        /*
        Get Current User From Database
         */
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

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

                /*
                showing all posts to home fragment
                */
                readUsers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readUsers() {

        //DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(currentUserId).child("following");
        firebaseFirestore = FirebaseFirestore.getInstance();
        userReference = firebaseFirestore.collection("Users");

        userReference.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        usersList.clear();
                        for (DocumentSnapshot snapshot : list) {
                            Users model = snapshot.toObject(Users.class);
                            for (String id : followingList){
                                if (model.getUid().equals(id)){
                                    usersList.add(model);
                                    shimmer.setVisibility(View.INVISIBLE);
                                    shimmer.stopShimmer();
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                            if (followingList.isEmpty()){
                                shimmer.setVisibility(View.INVISIBLE);
                                shimmer.stopShimmer();
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                });

    }
}