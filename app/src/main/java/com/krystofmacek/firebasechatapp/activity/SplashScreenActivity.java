package com.krystofmacek.firebasechatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.krystofmacek.firebasechatapp.R;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //inicializace firestore a prihlaseneho uzivatele
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Intent intent;

        // pokud neni nikdo prihlasen spusti se signUp aktivita
        if(user == null) {
            intent = new Intent(getApplicationContext(), SignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            // jinak se spusti main aktivita
        } else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }


    }
}
