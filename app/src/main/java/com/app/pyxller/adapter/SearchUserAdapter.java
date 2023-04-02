  package com.app.pyxller.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.model.Users;
import com.app.pyxller.activity.MainActivity;
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

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder> {

    private List<Users> userList;
    private Context mContext;
    private boolean isFragment;

    public SearchUserAdapter(List<Users> userList, Context mContext, boolean isFragment) {
        this.userList = userList;
        this.mContext = mContext;
        this.isFragment = isFragment;
    }

    private FirebaseUser firebaseUser;
    private String currentUserId;

    @NonNull
    @Override
    public SearchUserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.search_item, parent, false);

        return new SearchUserAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserAdapter.UserViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        Users users = userList.get(position);

        holder.btnFollow.setVisibility(View.VISIBLE);
        isFollowing(users, holder.btnFollow);

        userInfo(users, holder.txtName, holder.txtUsername, holder.imgProfile);

        if (users.getUid().equals(currentUserId)){
            holder.btnFollow.setVisibility(View.GONE);
        }

        usersItemClick(users, holder.txtName, holder.txtUsername, holder.imgProfile, holder.itemView, holder.btnFollow);
    }

    private void usersItemClick(Users users, TextView txtName, TextView txtUsername, CircleImageView imgProfile, View itemView, Button btnFollow) {
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

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfileFrag(users.getUid(), itemView);
            }
        });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnFollow.getText().toString().equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUserId)
                            .child("following").child(users.getUid()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(users.getUid())
                            .child("followers").child(currentUserId).setValue(true);

                    //addNotification(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUserId)
                            .child("following").child(users.getUid()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(users.getUid())
                            .child("followers").child(currentUserId).removeValue();
                }
            }
        });
    }

    private void isFollowing(Users users, Button btnFollow) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(currentUserId).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(String.valueOf(users.getUid())).exists()){
                    btnFollow.setText("following");
                }else {
                    btnFollow.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void goToProfileFrag(String uid, View itemView) {

        if (isFragment){
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", uid);
            editor.apply();

            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isAcceptingText()){
                inputMethodManager.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
            }

            ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                    new ProfileUserFragment()).commit();
        }else {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("publisherid", uid);
            mContext.startActivity(intent);
        }

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
        private Button btnFollow;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.img_profile);
            txtName = itemView.findViewById(R.id.txt_name);
            txtUsername = itemView.findViewById(R.id.txt_username);
            btnFollow = itemView.findViewById(R.id.btn_follow);

        }
    }
}
