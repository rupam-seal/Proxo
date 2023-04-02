package com.app.pyxller.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.fragment.MessageChatFragment;
import com.app.pyxller.model.Users;
import com.app.pyxller.fragment.ProfileUserFragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class MessageConnectionsAdapter extends RecyclerView.Adapter<MessageConnectionsAdapter.UserViewHolder> {

    private List<Users> userList;
    private Context mContext;

    public MessageConnectionsAdapter(List<Users> userList, Context mContext) {
        this.userList = userList;
        this.mContext = mContext;
    }

    private FirebaseUser firebaseUser;
    private String currentUserId;

    @NonNull
    @Override
    public MessageConnectionsAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.message_connections_item, parent, false);

        return new MessageConnectionsAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageConnectionsAdapter.UserViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        Users users = userList.get(position);
        userInfo(users, holder.txtName, holder.txtUsername, holder.imgProfile);

        usersItemClick(users, holder.txtName, holder.txtUsername, holder.imgProfile, holder.itemView, holder.btnSend);
    }

    private void usersItemClick(Users users, TextView txtName, TextView txtUsername, CircleImageView imgProfile, View itemView, Button btnSend) {
        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfileFrag(users.getUid(), itemView);
            }
        });

        txtUsername.setOnClickListener(new View.OnClickListener() {
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

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatFrag(users.getUid());
            }
        });

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

    private void userInfo(Users users, TextView txtName, TextView txtUsername, CircleImageView imgProfile) {

        txtName.setText(users.getFullname());
        txtUsername.setText(users.getUsername());

        Glide.with(mContext).load(users.getProfileimage()).into(imgProfile);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imgProfile;
        private TextView txtName, txtUsername;
        private AppCompatButton btnSend;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.img_profile);
            txtName = itemView.findViewById(R.id.txt_name);
            txtUsername = itemView.findViewById(R.id.txt_username);
            btnSend = itemView.findViewById(R.id.btn_send);

        }
    }
}
