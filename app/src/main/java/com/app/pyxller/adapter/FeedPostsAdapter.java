package com.app.pyxller.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
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
import com.app.pyxller.utils.TimeAgo;
import com.app.pyxller.model.Posts;
import com.app.pyxller.fragment.ProfileUserFragment;
import com.app.pyxller.fragment.CommentsFragment;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileNotFoundException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import xyz.hanks.library.bang.SmallBangView;

import static android.content.Context.MODE_PRIVATE;

public class FeedPostsAdapter extends RecyclerView.Adapter<FeedPostsAdapter.PostsViewHolder> {

    public Context mContext;
    public List<Posts> mPosts;
    public FirebaseUser firebaseUser;

    public FeedPostsAdapter(Context mContext, List<Posts> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    private String uId;
    private Uri imageUri;
    long time;

    @NonNull
    @Override
    public FeedPostsAdapter.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.feed_posts_item, parent, false);

        return new FeedPostsAdapter.PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedPostsAdapter.PostsViewHolder holder, int i) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uId = firebaseUser.getUid();
        }

        final Posts posts = mPosts.get(i);

        Glide.with(mContext).load(posts.getPostImage()).into(holder.postImage);

        holder.txtDescriptionTitle.setText(posts.getDesc1());
        holder.txtDescription.setText(posts.getDesc2());

        try
        {
            time = Long.parseLong(posts.getTime());
        }
        catch (NumberFormatException e)
        {
            System.out.println(e);
        }

        String timeAgo = TimeAgo.getTimeAgo(time);

        holder.time.setText(timeAgo);


        if (posts.getPostid() != null) {
            isLiked(posts.getPostid(), holder.imgLike, holder.imgLikeContainer);
            numLikes(holder.numLikes, posts.getPostid());
            numComment(holder.numComment, posts.getPostid());
        }

        holder.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.imgLike.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(posts.getPostid()).child(firebaseUser.getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(posts.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.imgComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("postid", posts.getPostid());
                editor.putString("username", posts.getUsername());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CommentsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        holder.imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore postDb = FirebaseFirestore.getInstance();
                CollectionReference postCReference = postDb.collection("Posts");

                PopupMenu popup = new PopupMenu(holder.imgOptions.getContext(), v);
                popup.inflate(R.menu.post_menu);

                final MenuItem delete = popup.getMenu().findItem(R.id.delete_post);
                final MenuItem edit = popup.getMenu().findItem(R.id.edit_post);

                if (posts.getUid().equals(uId)) {
                    delete.setVisible(true);
                    edit.setVisible(true);
                } else {
                    delete.setVisible(false);
                    edit.setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.delete_post:
                                Toast.makeText(v.getContext(), "delete", Toast.LENGTH_SHORT).show();
                                if (posts.getUid().equals(uId)) {
                                    final String id = posts.getPostid();
                                    FirebaseDatabase.getInstance().getReference("Posts")
                                            .child(posts.getPostid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Toast.makeText(mContext, "ssss", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                                return true;

                            case R.id.edit_post:
                                Toast.makeText(v.getContext(), "edit", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.share_post:
                                ImageView content = holder.itemView.findViewById(R.id.h_postImage);
                                content.setDrawingCacheEnabled(true);
                                try {
                                    imageUri = Uri.parse(MediaStore.Images.Media.insertImage(mContext.getContentResolver(), String.valueOf(content), "title", "description"));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(v.getContext(), "share", Toast.LENGTH_SHORT).show();
                                Intent wpIntent = new Intent(Intent.ACTION_SEND);
                                wpIntent.setType("text/plain");
                                wpIntent.setPackage("com.whatsapp");
                                wpIntent.putExtra(Intent.EXTRA_TEXT, "Text you want to share");
                                wpIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                wpIntent.setType("image/*");
                                wpIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                try {
                                    mContext.startActivity(wpIntent);
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }

                                return true;

                            case R.id.copyLink_post:
                                Toast.makeText(v.getContext(), "copy", Toast.LENGTH_SHORT).show();
                                return true;

                            case R.id.report_post:
                                Toast.makeText(v.getContext(), "report", Toast.LENGTH_SHORT).show();
                                return true;

                            default:
                                return false;
                        }
                    }
                });

                popup.show();

            }
        });

        userInfo(holder.imgProfile, holder.txtName, holder.txtUsername, posts);

    }

    private void numLikes(TextView numLikes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numLikes.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void numComment(TextView numComment, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numComment.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked(String postid, ImageView imageView, SmallBangView imgLikeContainer) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_heart_red);
                    if (imgLikeContainer != null) {
                        imgLikeContainer.setSelected(true);
                        imgLikeContainer.likeAnimation();
                    }
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource((R.drawable.ic_heart));
                    if (imgLikeContainer != null) {
                        imgLikeContainer.setSelected(false);
                    }
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userInfo(CircleImageView imgProfile, TextView txtName, TextView txtUsername, Posts posts) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection("Users");

        /*
        Get the imgProfile, txtName, username from database
        Set info on PostActivity
         */
        reference.document(posts.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String strFullName = document.getString("fullname");
                        String strUserName = document.getString("username");
                        String strProfile = document.getString("profileimage");

                    /*
                    Set full txtName from database
                     */
                        txtName.setText(strFullName);

                    /*
                    Set user txtName from database
                     */
                        txtUsername.setText(strUserName);

                    /*
                    Set imgProfile from database
                     */
                        Glide.with(mContext)
                                .load(strProfile)
                                .into(imgProfile);

                    }
                }
            }
        });

        Glide.with(mContext).load(posts.getProfileimage()).into(imgProfile);

        imgProfile.setOnClickListener(v -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", posts.getUid());
            editor.apply();

            ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileUserFragment())
                    .addToBackStack(null)
                    .commit();
        });

        txtName.setOnClickListener(v -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", posts.getUid());
            editor.apply();

            ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileUserFragment())
                    .addToBackStack(null)
                    .commit();
        });

        txtUsername.setOnClickListener(v -> {
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", posts.getUid());
            editor.apply();

            ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileUserFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imgProfile;
        public TextView txtName, txtUsername, txtDescriptionTitle, txtDescription;
        public ImageView postImage, imgLike, imgComment, imgOptions;
        public SmallBangView imgLikeContainer;
        public TextView numLikes, numComment, time;

        public PostsViewHolder(@NonNull View v) {
            super(v);

            imgProfile = v.findViewById(R.id.img_profile);
            txtName = v.findViewById(R.id.txt_name);
            txtUsername = v.findViewById(R.id.txt_username);
            txtDescriptionTitle = v.findViewById(R.id.txt_description_title);
            txtDescription = v.findViewById(R.id.txt_description);
            postImage = v.findViewById(R.id.img_post);
            imgLike = v.findViewById(R.id.img_like);
            imgComment = v.findViewById(R.id.img_comment);
            imgOptions = v.findViewById(R.id.btn_options);
            imgLikeContainer = v.findViewById(R.id.img_like_container);
            numLikes = v.findViewById(R.id.txt_num_like);
            numComment = v.findViewById(R.id.txt_num_comment);
            time = v.findViewById(R.id.time);
        }
    }
}
