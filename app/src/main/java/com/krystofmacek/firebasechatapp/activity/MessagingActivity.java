package com.krystofmacek.firebasechatapp.activity;


import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.MessageAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    // ui elementy
    private ImageButton sendMsgBtn;
    private EditText inputMessageText;
    private RecyclerView messageRecycler;
    private TextView heading;
    private ImageButton addFriendButton;
    private LinearLayout addFriendBar;
    private String chatId;

    // firesbase obj
    private FirestoreService firestoreService;
    private DocumentReference signedUser;

    private MessageAdapter adapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        //inicializace ui elementu
        sendMsgBtn = findViewById(R.id.messaging_btnSend);
        inputMessageText = findViewById(R.id.messaging_inputMessage);
        messageRecycler = findViewById(R.id.messaging_recycler);
        addFriendButton = findViewById(R.id.messaging_addFriendButton);
        addFriendBar = findViewById(R.id.messaging_addFriendBar);


        //inicializace firestore service a prihlaseneho uzivatele
        firestoreService = new FirestoreService();
        signedUser = firestoreService.getSignedUserDocumentRef();

        // nacteni ID uzivatele s kterym komunikujeme
        userId = getIntent().getStringExtra("userid");

        setupTopBar(userId);
        loadMessages(userId);
        setupAddFriend(userId);

    }

    private void setupTopBar(String userId) {
        heading = findViewById(R.id.toolbar_heading);
        // Nastaveni nadpisu na jmeno uzivatele
        firestoreService.getDocumentReference("Profiles", userId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    heading.setText(user.getDisplayName());
                }

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadMessages(final String userId){
        //Nacteni chatu daneho uzivatele
        firestoreService
                .queryByArrayContains("Chats", "members", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean newChat = true;
                        for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                            Chat chat = snap.toObject(Chat.class);
                            // vyber chat kde members obsahuje obje id
                            if(chat.getOtherMember(userId).equals(signedUser.getId())) {
                                chatId = chat.getUid();
                                newChat = false;
                                if(chatId != null) {
                                    firestoreService.addChatToActive(chatId);
                                    // na kolekci Messages pripojime snapshot listener
                                    // ktery sleduje zmeny provedene v teto kolekci - pridani dokumentu msg
                                    listenForMessages(chatId);
                                    sendMessageSetup(chatId);
                                }

                            }
                        }
                        // pokud chat jeste neexistuje, vytvori se novy
                        if(newChat) {
                            String newChatId = firestoreService.initializeNewChat(userId);
                            listenForMessages(newChatId);
                            sendMessageSetup(newChatId);
                        }
                    }
                });
    }

    private void listenForMessages(final String chat) {
        // pridame snapshot listener na kolekci zpráv
        firestoreService.queryMessageCollection(chat)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null) {
                            // vytvoreni listu vsech zprav, serazenych podle timestamp
                            List<Message> messagesList = queryDocumentSnapshots.toObjects(Message.class);

                            // aktualizace zprav na prectene
                            for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                                if(doc.getString("recieverId").equals(signedUser.getId())) {
                                    doc.getReference().update("seen", true);
                                }
                            }

                            // naplneni ui zpravami
                            adapter = new MessageAdapter(getApplicationContext(), messagesList);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                            messageRecycler.setLayoutManager(layoutManager);
                            messageRecycler.setAdapter(adapter);
                            messageRecycler.scrollToPosition(messagesList.size()-1);
                        }
                    }
                });
    }

    // nastaveni tlacitka pro odeslani zpravy
    private void sendMessageSetup(final String chatId){
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // vyvtoreni dokumentu nove zpravy
                final DocumentReference newMessageDoc = firestoreService.getEmptyMessageDocument(chatId);
                // Do dokumentu se ulozi objekt nove zpravy
                Message newMessage = new Message(
                        signedUser.getId(),
                        userId,
                        inputMessageText.getText().toString(),
                        Timestamp.now());

                newMessageDoc.set(newMessage);
                inputMessageText.setText("");

                // aktualizace casu posledni zpravy v dokumentu reprezentujiciho chat
                firestoreService.updateField(
                        "Chats",
                        chatId,
                        "lastMessageTime",
                        newMessage.getTimestamp());
            }
        });
    }

    private void setupAddFriend(final String userId) {
        // pokud je uzivatel v seznamu pratel, odstranime tlacitko pro pridani
        firestoreService
                .getSignedUserDocumentRef()
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                List<String> friends = documentSnapshot.toObject(User.class).getFriends();
                for(String f : friends) {
                    if(userId.equals(f)) {
                        addFriendBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        //nastaveni onClick tlacitka pridavajiciho uzivatele mezi pratele
        addFriendButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Vytvoreni dialogu pro potvrzeni pridani uzivatele
                        final Dialog confirmFriendDialog = new Dialog(MessagingActivity.this);
                        confirmFriendDialog.setContentView(R.layout.dialog_add_friend);
                        confirmFriendDialog.show();

                        Button add = confirmFriendDialog.findViewById(R.id.dialog_friend_add);
                        Button cancel = confirmFriendDialog.findViewById(R.id.dialog_friend_cancel);

                        // po potvrzeni ho pridame
                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                firestoreService
                                        .updateField("Profiles", signedUser.getId(), "friends", FieldValue.arrayUnion(userId));
                                confirmFriendDialog.cancel();
                                addFriendBar.setVisibility(View.GONE);
                            }
                        });

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                confirmFriendDialog.cancel();
                            }
                        });

                    }
                }
        );

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

}
