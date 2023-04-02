package com.app.pyxller.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.app.pyxller.R;
import com.app.pyxller.adapter.MessagePagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class MessageFragment extends Fragment {
    public static final int TAB_POSITION = 3;

    private TabLayout tabLayout;
    private MessagePagerAdapter messagePagerAdapter;
    private ViewPager viewPager;
    private ImageButton btnOptions;

    //region newInstance
    public static Fragment newInstance() {
        return new MessageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.message_fragment, container, false);



        viewPager = view.findViewById(R.id.viewpager);

        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Messages"));
        tabLayout.addTab(tabLayout.newTab().setText("Connections"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        messagePagerAdapter = new MessagePagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(messagePagerAdapter);
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

        return view;
    }
}