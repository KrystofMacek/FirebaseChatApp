package com.krystofmacek.firebasechatapp.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.krystofmacek.firebasechatapp.R;
import com.krystofmacek.firebasechatapp.adapters.ChatAdapter;
import com.krystofmacek.firebasechatapp.model.Chat;
import com.krystofmacek.firebasechatapp.model.User;

import java.util.ArrayList;
import java.util.List;


public class NewConversationsFragment extends Fragment {

    FirebaseFirestore firestore;
    FirebaseUser signedUser;
    RecyclerView newChatRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_conversations, container, false);

        // inicializce ui a fireb obj
        firestore = FirebaseFirestore.getInstance();
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
        newChatRecycler = view.findViewById(R.id.fChats_New_recycler);

        loadNewChats();

        return view;
    }

    private void loadNewChats() {
        final List<Chat> newChats = new ArrayList<>();

        // nacteme seznam aktivnich chatu
        firestore.collection("Profiles").document(signedUser.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final List<String> activeChats =
                        documentSnapshot.toObject(User.class).getActiveChats();

                // nacteme vsechny chaty ve kterych je uzivatel clenem
                firestore.collection("Chats")
                        .whereArrayContains("members", signedUser.getUid())
                        .orderBy("lastMessageTime")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                List<Chat> allChats = queryDocumentSnapshots.toObjects(Chat.class);
                                newChats.clear();
                                // zjistime ktere z nactenych jsou nove
                                for(Chat c : allChats) {
                                    boolean isNewChat = true;
                                    for(String activeChatId : activeChats) {
                                        if(c.getUid().equals(activeChatId)) {
                                            isNewChat = false;
                                        }
                                    }
                                    // nove pridame do seznamu
                                    if(isNewChat) {
                                        newChats.add(c);
                                    }
                                }
                                // zobrazime seznam novych chatu
                                ChatAdapter adapter = new ChatAdapter(getContext(), newChats);
                                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                newChatRecycler.setLayoutManager(layoutManager);
                                newChatRecycler.setAdapter(adapter);
                            }
                        });
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        loadNewChats();
    }
}
