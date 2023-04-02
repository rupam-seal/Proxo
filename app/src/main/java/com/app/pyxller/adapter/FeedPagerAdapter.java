package com.app.pyxller.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.app.pyxller.fragment.FeedHomeFragment;
import com.app.pyxller.fragment.FeedExploreFragment;

public class FeedPagerAdapter extends FragmentStatePagerAdapter {

    int numOfTabs;

    public FeedPagerAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm, numOfTabs);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                return new FeedHomeFragment();
            case 1:
                return new FeedExploreFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
