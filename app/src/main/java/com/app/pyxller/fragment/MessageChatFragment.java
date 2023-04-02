package com.app.pyxller.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.adapter.MessageChatAdapter;
import com.app.pyxller.model.MessageChat;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class MessageChatFragment extends Fragment {

    private CircleImageView imgProfile, imgUserProfile;
    private TextView txtName, txtUsername;
    private ImageView sendBtn;
    private EditText edtChat;
    private String uId, currentUserId;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private Context mContext;

    String strProfile;

    MessageChatAdapter messageChatAdapter;
    List<MessageChat> mChat;

    DatabaseReference reference;

    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_chat_fragment, container, false);

        imgProfile = view.findViewById(R.id.img_profile);
        imgUserProfile = view.findViewById(R.id.img_user_profile);
        txtName = view.findViewById(R.id.txt_name);
        txtUsername = view.findViewById(R.id.txt_username);
        sendBtn = view.findViewById(R.id.btn_send);
        edtChat = view.findViewById(R.id.edt_chat);

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        reference = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        uId = prefs.getString("uid", "none");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        mContext = getContext();
        db = FirebaseFirestore.getInstance();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = edtChat.getText().toString();

                if (!msg.equals((""))) {
                    sendMessage(currentUserId, uId, msg);
                    edtChat.setText("");
                } else {
                    Toast.makeText(mContext, "you can't send empty message!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*
        get all user information from Firebase FireStore
         */
        getUserInfo(imgProfile, txtName, txtUsername, imgUserProfile);

        return view;
    }

    private void sendMessage(String sender, String receiver, String message) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("chats").push().setValue(hashMap);

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(currentUserId)
                .child(uId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(uId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(CircleImageView imgProfile, TextView txtName, TextView txtUsername, CircleImageView imgUserProfile) {
        db.collection("Users").document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String strFullName = document.getString("fullname");
                        String strUserName = document.getString("username");
                        strProfile = document.getString("profileimage");

                    /*
                    Set full name from database
                     */
                        txtName.setText(strFullName);

                    /*
                    Set user name from database
                     */
                        txtUsername.setText(strUserName);

                    /*
                    Set profile from database
                     */
                        Glide.with(mContext)
                                .load(strProfile)
                                .into(imgUserProfile);

                        readChat(currentUserId, uId);

                    }
                }
            }
        });

        db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String strProfile = document.getString("profileimage");

                        /*
                        Set profile from database
                        */
                        Glide.with(mContext)
                                .load(strProfile)
                                .into(imgProfile);

                    }
                }
            }
        });

    }

    private void readChat(String myId, String userId) {
        mChat = new ArrayList<>();

        DatabaseReference rf = FirebaseDatabase.getInstance().getReference("chats");
        rf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageChat model = snapshot.getValue(MessageChat.class);
                    if (model.getReceiver().equals(myId) && model.getSender().equals(userId) ||
                            model.getReceiver().equals(userId) && model.getSender().equals(myId)) {
                        mChat.add(model);
                    }
                    messageChatAdapter = new MessageChatAdapter(getContext(), mChat);
                    recyclerView.setAdapter(messageChatAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}