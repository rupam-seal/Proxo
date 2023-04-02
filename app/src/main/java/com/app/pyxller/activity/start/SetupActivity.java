package com.app.pyxller.activity.start;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText edtName, edtUsername;
    private String username, fullname;

    private Button btnCreate;
    private CircleImageView imgSelectProfile;

    private ProgressDialog pd;

    private FirebaseAuth mAuth;
    private StorageReference userProfileImageRef;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentUserID;
    String downloadUrl;

    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_setup_activity);

        edtName = findViewById(R.id.edt_name);
        edtUsername = findViewById(R.id.edt_username);
        btnCreate = findViewById(R.id.btn_create);
        imgSelectProfile = findViewById(R.id.img_select_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        pd = new ProgressDialog(this);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        imgSelectProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });



    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // some conditions for the picture
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        // Get the cropped image
        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK) {
                pd.setTitle("Profile Image");
                pd.setMessage("Please wait, while we updating your profile image...");
                pd.show();
                pd.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadUrl = uri.toString();
                                pd.dismiss();
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SetupActivity.this, "Error occur while uploading your profile", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        });

                    }

                });

            }
            else
            {
                Toast.makeText(this, "Error Occur : Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }

    }

    private void saveUserInformation() {

        username = edtUsername.getText().toString();
        fullname = edtName.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "please write your username...", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "please write your full name...", Toast.LENGTH_SHORT).show();
        }else {

            pd.setTitle("Saving Information");
            pd.setMessage("Please wait, while we are saving your information");
            pd.show();
            pd.setCanceledOnTouchOutside(true);

            Map<String, Object> user = new HashMap<>();
            user.put("username", username);
            user.put("fullname", fullname);
            user.put("bio", "");
            user.put("profileimage", downloadUrl);
            user.put("uid", currentUserID);

            db.collection("Users")
                    .document(currentUserID)
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "your account is created successfully", Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "error occur " + message, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            });

        }


    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}