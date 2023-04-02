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
import com.app.pyxller.adapter.MessageIndividualAdapter;
import com.app.pyxller.model.Message;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessageIndividualFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Users> mUser;
    private String currentUserId;
    private FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    private List<Message> userList;

    private ShimmerFrameLayout shimmer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_individual_fragment, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();

        shimmer = view.findViewById(R.id.shimmer);
        shimmer.startShimmer();

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUserId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    userList.add(message);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void chatList() {
        mUser = new ArrayList<>();
        FirebaseFirestore userDb = FirebaseFirestore.getInstance();
        userDb.collection("Users").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        mUser.clear();
                        for (DocumentSnapshot snapshot : list) {
                            Users users = snapshot.toObject(Users.class);
                            for (Message message : userList) {
                                if (users.getUid().equals(message.getId())) {
                                    mUser.add(users);
                                    shimmer.setVisibility(View.INVISIBLE);
                                    shimmer.stopShimmer();
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                            if (userList.isEmpty()) {
                                shimmer.setVisibility(View.INVISIBLE);
                                shimmer.stopShimmer();
                            }
                        }
                        MessageIndividualAdapter messageIndividualAdapter = new MessageIndividualAdapter(getContext(), mUser);
                        recyclerView.setAdapter(messageIndividualAdapter);
                    }
                });
    }

}