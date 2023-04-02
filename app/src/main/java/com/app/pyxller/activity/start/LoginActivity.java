package com.app.pyxller.activity.start;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private Button btnRegister, btnLogin;
    private TextView txtRegister;
    private TextInputEditText edtEmail, edtPass;
    private ProgressDialog pd;

    private String strEmail, strPassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_login_activity);

        btnRegister = findViewById(R.id.btn_register);
        txtRegister = findViewById(R.id.btn_register);
        edtEmail = findViewById(R.id.edt_email);
        edtPass = findViewById(R.id.edt_pass);
        btnLogin = findViewById(R.id.btn_login);

        /*
        Send User To SignUp Activity (Don't have an account button)
         */
        btnRegister.setOnClickListener(v -> {
            sendUserToSignUpActivity();
        });

        /*
        Send User To SignUp Activity (Don't have an account button)
         */
        txtRegister.setOnClickListener(v -> {
            sendUserToSignUpActivity();
        });

        /*
        Login Button onClickListener
         */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClick();
            }
        });

        /*
        Firebase Auth
         */
        mAuth = FirebaseAuth.getInstance();

    }

    /**
     * Login Button Click
     */
    private void loginClick() {
        pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Please wait...");
        pd.show();

        strEmail = edtEmail.getText().toString();
        strPassword = edtPass.getText().toString();

        if (TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else {
            login();
        }
    }

    /**
     * Login To The App
     */
    private void login() {
        mAuth.signInWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(mAuth.getCurrentUser().getUid());

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    pd.dismiss();
                                    sendUserToMainActivity();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    pd.dismiss();
                                }
                            });
                        }else {
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Send User To Main Activity
     */
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    /**
     * Send User To SignUp Activity
     */
    private void sendUserToSignUpActivity() {
        Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(signUpIntent);
    }
}