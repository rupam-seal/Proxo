package com.app.pyxller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.model.MessageChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageChatAdapter extends RecyclerView.Adapter<MessageChatAdapter.UserViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<MessageChat> mChat;

    public MessageChatAdapter(Context mContext, List<MessageChat> mChat) {
        this.mContext = mContext;
        this.mChat = mChat;
    }

    private FirebaseUser firebaseUser;
    private String currentUserId;

    @NonNull
    @Override
    public MessageChatAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(mContext).inflate(R.layout.message_chat_right_item, parent, false);
        }else {
            view = LayoutInflater.from(mContext).inflate(R.layout.message_chat_left_item, parent, false);
        }
        return new MessageChatAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageChatAdapter.UserViewHolder holder, int position) {
        MessageChat chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView show_message;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.txt_msg);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        if (mChat.get(position).getSender().equals(currentUserId)){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }

    }
}

