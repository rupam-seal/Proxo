package com.app.pyxller.activity;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class PyxllerOffline extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}