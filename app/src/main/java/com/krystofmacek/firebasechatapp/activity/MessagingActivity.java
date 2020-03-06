package com.krystofmacek.firebasechatapp.activity;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.MessageAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.Message;
import com.krystofmacek.firebasechatapp.model.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private ImageButton sendMsgBtn;
    private EditText inputMessageText;
    private RecyclerView messageRecycler;
    private TextView heading;
    private ImageButton addFriendButton;

    private FirebaseFirestore firestore;
    private FirebaseUser signedUser;
    private String chatId;

    MessageAdapter adapter;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        sendMsgBtn = findViewById(R.id.messaging_btnSend);
        inputMessageText = findViewById(R.id.messaging_inputMessage);
        messageRecycler = findViewById(R.id.messaging_recycler);
        addFriendButton = findViewById(R.id.messaging_addFriendButton);

        firestore = FirebaseFirestore.getInstance();
        signedUser = FirebaseAuth.getInstance().getCurrentUser();

        userId = getIntent().getStringExtra("userid");

        setupTopBar(userId);
        loadMessages(userId);
        setupAddFriend(userId);

    }

    private void setupTopBar(String userId) {
        heading = findViewById(R.id.toolbar_heading);
        firestore.collection("Profiles")
                .document(userId)
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
        final List<Message> messages = new ArrayList<>();
        // nejdrive najdeme chat
        firestore.collection("Chats")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean newChat = true;
                        for(DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                            Chat chat = snap.toObject(Chat.class);
                            if(chat.getOtherMember(userId).equals(signedUser.getUid())) {
                                chatId = chat.getUid();
                                newChat = false;
                                if(chatId != null) {
                                    //pridame mezi actvieChaty pokud je neobsahuje
                                    firestore.collection("Profiles").document(signedUser.getUid())
                                            .update("activeChats", FieldValue.arrayUnion(chatId));
                                    // na kolekci Messages pripojime snapshot listener
                                    // ktery sleduje zmeny provedene v teto kolekci - pridani dokumentu msg
                                    firestore.collection("Chats")
                                            .document(chatId)
                                            .collection("Messages")
                                            .orderBy("timestamp", Query.Direction.ASCENDING)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                    if(queryDocumentSnapshots != null) {
                                                        List<Message> messagesList = queryDocumentSnapshots.toObjects(Message.class);
                                                        adapter = new MessageAdapter(getApplicationContext(), messagesList);
                                                        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                                                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                                                messageRecycler.setLayoutManager(layoutManager);
                                                                messageRecycler.setAdapter(adapter);
                                                                messageRecycler.scrollToPosition(messagesList.size()-1);
                                                    }
//
                                                }
                                            });
                                    sendMessageSetup(chatId);
                                }

                            }
                        }
                        // zalozime novy chat

                        if(newChat) {
                            DocumentReference ref = firestore.collection("Chats").document();
                            String newId = ref.getId();
                            List<String> members = new ArrayList<>();
                            members.add(signedUser.getUid());
                            members.add(userId);
                            Chat chat = new Chat(
                                    newId,
                                    members
                            );
                            firestore.collection("Chats").document(newId)
                                    .set(chat);

                            firestore.collection("Profiles").document(signedUser.getUid())
                                    .update("activeChats", FieldValue.arrayUnion(newId));

                            sendMessageSetup(chatId);
                        }
                    }
                });
    }

    private void sendMessageSetup(final String chatId){
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DocumentReference newMessageDoc = firestore.collection("Chats")
                        .document(chatId)
                        .collection("Messages")
                        .document();

                Message newMessage = new Message(signedUser.getUid(), inputMessageText.getText().toString(), Timestamp.now());
                newMessageDoc.set(newMessage);
                inputMessageText.setText("");

                firestore.collection("Chats")
                        .document(chatId)
                        .update("lastMessageTime", newMessage.getTimestamp());

            }
        });
    }

    private void setupAddFriend(final String userId) {

        addFriendButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Dialog confirmFriendDialog = new Dialog(MessagingActivity.this);
                        confirmFriendDialog.setTitle("Edit your profile");
                        confirmFriendDialog.setContentView(R.layout.dialog_add_friend);
                        confirmFriendDialog.show();

                        Button add = confirmFriendDialog.findViewById(R.id.dialog_friend_add);
                        Button cancel = confirmFriendDialog.findViewById(R.id.dialog_friend_cancel);

                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                firestore.collection("Profiles")
                                        .document(signedUser.getUid())
                                        .update("friends", FieldValue.arrayUnion(userId));
                                confirmFriendDialog.cancel();
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
