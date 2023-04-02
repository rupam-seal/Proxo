package com.app.pyxller.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.app.pyxller.fragment.MessageConnectionsFragment;
import com.app.pyxller.fragment.MessageIndividualFragment;

public class MessagePagerAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;

    public MessagePagerAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm, numOfTabs);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                return new MessageIndividualFragment();
            case 1:
                return new MessageConnectionsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}

