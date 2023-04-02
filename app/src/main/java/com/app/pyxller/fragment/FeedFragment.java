package com.app.pyxller.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.app.pyxller.R;
import com.app.pyxller.adapter.FeedPagerAdapter;
import com.app.pyxller.activity.PostActivity;
import com.google.android.material.tabs.TabLayout;

public class FeedFragment extends Fragment {
    public static final int TAB_POSITION = 0;

    private TabLayout tabLayout;
    private FeedPagerAdapter feedPagerAdapter;
    private ViewPager viewPager;
    private ImageButton btnCreate;

    public static Fragment newInstance() {
        return new MessageFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.feed_fragment, container, false);

        /*
        send user to post activity
         */
        btnCreate = view.findViewById(R.id.btn_create);

        /*
        Add Button Click
        */
        btnCreate.setOnClickListener(v -> {
           sendUserToPostActivity();
        });

        viewPager = view.findViewById(R.id.viewpager);

        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Explore"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        feedPagerAdapter = new FeedPagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(feedPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return  view;
    }

    /**
     * Send User To PostActivity
     */
    private void sendUserToPostActivity() {
        Intent postIntent = new Intent(getContext(), PostActivity.class);
        startActivity(postIntent);
    }

}