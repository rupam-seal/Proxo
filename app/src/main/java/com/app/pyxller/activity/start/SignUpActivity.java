package com.app.pyxller.activity.start;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class SignUpActivity extends AppCompatActivity {

    private EditText su_name, su_username, su_email, su_password, su_confirmPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private Button loginBtn, btnSignUp;
    private TextView loginTxt;

    private ProgressDialog progressDialog;

    private FirebaseUser currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_register_activity);

        loginBtn = findViewById(R.id.su_btn_login);
        loginTxt = findViewById(R.id.su_txt_login);
        btnSignUp = findViewById(R.id.su_btn_signUp);
        su_email = findViewById(R.id.su_email);
        su_password = findViewById(R.id.su_password);
        su_confirmPassword = findViewById(R.id.su_confirm_password);
        /*
        Send User To Login Activity (Already have an account button)
         */
        loginBtn.setOnClickListener(v -> {
            sendUserToLoginActivity();
        });

        /*
        Send User To Login Activity (Already have an account txt)
         */
        loginTxt.setOnClickListener(v -> {
            sendUserToLoginActivity();
        });

        /*
        SignUp Button Click
         */
        btnSignUp.setOnClickListener(v -> signUp());

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId != null){
            sendUserToMainActivity();
        }
    }

    /**
     * SignUp Button Click
     */
    private void signUp() {

        String strEmail = su_email.getText().toString();
        String strPassword = su_password.getText().toString();
        String strConfirmPassword = su_confirmPassword.getText().toString();

        if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)
                || TextUtils.isEmpty(strConfirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (strPassword.length() < 6) {
            Toast.makeText(this, "Password must have 6 characters", Toast.LENGTH_SHORT).show();
        } else if (!strPassword.equals(strConfirmPassword)) {
            Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show();
        } else {
            register(strEmail, strPassword, strConfirmPassword);
        }

    }

    private void register(String strEmail, String strPassword, String strConfirmPassword) {
        progressDialog.setTitle("Creating new account");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(true);

        mAuth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(SignUpActivity.this, task -> {

                    if (task.isSuccessful()) {

                        sendUserToSetupActivity();

                        Toast.makeText(SignUpActivity.this, "you are authenticated successfully...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "you can't register with this email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(SignUpActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
    }

    /**
     * Send User To Main Activity
     */
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SignUpActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    /**
     * Send User To Login Activity
     */
    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(loginIntent);
    }
}