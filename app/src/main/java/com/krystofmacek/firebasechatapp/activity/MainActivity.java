package com.krystofmacek.firebasechatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.BlendMode;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.krystofmacek.firebasechatapp.R;

public class MainActivity extends AppCompatActivity {

    TextView heading;
    BottomNavigationView botNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setupNavigation();

    }

    private void setupNavigation() {

        heading = findViewById(R.id.toolbar_heading);
        heading.setText("Home");

        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        botNavigation = findViewById(R.id.bot_navigation);
        botNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //TODO: For each item ID navigate
                    case R.id.nav_item_home:
                        break;
                    case R.id.nav_item_friends:
                        break;
                    case R.id.nav_item_search:
                        break;
                    case R.id.nav_item_chats:
                        break;
                }
                return false;
            }
        });


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
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
                finish();
                return true;
        }
        return false;
    }
}
