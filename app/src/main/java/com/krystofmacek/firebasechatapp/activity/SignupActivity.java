package com.krystofmacek.firebasechatapp.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.model.User;

import java.util.Arrays;

public class SignupActivity extends AppCompatActivity {

    private AuthUI mAuthUI;
    //request code value
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuthUI = AuthUI.getInstance();


        // Enable sign-in providers
        startActivityForResult(mAuthUI
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                        ))
                        .setLogo(R.drawable.splash_image)
                        .build(),
                RC_SIGN_IN);

        // TODO: přidat Facebook authenticaion

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Intent intent;

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, "Sign in cancelled.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, "Unknown error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
