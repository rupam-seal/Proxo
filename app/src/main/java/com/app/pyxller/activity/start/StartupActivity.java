package com.app.pyxller.activity.start;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.app.pyxller.R;
import com.app.pyxller.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartupActivity extends AppCompatActivity {

    private Button btnLogin, btnRegister;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_startup_activity);

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                /**
                 *         Pair[] pairs = new Pair[1];
                 *         pairs[0] = new Pair<View, String>(findViewById(R.id.login_btn), "transition_login");
                 *
                 *         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                 *             ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(StartupActivity.this, pairs);
                 *             startActivity(intent, options.toBundle());
                 *         }else{
                 *             startActivity(intent);
                 *         }
                 */
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);

                /**
                 *  Pair[] pairs = new Pair[1];
                 *         pairs[0] = new Pair<View, String>(findViewById(R.id.signup_btn), "transition_signup");
                 *
                 *         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                 *             ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(StartupActivity.this, pairs);
                 *             startActivity(intent, options.toBundle());
                 *         }else{
                 *             startActivity(intent);
                 *         }
                 */
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        /*
        Check if user is null
         */
        if (firebaseUser != null){
            sendUserToMainActivity();
        }

    }

    /**
     * Send User To Main Activity
     */
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(StartupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}