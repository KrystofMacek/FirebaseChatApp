package com.krystofmacek.firebasechatapp.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MessageNotificationsService extends FirebaseMessagingService {

    FirestoreService firestoreService = new FirestoreService();
    // Aktualizace registracniho tokenu při přijetí nového
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser signedUser = FirebaseAuth.getInstance().getCurrentUser();

        if(signedUser != null) {
            // aktualizace pole v databázi
            firestoreService.updateField("Profiles", signedUser.getUid(), "registrationToken", s);
        }
    }

    // Zpracování přijetí upozornění
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() != null) {
            Log.i("Notification", remoteMessage.getData().toString());
        }
    }
}
