package com.krystofmacek.firebasechatapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ChatAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.User;
import com.krystofmacek.firebasechatapp.services.FirestoreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecentConversationsFragment extends Fragment {

    private FirestoreService firestoreService;
    private RecyclerView recentChatRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent_conversations, container, false);

        // inicializace firebase obj a UI
        firestoreService = new FirestoreService();
        recentChatRecycler = view.findViewById(R.id.fChats_Recent_recycler);

        loadRecentChats();

        return view;
    }


    private void loadRecentChats() {
        final List<Chat> recentChats = new ArrayList<>();
        // Nacteni dokumentu uzivatele
        firestoreService
                .getSignedUserDocumentRef()
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                List<String> chatIds = Objects.requireNonNull(user).getActiveChats();
                if(!chatIds.isEmpty()){
                    // nacteme vsechny chaty, serazene podle casu posledni zpravy od nejnovejsich
                    // Pridan custom index ve fristore
                    firestoreService.queryByWhereIn("Chats", "uid", chatIds)
                            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if(queryDocumentSnapshots != null) {
                                        recentChats.clear();
                                        recentChats.addAll(queryDocumentSnapshots.toObjects(Chat.class));
                                    }
                                    ChatAdapter adapter = new ChatAdapter(getContext(), recentChats);
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                    recentChatRecycler.setLayoutManager(layoutManager);
                                    recentChatRecycler.setAdapter(adapter);
                                }
                            });
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentChats();
    }
}
