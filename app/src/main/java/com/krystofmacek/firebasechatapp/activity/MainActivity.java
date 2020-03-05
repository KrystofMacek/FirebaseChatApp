package com.krystofmacek.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.fragments.ChatsFragment;
import com.krystofmacek.firebasechatapp.fragments.FriendsFragment;
import com.krystofmacek.firebasechatapp.fragments.HomeFragment;
import com.krystofmacek.firebasechatapp.fragments.SearchFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<String> navigationLabels =
            new ArrayList<>(Arrays.asList("home", "chats", "search", "friends"));

    private TextView heading;
    private BottomNavigationView botNavigation;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
    }


    /**
     Metody poskytující navigaci mezi Aktivitami
     setupNavigation(), onCreateOptionsMenu(Menu menu), onOptionsItemSelected(@NonNull MenuItem item)
     */
    private void setupNavigation() {

        heading = findViewById(R.id.toolbar_heading);
        heading.setText("Profile");

        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        // Navigace mezi jednotlivymi fragmenty
        frameLayout = findViewById(R.id.fragment_container);
        botNavigation = findViewById(R.id.bot_navigation);
        botNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.nav_item_home:
                        ft.replace(R.id.fragment_container, new HomeFragment(), navigationLabels.get(0));
                        item.setChecked(true);
                        ft.addToBackStack("home");
                        ft.commit();
                        heading.setText("Profile");
                        break;
                    case R.id.nav_item_chats:
                        ft.replace(R.id.fragment_container, new ChatsFragment(), navigationLabels.get(1));
                        item.setChecked(true);
                        ft.addToBackStack("chats");
                        ft.commit();
                        heading.setText("Chats");
                        break;
                    case R.id.nav_item_search:
                        ft.replace(R.id.fragment_container, new SearchFragment(), navigationLabels.get(2));
                        item.setChecked(true);
                        ft.addToBackStack("search");
                        ft.commit();
                        heading.setText("Search");
                        break;
                    case R.id.nav_item_friends:
                        ft.replace(R.id.fragment_container, new FriendsFragment(), navigationLabels.get(3));
                        item.setChecked(true);
                        ft.addToBackStack("friends");
                        ft.commit();
                        heading.setText("Friends");
                        break;
                }
                return false;
            }
        });

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_home);
        if(fragment == null || !fragment.isInLayout()) {
            FragmentTransaction ft =
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment());
            botNavigation.setSelectedItemId(R.id.nav_item_home);
            ft.commit();
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_bar_dropdown_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
        }
        return false;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this, SignupActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment currentFragment;
        for (String tag: navigationLabels) {
            currentFragment = getSupportFragmentManager().findFragmentByTag(tag);
            if(currentFragment != null) {
                if(currentFragment.isVisible()) {
                    switch (tag) {
                        case "home":
                            botNavigation.getMenu().getItem(0).setChecked(true);
                            heading.setText("Profile");
                            break;
                        case "chats":
                            botNavigation.getMenu().getItem(1).setChecked(true);
                            heading.setText("Chats");
                            break;
                        case "search":
                            botNavigation.getMenu().getItem(2).setChecked(true);
                            heading.setText("Search");
                            break;
                        case "friends":
                            botNavigation.getMenu().getItem(3).setChecked(true);
                            heading.setText("Friends");
                            break;
                    }
                    return;
                }
            } else {
                botNavigation.getMenu().getItem(0).setChecked(true);
                break;
            }
        }

    }
}


