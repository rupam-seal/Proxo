package com.app.pyxller.activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.pyxller.R;
import com.app.pyxller.fragment.MessageFragment;
import com.app.pyxller.fragment.FeedFragment;
import com.app.pyxller.fragment.ProfileFragment;
import com.app.pyxller.fragment.SearchFragment;
import com.app.pyxller.fragment.StatusFragment;
import com.app.pyxller.utils.NetworkConnected;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // firebaseUser for getting currentUserId
    private FirebaseUser firebaseUser;
    // currentUserId string variable
    private String uid;
    RelativeLayout offline;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    final Fragment fragment1 = new FeedFragment();
    final Fragment fragment2 = new StatusFragment();
    final Fragment fragment3 = new SearchFragment();
    final Fragment fragment4 = new MessageFragment();
    final Fragment fragment5 = new ProfileFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active = fragment1;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        offline = findViewById(R.id.offline);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        setFragment(fragment1, "1", 0);

        /*
        current user id
        */
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            uid = firebaseUser.getUid();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {

                    /**
                     *                     bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.colorC));
                     *                     bottomNavigationView.setItemBackgroundResource(R.drawable.transparent_background);
                     */

                    switch (menuItem.getItemId()){

                        case R.id.nav_home:
                            setFragment(fragment1, "1", 0);
                            return true;

                        case R.id.nav_status:
                            setFragment(fragment2, "1", 0);
                            return true;

                        case R.id.nav_search:
                            setFragment(fragment3, "1", 0);
                            return true;

                        case R.id.nav_chat:
                            setFragment(fragment4, "1", 0);
                            return true;

                        case R.id.nav_profile:
                            setFragment(fragment5, "1", 0);
                            return true;
                    }
                    return false;
                }
            };

    public void setFragment(Fragment fragment, String tag, int position) {
        if (fragment.isAdded()) {
            fm.beginTransaction().hide(active).show(fragment).commit();
            fm.popBackStack();
        } else {
            fm.beginTransaction().add(R.id.fragment_container, fragment, tag).commit();
        }
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
        active = fragment;
        fm.popBackStack();
    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }


    public class NetworkChangeListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!NetworkConnected.isConnectedToInternet(context)){
                offline.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Offline", Toast.LENGTH_LONG).show();
            } else {
                offline.setVisibility(View.GONE);
                Toast.makeText(context, "Online", Toast.LENGTH_LONG).show();
            }
        }
    }
}