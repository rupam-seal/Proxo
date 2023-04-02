package com.app.pyxller.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.fragment.MessageChatFragment;
import com.app.pyxller.model.Users;
import com.app.pyxller.model.MessageChat;
import com.app.pyxller.fragment.ProfileUserFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class MessageIndividualAdapter extends RecyclerView.Adapter<MessageIndividualAdapter.ChatViewHolder> {

    private Context mContext;
    private List<Users> mUser;

    public MessageIndividualAdapter(Context mContext, List<Users> mUser) {
        this.mContext = mContext;
        this.mUser = mUser;
    }

    private FirebaseUser firebaseUser;
    private String currentUserId;
    private String str_lastMsg;

    @NonNull
    @Override
    public MessageIndividualAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.message_individual_item, parent, false);

        return new MessageIndividualAdapter.ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageIndividualAdapter.ChatViewHolder chatViewHolder, int i) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        Users users = mUser.get(i);

        lastMessage(users.getUid(), chatViewHolder.lastMsg);

        userInfo(users, chatViewHolder);
        usersItemClick(users, chatViewHolder.imgProfile, chatViewHolder.txtName, chatViewHolder.itemView);
    }

    private void usersItemClick(Users users, CircleImageView imgProfile, TextView txtName, View itemView) {
        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfileFrag(users.getUid(), itemView);
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfileFrag(users.getUid(), itemView);
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatFrag(users.getUid());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgProfile;
        TextView txtName, lastMsg;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_profile);
            txtName = itemView.findViewById(R.id.txt_name);
            lastMsg = itemView.findViewById(R.id.txt_last_msg);
        }
    }

    private void userInfo(Users users, ChatViewHolder chatViewHolder) {
        chatViewHolder.txtName.setText(users.getFullname());
        Glide.with(mContext).load(users.getProfileimage()).into(chatViewHolder.imgProfile);
    }

    private void goToChatFrag(String uId) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("uid", uId);
        editor.apply();

        ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MessageChatFragment())
                .addToBackStack(null)
                .commit();
    }

    private void goToProfileFrag(String uid, View itemView) {

        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("profileid", uid);
        editor.apply();

        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
        }

        ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                new ProfileUserFragment()).commit();


    }

    private void lastMessage(String uId, TextView lastMsg){
        str_lastMsg = "default";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    MessageChat chat = snapshot.getValue(MessageChat.class);
                    if (chat.getReceiver().equals(currentUserId) && chat.getSender().equals(uId) ||
                            chat.getReceiver().equals(uId) && chat.getSender().equals(currentUserId)){
                        str_lastMsg = chat.getMessage();
                    }
                }

                switch (str_lastMsg){
                    case "default":
                        lastMsg.setText("No Message");
                        break;

                    default:
                        lastMsg.setText(str_lastMsg);
                        break;
                }
                str_lastMsg = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
