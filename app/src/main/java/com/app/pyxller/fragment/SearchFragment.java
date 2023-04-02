package com.app.pyxller.fragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.pyxller.R;
import com.app.pyxller.model.Users;
import com.app.pyxller.adapter.SearchUserAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    public static final int TAB_POSITION = 2;
    private EditText edtSearch;
    private RelativeLayout emptyScreen;
    private RecyclerView recyclerView;
    private List<Users> usersList;
    private SearchUserAdapter searchUserAdapter;

    private String empty, searchTxt;

    private ShimmerFrameLayout shimmer;

    //region newInstance
    public static Fragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        edtSearch = view.findViewById(R.id.edt_search);
        recyclerView = view.findViewById(R.id.recyclerview);
        emptyScreen = view.findViewById(R.id.empty_screen);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<>();
        searchUserAdapter = new SearchUserAdapter(usersList, getContext(), true);
        recyclerView.setAdapter(searchUserAdapter);

        shimmer = view.findViewById(R.id.shimmer);

        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    performSearch(view, edtSearch);
                    return true;
                }
                return false;
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                empty = "";
                searchTxt = s.toString().trim();

                if (searchTxt.equals(empty)){
                    recyclerView.setVisibility(View.GONE);
                    emptyScreen.setVisibility(View.VISIBLE);
                    shimmer.setVisibility(View.INVISIBLE);
                    shimmer.stopShimmer();
                }else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyScreen.setVisibility(View.INVISIBLE);
                    shimmer.setVisibility(View.VISIBLE);
                    shimmer.startShimmer();
                    searchUser(searchTxt);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setupUi(view);

        return view;
    }

    private void searchUser(String searchTxt) {
        Query query = FirebaseFirestore.getInstance()
                .collection("Users")
                .orderBy("username")
                .startAt(searchTxt)
                .endAt(searchTxt + "\uf8ff");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                usersList.clear();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot snapshot : list) {
                    Users model = snapshot.toObject(Users.class);
                    usersList.add(model);
                    shimmer.setVisibility(View.INVISIBLE);
                    shimmer.stopShimmer();
                }
                searchUserAdapter.notifyDataSetChanged();
            }
        });
    }

    private void performSearch(View view, EditText searchEditTxt) {
        searchEditTxt.clearFocus();
        InputMethodManager in = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (in.isAcceptingText()){
            in.hideSoftInputFromWindow(view.getWindowToken(), 0);
            shimmer.setVisibility(View.VISIBLE);
            emptyScreen.setVisibility(View.INVISIBLE);
            shimmer.startShimmer();
            searchUser(searchTxt);
        }

    }

    public void setupUi(View view){
        if (!(view instanceof EditText)){
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(view);
                    edtSearch.clearFocus();
                    return false;
                }
            });
        }

        if (view instanceof ViewGroup){
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++){
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUi(innerView);
            }
        }
    }

    public void hideKeyboard(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}