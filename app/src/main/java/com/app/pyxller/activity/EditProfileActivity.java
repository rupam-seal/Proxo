package com.app.pyxller.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.fragment.ProfileFragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView imgProfile;
    private TextView btnChangeProfile;
    private EditText edtName, edtUsername, edtBio;
    private Button btnUpdate;

    private ProgressDialog loadingBar;

    private FirebaseFirestore db;
    private CollectionReference reference;
    private FirebaseUser firebaseUser;
    private StorageReference userProfileImageRef;
    private String uId;

    private String strFullName, strUserName, strProfile, strBio;
    private String strEdName, strEdUserName, strEdBio;

    private StorageTask uploadTask;

    final static int GALLERY_PICK = 1;
    private Uri imageUri;
    //Request code
    private final int PICK_IMAGE_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgProfile = findViewById(R.id.img_profile);
        edtBio = findViewById(R.id.edt_bio);
        btnChangeProfile = findViewById(R.id.btn_change_profile);
        edtName = findViewById(R.id.edt_name);
        edtUsername = findViewById(R.id.edt_username);
        btnUpdate = findViewById(R.id.btn_update);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
                strEdName = edtName.getText().toString();
                strEdUserName = edtUsername.getText().toString();
                strEdBio = edtBio.getText().toString();

                Map<String, Object> user = new HashMap<>();
                user.put("username", strEdUserName);
                user.put("fullname", strEdName);
                user.put("bio", strEdBio);

                reference.document(uId).update(user);
                uploadImage();
            }
        });

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.text_900));
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        /*
        Getting current user id
         */
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uId = firebaseUser.getUid();
        }

        db = FirebaseFirestore.getInstance();
        reference = db.collection("Users");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        /*
        Get the profile, name, username from database
        Set info on PostActivity
         */
        reference.document(uId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        strFullName = document.getString("fullname");
                        strUserName = document.getString("username");
                        strProfile = document.getString("profileimage");
                        strBio = document.getString("bio");

                    /*
                    Set full name from database
                     */
                        edtName.setText(strFullName);

                    /*
                    Set user name from database
                     */
                        edtUsername.setText(strUserName);

                    /*
                    Set user bio from database
                     */
                        edtBio.setText(strBio);

                    /*
                    Set profile from database
                     */
                        Glide.with(getApplicationContext())
                                .load(strProfile)
                                .into(imgProfile);

                    }
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

                /*
        Checking request code and result code
        if request code is PICK_IMAGE_REQUEST and
        result code is RESULT_OK
        then set image in the image view
         */
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            /*
            Get the uri of the data
             */
            imageUri = data.getData();
            try {
                /*
                Setting image into imageView using Bitmap
                 */
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                imageUri);

                imgProfile.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null){
            final StorageReference filePath = userProfileImageRef.child(uId + ".jpg");
            uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = (Uri) task.getResult();
                        String miUrlOk = downloadUri.toString();
                        Map<String, Object> user = new HashMap<>();
                        user.put("profileimage", miUrlOk);
                        FirebaseFirestore.getInstance().collection("Users").document(uId).update(user);
                        pd.dismiss();
                        sendUserToMainActivity();
                    }else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void sendUserToMainActivity() {
        ((FragmentActivity)getApplicationContext()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent mainIntent = new Intent(EditProfileActivity.this, ProfileFragment.class);
            startActivity(mainIntent);
        }

        return super.onOptionsItemSelected(item);
    }


}