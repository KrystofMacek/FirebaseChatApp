package com.krystofmacek.firebasechatapp.fragments;
import android.graphics.Color;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecentConversationsFragment extends Fragment {

    FirebaseFirestore firestore;
    FirebaseUser signedUser;
    RecyclerView recentChatRecycler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent_conversations, container, false);

        firestore = FirebaseFirestore.getInstance();
        signedUser = FirebaseAuth.getInstance().getCurrentUser();
        recentChatRecycler = view.findViewById(R.id.fChats_Recent_recycler);

        loadRecentChats();

        return view;
    }


    private void loadRecentChats() {
        final List<Chat> chats = new ArrayList<>();

        firestore.collection("Profiles").document(signedUser.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                List<String> chatIds = u.getActiveChats();
                if(!chatIds.isEmpty()){
                    // Pridan custom index ve fristore
                    firestore.collection("Chats")
                            .whereIn("uid", chatIds)
                            .orderBy("lastMessageTime")
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                    for(Chat chat : queryDocumentSnapshots.toObjects(Chat.class)) {
                                        chats.add(chat);
                                    }
                                    ChatAdapter adapter = new ChatAdapter(getContext(), chats);
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

}
