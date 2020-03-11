package com.krystofmacek.firebasechatapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ViewPagerAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ChatsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // Inicializace viewPager a TabLayout pro zobrazovani ruznuch chat fragmentu
        ViewPager viewPager = view.findViewById(R.id.fChats_viewPager);
        setupViewPager(viewPager);
        TabLayout tabLayout = view.findViewById(R.id.fChats_tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    //
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter =
                new ViewPagerAdapter(getChildFragmentManager(),
                        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        // Pridani fragmentu do adapteru
        adapter.addFragment(new NewConversationsFragment(), "New");
        adapter.addFragment(new RecentConversationsFragment(), "Recent");
        viewPager.setAdapter(adapter);
    }
}
