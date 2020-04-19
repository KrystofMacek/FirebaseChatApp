package com.krystofmacek.firebasechatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.krystofmacek.firebasechatapp.R;

import java.util.Arrays;

public class SignupActivity extends AppCompatActivity {

    //request code value
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        AuthUI mAuthUI = AuthUI.getInstance();
        // Pridani jednotlivych sign-in poskytovatelu
        startActivityForResult(
                    mAuthUI
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                // Email-Heslo
                                new AuthUI.IdpConfig
                                        .EmailBuilder()
                                        .setRequireName(false)
                                        .build(),
                                // Google Account
                                new AuthUI.IdpConfig
                                        .GoogleBuilder()
                                        .setSignInOptions(
                                            new GoogleSignInOptions
                                                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                    .requestIdToken(getString(R.string.default_web_client_id))
                                                    .requestEmail()
                                                    .build()
                                        ).build(),
                                // Facebook account
                                new AuthUI.IdpConfig
                                        .FacebookBuilder()
                                        .build()
                        ))
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.ic_launcher_round)
                        .build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // RC_SIGN_IN je stejny jako pozadavek ktery jsme vlozili do
        // metody startActivityForResult(...) na zacatku prihlasovaciho procesu.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // V pripade uspesneho prihlaseni
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();
            } else {
                // pripad zruseni prihlaseni
                if (response == null) {
                    // uzivatel zrusil prihlasovaci proces
                    Toast.makeText(this, "Sign in cancelled.", Toast.LENGTH_SHORT).show();
                    return;
                }
                // prihlaseni se nepodarilo z duvodu nedostupnosti internetu
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
