package com.krystofmacek.firebasechatapp.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.krystofmacek.firebasechatapp.R;

import androidx.fragment.app.Fragment;

public class RecentConverasationsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent_converasations, container, false);
    }

}
