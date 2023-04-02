package com.app.pyxller.activity;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostActivity extends AppCompatActivity {

    //Toolbar for back button press and Title
    private Toolbar mToolbar;

    //Button for select image from gallery
    private ImageView btnCamera, btnGallery1, btnGallery2;

    //Button for uploading the post
    private TextView btnPost;

    //ImageView For view the image from gallery
    private ImageView imgPost;

    //ProgressDialog for showing time of uploading
    private ProgressDialog pd;

    //EditText for post title and description
    private EditText edtDesc1, edtDesc2;

    //CircleImageView for user profile image in post activity
    private CircleImageView profileImage;

    //TextView for user Full Name and username
    private TextView fullName, userName;

    //Strings
    private String uId, saveCurrentDate, saveCurrentTime, postRandomName;
    private String strFullName, strUserName, strProfile, strDesc1, strDesc2;
    private String downloadUrl;

    //Uri indicates where the image will be picked from
    private Uri filePath;

    //Request code
    private final int PICK_IMAGE_REQUEST = 22;

    //Instance for firebase users
    FirebaseUser firebaseUser;

    //Instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    //Instance for firebase database reference
    //User Reference
    //Post Reference
    FirebaseFirestore db, dbPosts;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        /*
        Initialise Values
         */
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery1 = findViewById(R.id.btn_gallery1);
        btnGallery2 = findViewById(R.id.btn_gallery2);
        btnPost = findViewById(R.id.btn_post);
        imgPost = findViewById(R.id.img_post);
        edtDesc1 = findViewById(R.id.edt_description1);
        edtDesc2 = findViewById(R.id.edt_description2);
        profileImage = findViewById(R.id.img_profile);
        fullName = findViewById(R.id.txt_name);
        userName = findViewById(R.id.txt_username);

        /*
        Initialize and customise toolbar
         */
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text_900));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Create Post");
        }

        /*
        Initialise progress dialog
         */
        pd = new ProgressDialog(this);

        /*
        Getting current user id
         */
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uId = firebaseUser.getUid();
        }

        /*
        Get the firebase storage reference
         */
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        /*
        Get the firebase database reference
         */
        db = FirebaseFirestore.getInstance();
        dbPosts = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");


        /*
        On Pressing btnSelect selectImage() method is called
         */
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnGallery1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnGallery2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        /*
        On Pressing btnUpload uploadImage() method is called
         */
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });

        /*
        User Info on Profile Fragment
        Profile Picture
        Name
        UserName
        Bio
         */
        db = FirebaseFirestore.getInstance();

        db.collection("Users").document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String strFullName = document.getString("fullname");
                        String strUserName = document.getString("username");
                        String strProfile = document.getString("profileimage");

                    /*
                    Set full name from database
                     */
                        fullName.setText(strFullName);

                    /*
                    Set user name from database
                     */
                        userName.setText(strUserName);


                    /*
                    Set profile from database
                     */
                        if (getContext() != null) {
                            Glide.with(PostActivity.this)
                                    .load(strProfile)
                                    .into(profileImage);
                        }

                    }
                }
            }
        });


    }


    /**
     * OnOptionItemSelected listener
     * Toolbar back button pressed
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Select image from gallery
     */
    private void selectImage() {
        /*
        Defining Implicit Intent To Mobile Gallery
         */
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select image from here"),
                PICK_IMAGE_REQUEST);
    }

    /**
     * Override OnActivityResult Method
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
        Checking request code and result code
        if request code is PICK_IMAGE_REQUEST and
        result code is RESULT_OK
        then set image in the image view
         */
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            /*
            Get the uri of the data
             */
            filePath = data.getData();
            try {
                /*
                Setting image into imageView using Bitmap
                 */
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);

                imgPost.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Validate post information
     */
    private void validatePostInfo() {
        strDesc1 = edtDesc1.getText().toString();
        strDesc2 = edtDesc2.getText().toString();

        if (filePath == null) {
            /*
            Toast message show if post image is not selected
             */
            Toast.makeText(this, "please select post image", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(strDesc1)) {
            /*
            Toast message show if description is empty
             */
            Toast.makeText(this, "please say something about your image", Toast.LENGTH_SHORT).show();
        } else {
            /*
            Code for showing pd while uploading
             */
            pd.setTitle("Uploading...");
            pd.show();

            uploadImage(strDesc1, strDesc2);
        }
    }

    /**
     * Upload Image Into Firebase Storage
     */
    private void uploadImage(String desc1, String desc2) {
        if (filePath != null) {
            /*
            Get user current date and time
             */
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            postRandomName = saveCurrentDate + "-" + saveCurrentTime;


            /*
            Defining the child of StorageReference
             */
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

            /*
            Adding listeners on upload
            or failure of image
             */
            ref.putFile(filePath).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();
                        Toast.makeText(PostActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                        downloadUrl = downUri.toString();
                        savePostInformation(desc1, desc2);
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void savePostInformation(String d1, String d2) {

        CollectionReference reference = dbPosts.collection("Posts");

        String postId = reference.document().getId();

        /*
        Put post information to database
         */

        long now = System.currentTimeMillis();

        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uId);
        map.put("postid", postId);
        map.put("time", String.valueOf(now));
        map.put("desc1", d1);
        map.put("desc2", d2);
        map.put("postImage", downloadUrl);

        databaseReference.child(postId).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(PostActivity.this, "new post is updated successfully", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                } else {
                    Toast.makeText(PostActivity.this, "error occur while updating your post", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }
        });

    }

    /**
     * Send user to main activity
     * if posts is successfully upload to database
     */
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}