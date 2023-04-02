package com.app.pyxller.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.app.pyxller.R;
import com.app.pyxller.model.Comments;
import com.app.pyxller.fragment.ProfileUserFragment;
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

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context mContext;
    private List<Comments> mComment;
    private String postid;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private String uid;
    private String strProfile, strUserName;

    public CommentsAdapter(Context mContext, List<Comments> mComment, String postid) {
        this.mContext = mContext;
        this.mComment = mComment;
        this.postid = postid;
    }

    @NonNull
    @Override
    public CommentsAdapter.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comments_item, parent, false);
        return new CommentsAdapter.CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentsViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        }

        Comments commentList = mComment.get(position);

        /*
        Getting Comment From Firebase Database
         */
        holder.txtComment.setText(commentList.getComment());

        /*
        Getting Comment From Firebase FireStore
         */
        getUserInfo(holder.imgProfile, holder.txtUsername, commentList.getPublisher());

        /*
        Comment items click listener
         */
        commentItemClick(holder.imgProfile, holder.txtUsername, holder.itemView, commentList.getPublisher(), commentList, uid, postid, commentList.getCommentid());

    }

    /**
     * Comment item user information from FirebaseFireStore
     * @param imgProfile
     * @param txtUsername
     * @param publisher
     */
    private void getUserInfo(ImageView imgProfile, TextView txtUsername, String publisher) {
        /*
        Get the profile, name, username from database
        Set info on PostActivity
         */
        if (publisher != null){
            firebaseFirestore.collection("Users").document(publisher).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            //strFullName = document.getString("fullname");
                            strUserName = document.getString("username");
                            strProfile = document.getString("profileimage");

                    /*
                    Set full name from database
                     */
                            //epName.setText(strFullName);

                    /*
                    Set user name from database
                     */
                            txtUsername.setText(strUserName);

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
    }

    /**
     *
     * Comments item(image_profile, username, itemView) onClick
     * @param imgProfile
     * @param txtUsername
     * @param itemView
     * @param publisher
     * @param commentLs
     * @param s
     * @param postid
     * @param commentUid
     */
    private void commentItemClick(ImageView imgProfile, TextView txtUsername, View itemView, String publisher, Comments commentLs,
                                  String s, String postid, String commentUid) {
        /*
        comment item image profile click
         */
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", publisher);
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileUserFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        /*
        comment item username click
         */
        txtUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", publisher);
                editor.apply();

                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileUserFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        /*
        comment itemView long press
         */
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popup = new PopupMenu(itemView.getContext(), v);
                popup.inflate(R.menu.comment_menu);

                final MenuItem delete = popup.getMenu().findItem(R.id.delete_comment);
                final MenuItem edit = popup.getMenu().findItem(R.id.edit_comment);
                final MenuItem hide = popup.getMenu().findItem(R.id.hide_comment);

                if (commentLs.getPublisher().equals(s)){
                    delete.setVisible(true);
                    edit.setVisible(true);
                    hide.setVisible(true);
                }else {
                    delete.setVisible(false);
                    edit.setVisible(false);
                    hide.setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){

                            case R.id.go_to_profile:
                                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                                editor.putString("profileid", publisher);
                                editor.apply();

                                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new ProfileUserFragment())
                                        .addToBackStack(null)
                                        .commit();
                                return true;

                            case R.id.edit_comment:
                                Toast.makeText(mContext, "edit", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.delete_comment:
                                DatabaseReference deleteReference = FirebaseDatabase.getInstance().getReference().child("Comments")
                                        .child(postid).child(commentUid);
                                deleteReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            snapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                return true;

                            case R.id.hide_comment:
                                Toast.makeText(mContext, "share", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.report_comment:
                                Toast.makeText(mContext, "report", Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popup.show();

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgProfile;
        public TextView txtUsername, txtComment;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.img_profile);
            txtUsername = itemView.findViewById(R.id.txt_username);
            txtComment = itemView.findViewById(R.id.txt_comment);

        }
    }
}
