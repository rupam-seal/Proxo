package com.app.pyxller.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.pyxller.R;


public class StatusFragment extends Fragment {

    public static final int TAB_POSITION = 1;

    //region newInstance
    public static Fragment newInstance() {
        return new StatusFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.status_fragment, container, false);

        return view;
    }
}